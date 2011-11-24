package com.cs456.project.client;

import com.cs456.project.common.Credentials;
import com.cs456.project.exceptions.AuthenticationException;
import com.cs456.project.exceptions.DisconnectionException;
import com.cs456.project.exceptions.RequestExecutionException;

public class MyClient {
	
	public static void main(String[] args) {
		ClientConnection cc = new ClientConnection(new Credentials("janson", "abc123"));
		try {
			cc.requestFileDownload("C:\\Users\\Janson\\workspace\\FileServer\\file.mp3");
//			cc.requestFileUpload("C:\\Users\\Janson\\workspace\\FileServer\\file.mp3");
//			cc.requestFileDeletion("C:\\Users\\Janson\\workspace\\FileServer\\upload\\file.mp3");
//			cc.requestRemoteFileDownload("http://download.tuxfamily.org/notepadplus/5.9.6.2/npp.5.9.6.2.Installer.exe", "C:\\Users\\Janson\\workspace\\FileServer\\upload\\npp.5.9.6.2.Installer.exe");
		} catch (DisconnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AuthenticationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RequestExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
