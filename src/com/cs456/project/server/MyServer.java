package com.cs456.project.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.apache.log4j.Logger;

public class MyServer extends BaseClass {
	private static Logger logger = Logger.getLogger(MyServer.class);
	
	ServerSocket mySocket = null;
	Socket clientSocket = null;
	OutputStream out = null;
	PrintWriter pw = null;
	
	public void start() {
		try {
			mySocket = new ServerSocket(port);
			mySocket.setReuseAddress(true);
			mySocket.setSoTimeout(0);
		} catch (SocketException e) {
			logger.error("Unable to open server socket on port " + port, e);
			return;
		}
		catch (IOException e) {
			logger.error("Unable to configure server socket connection", e);
			return;
		}
		
		logger.info("The server has started up on port " + port);
		
		while(true) {
			try {
				getClientConnection();
			} catch (IOException e) {
				logger.error("An error occurred while initiating connection with the client", e);
				closeClientConnection();
				continue;
			}
			
			Request request = null;
			try {
				request = getClientRequest();
			} catch (InvalidRequestException e) {
				logger.error("The client <" + clientSocket.getInetAddress() + "> has sent an invalid request: <" + e.getInvalidRequest() + ">");
				closeClientConnection();
				continue;
			} catch (IOException e) {
				logger.error("An error occurred when trying to obtain the clients <" + clientSocket.getInetAddress() + "> request", e);
				closeClientConnection();
				continue;
			}
						
			switch(request.getRequestType()) {
			case DOWNLOAD:
				try {
					boolean success = sendFile((DownloadRequest)request);
					
					if(!success) {
						logger.error("Sending the requested file to the client was not successful");
						closeClientConnection();
						continue;
					}					
				} catch (FileNotFoundException e) {
					logger.error("The file requested to be downloaded could not be found", e);
					closeClientConnection();
					continue;
				} catch (IOException e) {
					logger.error("An error occurred while sending the requested file", e);
					closeClientConnection();
					continue;
				}
				break;
			case UPLOAD:
				try {
					boolean success = receiveFile((UploadRequest)request);
					
					if(!success) {
						logger.error("Did not receive the entire file being uploaded.  The client must reconnect and send the rest of it.");
						closeClientConnection();
						continue;
					}
				} catch (FileNotFoundException e) {
					logger.error("Could not create the requested file on the server", e);
					closeClientConnection();
					continue;
				} catch (IOException e) {
					logger.error("An error occurred while receiving the requested file", e);
					closeClientConnection();
					continue;
				}
				
				break;
			case GOODBYE:
				closeClientConnection();
				continue;
			case DELETE:
				boolean success = deleteFile((DeleteRequest)request);
				
				if(!success) {
					logger.error("Failed to delete the file");
					closeClientConnection();
					continue;
				}
				break;
			}
		}
	}
	
	private void getClientConnection() throws IOException {
		logger.info("The server is waiting for a client");

		clientSocket = mySocket.accept();

		logger.info("A client <" + clientSocket.getInetAddress() + "> has connected to the server");

		out = clientSocket.getOutputStream();
		pw = new PrintWriter(out);

		String line = null;

		logger.info("Waiting for greeting from client");

		line = readLine(clientSocket);
		if (!GREETING.equals(line)) {
			logger.info("The client did not properly do the handshake, " +
					"and thus the client <" + clientSocket.getInetAddress() + "> is being rejected");
			
			
		}

		logger.info("The client <" + clientSocket.getInetAddress() + "> has sent the appropriate greeting...returning the favour");

		pw.write(GREETING + "\n");
		pw.flush();
	}
	
