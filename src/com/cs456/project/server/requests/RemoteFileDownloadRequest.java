package com.cs456.project.server.requests;

import com.cs456.project.server.protocol.Credentials;
import com.cs456.project.server.requests.Request.RequestType;

public class RemoteFileDownloadRequest extends Request {
	private String url = null;
	private String serverLocation = null;
	
	public RemoteFileDownloadRequest(String url, String serverLocation, Credentials credentials) {
		super(credentials);
		
		this.request = RequestType.REMOTE_FILE_DOWNLOAD;
		this.url = url;
		this.serverLocation = serverLocation;
	}
	
	public String getUrl() {
		return this.url;
	}
	
	public String getServerLocation() {
		return this.serverLocation;
	}
}
