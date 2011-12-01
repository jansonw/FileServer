package com.cs456.project.server.requests;

import com.cs456.project.common.Credentials;

public class FileListRequest extends Request {
	private String rootPath = null;
	
	public FileListRequest(String rootPath, Credentials credentials) {
		super(credentials);
		
		this.request = RequestType.FILE_LIST;
		this.rootPath = rootPath;
	}
	
	public String getRootPath() {
		return this.rootPath;
	}
}
