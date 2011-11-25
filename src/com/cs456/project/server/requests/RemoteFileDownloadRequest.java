package com.cs456.project.server.requests;

import com.cs456.project.common.Credentials;

public class RemoteFileDownloadRequest extends Request {
	private String url = null;
	private String serverLocation = null;
	private boolean isShared = false;
	
	public RemoteFileDownloadRequest(String url, String serverLocation, boolean isShared, Credentials credentials) {
		super(credentials);
		
		this.request = RequestType.REMOTE_FILE_DOWNLOAD;
		this.url = url;
		this.serverLocation = serverLocation;
		this.isShared = isShared;
	}
	
	public String getUrl() {
		return this.url;
	}
	
	public String getServerLocation() {
		return this.serverLocation;
	}
	
	public boolean isShared() {
		return this.isShared;
	}
}
