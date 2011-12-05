package com.cs456.client;

import com.cs456.project.client.ClientConnection;

public class CC {
	
	private ClientConnection cc;
	private static CC singleton = null; // A reference to the singleton object
	
	/**
	 * Gets the instance of the singleton logger
	 * @return The singleton object
	 */
	public static CC getInstance() {
		if(singleton == null) {
			singleton = new CC();
		}
		
		return singleton;
	}
	
	/**
	 * Initializes the logger by opening the log file
	 */
	private CC() {
	    cc = new ClientConnection(null);
	}
	
	public ClientConnection getCC() {
	    return this.cc; 
	}
	
	public void setCC(ClientConnection context) {
	    this.cc = context;
	}
}

