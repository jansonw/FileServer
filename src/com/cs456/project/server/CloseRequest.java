package com.cs456.project.server;

public class CloseRequest extends Request {
	public CloseRequest() {
		this.request = RequestType.GOODBYE;
	}
}
