package com.cs456.project.server.exceptions;

@SuppressWarnings("serial")
public class InvalidRequestException extends Exception {
	private String invalidRequest = null;
	
	public InvalidRequestException(String message, String invalidRequest) {
		super(message);
		
		this.invalidRequest = invalidRequest;
	}
	
	public String getInvalidRequest() {
		return this.invalidRequest;
	}
}