	private Request getClientRequest() throws InvalidRequestException, IOException {
		Request request = null;
		
		logger.info("Waiting on client <" + clientSocket.getInetAddress() + "> to send a request");

		String line = readLine(clientSocket);
		
		// UPLOAD
		if (line != null && line.startsWith(UPLOAD_REQUEST)) {
			logger.info("The client <" + clientSocket.getInetAddress() + "> has sent an upload request: <" + line + ">");
			
			String stringArguments = line.substring(UPLOAD_REQUEST.length()).trim();
			String[] arguments = stringArguments.split(" ");
			
			String nameOnServer = arguments[0].trim();
			long fileSize = Long.parseLong(arguments[1].trim());
			
			request = new UploadRequest(nameOnServer, fileSize);			
		}
		// DOWNLOAD
		else if (line != null && line.startsWith(DOWNLOAD_REQUEST)) {
			logger.info("The client <" + clientSocket.getInetAddress() + "> has sent a download request: <" + line + ">");
			
			String stringArguments = line.substring(DOWNLOAD_REQUEST.length()).trim();
			String[] arguments = stringArguments.split(" ");
			
			
			String downloadFilename = arguments[0].trim();
			long startPosition = Long.parseLong(arguments[1].trim());
			
			request = new DownloadRequest(downloadFilename, startPosition);			
		} 
		else if (GOODBYE.equals(line)) {
			logger.info("The client <" + clientSocket.getInetAddress() + "> has sent the goodbye message");
			request = new CloseRequest();
		}
		else if (line != null && line.startsWith(DELETE_REQUEST)) {
			String stringArguments = line.substring(DELETE_REQUEST.length()).trim();
			String[] arguments = stringArguments.split(" ");
			
			String filename = arguments[0];
			
			logger.info("The client <" + clientSocket.getInetAddress() + "> has requested to delete file: " + filename);
			
			request = new DeleteRequest(filename);
		}
		else {
			logger.info("The client <" + clientSocket.getInetAddress() + "> has sent an invalid request: <" + line + ">");
			throw new InvalidRequestException("An invalid client request occurred", line);
		}
		
		return request;
	}
	
	private boolean deleteFile(DeleteRequest request) {
		File fileToDelete = new File(request.getFileName());
		
		if(!fileToDelete.exists()) {
			logger.error("Unable to delete the file < " + request.getFileName() + "> as the file does not exist");
			
			pw.write(DELETE_FAIL + "\n");
			pw.flush();
			
			return false;
		}
				
		if (!fileToDelete.delete()) {
			logger.error("Unable to delete the file < " + request.getFileName() + ">");
			
			pw.write(DELETE_FAIL + "\n");
			pw.flush();
			
		    return false;
		}
		
		pw.write(DELETE_SUCCESS + "\n");
		pw.flush();
		
		return true;
	}
	
	private boolean sendFile(DownloadRequest request) throws FileNotFoundException, IOException {
		File downloadFile = new File(request.getFileName());
		
		logger.info("The client <" + clientSocket.getInetAddress() + "> is requesting the following file: " +
				downloadFile.getName() + " <" +  downloadFile.length() + " bytes>");

		FileInputStream fis = null;
		
		fis = new FileInputStream(downloadFile);
		logger.warn("Unable to read the file");
		

		logger.info("Telling the client <" + clientSocket.getInetAddress() + "> that their download request is going to be serviced");

		pw.write(DOWNLOAD_OK + " " + downloadFile.length() + "\n");
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

		String line = readLine(clientSocket);

		if (!DOWNLOAD_FINISHED.equals(line)) {
			logger.warn("The client did not send the download finished message.  It sent: <" + line + ">");
			return false;
		}
		
		return true;
	}
	
	private boolean receiveFile(UploadRequest request) throws FileNotFoundException, IOException {
		logger.info("The client <" + clientSocket.getInetAddress() + "> has sent an upload request for a file of size " + 
				request.getFileSize() + " bytes");
		
		File destinationPart = new File(request.getNameOnServer() + ".part");
		File destination = new File(request.getNameOnServer());
		
		if(destination.exists()) {
			logger.error("Unable to upload the file as the file already exists: " + destination.getPath());
			return false;
		}
		
		long partFileLength = destinationPart.exists() ? destinationPart.length() : 0;
		
		pw.write(UPLOAD_OK + " " + partFileLength + "\n");
		pw.flush();

		FileOutputStream fileOut = new FileOutputStream(destinationPart, partFileLength != 0);

		byte[] buffer = new byte[64000];
		long totalBytesRead = partFileLength;

		InputStream inFile = clientSocket.getInputStream();

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

		pw.write(UPLOAD_FINISHED + "\n");
		pw.flush();
		
		return true;
	}
	
	private void closeClientConnection() {
		try {
			logger.info("Closing the client socket");
			clientSocket.close();
		} catch (IOException e) {
			logger.error("An error occurred while closing the client socket", e);
		}
	}


	public static void main(String[] args) {
		MyServer ms = new MyServer();
		ms.start();
	}
}
