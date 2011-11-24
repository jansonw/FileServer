package com.cs456.project.client;

import com.cs456.project.exceptions.AuthenticationException;
import com.cs456.project.exceptions.DisconnectionException;
import com.cs456.project.exceptions.RequestExecutionException;

public interface RequestInterface {
	public void requestFileDownload(String fileLocationOnServer) 
			throws DisconnectionException, AuthenticationException, RequestExecutionException;
	
	public void requestFileUpload(String fileLocation) 
			throws DisconnectionException, AuthenticationException, RequestExecutionException;
	
	public void requestRemoteFileDownload(String urlLocation, String locationOnServer) 
			throws DisconnectionException, AuthenticationException, RequestExecutionException;
	
	public void requestFileDeletion(String fileLocationOnServer) 
			throws DisconnectionException, AuthenticationException, RequestExecutionException;
}
