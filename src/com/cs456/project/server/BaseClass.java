package com.cs456.project.server;

import java.io.IOException;
import java.net.Socket;

public class BaseClass {
	protected final int port = 62009;
	protected final String hostname = "localhost";
	
	protected final String GREETING = "HELLO";
	protected final String UPLOAD_REQUEST = "UPLOAD";
	protected final String UPLOAD_OK = "UPLOAD_OK";
	protected final String UPLOAD_FINISHED = "UPLOAD_FINISHED";
	protected final String DOWNLOAD_REQUEST = "DOWNLOAD";
	protected final String DOWNLOAD_OK = "DOWNLOAD_OK";
	protected final String DOWNLOAD_FINISHED = "DOWNLOAD_FINISHED";
	protected final String GOODBYE = "GOODBYE";
	protected final String END_OF_TRANSFER = "EOF";
	
	
	public String readLine(Socket socket) {
		String line = new String();
		int c;

		try {
			while ((c = socket.getInputStream().read()) != '\n') {
				line += (char) c;
			}
		} catch (IOException e) {
			System.err.println("Error reading line from stream");
			System.exit(-14);
		}

		// We may have a trailing return
		line = line.trim();

		// System.out.println("Read Line " + line);
		return line;
	}
}
