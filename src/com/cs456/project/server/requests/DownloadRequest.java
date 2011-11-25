package com.cs456.project.server.requests;

import com.cs456.project.common.Credentials;

public class DownloadRequest extends Request {
	private String fileName = null;
	private long startPosition = 0;
	private String owner = null;
	
	public DownloadRequest(String fileName, long startPosition, String owner, Credentials credentials) {
		super(credentials);
		
		this.request = RequestType.DOWNLOAD;
		this.fileName = fileName;
		this.startPosition = startPosition;
		this.owner = owner;
	}
	
	public String getFileName() {
		return this.fileName;
	}
	
	public long getStartPosition() {
		return this.startPosition;
	}
	
	public String getOwner() {
		return this.owner;
	}
}
