package com.cs456.project.server.requests;

import com.cs456.project.server.protocol.Credentials;

public abstract class Request {
	public static enum RequestType { DOWNLOAD, UPLOAD, GOODBYE, DELETE, REMOTE_FILE_DOWNLOAD }
	
	protected RequestType request = null;
	protected String username = null;
	protected String password = null;
	
	protected Request(Credentials credentials) {
		this.username = username;
		this.password = password;
	}
	
	public RequestType getRequestType() {
		return this.request;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public String getPassword() {
		return this.password;
	}
}
