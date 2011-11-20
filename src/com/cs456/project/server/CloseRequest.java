package com.cs456.project.server;

public class CloseRequest extends Request {
	public CloseRequest(Credentials credentials) {
		super(credentials);
		
		this.request = RequestType.GOODBYE;
	}
}
