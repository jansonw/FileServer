package com.cs456.project.server.protocol;

public class Credentials {
	private String username = null;
	private String password = null;
	
	public Credentials(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public String getPassword() {
		return this.password;
	}
}
