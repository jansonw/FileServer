package com.cs456.project.client;

import com.cs456.project.common.Credentials;
import com.cs456.project.common.FileListManager;
import com.cs456.project.common.FileListObject;
import com.cs456.project.exceptions.AuthenticationException;
import com.cs456.project.exceptions.DeletionDelayedException;
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
//			cc.requestUserRegistration("jansON", "abc123");
//			cc.requestUserRegistration("TeSt1", "password");
//			cc.setCredentials(new Credentials("tEsT1", "password"));
//			cc.requestPasswordChange("password", "newPassword");
			
			cc.setCredentials(new Credentials("janson", "abc123"));
//			cc.setCredentials(new Credentials("test1", "newPassword"));
			
//			cc.verifyCredentials();
			
//			cc.requestFileUpload("C:\\Users\\Janson\\workspace\\FileServer\\file.mp3", "file1.mp3", true);
//			cc.requestFileExistance("file1.mp3", "janson");
//			cc.requestFileDownload("download\\file9.mp3", "folder\\file1.mp3", "janson");
			cc.requestFileDeletion("file1.mp3");
//			cc.requestRemoteFileDownload("http://download.tuxfamily.org/notepadplus/5.9.6.2/npp.5.9.6.2.Installer.exe", "npp.5.9.6.2.Installer3.exe", false);
//			cc.requestFileExistance("npp.5.9.6.2.Installer.exe", "test1");
//			cc.requestPermissionsChange("folder\\file1.mp3", true);
			
			FileListManager fileListManager = cc.getFileList("janson");
			
			for(FileListObject o : fileListManager.getAll(true)) {
				System.out.println(o.getDisplayName() + "\tis_directory: " + o.isDirectory() + "\tis_delete_only: " + o.isDeleteOnly());
			}

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
		} catch (DeletionDelayedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
