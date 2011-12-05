package com.cs456.client;

import android.content.Context;

public class Track {
	
	private Context con = null;
	private static Track singleton = null; // A reference to the singleton object
	
	/**
	 * Gets the instance of the singleton logger
	 * @return The singleton object
	 */
	public static Track getInstance() {
		if(singleton == null) {
			singleton = new Track();
		}
		
		return singleton;
	}
	
	/**
	 * Initializes the logger by opening the log file
	 */
	private Track() {
	}
	
	public Context getContext() {
	    return this.con; 
	}
	
	public void setContext(Context context) {
	    this.con = context;
	}
}

