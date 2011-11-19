package com.cs456.project.server;

public class DeleteRequest extends Request {
	private String fileName = null;
	
	public DeleteRequest(String fileName) {
		this.request = RequestType.DELETE;
		this.fileName = fileName;
	}
	
	public String getFileName() {
		return this.fileName;
	}
}
