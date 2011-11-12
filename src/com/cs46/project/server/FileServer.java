package com.cs46.project.server;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.log4j.Logger;

public class FileServer {
	private static final String WWW_HOME = "C:\\Users\\Janson\\workspace\\CS456 Project";
	private static final int PORT = 80;
	private static Logger logger = Logger.getLogger(FileServer.class);

	public static void main(String[] args) {
		// open server socket
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(PORT);
		} catch (IOException e) {
			System.err.println("Could not start server:\t" + e);
			System.exit(-1);
		}
		
		logger.info("The file server is listening on port " + PORT);

		// request handler loop
		while (true) {
			Socket client = null;
			try {
				// wait for request
				client = socket.accept();
				
				BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				OutputStream out = new BufferedOutputStream(client.getOutputStream());

				// read first line of request (ignore the rest)
				String request = in.readLine();
				if (request == null) {
					continue;
				}
				
				logger.info("Accepted new connection.\t" + getClientRequestString(client, request));
				
				while (true) {
					String junk = in.readLine();
					if (junk == null || junk.length() == 0) {
						break;
					}
				}

				if (!request.startsWith("GET")
						|| request.length() < 14
						|| !(request.endsWith("HTTP/1.0") 
						|| request.endsWith("HTTP/1.1"))) {
					logger.error("Bad Request.\n" + getClientRequestString(client, request));
				} 
				else {
					String req = request.substring(4, request.length() - 9).trim();
					
					logger.info("Received a request for file: " + req);
					
					if (req.indexOf("..") != -1 || req.indexOf("/.ht") != -1|| req.endsWith("~")) {
						// evil hacker trying to read non-wwwhome or secret file
						logger.error("No permission to access this file.\t" + getClientRequestString(client, request));
					} 
					else {
						String path = WWW_HOME + "/" + req;
						
						logger.info("Looking for file: " + path);
						
						File f = new File(path);

						try {
							// send file
							InputStream file = new FileInputStream(f);
							sendFile(file, out); // send raw file
							
							logger.info("File was sent");
						} catch (FileNotFoundException e) {
							logger.error("The requested URL was not found on this server.\t" + getClientRequestString(client, request));
						}
					}
				}
				
				out.flush();
			} catch (IOException e) {
				logger.error(e);
			}
			
			try {
				if (client != null)
					client.close();
			} catch (IOException e) {
				logger.error("An error has occurred while trying to close the client connection:\t" + e);
			}
			
		}
	}

	private static String getClientRequestString(Socket connection, String msg) {
		return("[ " + connection.getInetAddress().getHostAddress() + ":" + connection.getPort() + "] " + msg);
	}
	
	private static void sendFile(InputStream file, OutputStream out) {
		try {
			byte[] buffer = new byte[1000];
			while (file.available() > 0)
				out.write(buffer, 0, file.read(buffer));
		} catch (IOException e) {
			logger.error("The following error occurred while trying to send the file:\t" + e);
		}
	}
}