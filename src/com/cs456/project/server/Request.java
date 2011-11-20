package com.cs456.project.server;

public abstract class Request {
	public static enum RequestType { DOWNLOAD, UPLOAD, GOODBYE, DELETE, REMOTE_FILE_DOWNLOAD }
	
	protected RequestType request = null;
	
	public RequestType getRequestType() {
		return this.request;
	}
}