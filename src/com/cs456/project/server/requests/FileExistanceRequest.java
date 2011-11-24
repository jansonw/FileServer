package com.cs456.project.server.requests;

import com.cs456.project.common.Credentials;

public class FileExistanceRequest extends Request {
	private String fileName = null;
	
	public FileExistanceRequest(String fileName, Credentials credentials) {
		super(credentials);
		
		this.request = RequestType.FILE_EXISTANCE;
		this.fileName = fileName;
	}
	
	public String getFileName() {
		return this.fileName;
	}
}
