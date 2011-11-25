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

import com.cs456.project.common.ConnectionSettings;
import com.cs456.project.common.Credentials;
import com.cs456.project.common.FileWrapper;
import com.cs456.project.exceptions.AuthenticationException;
import com.cs456.project.exceptions.InvalidRequestException;
import com.cs456.project.exceptions.RegistrationException;
import com.cs456.project.exceptions.RequestExecutionException;
import com.cs456.project.server.database.DatabaseManager;
import com.cs456.project.server.requests.DeleteRequest;
import com.cs456.project.server.requests.DownloadRequest;
import com.cs456.project.server.requests.FileExistanceRequest;
import com.cs456.project.server.requests.PasswordChangeRequest;
import com.cs456.project.server.requests.RemoteFileDownloadRequest;
import com.cs456.project.server.requests.Request;
import com.cs456.project.server.requests.UploadRequest;

public class ServerConnectionThread extends Thread {
	private static Logger logger = Logger.getLogger(ServerConnectionThread.class);
	private Socket socket = null;
	
	private final String rootUploadDir = "upload";
	
	OutputStream out = null;
	PrintWriter pw = null;
	
	DatabaseManager dbm = null;
	
	ServerConnectionThread(Socket socket) {
		this.dbm = DatabaseManager.getInstance();
		this.socket = socket;
	}

	@Override
	public void run() {
		Credentials credentials = null;
		
		try {
			credentials = initiateClientConnection();
			
			if(credentials == null) {
				logger.info("The client successfully registered a new account.  Closing the connection...");
				closeClientConnection();
				return;
			}
		} catch (IOException e) {
			logger.error("An error occurred while initiating connection with the client", e);
			closeClientConnection();
			return;
		} catch (SQLException e) {
			logger.error("An error occurred while querying the database for the requested username and password", e);
			closeClientConnection();
			return;
		} catch (AuthenticationException e) {
			logger.error("The client was not authenticated and thus is being kicked out.  The attempted username and password were:" +
					" username=" + e.getUsername() + " password=" + e.getPassword());
			closeClientConnection();
			return;
		} catch (RegistrationException e) {
			logger.error("An error occurred while registering the client.", e);
			closeClientConnection();
			return;
		} catch (InvalidRequestException e) {
			logger.error("The user sent an invalid request during authentication/registration.", e);
		}
		
		Request request = null;
		try {
			request = getClientRequest(credentials);
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
		case PASSWORD_CHANGE:
			boolean passSuccess = passwordChange((PasswordChangeRequest)request);
			
			if(!passSuccess) {
				logger.error("Failed to change the user's password");
				closeClientConnection();
				return;
			}
			break;
		case FILE_EXISTANCE:
			fileExistance((FileExistanceRequest)request);
			break;
		}
		
		closeClientConnection();		
	}
	
	
	private Credentials initiateClientConnection() throws IOException, SQLException, AuthenticationException, RegistrationException, InvalidRequestException {
		out = socket.getOutputStream();
		pw = new PrintWriter(out);

		String line = null;

		logger.info("Waiting for greeting from client");

		line = readLine(socket);
		
		Credentials credentials = null;
		
		if(line != null && line.startsWith(ConnectionSettings.GREETING)) {
			credentials = authenticateClient(line);
		}
		else if(line != null && line.startsWith(ConnectionSettings.REGISTRATION_REQUEST)) {
			registerUser(line);
		}
		else {
			logger.info("The client did not properly do the handshake, " +
					"and thus the client <" + socket.getInetAddress() + "> is being rejected");
			
		}
		
		return credentials;
	}
	
