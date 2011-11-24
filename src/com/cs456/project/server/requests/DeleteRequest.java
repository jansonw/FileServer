package com.cs456.project.server.requests;

import com.cs456.project.common.Credentials;

public class DeleteRequest extends Request {
	private String fileName = null;
	
	public DeleteRequest(String fileName, Credentials credentials) {
		super(credentials);
		
		this.request = RequestType.DELETE;
		this.fileName = fileName;
	}
	
	public String getFileName() {
		return this.fileName;
	}
}
