package com.cs456.project.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class ServerConnectionThread extends Thread {
	private static Logger logger = Logger.getLogger(ServerConnectionThread.class);
	private Socket socket = null;
	
	OutputStream out = null;
	PrintWriter pw = null;
	
	DatabaseManager dbm = null;
	
	ServerConnectionThread(Socket socket) {
		this.dbm = DatabaseManager.getInstance();
		this.socket = socket;
	}

	@Override
	public void run() {		
		try {
			initiateClientConnection();
		} catch (IOException e) {
			logger.error("An error occurred while initiating connection with the client", e);
			closeClientConnection();
			return;
		} catch (SQLException e) {
			logger.error("An error occurred while querying the database for the requested username and password", e);
			closeClientConnection();
			return;
		} catch (BadAuthenticationException e) {
			logger.error("The client was not authenticated and thus is being kicked out.  The attempted username and password were:" +
					" username=" + e.getUsername() + " password=" + e.getPassword());
			closeClientConnection();
			return;
		}
		
		Request request = null;
		try {
			request = getClientRequest();
		} catch (InvalidRequestException e) {
			logger.error("The client <" + socket.getInetAddress() + "> has sent an invalid request: <" + e.getInvalidRequest() + ">");
			closeClientConnection();
			return;
		} catch (IOException e) {
			logger.error("An error occurred when trying to obtain the clients <" + socket.getInetAddress() + "> request", e);
			closeClientConnection();
			return;
		}
					
		switch(request.getRequestType()) {
		case DOWNLOAD:
			try {
				boolean success = sendFile((DownloadRequest)request);
				
				if(!success) {
					logger.error("Sending the requested file to the client was not successful");
					closeClientConnection();
					return;
				}					
			} catch (FileNotFoundException e) {
				logger.error("The file requested to be downloaded could not be found", e);
				closeClientConnection();
				return;
			} catch (IOException e) {
				logger.error("An error occurred while sending the requested file", e);
				closeClientConnection();
				return;
			}
			break;
		case UPLOAD:
			try {
				boolean success = receiveFile((UploadRequest)request);
				
				if(!success) {
					logger.error("Did not receive the entire file being uploaded.  The client must reconnect and send the rest of it.");
					closeClientConnection();
					return;
				}
			} catch (FileNotFoundException e) {
				logger.error("Could not create the requested file on the server", e);
				closeClientConnection();
				return;
			} catch (IOException e) {
				logger.error("An error occurred while receiving the requested file", e);
				closeClientConnection();
				return;
			}
			
			break;
		case GOODBYE:
			closeClientConnection();
			return;
		case DELETE:
			boolean success = deleteFile((DeleteRequest)request);
			
			if(!success) {
				logger.error("Failed to delete the file");
				closeClientConnection();
				return;
			}
			break;
		case REMOTE_FILE_DOWNLOAD:
			boolean remoteSuccess = remoteFileDownload((RemoteFileDownloadRequest)request);
			
			if(!remoteSuccess) {
				logger.error("Failed to remotely download the file");
				closeClientConnection();
				return;
			}
			break;
		}
		
		closeClientConnection();		
	}
	
	
	private void initiateClientConnection() throws IOException, SQLException, BadAuthenticationException {
		out = socket.getOutputStream();
		pw = new PrintWriter(out);

		String line = null;

		logger.info("Waiting for greeting from client");

		line = readLine(socket);
		if (line == null || !line.startsWith(ConnectionSettings.GREETING)) {
			logger.info("The client did not properly do the handshake, " +
					"and thus the client <" + socket.getInetAddress() + "> is being rejected");
		}
		
		String stringArguments = line.substring(ConnectionSettings.GREETING.length()).trim();
		String[] arguments = stringArguments.split(" ");
		
		String username = arguments[0];
		String password = arguments[1];
		
		String query = "Select * from USERS where username = '" + username + "'";
		ResultSet rs = dbm.executeQuery(query);
		
		if(!rs.next()) {
			logger.error("The client <" + socket.getInetAddress() + "> attempted to authenticate with the following invalid credentials, the username does not exist:" +
					" username=" + username + " password=" + password);
			
			pw.write(ConnectionSettings.BAD_AUTHENTICATION + "\n");
			pw.flush();
			
			throw new BadAuthenticationException("The username combination was not found in the database", username, password, false);
		}
		
		if(!password.equals(rs.getString("password"))) {
			logger.error("The client <" + socket.getInetAddress() + "> attempted to authenticate with the following invalid credentials, the password is incorrect:" +
					" username=" + username + " password=" + password);
			
			pw.write(ConnectionSettings.BAD_AUTHENTICATION + "\n");
			pw.flush();
			
			int numFail = rs.getInt("num_fail") + 1;
			
			if(numFail == 3) {
				dbm.executeQuery("UPDATE USERS set num_fail='" + numFail + "', is_locked='Y' where username='" + username + "'");
			}
			else {
				dbm.executeQuery("UPDATE USERS set num_fail='" + numFail + "'where username='" + username + "'");
			}
		    			
			throw new BadAuthenticationException("The username/password combination was not found in the database", username, password, false);
		}
		
		
		
		
		boolean isLocked = "Y".equals(rs.getString("is_locked"));
		
		if(isLocked) {
			pw.write(ConnectionSettings.LOCKED_OUT + "\n");
			pw.flush();
			
			throw new BadAuthenticationException("The user is locked out", username, password, true);
		}	
		
		if(rs.getInt("num_fail") != 0) {
			dbm.executeQuery("UPDATE USERS set num_fail='0' where username='" + username + "'");
		}

		logger.info("The client <" + socket.getInetAddress() + "> has sent the appropriate greeting...returning the favour");

		pw.write(ConnectionSettings.GREETING + "\n");
		pw.flush();
	}
	
	private Request getClientRequest() throws InvalidRequestException, IOException {
		Request request = null;
		
		logger.info("Waiting on client <" + socket.getInetAddress() + "> to send a request");

		String line = readLine(socket);
		
		// UPLOAD
		if (line != null && line.startsWith(ConnectionSettings.UPLOAD_REQUEST)) {
			logger.info("The client <" + socket.getInetAddress() + "> has sent an upload request: <" + line + ">");
			
			String stringArguments = line.substring(ConnectionSettings.UPLOAD_REQUEST.length()).trim();
			String[] arguments = stringArguments.split(" ");
			
			String nameOnServer = arguments[0].trim();
			long fileSize = Long.parseLong(arguments[1].trim());
			
			request = new UploadRequest(nameOnServer, fileSize);			
		}
		// DOWNLOAD
		else if (line != null && line.startsWith(ConnectionSettings.DOWNLOAD_REQUEST)) {
			logger.info("The client <" + socket.getInetAddress() + "> has sent a download request: <" + line + ">");
			
			String stringArguments = line.substring(ConnectionSettings.DOWNLOAD_REQUEST.length()).trim();
			String[] arguments = stringArguments.split(" ");
			
			
			String downloadFilename = arguments[0].trim();
			long startPosition = Long.parseLong(arguments[1].trim());
			
			request = new DownloadRequest(downloadFilename, startPosition);			
		} 
		else if (ConnectionSettings.GOODBYE.equals(line)) {
			logger.info("The client <" + socket.getInetAddress() + "> has sent the goodbye message");
			request = new CloseRequest();
		}
		else if (line != null && line.startsWith(ConnectionSettings.DELETE_REQUEST)) {
			String stringArguments = line.substring(ConnectionSettings.DELETE_REQUEST.length()).trim();
			String[] arguments = stringArguments.split(" ");
			
			String filename = arguments[0];
			
			logger.info("The client <" + socket.getInetAddress() + "> has requested to delete file: " + filename);
			
			request = new DeleteRequest(filename);
		}
		else if (line != null && line.startsWith(ConnectionSettings.REMOTE_DOWNLOAD_REQUEST)) {
			String stringArguments = line.substring(ConnectionSettings.REMOTE_DOWNLOAD_REQUEST.length()).trim();
			String[] arguments = stringArguments.split(" ");
			
			String url = arguments[0];
			String serverLocation = arguments[1];
			
			logger.info("The client <" + socket.getInetAddress() + "> has requested to remotely download the file: " + url +
					" and store it here: " + serverLocation);
			
			request = new RemoteFileDownloadRequest(url, serverLocation);
		}
		else {
			logger.info("The client <" + socket.getInetAddress() + "> has sent an invalid request: <" + line + ">");
			throw new InvalidRequestException("An invalid client request occurred", line);
		}
		
		return request;
	}
	
	private boolean deleteFile(DeleteRequest request) {
		File fileToDelete = new File(request.getFileName());
		
		if(!fileToDelete.exists()) {
			logger.error("Unable to delete the file < " + request.getFileName() + "> as the file does not exist");
			
			pw.write(ConnectionSettings.DELETE_FAIL + "\n");
			pw.flush();
			
			return false;
		}
				
		if (!fileToDelete.delete()) {
			logger.error("Unable to delete the file < " + request.getFileName() + ">");
			
			pw.write(ConnectionSettings.DELETE_FAIL + "\n");
			pw.flush();
			
		    return false;
		}
		
		pw.write(ConnectionSettings.DELETE_SUCCESS + "\n");
		pw.flush();
		
		return true;
	}
	
	private boolean sendFile(DownloadRequest request) throws FileNotFoundException, IOException {
		File downloadFile = new File(request.getFileName());
		
		logger.info("The client <" + socket.getInetAddress() + "> is requesting the following file: " +
				downloadFile.getName() + " <" +  downloadFile.length() + " bytes>");

		FileInputStream fis = null;
		
		fis = new FileInputStream(downloadFile);
		logger.warn("Unable to read the file");
		

		logger.info("Telling the client <" + socket.getInetAddress() + "> that their download request is going to be serviced");

		pw.write(ConnectionSettings.DOWNLOAD_OK + " " + downloadFile.length() + "\n");
		pw.flush();

		byte[] buffer = new byte[64000];

		boolean readPartFile = request.getStartPosition() != 0;
		
		long totalBytesRead = 0;
		
		while (true) {
			int numBytesRead = 0;
			numBytesRead = fis.read(buffer);
			if (numBytesRead == -1)	break;
			
			totalBytesRead += numBytesRead;

			if(readPartFile) {
				if(totalBytesRead > request.getStartPosition()) {
					int numBytesLeft = (int)(request.getStartPosition() % 64000);
					out.write(buffer, numBytesLeft, numBytesRead - numBytesLeft);
					out.flush();
					readPartFile = false;
				}
			}
			else {
				out.write(buffer, 0, numBytesRead);
				out.flush();
			}
			
			logger.debug("Sent " + totalBytesRead + "/" + downloadFile.length() + " bytes");
		}
		
		fis.close();
		
		logger.info("The requested download file has been send in its entirety");

		String line = readLine(socket);

		if (!ConnectionSettings.DOWNLOAD_FINISHED.equals(line)) {
			logger.warn("The client did not send the download finished message.  It sent: <" + line + ">");
			return false;
		}
		
		return true;
	}
	
	private boolean receiveFile(UploadRequest request) throws FileNotFoundException, IOException {
		logger.info("The client <" + socket.getInetAddress() + "> has sent an upload request for a file of size " + 
				request.getFileSize() + " bytes");
		
		File destinationPart = new File(request.getNameOnServer() + ".part");
		File destination = new File(request.getNameOnServer());
		
		if(destination.exists()) {
			logger.error("Unable to upload the file as the file already exists: " + destination.getPath());
			return false;
		}
		
		long partFileLength = destinationPart.exists() ? destinationPart.length() : 0;
		
		pw.write(ConnectionSettings.UPLOAD_OK + " " + partFileLength + "\n");
		pw.flush();

		FileOutputStream fileOut = new FileOutputStream(destinationPart, partFileLength != 0);

		byte[] buffer = new byte[64000];
		long totalBytesRead = partFileLength;

		InputStream inFile = socket.getInputStream();

		logger.info("Starting the upload of the file");

		while (true) {
			if (totalBytesRead == request.getFileSize())
				break;
			
			int numBytesRead = inFile.read(buffer);
			if (numBytesRead == -1)
				break;
			
			fileOut.write(buffer, 0, numBytesRead);
			fileOut.flush();

			totalBytesRead += numBytesRead;
			
			logger.debug("Received " + totalBytesRead + "/" + request.getFileSize() + " bytes of the file");
		}
		
		fileOut.close();
		
		if(!destinationPart.renameTo(destination)) {
			logger.error("Although the .part file is complete, could not rename the file to remove the .part..." +
					"please fix the problem and run the upload again, or manually remove the .part extension");
			return false;
		}

		if (totalBytesRead != request.getFileSize()) {
			logger.warn("Only received " + totalBytesRead + "/" + request.getFileSize() + " bytes of the uploaded file");
			return false;
		}

		logger.info("Received the file in its entirety");

		pw.write(ConnectionSettings.UPLOAD_FINISHED + "\n");
		pw.flush();
		
		return true;
	}
	
	private boolean remoteFileDownload(RemoteFileDownloadRequest request) {
		File serverFilePart = new File(request.getServerLocation() + ".part");
		File serverFile = new File(request.getServerLocation());
		
		if(serverFilePart.exists()) {
			logger.error("Unable to download file as the part file already exists < " + serverFilePart.getName() + ">");
			
			pw.write(ConnectionSettings.REMOTE_DOWNLOAD_DECLINE + "\n");
			pw.flush();
			
			return false;
		}
		
		if(serverFile.exists()) {
			logger.error("Unable to download file as the file already exists < " + serverFile.getName() + ">");
			
			pw.write(ConnectionSettings.REMOTE_DOWNLOAD_DECLINE + "\n");
			pw.flush();
			
			return false;
		}
		
		pw.write(ConnectionSettings.REMOTE_DOWNLOAD_ACCEPT + "\n");
		pw.flush();
		
		try {
			URL url = new URL(request.getUrl());
		    ReadableByteChannel rbc = Channels.newChannel(url.openStream());
		    FileOutputStream fos = new FileOutputStream(serverFilePart);
		    fos.getChannel().transferFrom(rbc, 0, 1 << 24);
		    fos.close();
		    
		    boolean success = serverFilePart.renameTo(serverFile);
		    
		    if(!success) {
		    	logger.error("Although the .part file was fully downloaded, it could not be renamed to remove the .part..." +
						"please manually remove the .part extension, or delete the .part file and request the remote download again");
				return false;
		    }
		} catch (MalformedURLException e) {
			logger.error("Bad URL: " + request.getUrl(), e);
			return false;
		} catch (IOException e) {
			logger.error("An error has occurred while downloading the file", e);
			return false;
		}
		
		return true;
	}
	
	private String readLine(Socket socket) throws IOException {
		String line = new String();
		int c;

		while ((c = socket.getInputStream().read()) != '\n') {
			if(c == -1) {
				throw new IOException("The socket closed before being able to read the end of the line");
			}
			
			line += (char) c;
		}

		return line.trim();
	}
	
	private void closeClientConnection() {
		try {
			logger.info("Closing the client socket");
			socket.close();
		} catch (IOException e) {
			logger.error("An error occurred while closing the client socket", e);
		}
	}
	
}