	private Credentials authenticateClient(String line) throws SQLException, AuthenticationException, InvalidRequestException {
		String stringArguments = line.substring(ConnectionSettings.GREETING.length()).trim();
		String[] arguments = stringArguments.split(" ");
		
		if(arguments.length != 2) {
			throw new InvalidRequestException("The user did not provide their username and/or password", line);
		}
		
		String username = arguments[0];
		String password = arguments[1];
		
		String query = "Select * from USERS where username=upper('" + username + "')";
		ResultSet rs = dbm.executeQuery(query);
		
		if(!rs.next()) {
			logger.error("The client <" + socket.getInetAddress() + "> attempted to authenticate with the following invalid credentials, the username does not exist:" +
					" username=" + username + " password=" + password);
			
			pw.write(ConnectionSettings.BAD_AUTHENTICATION + "\n");
			pw.flush();
			
			throw new AuthenticationException("The username was not found in the database", username, password, false);
		}
		
		if(!password.equals(rs.getString("password"))) {
			logger.error("The client <" + socket.getInetAddress() + "> attempted to authenticate with the following invalid credentials, the password is incorrect:" +
					" username=" + username + " password=" + password);
			
			pw.write(ConnectionSettings.BAD_AUTHENTICATION + "\n");
			pw.flush();
			
			int numFail = rs.getInt("num_fail") + 1;
			
			if(numFail == 3) {
				dbm.executeQuery("UPDATE USERS set num_fail='" + numFail + "', is_locked='Y' where username=upper('" + username + "')");
			}
			else {
				dbm.executeQuery("UPDATE USERS set num_fail='" + numFail + "'where username=upper('" + username + "')");
			}
		    			
			throw new AuthenticationException("The username/password combination was not found in the database", username, password, false);
		}
		
		
		
		
		boolean isLocked = "Y".equals(rs.getString("is_locked"));
		
		if(isLocked) {
			pw.write(ConnectionSettings.LOCKED_OUT + "\n");
			pw.flush();
			
			throw new AuthenticationException("The user is locked out", username, password, true);
		}	
		
		if(rs.getInt("num_fail") != 0) {
			dbm.executeQuery("UPDATE USERS set num_fail='0' where username='" + username + "'");
		}

		logger.info("The client <" + socket.getInetAddress() + "> has sent the appropriate greeting...returning the favour");

		pw.write(ConnectionSettings.GREETING + "\n");
		pw.flush();
		
		return new Credentials(username, password);
	}
	
