package com.cs456.project.server.requests;

import com.cs456.project.server.protocol.Credentials;
import com.cs456.project.server.requests.Request.RequestType;

public class DownloadRequest extends Request {
	private String fileName = null;
	private long startPosition = 0;
	
	public DownloadRequest(String fileName, long startPosition, Credentials credentials) {
		super(credentials);
		
		this.request = RequestType.DOWNLOAD;
		this.fileName = fileName;
		this.startPosition = startPosition;
	}
	
	public String getFileName() {
		return this.fileName;
	}
	
	public long getStartPosition() {
		return this.startPosition;
	}
}
