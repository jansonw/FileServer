package com.cs456.project.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.apache.log4j.Logger;

public class MyServer {
	private static Logger logger = Logger.getLogger(MyServer.class);
	
	ServerSocket mySocket = null;
	Socket clientSocket = null;
		
	public void start() {
		DatabaseManager dbm = DatabaseManager.getInstance();
		if(dbm == null) {
			logger.error("An error occurred while trying to connect to the database.  Stopping server!");
			return;
		}
		
		
		try {
			mySocket = new ServerSocket(ConnectionSettings.port);
			mySocket.setReuseAddress(true);
			mySocket.setSoTimeout(0);
		} catch (SocketException e) {
			logger.error("Unable to open server socket on port " + ConnectionSettings.port, e);
			return;
		}
		catch (IOException e) {
			logger.error("Unable to configure server socket connection", e);
			return;
		}
		
		logger.info("The server has started up on port " + ConnectionSettings.port);
		
		while(true) {
			logger.info("The server is waiting for a client");

			try {
				clientSocket = mySocket.accept();
				logger.info("A client <" + clientSocket.getInetAddress() + "> has connected to the server");
				
				new ServerConnectionThread(clientSocket).start();
			} catch (IOException e) {
				logger.error("An error occurred while accepting the client connection", e);
			}
		}
	}
	
	public static void main(String[] args) {
		new MyServer().start();		
	}
}
