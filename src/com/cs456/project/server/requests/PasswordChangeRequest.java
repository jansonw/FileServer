package com.cs456.project.server.requests;

import com.cs456.project.common.Credentials;

public class PasswordChangeRequest extends Request {
	private String oldPassword = null;
	private String newPassword = null;
	
	public PasswordChangeRequest(String oldPassword, String newPassword, Credentials credentials) {
		super(credentials);
		
		this.request = RequestType.PASSWORD_CHANGE;
		this.oldPassword = oldPassword;
		this.newPassword = newPassword;
	}
	
	public String getOldPassword() {
		return this.oldPassword;
	}
	
	public String getNewPassword() {
		return this.newPassword;
	}
}