	private Request getClientRequest(Credentials credentials) throws InvalidRequestException, IOException {
		Request request = null;
		
		logger.info("Waiting on client <" + socket.getInetAddress() + "> to send a request");

		String line = readLine(socket);
		
		// UPLOAD
		if (line != null && line.startsWith(ConnectionSettings.UPLOAD_REQUEST)) {
			logger.info("The client <" + socket.getInetAddress() + "> has sent an upload request: <" + line + ">");
			
			String stringArguments = line.substring(ConnectionSettings.UPLOAD_REQUEST.length()).trim();
			String[] arguments = stringArguments.split(" ");
			
			if(arguments.length != 3) {
				throw new InvalidRequestException("The user did not provide the correct number of arguments for their upload request", line);
			}
			
			String nameOnServer = arguments[0].trim();
			long fileSize = Long.parseLong(arguments[1].trim());
			boolean isShared = FileWrapper.charToBoolean(arguments[2].trim());
			
			request = new UploadRequest(nameOnServer, fileSize, isShared, credentials);			
		}
		// DOWNLOAD
		else if (line != null && line.startsWith(ConnectionSettings.DOWNLOAD_REQUEST)) {
			logger.info("The client <" + socket.getInetAddress() + "> has sent a download request: <" + line + ">");
			
			String stringArguments = line.substring(ConnectionSettings.DOWNLOAD_REQUEST.length()).trim();
			String[] arguments = stringArguments.split(" ");
			
			if(arguments.length != 2) {
				throw new InvalidRequestException("The user did not provide the correct number of arguments for their download request", line);
			}
			
			String downloadFilename = arguments[0].trim();
			long startPosition = Long.parseLong(arguments[1].trim());
			
			request = new DownloadRequest(downloadFilename, startPosition, credentials);			
		}
		else if (line != null && line.startsWith(ConnectionSettings.DELETE_REQUEST)) {
			String stringArguments = line.substring(ConnectionSettings.DELETE_REQUEST.length()).trim();
			String[] arguments = stringArguments.split(" ");
			
			if(arguments.length != 1) {
				throw new InvalidRequestException("The user did not provide the correct number of arguments for their delete request", line);
			}
			
			String filename = arguments[0];
			
			logger.info("The client <" + socket.getInetAddress() + "> has requested to delete file: " + filename);
			
			request = new DeleteRequest(filename, credentials);
		}
		else if (line != null && line.startsWith(ConnectionSettings.REMOTE_DOWNLOAD_REQUEST)) {
			String stringArguments = line.substring(ConnectionSettings.REMOTE_DOWNLOAD_REQUEST.length()).trim();
			String[] arguments = stringArguments.split(" ");
			
			if(arguments.length != 3) {
				throw new InvalidRequestException("The user did not provide the correct number of arguments for their remote download request", line);
			}
			
			String url = arguments[0];
			String serverLocation = arguments[1];
			boolean isShared = FileWrapper.charToBoolean(arguments[2]);
			
			logger.info("The client <" + socket.getInetAddress() + "> has requested to remotely download the file: " + url +
					" and store it here: " + serverLocation);
			
			request = new RemoteFileDownloadRequest(url, serverLocation, isShared, credentials);
		}
		else if(line != null && line.startsWith(ConnectionSettings.PASSWORD_CHANGE_REQUEST)) {
			String stringArguments = line.substring(ConnectionSettings.PASSWORD_CHANGE_REQUEST.length()).trim();
			String[] arguments = stringArguments.split(" ");
			
			if(arguments.length != 2) {
				throw new InvalidRequestException("The user did not provide the correct number of arguments for their password change request", line);
			}
			
			String oldPassword = arguments[0];
			String newPassword = arguments[1];
			
			logger.info("The client <" + socket.getInetAddress() + "> has requested to change their password from: " + oldPassword +
					" to: " + newPassword);
			
			request = new PasswordChangeRequest(oldPassword, newPassword, credentials);
		}
		else if(line != null && line.startsWith(ConnectionSettings.FILE_EXISTS_REQUEST)) {
			String stringArguments = line.substring(ConnectionSettings.FILE_EXISTS_REQUEST.length()).trim();
			String[] arguments = stringArguments.split(" ");
			
			if(arguments.length != 1) {
				throw new InvalidRequestException("The user did not provide the correct number of arguments for their file existance request", line);
			}
			
			String file = arguments[0];
			
			logger.info("The client <" + socket.getInetAddress() + "> has ask whether the file: " + file + "exists on the server");
			
			request = new FileExistanceRequest(file, credentials);
		}
		else {
			logger.info("The client <" + socket.getInetAddress() + "> has sent an invalid request: <" + line + ">");
			throw new InvalidRequestException("An invalid client request occurred", line);
		}
		
		return request;
	}
	
