package com.cs456.project.server;

public class UploadRequest extends Request {
	private String nameOnServer = null;
	private long fileSize = -1;
	
	public UploadRequest(String nameOnServer, long fileSize, Credentials credentials) {
		super(credentials);
		
		this.request = RequestType.UPLOAD;
		this.nameOnServer = nameOnServer;
		this.fileSize = fileSize;
	}
	
	public String getNameOnServer() {
		return this.nameOnServer;
	}
	
	public long getFileSize() {
		return this.fileSize;
	}
}
