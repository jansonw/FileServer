package com.cs456.project.server.requests;

import com.cs456.project.common.Credentials;

public class DownloadRequest extends Request {
	private String fileName = null;
	private String owner = null;
	
	public DownloadRequest(String fileName, String owner, Credentials credentials) {
		super(credentials);
		
		this.request = RequestType.DOWNLOAD;
		this.fileName = fileName;
		this.owner = owner;
	}
	
	public String getFileName() {
		return this.fileName;
	}
	
	public String getOwner() {
		return this.owner;
	}
}
