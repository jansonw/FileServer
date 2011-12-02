package com.cs456.project.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.cs456.project.common.ConnectionSettings;
import com.cs456.project.common.Credentials;
import com.cs456.project.common.FileListManager;
import com.cs456.project.common.FileListObject;
import com.cs456.project.common.FileWrapper;
import com.cs456.project.exceptions.AuthenticationException;
import com.cs456.project.exceptions.DisconnectionException;
import com.cs456.project.exceptions.RequestExecutionException;
import com.cs456.project.exceptions.RequestPermissionsException;

public class ClientConnection implements RequestInterface {
	private Socket mySocket = null;	
	private PrintWriter pw = null;
	
	private Credentials credentials = null;
	
	public ClientConnection(Credentials credentials) {
		this.credentials = credentials;
	}
	
	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}

	@Override
	public void requestFileDownload(String localFileLocation, String fileLocationOnServer, String owner) throws DisconnectionException, AuthenticationException, RequestExecutionException, RequestPermissionsException {
		try {	
			openConnection();
			initiateRequestConnection();
			downloadFile(localFileLocation, fileLocationOnServer, owner);
			closeConnection();
		} catch (AuthenticationException e) {
			closeConnection();			
			throw e;
		} catch (RequestPermissionsException e) {
			closeConnection();			
			throw e;
		} catch (RequestExecutionException e) {
			closeConnection();			
			throw e;
		} catch (DisconnectionException e) {
			closeConnection();			
			throw e;
		}
	}

	@Override
	public void requestFileUpload(String fileLocation, String serverFilename, boolean isShared) throws DisconnectionException, AuthenticationException, RequestExecutionException, RequestPermissionsException {
		try {
			openConnection();
			initiateRequestConnection();
			uploadFile(fileLocation, serverFilename, isShared);
			closeConnection();
		} catch (AuthenticationException e) {
			closeConnection();			
			throw e;
		} catch (RequestPermissionsException e) {
			closeConnection();			
			throw e;
		} catch (RequestExecutionException e) {
			closeConnection();			
			throw e;
		} catch (DisconnectionException e) {
			closeConnection();			
			throw e;
		}
	}

	@Override
	public void requestRemoteFileDownload(String urlLocation, String locationOnServer, boolean isShared) throws DisconnectionException, AuthenticationException, RequestExecutionException, RequestPermissionsException {
		try {
			openConnection();
			initiateRequestConnection();
			remoteFileDownload(urlLocation, locationOnServer, isShared);
			closeConnection();
		} catch (AuthenticationException e) {
			closeConnection();			
			throw e;
		} catch (RequestPermissionsException e) {
			closeConnection();			
			throw e;
		} catch (RequestExecutionException e) {
			closeConnection();			
			throw e;
		} catch (DisconnectionException e) {
			closeConnection();			
			throw e;
		}
	}

	@Override
	public void requestFileDeletion(String fileLocationOnServer) throws DisconnectionException, AuthenticationException, RequestExecutionException, RequestPermissionsException {
		try {
			openConnection();
			initiateRequestConnection();
			deleteFile(fileLocationOnServer);
			closeConnection();
		} catch (AuthenticationException e) {
			closeConnection();			
			throw e;
		} catch (RequestPermissionsException e) {
			closeConnection();			
			throw e;
		} catch (RequestExecutionException e) {
			closeConnection();			
			throw e;
		} catch (DisconnectionException e) {
			closeConnection();			
			throw e;
		}
	}
	
	@Override
	public void requestUserRegistration(String username, String password) throws RequestExecutionException, DisconnectionException {
		try {
			openConnection();
			registerUser(username, password);
			closeConnection();
		} catch (RequestExecutionException e) {
			closeConnection();			
			throw e;
		} catch (DisconnectionException e) {
			closeConnection();			
			throw e;
		}
	}
	
	@Override
	public void requestPasswordChange(String oldPassword, String newPassword) throws AuthenticationException, RequestPermissionsException, RequestExecutionException, DisconnectionException {
		try {
			openConnection();
			initiateRequestConnection();
			changePassword(oldPassword, newPassword);
			credentials.setPassword(newPassword);
			closeConnection();
		} catch (AuthenticationException e) {
			closeConnection();			
			throw e;
		} catch (RequestPermissionsException e) {
			closeConnection();			
			throw e;
		} catch (RequestExecutionException e) {
			closeConnection();			
			throw e;
		} catch (DisconnectionException e) {
			closeConnection();			
			throw e;
		}
	}
	
	@Override
	public void requestFileExistance(String serverFilePath, String owner) throws AuthenticationException, RequestPermissionsException, RequestExecutionException, DisconnectionException {
		try {
			openConnection();
			initiateRequestConnection();
			fileExistance(serverFilePath, owner);
			closeConnection();
		} catch (AuthenticationException e) {
			closeConnection();			
			throw e;
		} catch (RequestPermissionsException e) {
			closeConnection();			
			throw e;
		} catch (RequestExecutionException e) {
			closeConnection();			
			throw e;
		} catch (DisconnectionException e) {
			closeConnection();			
			throw e;
		}
	}
	
	@Override
	public void requestPermissionsChange(String serverFilePath, boolean newPermissions) throws AuthenticationException, RequestPermissionsException, RequestExecutionException, DisconnectionException {
		try {
			openConnection();
			initiateRequestConnection();
			permissionsChange(serverFilePath, newPermissions);
			closeConnection();
		} catch (AuthenticationException e) {
			closeConnection();			
			throw e;
		} catch (RequestPermissionsException e) {
			closeConnection();			
			throw e;
		} catch (RequestExecutionException e) {
			closeConnection();			
			throw e;
		} catch (DisconnectionException e) {
			closeConnection();			
			throw e;
		}
	}
	
	@Override
	public FileListManager getFileList(String rootPath) throws AuthenticationException, RequestPermissionsException, RequestExecutionException, DisconnectionException {
		FileListManager fileListManager = null;
		
		try {
			openConnection();
			initiateRequestConnection();
			fileListManager = fileList(rootPath);
			closeConnection();
		} catch (AuthenticationException e) {
			closeConnection();			
			throw e;
		} catch (RequestPermissionsException e) {
			closeConnection();			
			throw e;
		} catch (RequestExecutionException e) {
			closeConnection();			
			throw e;
		} catch (DisconnectionException e) {
			closeConnection();			
			throw e;
		}
		
		return fileListManager;
	}
	
	private void openConnection() throws DisconnectionException {
		try {
			mySocket = new Socket(ConnectionSettings.hostname, ConnectionSettings.port);
			pw = new PrintWriter(mySocket.getOutputStream());
		} catch (UnknownHostException e) {
			throw new DisconnectionException("Could not find the server: " + ConnectionSettings.hostname);
		} catch (IOException e) {
			throw new DisconnectionException("Unable to establish a connection with the server, please try again later");
		}
	}
		
	private void initiateRequestConnection() throws DisconnectionException, AuthenticationException, RequestPermissionsException {
		if(credentials == null) {
			throw new RequestPermissionsException("You must log in before you can make any requests");
		}
		
		System.out.println("C - Sending Greeting");
		
		pw.write(ConnectionSettings.GREETING + " " + credentials.getUsername() + " " + credentials.getPassword() + "\n");
		pw.flush();
				
		System.out.println("C - Waiting for Greeting response");
		
		String line;
		try {
			line = readLine(mySocket);
		} catch (IOException e) {
			throw new DisconnectionException("A disconnect occurred while trying to establish a connection with the server, please try again later");
		}
		
		if(ConnectionSettings.GREETING.equals(line)) {
			System.out.println("I was authenticated!");
		}
		else if(ConnectionSettings.BAD_AUTHENTICATION.equals(line)) {
			System.err.println("I was not authenticated using: username=" + credentials.getUsername() + " password=" + credentials.getPassword());
			throw new AuthenticationException("The username and/or password you provided is incorrect", credentials.getUsername(), credentials.getPassword(), false);
		}
		else if(ConnectionSettings.LOCKED_OUT.equals(line)) {
			System.err.println("I am locked out using: username=" + credentials.getUsername() + " password=" + credentials.getPassword());
			throw new AuthenticationException("The account you are attempting to use is currently locked. " +
					"Please contact customer support at support@filestorage.com to unlock your account", credentials.getUsername(), credentials.getPassword(), true);
		}
		else {
			System.err.println("The server returned a weird response: " + line);
		}
	}
	
	private void closeConnection() {
		System.out.println("C - Closing Connection");
			
		try {
			mySocket.close();
		} catch (IOException e) {
			System.err.println("An error occurred while trying to close the socket");
			e.printStackTrace();
		}
	}
	
	private void remoteFileDownload(String url, String serverLocation, boolean isShared) throws RequestExecutionException, DisconnectionException {
		System.out.println("C - Got Greeting response... requesting to do a remote file download");
		
		pw.write(ConnectionSettings.REMOTE_DOWNLOAD_REQUEST + " " + url + " " + serverLocation + " " + isShared + "\n");
        pw.flush();
        
        String line;
		try {
			line = readLine(mySocket);
		} catch (IOException e) {
			throw new DisconnectionException("Your connection with the server has been interrupted." +
					" Please ensure you are connected to the internet and then try your remote download request again");
		}
        
        if(ConnectionSettings.REMOTE_DOWNLOAD_ACCEPT.equals(line)) {
        	System.out.println("The remote file download was accepted");
        }
        else if(ConnectionSettings.REMOTE_DOWNLOAD_DECLINE.equals(line)) {
        	System.err.println("The remote file download was rejected");
        	throw new RequestExecutionException("The server has rejected your request for the remote download." +
        			" Please ensure you have the proper privledges and the file dows not already exist, then try your request again");
        }	
	}
	
	private void downloadFile(String localFilename, String serverFilename, String owner) throws RequestExecutionException, DisconnectionException {
		System.out.println("C - Got Greeting response.. requesting download");
		
		File destinationPart = new File(localFilename + ".part");
		File destination = new File(localFilename);
		
		if(destination.exists()) {
			System.err.println("Unable to download the file as the file already exists: " + destination.getPath());
			throw new RequestExecutionException("The requested file could not be downloaded as a file with the same name" +
					" already exists in the download directory.  The file name and location you chose was: " + destination.getPath() +
					"\nPlease choose another file name for the requested file download");
		}
		
		long partFileLength = destinationPart.exists() ? destinationPart.length() : 0;
		
		pw.write(ConnectionSettings.DOWNLOAD_REQUEST + " " + serverFilename + " " + partFileLength + " " + owner + "\n");
		pw.flush();
		
		try {
			String line = readLine(mySocket);
			
			if(line != null && line.startsWith(ConnectionSettings.DOWNLOAD_OK)) {
				System.out.println("Download request was accepted");
			}
			else if(line != null && line.startsWith(ConnectionSettings.DOWNLOAD_REJECT)) {
				System.err.println("Server rejected file download");
				throw new RequestExecutionException("The server has rejected your request to download the file.  Please ensure you have the " +
						"proper privledges and then try again");
			}
			
			System.out.println("C - Got download response");
			
			long fileLength = Long.parseLong(line.substring(ConnectionSettings.DOWNLOAD_OK.length()).trim());
				
			FileOutputStream fileOut;
			try {
				fileOut = new FileOutputStream(destinationPart, partFileLength!=0);
			} catch (FileNotFoundException e) {
				throw new RequestExecutionException("Unable to write to the file: " + destinationPart.getName() + 
						"\nPlease try your request again");
			}
			
			byte[] buffer = new byte[64000];
			long totalBytesRead = partFileLength;
			
			InputStream inFile = mySocket.getInputStream();
			
			while (true) {
				System.out.println("BYTES READ: " + totalBytesRead + "/" + fileLength);
							
				if(totalBytesRead == fileLength) break;
							
				int numBytesRead = inFile.read(buffer);
				if(numBytesRead == -1) break;
				
				totalBytesRead += numBytesRead;
							
				fileOut.write(buffer, 0, numBytesRead);
				fileOut.flush();
			}
			
			if(totalBytesRead != fileLength) {
				System.err.println("Did not receive the whole file, only got: " + totalBytesRead + " bytes");
				throw new RequestExecutionException("The file you requested was not completely downloaded.  This is most likely due to" +
						" you temporarily losing internet connectivity.  Please ensure you are connected to the internet and attempt" +
						" to download this file again.  The download will resume where you left off");
			}
			
			System.out.println("C - Got the whole file");
			
			fileOut.close();
		} catch(IOException e) {
			throw new DisconnectionException("Your connection with the server has been interrupted. Please reestablish your connection" +
					" to the internet and then try your download request again.  If the download had begun, it will resume where it left off");
		}
		
		if(!destinationPart.renameTo(destination)) {
			System.err.println("Although the .part file is complete, could not rename the file to remove the .part..." +
					"please fix the problem and run the download again, or manually remove the .part extension");
			throw new RequestExecutionException("An error occurred while removing the \".part\" extension on the file you were downloading." +
					" The file download has completed, so you can either manually remove this file extension, or run the download again." +
					" If you choose the download approach, please note that the file will not be downloaded again, rather the program will" +
					" attempt to rename the file once again for you.");
		}
		
		pw.write(ConnectionSettings.DOWNLOAD_FINISHED + "\n");
		pw.flush();
	}
	
	private void uploadFile(String filename, String serverFilename, boolean isShared) throws RequestExecutionException, DisconnectionException {
		File uploadFile = new File(filename);
		System.out.println("C - Got Greeting response... requesting to Upload");

        FileInputStream fis = null;

        try {
			fis = new FileInputStream(uploadFile);
		} catch (FileNotFoundException e1) {
			throw new RequestExecutionException("Unable to find the file you requested to upload: " + uploadFile.getName() + 
					"\nPlease ensure the path to the file you provided is correct and then try your request again");
		}
        
        pw.write(ConnectionSettings.UPLOAD_REQUEST + " " + serverFilename + " " + uploadFile.length() + " " + FileWrapper.booleanToChar(isShared) + "\n");
        pw.flush();
        
        try {
	        String line = readLine(mySocket);
	        
	        if(line != null && line.startsWith(ConnectionSettings.UPLOAD_OK)) {
	        	System.out.println("Got the ok from the server");
	        }
	        else if(line != null && line.startsWith(ConnectionSettings.UPLOAD_REJECT)) {
	        	System.err.println("Did not get the upload ok from server: " + line);
	        	throw new RequestExecutionException("The server has rejected your request to upload the file.  Please ensure you have" +
	        			" the proper privledges and the file dows not already exist, then try your request again");
	        }
	        
	        String stringArugments = line.substring(ConnectionSettings.UPLOAD_OK.length()).trim();
	        String[] arguments = stringArugments.split(" ");
	        
	        long startPosition = Long.parseLong(arguments[0]);
	        
	        boolean readPartFile = startPosition != 0; 
	        
	        System.out.println("C - Received upload acceptance");
	        
	        byte[] buffer = new byte[64000];
	        
	        OutputStream out = mySocket.getOutputStream();
			
	        long totalBytesRead = 0;
	        
			while (true) {
				int numBytesRead = fis.read(buffer);
				if(numBytesRead == -1) break;
				
				totalBytesRead += numBytesRead;
				
				if(readPartFile) {
					if(totalBytesRead > startPosition) {
						int numBytesLeft = (int)(startPosition % 64000);
						out.write(buffer, numBytesLeft, numBytesRead - numBytesLeft);
						out.flush();
						readPartFile = false;
					}
				}
				else {
					out.write(buffer, 0, numBytesRead);
					out.flush();
				}
			}
			
			fis.close();
			
			System.out.println("C - Sent the whole file");
			
			line = readLine(mySocket);
			
			if(ConnectionSettings.UPLOAD_FINISHED.equals(line)) {
				System.out.println("The upload was successful!");
			}
			else if(line != null && line.startsWith(ConnectionSettings.UPLOAD_FAIL)) {
				String stringArguments = line.substring(ConnectionSettings.UPLOAD_FAIL.length()).trim();
				arguments = stringArguments.split(" ");
				
				long amountUploaded = Long.parseLong(arguments[0].trim());
				
				if(amountUploaded == uploadFile.length()) {
					throw new RequestExecutionException("An error has occurred on the server's side while attempting remove the \".part\" extension on the file. " +
							" Please try your upload request again to resolve this issue");
				}
				else {
					throw new RequestExecutionException("The server did not receive the entire file. Please upload the file again so that the server" +
							" can continue where it left off on the upload");
				}
			}
        } catch(IOException e) {
        	throw new DisconnectionException("Your connection with the server has been interrupted. Please reestablish your connection" +
					" to the internet and then try your upload request again.  If the upload had begun, it will resume where it left off");
        }
	}
	
	private void deleteFile(String filename) throws RequestExecutionException, DisconnectionException {
		System.out.println("C - Got Greeting response... requesting to delete");
		
		pw.write(ConnectionSettings.DELETE_REQUEST + " " + filename + "\n");
        pw.flush();
        
        String line;
		try {
			line = readLine(mySocket);
		} catch (IOException e) {
			throw new DisconnectionException("Your connection with the server has been interrupted. Please reestablish your connection" +
					" to the internet and then try your deletion request again");
		}
        
        if(ConnectionSettings.DELETE_SUCCESS.equals(line)) {
        	System.out.println("The deletion was successful");
        }
        else {
        	System.err.println("The deletion has failed");
        	throw new RequestExecutionException("The file was unable to be deleted on the server.  Please ensure you have the" +
        			" proper privledges and then try your request again");
        }
	}

	private void registerUser(String username, String password) throws DisconnectionException, RequestExecutionException {
		System.out.println("C - Got Greeting response... requesting to register user");
		
		pw.write(ConnectionSettings.REGISTRATION_REQUEST + " " + username + " " + password + "\n");
        pw.flush();
        
        String line;
		try {
			line = readLine(mySocket);
		} catch (IOException e) {
			throw new DisconnectionException("Your connection with the server has been interrupted. Please reestablish your connection" +
					" to the internet and then try your registration request again");
		}
        
        if(ConnectionSettings.REGISTRATION_OK.equals(line)) {
        	System.out.println("The registration was successful");
        }
        else if(ConnectionSettings.REGISTRATION_INVALID.equals(line)) {
        	System.err.println("The username is already taken: " + username);
        	throw new RequestExecutionException("The username you chose: " + username + " has already been chosen. Please choose a" +
        			" different username and try the registry again");
        }
        else if(ConnectionSettings.REGISTRATION_FAILED.equals(line)) {
        	throw new RequestExecutionException("An error occurred while attempting to register your username: " + username + " on the server." +
        			" Please try your registry again");
        			
        }
	}
	
	private void changePassword(String oldPassword, String newPassword) throws RequestExecutionException, DisconnectionException {
		System.out.println("C - Got Greeting response... requesting password change");
		
		pw.write(ConnectionSettings.PASSWORD_CHANGE_REQUEST + " " + oldPassword + " " + newPassword + "\n");
        pw.flush();
        
        String line;
		try {
			line = readLine(mySocket);
		} catch (IOException e) {
			throw new DisconnectionException("Your connection with the server has been interrupted. Please reestablish your connection" +
					" to the internet and then try your password change request again");
		}
        
        if(ConnectionSettings.PASSWORD_CHANGE_OK.equals(line)) {
        	System.out.println("The password change was successful");
        }
        else if(ConnectionSettings.PASSWORD_CHANGE_FAILED.equals(line)) {
        	System.err.println("The password change was not successful");
        	throw new RequestExecutionException("Your password change request could not be serviced at this time.  Please try again later");
        }		
	}
	
	private void fileExistance(String serverFilePath, String owner) throws DisconnectionException, RequestExecutionException {
		System.out.println("C - Got Greeting response... requesting file existance");
		
		pw.write(ConnectionSettings.FILE_EXISTS_REQUEST + " " + serverFilePath + " " + owner + "\n");
        pw.flush();
        
        String line;
		try {
			line = readLine(mySocket);
		} catch (IOException e) {
			throw new DisconnectionException("Your connection with the server has been interrupted. Please reestablish your connection" +
					" to the internet and then try your file existance request again");
		}
        
        if(ConnectionSettings.FILE_EXISTS_YES.equals(line)) {
        	System.out.println("The file exists");
        }
        else if(ConnectionSettings.FILE_EXISTS_NO.equals(line)) {
        	System.err.println("The file does not exist");
        	throw new RequestExecutionException("The file you are looking for does not exist.");
        }
	}
	
	private void permissionsChange(String serverFilePath, boolean newPermissions) throws DisconnectionException, RequestExecutionException {
		System.out.println("C - Got Greeting response... requesting file permission change");
		
		pw.write(ConnectionSettings.PERMISSION_CHANGE_REQUEST + " " + serverFilePath + " " + FileWrapper.booleanToChar(newPermissions) + "\n");
        pw.flush();
        
        String line;
		try {
			line = readLine(mySocket);
		} catch (IOException e) {
			throw new DisconnectionException("Your connection with the server has been interrupted. Please reestablish your connection" +
					" to the internet and then try your file permission change request again");
		}
        
        if(ConnectionSettings.PERMISSION_CHANGE_SUCCESS.equals(line)) {
        	System.out.println("The permission change was successful");
        }
        else if(ConnectionSettings.PERMISSION_CHANGE_FAIL.equals(line)) {
        	System.err.println("The permission change was not successful");
        	throw new RequestExecutionException("The file permission change you requested failed.   Please ensure you have" +
	        			" the proper privledges and the file does not already exist, then try your request again");
        }
	}
	
	private FileListManager fileList(String rootPath) throws DisconnectionException, RequestExecutionException {
		System.out.println("C - Got Greeting response... requesting file list");
		
		pw.write(ConnectionSettings.FILE_LIST_REQUEST + " " + rootPath + "\n");
        pw.flush();
        
        String line;
        List<FileListObject> fileList = new ArrayList<FileListObject>();
        
		try {
			line = readLine(mySocket);
		        
	        if(line != null && line.startsWith(ConnectionSettings.FILE_LIST_SUCCESS)) {
	        	String stringArguments = line.substring(ConnectionSettings.FILE_LIST_SUCCESS.length()).trim();
				String[] arguments = stringArguments.split(" ");
				
				if(arguments.length != 1) {
					throw new RequestExecutionException("The server did not provide the required information for the file list retrieval.  Please contact customer support regarding this issue.");
				}
				
				int numFiles = Integer.parseInt(arguments[0].trim());
				
				System.out.println("The file list request was accepted and there are " + numFiles + " files to be retrieved");
				
				int numReceived = 0;
				
				ObjectInputStream ois = new ObjectInputStream(mySocket.getInputStream());
				
				
				while(true) {
					if(numReceived == numFiles) break;
					fileList.add((FileListObject)ois.readObject());
					
					numReceived++;
				}
				
				if(numReceived != numFiles) {
					throw new RequestExecutionException("Did not receive all entries for the file list request." +
							"Please ensure you have a stable internet connection and then try your request again");
				}
	        }
	        else if(ConnectionSettings.FILE_LIST_FAIL.equals(line)) {
	        	System.err.println("The file list request was not successful");
	        	throw new RequestExecutionException("The file list request failed.   Please ensure you have" +
		        			" the proper privledges then try your request again");
	        }
		} catch (IOException e) {
			throw new DisconnectionException("Your connection with the server has been interrupted. Please reestablish your connection" +
					" to the internet and then try your file list request again");
		} catch (ClassNotFoundException e) {
			throw new RequestExecutionException("The file list request failed due to missing classes.  Please contact customer support");
		}
		
		return new FileListManager(fileList);
	}
	
	private String readLine(Socket socket) throws IOException {
		String line = new String();
		int c;

		while ((c = socket.getInputStream().read()) != '\n') {
			if(c == -1) {
				throw new IOException("The socket closed before being able to read the end of the line");
			}
			
			line += (char) c;
		}

		return line.trim();
	}
}
