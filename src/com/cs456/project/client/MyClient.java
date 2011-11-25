package com.cs456.project.client;

import com.cs456.project.common.Credentials;
import com.cs456.project.exceptions.AuthenticationException;
import com.cs456.project.exceptions.DisconnectionException;
import com.cs456.project.exceptions.RequestExecutionException;
import com.cs456.project.exceptions.RequestPermissionsException;

public class MyClient {
	
	public static void main(String[] args) {
		runAll();
	}
	
	private static void runAll() {
		ClientConnection cc = new ClientConnection(null);
		try {
//			cc.requestUserRegistration("test1", "password");
//			cc.setCredentials(new Credentials("test1", "password"));
//			cc.requestPasswordChange("password", "newPassword");
			
			cc.setCredentials(new Credentials("janson", "abc123"));
			
			cc.requestFileUpload("C:\\Users\\Janson\\workspace\\FileServer\\file.mp3", "file.mp3", true);
//			cc.requestFileExistance("file.mp3");
//			cc.requestFileDownload("download\\file.mp3", "file.mp3");
//			cc.requestFileDeletion("file.mp3");
//			cc.requestRemoteFileDownload("http://download.tuxfamily.org/notepadplus/5.9.6.2/npp.5.9.6.2.Installer.exe", "npp.5.9.6.2.Installer.exe");

		} catch (DisconnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AuthenticationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RequestExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RequestPermissionsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
