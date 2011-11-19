package com.cs456.project.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class MyClient extends BaseClass {
	private Socket mySocket = null;	
	private PrintWriter pw = null;
		
	private void initiateConnection() throws UnknownHostException, IOException {
		mySocket = new Socket(hostname, port);
	
		// Create a PrintWriter to use for the output stream
		pw = new PrintWriter(mySocket.getOutputStream());
		
					
		System.out.println("C - Sending Greeting");
		
		pw.write(GREETING + "\n");
		pw.flush();
		
		String line = null;
		
		System.out.println("C - Waiting for Greeting response");
		
		line = readLine(mySocket);
		
		if(!GREETING.equals(line)) {
			System.err.println("No Greeting returned: " + line);
		}
	}
	
	private void closeConnection() throws IOException {
		pw.write(GOODBYE + "\n");
		pw.flush();
		
		System.out.println("C - Sending Goodbye");
			
		mySocket.close();
	}
	
	private void requestFileDownload(String filename) {
		try {
			initiateConnection();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		try {
			boolean downloadSuccessful = downloadFile(filename);
			
			if(!downloadSuccessful) {
				System.err.println("The download for file: " + filename + " was not successful");
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		try {
			closeConnection();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	private void requestFileUpload(String filename) {
		try {
			initiateConnection();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		try {
			boolean uploadSuccessful = uploadFile(filename);
			
			if(!uploadSuccessful) {
				System.err.println("The upload for file: " + filename + " was not successful");
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		try {
			closeConnection();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	private void requestFileDeletion(String filename) {
		try {
			initiateConnection();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		try {
			boolean deleteSuccessful = deleteFile(filename);
			
			if(!deleteSuccessful) {
				System.err.println("The deletion of file: " + filename + " was not successful");
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		try {
			closeConnection();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	private void requestRemoteFileDownload(String url, String serverLocation) {
		try {
			initiateConnection();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		try {
			boolean success = remoteFileDownload(url, serverLocation);
			
			if(!success) {
				System.err.println("The remote file download of: " + url + " was not successful");
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		try {
			closeConnection();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	private boolean downloadFile(String filename) throws IOException {
		System.out.println("C - Got Greeting response.. requesting download");
		
		File destinationPart = new File("C:\\Users\\Janson\\workspace\\FileServer\\download\\file.mp3.part");
		File destination = new File("C:\\Users\\Janson\\workspace\\FileServer\\download\\file.mp3");
		
		if(destination.exists()) {
			System.err.println("Unable to download the file as the file already exists: " + destination.getPath());
			return false;
		}
		
		long partFileLength = destinationPart.exists() ? destinationPart.length() : 0;
		
		pw.write(DOWNLOAD_REQUEST + " " + filename + " " + partFileLength + "\n");
		pw.flush();
		
		String line = readLine(mySocket);
		
		if(line == null || !line.startsWith(DOWNLOAD_OK)) {
			System.err.println("Unable to download file: " + line);
		}
		
		System.out.println("C - Got download response");
		
		long fileLength = Long.parseLong(line.substring(DOWNLOAD_OK.length()).trim());
			
		FileOutputStream fileOut = new FileOutputStream(destinationPart, partFileLength!=0);
		
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
			return false;
		}
		
		System.out.println("C - Got the whole file");
		
		fileOut.close();
		
		if(!destinationPart.renameTo(destination)) {
			System.err.println("Although the .part file is complete, could not rename the file to remove the .part..." +
					"please fix the problem and run the download again, or manually remove the .part extension");
			return false;
		}
		
		pw.write(DOWNLOAD_FINISHED + "\n");
		pw.flush();
		
		return true;
	}
	
	private boolean uploadFile(String filename) throws IOException {
		File uploadFile = new File(filename);
		System.out.println("C - Got Greeting response... requesting to Upload");

        FileInputStream fis = null;

        fis = new FileInputStream(uploadFile);
        
        pw.write(UPLOAD_REQUEST + " " + 
        		"C:\\Users\\Janson\\workspace\\FileServer\\upload\\file.mp3" + " " 
        		+ uploadFile.length() +"\n");
        pw.flush();
        
        String line = readLine(mySocket);
        
        if(line == null || !line.startsWith(UPLOAD_OK)) {
        	System.err.println("Did not get the upload ok from server: " + line);
        	return false;
        }
        
        String stringArugments = line.substring(UPLOAD_OK.length()).trim();
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
			
//			System.out.println("C - Sending " + bytesRead + " bytes");
			
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
		
		if(!UPLOAD_FINISHED.equals(line)) {
			System.err.println("Did not receive download finished: " + line);
			return false;
		}
		
		return true;
	}
	
	private boolean deleteFile(String filename) throws IOException {
		System.out.println("C - Got Greeting response... requesting to delete");
		
		pw.write(DELETE_REQUEST + " " + filename + "\n");
        pw.flush();
        
        String line = readLine(mySocket);
        
        if(DELETE_SUCCESS.equals(line)) {
        	System.out.println("The deletion was successful");
        }
        else if(DELETE_FAIL.equals(line)) {
        	System.err.println("The deletion has failed");
        	return false;
        }
        else {
        	System.err.println("The server did not return DELETE_SUCCESS or DELETE_FAIL...it returned: " + line);
        	return false;
        }
		
		return true;
	}
	
	private boolean remoteFileDownload(String url, String serverLocation) throws IOException {
		System.out.println("C - Got Greeting response... requesting to do a remote file download");
		
		pw.write(REMOTE_DOWNLOAD_REQUEST + " " + url + " " + serverLocation +  "\n");
        pw.flush();
        
        String line = readLine(mySocket);
        
        if(REMOTE_DOWNLOAD_ACCEPT.equals(line)) {
        	System.out.println("The remote file download was accepted");
        }
        else if(REMOTE_DOWNLOAD_DECLINE.equals(line)) {
        	System.err.println("The remote file download was rejected");
        	return false;
        }
        else {
        	System.err.println("The server did not return REMOTE_DOWNLOAD_ACCEPT or REMOTE_DOWNLOAD_DECLINE...it returned: " + line);
        	return false;
        }		
		
		return true;
	}
	
	public static void main(String[] args) {
		MyClient mc = new MyClient();
		mc.requestFileDownload("C:\\Users\\Janson\\workspace\\FileServer\\file.mp3");
		mc.requestFileUpload("C:\\Users\\Janson\\workspace\\FileServer\\file.mp3");
		mc.requestFileDeletion("C:\\Users\\Janson\\workspace\\FileServer\\upload\\file.mp3");
		mc.requestRemoteFileDownload("http://download.tuxfamily.org/notepadplus/5.9.6.2/npp.5.9.6.2.Installer.exe", "C:\\Users\\Janson\\workspace\\FileServer\\upload\\npp.5.9.6.2.Installer.exe");
	}

}
