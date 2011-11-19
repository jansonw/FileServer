package com.cs456.project.server;

import java.io.IOException;
import java.net.Socket;

public abstract class BaseClass {
	protected final int port = 62009;
	protected final String hostname = "localhost";
	
	protected final String GREETING = "HELLO";
	protected final String GOODBYE = "GOODBYE";
	protected final String UPLOAD_REQUEST = "UPLOAD";
	protected final String UPLOAD_OK = "UPLOAD_OK";
	protected final String UPLOAD_FINISHED = "UPLOAD_FINISHED";
	protected final String DOWNLOAD_REQUEST = "DOWNLOAD";
	protected final String DOWNLOAD_OK = "DOWNLOAD_OK";
	protected final String DOWNLOAD_FINISHED = "DOWNLOAD_FINISHED";
	protected final String DELETE_REQUEST = "DELETE_REQUEST";
	protected final String DELETE_SUCCESS = "DELETE_SUCCESS";
	protected final String DELETE_FAIL = "DELETE_FAIL";
	
	public String readLine(Socket socket) throws IOException {
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
}
