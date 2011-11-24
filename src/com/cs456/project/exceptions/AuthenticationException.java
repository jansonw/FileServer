package com.cs456.project.exceptions;

@SuppressWarnings("serial")
public class AuthenticationException extends Exception {
	private String username = null;
	private String password = null;	
	private boolean isLockedOut = false;
	
	public AuthenticationException(String message, String username, String password, boolean isLockedOut) {
		super(message);
		
		this.username = username;
		this.password = password;
		this.isLockedOut = isLockedOut;		
	}

	public String getUsername() {
		return this.username;
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public boolean isLockedOut() {
		return isLockedOut;
	}
	
}
