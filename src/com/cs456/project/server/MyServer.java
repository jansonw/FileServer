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
			logger.info("The server has started up on port " + port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		while (true) {
			try {
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
					
					clientSocket.close();
					continue;
				}

				logger.info("The client <" + clientSocket.getInetAddress() + "> has sent the appropriate greeting...returning the favour");

				pw.write(GREETING + "\n");
				pw.flush();

				logger.info("Waiting on client <" + clientSocket.getInetAddress() + "> to send a request");

				line = readLine(clientSocket);

				if (line != null && line.startsWith(UPLOAD_REQUEST)) {
					long fileLength = Long.parseLong(line.substring(
							UPLOAD_REQUEST.length()).trim());

					logger.info("The client <" + clientSocket.getInetAddress() + "> has sent an upload request for a file of size " + 
							fileLength + " bytes");
					
					pw.write(UPLOAD_OK + "\n");
					pw.flush();

					File destination = new File("C:\\Users\\Janson\\workspace\\FileServer\\upload\\uploaded.mp3");

					FileOutputStream fileOut = new FileOutputStream(destination);

					byte[] buffer = new byte[64000];
					long totalBytesRead = 0;

					InputStream inFile = clientSocket.getInputStream();

					logger.info("Starting the upload of the file");
					
					while (true) {
						if (totalBytesRead == fileLength)
							break;
						
						int numBytesRead = inFile.read(buffer);
						if (numBytesRead == -1)
							break;
						
						fileOut.write(buffer, 0, numBytesRead);
						fileOut.flush();

						totalBytesRead += numBytesRead;
						
						logger.debug("Received " + totalBytesRead + "/" + fileLength + " bytes of the file");
					}

					if (totalBytesRead != fileLength) {
						logger.warn("Only received " + totalBytesRead + "/" + fileLength + " bytes of the uploaded file");
						clientSocket.close();
						continue;
					}

					logger.info("Received the file in its entirety");

					pw.write(UPLOAD_FINISHED + "\n");
					pw.flush();
					
				} else if (line != null && line.startsWith(DOWNLOAD_REQUEST)) {
					File downloadFile = new File(line.substring(DOWNLOAD_REQUEST.length()).trim());
					
					logger.info("The client <" + clientSocket.getInetAddress() + "> is requesting the following file: " +
							downloadFile.getName() + " <" +  downloadFile.length() + " bytes>");

					FileInputStream fis = null;

					try {
						fis = new FileInputStream(downloadFile);
					} catch (FileNotFoundException ex) {
						logger.warn("Unable to read the file");
						clientSocket.close();
						continue;
					}

					logger.info("Telling the client <" + clientSocket.getInetAddress() + "> that their download request is going to be serviced");

					pw.write(DOWNLOAD_OK + " " + downloadFile.length() + "\n");
					pw.flush();

					byte[] buffer = new byte[64000];

					long totalBytesRead = 0;
					
					while (true) {
						int numBytesRead = fis.read(buffer);
						if (numBytesRead == -1)
							break;

						out.write(buffer, 0, numBytesRead);
						out.flush();
						
						totalBytesRead += numBytesRead;
						
						logger.debug("Sent " + totalBytesRead + "/" + downloadFile.length() + " bytes");
					}
					
					logger.info("The requested download file has been send in its entirety");

					line = readLine(clientSocket);

					if (!DOWNLOAD_FINISHED.equals(line)) {
						logger.warn("The client did not send the download finished message.  It sent: <" + line + ">");
						clientSocket.close();
						continue;
					}
				} else if (GOODBYE.equals(line)) {
					logger.info("The client <" + clientSocket.getInetAddress() + "> has sent the goodbye message");
					
					clientSocket.close();
					continue;
				} else {
					logger.info("The client <" + clientSocket.getInetAddress() + "> has sent an invalid request: <" + line + ">");
					clientSocket.close();
					continue;
				}

				logger.info("Waiting on the client <" + clientSocket.getInetAddress() + "> to send the goodbye message");
				
				line = readLine(clientSocket);

				if (!GOODBYE.equals(line)) {
					logger.warn("The client <" + clientSocket.getInetAddress() + "> did not send the goodbye message.  It sent: <" + line + ">");
				}
				
				logger.info("The client <" + clientSocket.getInetAddress() + "> has sent the goodbye message");

			} catch (IOException e) {
				logger.error("An error has occurred:" + e);
			}

		}

	}

	public static void main(String[] args) {
		MyServer ms = new MyServer();
		ms.start();
	}
}
