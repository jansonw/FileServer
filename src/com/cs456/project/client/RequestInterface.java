package com.cs456.project.client;

import com.cs456.project.exceptions.AuthenticationException;
import com.cs456.project.exceptions.DisconnectionException;
import com.cs456.project.exceptions.RequestExecutionException;
import com.cs456.project.exceptions.RequestPermissionsException;

public interface RequestInterface {
	public void requestFileDownload(String fileLocationOnServer) 
			throws DisconnectionException, AuthenticationException, RequestExecutionException, RequestPermissionsException;
	
	public void requestFileUpload(String fileLocation) 
			throws DisconnectionException, AuthenticationException, RequestExecutionException, RequestPermissionsException;
	
	public void requestRemoteFileDownload(String urlLocation, String locationOnServer) 
			throws DisconnectionException, AuthenticationException, RequestExecutionException, RequestPermissionsException;
	
	public void requestFileDeletion(String fileLocationOnServer) 
			throws DisconnectionException, AuthenticationException, RequestExecutionException, RequestPermissionsException;
	
	public void requestUserRegistration(String username, String password) 
			throws RequestExecutionException, DisconnectionException;
	
	public void requestPasswordChange(String oldPassword, String newPassword) 
			throws AuthenticationException, RequestPermissionsException, RequestExecutionException, DisconnectionException;
	
	public void requestFileExistance(String serverFilePath) 
			throws AuthenticationException, RequestPermissionsException, RequestExecutionException, DisconnectionException;
}
