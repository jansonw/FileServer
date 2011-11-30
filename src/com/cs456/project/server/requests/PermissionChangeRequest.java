package com.cs456.project.server.requests;

import com.cs456.project.common.Credentials;

public class PermissionChangeRequest extends Request {
	private String fileName = null;
	private boolean isShared = false;
	
	public PermissionChangeRequest(String fileName, boolean isShared, Credentials credentials) {
		super(credentials);
		
		this.request = RequestType.PERMISSION_CHANGE;
		this.fileName = fileName;
		this.isShared = isShared;
	}
	
	public String getFileName() {
		return this.fileName;
	}
	
	public boolean isShared() {
		return this.isShared;
	}
}
