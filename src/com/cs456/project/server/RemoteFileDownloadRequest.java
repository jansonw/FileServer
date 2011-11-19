package com.cs456.project.server;

public class RemoteFileDownloadRequest extends Request {
	private String url = null;
	private String serverLocation = null;
	
	public RemoteFileDownloadRequest(String url, String serverLocation) {
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
