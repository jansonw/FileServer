package com.cs456.project.server.requests;

import com.cs456.project.server.protocol.Credentials;
import com.cs456.project.server.requests.Request.RequestType;

public class CloseRequest extends Request {
	public CloseRequest(Credentials credentials) {
		super(credentials);
		
		this.request = RequestType.GOODBYE;
	}
}
