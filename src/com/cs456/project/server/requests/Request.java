package com.cs456.project.server.requests;

import com.cs456.project.common.Credentials;

public abstract class Request {
	public static enum RequestType { DOWNLOAD, UPLOAD, GOODBYE, DELETE, REMOTE_FILE_DOWNLOAD }
	
	protected RequestType request = null;
	protected Credentials credentials = null;
	
	protected Request(Credentials credentials) {
		this.credentials = credentials;
	}
	
	public RequestType getRequestType() {
		return request;
	}
	
	public String getUsername() {
		return (credentials == null) ? null : credentials.getUsername();
	}
	
	public String getPassword() {
		return (credentials == null) ? null : credentials.getPassword();
	}
}
