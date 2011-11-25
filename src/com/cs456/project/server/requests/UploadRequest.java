package com.cs456.project.server.requests;

import com.cs456.project.common.Credentials;

public class UploadRequest extends Request {
	private String nameOnServer = null;
	private long fileSize = -1;
	private boolean isShared = false;
	
	public UploadRequest(String nameOnServer, long fileSize, boolean isShared, Credentials credentials) {
		super(credentials);
		
		this.request = RequestType.UPLOAD;
		this.nameOnServer = nameOnServer;
		this.fileSize = fileSize;
		this.isShared = isShared;
	}
	
	public String getNameOnServer() {
		return this.nameOnServer;
	}
	
	public long getFileSize() {
		return this.fileSize;
	}
	
	public boolean isShared() {
		return this.isShared;
	}
}