	private boolean deleteFile(DeleteRequest request) {
		if(request.getFileName().contains("..")) {
			logger.warn("User: " + request.getUsername() + " is trying to delete a file into another user's directory by using \"..\"." +
					" The path was: " + request.getFileName());
			
			pw.write(ConnectionSettings.DELETE_FAIL + "\n");
			pw.flush();
			return false;
		}
		
		String root = rootUploadDir + "\\" + request.getUsername().toUpperCase() + "\\";
		
		File fileToDelete = new File(root + request.getFileName());
		
		if(!fileToDelete.exists()) {
			logger.error("Unable to delete the file < " + request.getFileName() + "> as the file does not exist");
			
			pw.write(ConnectionSettings.DELETE_FAIL + "\n");
			pw.flush();
			
			return false;
		}
				
		try {
			FileWrapper wrapper = dbm.getFile(fileToDelete.getName());
			
			if(wrapper == null) {
				logger.error("Unable to delete the file < " + request.getFileName() + "> as the file does not exist");
				
				pw.write(ConnectionSettings.DELETE_FAIL + "\n");
				pw.flush();
			}
			
			if(!wrapper.getOwner().equals(request.getUsername())) {
				logger.error("The user: " + request.getUsername() + " was attempting to delete the file: " + wrapper.getFilePath() + " which is owned by: " + wrapper.getOwner());
				
				pw.write(ConnectionSettings.DELETE_FAIL + "\n");
				pw.flush();
			}
		} catch (SQLException e) {
			logger.error("A SQL error occurred while deleting the file: " + fileToDelete.getName(), e);
			
			pw.write(ConnectionSettings.DELETE_FAIL + "\n");
			pw.flush();
			
			return false;
			
		} 	
		
		try {
			dbm.deleteFile(fileToDelete.getName());
		} catch (SQLException e) {
			logger.error("A SQL error occurred while deleting the file: " + fileToDelete.getName(), e);
			
			pw.write(ConnectionSettings.DELETE_FAIL + "\n");
			pw.flush();
			
			return false;
			
		} catch (RequestExecutionException e) {
			logger.error("The deletion of the file record: " + fileToDelete.getName() + " failed", e);
			
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
		if(request.getFileName().contains("..")) {
			logger.warn("User: " + request.getUsername() + " is trying to download a file into another user's directory by using \"..\"." +
					" The path was: " + request.getFileName());
			
			pw.write(ConnectionSettings.DOWNLOAD_REJECT + "\n");
			pw.flush();
			return false;
		}
		
		String root = rootUploadDir + "\\" + request.getUsername().toUpperCase() + "\\";
		
		File downloadFile = new File(root + request.getFileName());
		
		try {
			FileWrapper wrapper = dbm.getFile(downloadFile.getName());
			
			if(wrapper == null) {
				logger.error("The file: " + downloadFile.getName() + " could not be found.");
				pw.write(ConnectionSettings.DOWNLOAD_REJECT + "\n");
				pw.flush();
				
				return false;
			}
			
			if(!wrapper.getOwner().equals(request.getUsername()) && !wrapper.isShared()) {
				logger.error("The user: " + request.getUsername() + " attempted to download the file: " + downloadFile.getName() + 
						" which is owned by: " + wrapper.getOwner() + " and is not shared");
				pw.write(ConnectionSettings.DOWNLOAD_REJECT + "\n");
				pw.flush();
			}
		} catch (SQLException e) {
			logger.error("A SQL error occurred while retrieving the file: " + downloadFile.getName() + " from the database", e);
			return false;		
		} 	
		
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
		
		logger.info("The requested download file has been sent in its entirety");

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
		
		if(request.getNameOnServer().contains("..")) {
			logger.warn("User: " + request.getUsername() + " is trying to upload a file into another user's directory by using \"..\"." +
					" The path was: " + request.getNameOnServer());
			
			pw.write(ConnectionSettings.UPLOAD_REJECT + "\n");
			pw.flush();
			return false;
		}
		
		String root = rootUploadDir + "\\" + request.getUsername().toUpperCase() + "\\";
		
		File destinationPart = new File(root + request.getNameOnServer() + ".part");
		File destination = new File(root + request.getNameOnServer());
		
		if(destination.exists()) {
			logger.error("Unable to upload the file as the file already exists: " + destination.getPath());
			
			pw.write(ConnectionSettings.UPLOAD_REJECT + "\n");
			pw.flush();
			return false;
		}
		
		long partFileLength = destinationPart.exists() ? destinationPart.length() : 0;
		
		FileOutputStream fileOut = new FileOutputStream(destinationPart, partFileLength != 0);
		
		FileWrapper wrapper = new FileWrapper(destinationPart.getName(), request.getUsername(), request.isShared(), false);
		if(partFileLength == 0) {
			try {
				dbm.addFile(wrapper);
			} catch (SQLException e) {
				logger.error("A SQL error occurred while adding a new file: " + wrapper.getFilePath() + " to the database", e);
				
				pw.write(ConnectionSettings.UPLOAD_FAIL + "\n");
				pw.flush();

				return false;				
			} catch (RequestExecutionException e) {
				logger.error("The insertion of the new file record: " + wrapper.getFilePath() + " failed", e);
				
				pw.write(ConnectionSettings.UPLOAD_FAIL + "\n");
				pw.flush();
				
				return false;
			}
		}
		else {
			try {
				dbm.updateFile(destinationPart.getName(), wrapper);
			} catch (SQLException e) {
				logger.error("A SQL error occurred while updating the file: " + wrapper.getFilePath() + " in the database", e);
				
				pw.write(ConnectionSettings.UPLOAD_FAIL + "\n");
				pw.flush();

				return false;
			} catch (RequestExecutionException e) {
				logger.error("The update of the file record: " + wrapper.getFilePath() + " failed", e);
				
				pw.write(ConnectionSettings.UPLOAD_FAIL + "\n");
				pw.flush();

				return false;
			}
		} 
		
		pw.write(ConnectionSettings.UPLOAD_OK + " " + partFileLength + "\n");
		pw.flush();
		
		
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
		
		if (totalBytesRead != request.getFileSize()) {
			logger.warn("Only received " + totalBytesRead + "/" + request.getFileSize() + " bytes of the uploaded file");
			
			pw.write(ConnectionSettings.UPLOAD_FAIL + " " + totalBytesRead + "\n");
			pw.flush();
			return false;
		}
		
		if(!destinationPart.renameTo(destination)) {
			logger.error("Although the .part file is complete, could not rename the file to remove the .part..." +
					"please fix the problem and run the upload again, or manually remove the .part extension");
			
			pw.write(ConnectionSettings.UPLOAD_FAIL + " " + totalBytesRead + "\n");
			pw.flush();
			return false;
		}
		
		wrapper = new FileWrapper(destination.getName(), request.getUsername(), request.isShared(), true);
		try {
			dbm.updateFile(destinationPart.getName(), wrapper);
		} catch (SQLException e) {
			logger.error("A SQL error occurred while updating the file: " + wrapper.getFilePath() + " in the database", e);
			
			pw.write(ConnectionSettings.UPLOAD_FAIL + "\n");
			pw.flush();

			return false;
		} catch (RequestExecutionException e) {
			logger.error("The update of the file record: " + wrapper.getFilePath() + " failed", e);
			
			pw.write(ConnectionSettings.UPLOAD_FAIL + "\n");
			pw.flush();

			return false;
		}		

		logger.info("Received the file in its entirety");

		pw.write(ConnectionSettings.UPLOAD_FINISHED + "\n");
		pw.flush();
		
		return true;
	}
	
	private boolean remoteFileDownload(RemoteFileDownloadRequest request) {
		if(request.getServerLocation().contains("..")) {
			logger.warn("User: " + request.getUsername() + " is trying to remotely download a file into another user's directory by using \"..\"." +
					" The path was: " + request.getServerLocation());
			
			pw.write(ConnectionSettings.REMOTE_DOWNLOAD_DECLINE + "\n");
			pw.flush();
			return false;
		}
		
		String root = rootUploadDir + "\\" + request.getUsername().toUpperCase() + "\\";
		
		File serverFilePart = new File(root + request.getServerLocation() + ".part");
		File serverFile = new File(root + request.getServerLocation());
		
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
		
		FileWrapper wrapper = new FileWrapper(serverFilePart.getName(), request.getUsername(), request.isShared(), false);
		try {
			dbm.addFile(wrapper);
		} catch (SQLException e) {
			logger.error("A SQL error occurred while adding a new file: " + wrapper.getFilePath() + " to the database", e);
			pw.write(ConnectionSettings.REMOTE_DOWNLOAD_DECLINE + "\n");
			pw.flush();
			
			return false;
			
		} catch (RequestExecutionException e) {
			logger.error("The insertion of the new file record: " + wrapper.getFilePath() + " failed", e);
			
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
		    
		    wrapper = new FileWrapper(serverFile.getName(), request.getUsername(), request.isShared(), true);
			try {
				dbm.updateFile(serverFilePart.getName(), wrapper);
			} catch (SQLException e) {
				logger.error("A SQL error occurred while updating the file: " + serverFilePart.getName() + " in the database", e);
				
				return false;
				
			} catch (RequestExecutionException e) {
				logger.error("The update of the file record: " + serverFilePart.getName() + " failed", e);
				
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
	
	private void registerUser(String line) throws RegistrationException, InvalidRequestException {
		String stringArguments = line.substring(ConnectionSettings.REGISTRATION_REQUEST.length()).trim();
		String[] arguments = stringArguments.split(" ");
		
		if(arguments.length != 2) {
			throw new InvalidRequestException("The user did not provide the correct number of arguments for their registration request", line);
		}
		
		String username = arguments[0];
		String password = arguments[1];
		
		logger.info("The client <" + socket.getInetAddress() + "> has requested to register a new user with username: " + username +
				" and password: " + password);
		
		try {
			dbm.registerUser(username, password);
		} catch (SQLException e) {
			logger.error("A SQL error occurred while trying to register a new user with username: " + username + " and password: " + password, e);
			
			pw.write(ConnectionSettings.REGISTRATION_FAILED + "\n");
			pw.flush();
			
			throw new RegistrationException("The registration was not successful due to an SQL error");
		} catch (RequestExecutionException e) {
			logger.error(e.getMessage());
			
			pw.write(ConnectionSettings.REGISTRATION_INVALID + "\n");
			pw.flush();
			
			throw new RegistrationException("The registration was not successful as the username is already in use");
		}
		
		pw.write(ConnectionSettings.REGISTRATION_OK + "\n");
		pw.flush();
		
		logger.info("The new client: " + username + " was successfully registered");
		
		File directory = new File(rootUploadDir + "\\" + username.toUpperCase());
		if(directory.exists()) {
			logger.error("Attempted to create home directory for new user but it already exists: " + username);
		}
		else {
			boolean success = directory.mkdir();
			if(!success) {
				logger.error("Unable to create the new user's home directory...otherwise the user is registered. <" + username.toUpperCase() + ">");
			}
		}
	}
	
	private boolean passwordChange(PasswordChangeRequest request) {
		try {
			dbm.passwordChange(request.getUsername(), request.getOldPassword(), request.getNewPassword());
		} catch (SQLException e) {
			pw.write(ConnectionSettings.PASSWORD_CHANGE_FAILED + "\n");
			pw.flush();
			
			logger.error("An SQL exception has occurred while attempting to change the user's password", e);
			return false;
		} catch (RequestExecutionException e) {
			pw.write(ConnectionSettings.PASSWORD_CHANGE_FAILED + "\n");
			pw.flush();
			
			logger.error("The old password the user provided was not correct");
			return false;
		}
		
		pw.write(ConnectionSettings.PASSWORD_CHANGE_OK + "\n");
		pw.flush();
		
		logger.info("The user's password was successfully changed");
		
		return true;
	}
	
	private void fileExistance(FileExistanceRequest request) {
		if(request.getFileName().contains("..")) {
			logger.warn("User: " + request.getUsername() + " is trying to look for a file in another user's directory by using \"..\"." +
					" The path was: " + request.getFileName());
			
			pw.write(ConnectionSettings.FILE_EXISTS_NO + "\n");
			pw.flush();
			return;
		}
		
		String root = rootUploadDir + "\\" + request.getUsername().toUpperCase() + "\\";
		
		File file = new File(root + request.getFileName());
		
		try {
			FileWrapper wrapper = dbm.getFile(file.getName());
			
			if(wrapper == null) {
				logger.info("The server does not have the requested file: " + request.getFileName());
				
				pw.write(ConnectionSettings.FILE_EXISTS_NO + "\n");
			}
			else {
				if(!wrapper.getOwner().equals(request.getUsername()) && !wrapper.isShared()) {
					logger.warn("The user: " + request.getUsername() + " requests the existance of a file: " + request.getFileName() + "which was: " + wrapper.getOwner() + " and was not shared");
					
					pw.write(ConnectionSettings.FILE_EXISTS_NO + "\n");
				}
				else {
					logger.info("The server has the requested file: " + request.getFileName());
					
					pw.write(ConnectionSettings.FILE_EXISTS_YES + "\n");
				}
			}
			
			pw.flush();
			
		} catch (SQLException e) {
			logger.error("A SQL error occurred while requesting for the existance of file: " + file.getName(), e);
			
			pw.write(ConnectionSettings.FILE_EXISTS_NO + "\n");
		}
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
