package com.cs456.project.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class MyClient extends BaseClass {
	Socket mySocket = null;
	String fileToDownload = "C:\\Users\\Janson\\workspace\\FileServer\\file.mp3";
	String fileToUpload = "C:\\Users\\Janson\\workspace\\FileServer\\file.mp3";
	
	
	boolean wantToUpload = false;
	
	public MyClient() {}
	
	public void start() {
		// Create a new socket
		try {
			mySocket = new Socket(hostname, port);
			
			// Create a PrintWriter to use for the output stream
			PrintWriter pw = new PrintWriter(mySocket.getOutputStream());
						
			System.out.println("C - Sending Greeting");
			
			pw.write(GREETING + "\n");
			pw.flush();
			
			String line = null;
			
			System.out.println("C - Waiting for Greeting response");
			
			line = readLine(mySocket);
			
			if(!GREETING.equals(line)) {
				System.err.println("No Greeting returned: " + line);
			}
			
			// UPLOAD
			if(wantToUpload) {
				File uploadFile = new File(fileToUpload);
				System.out.println("C - Got Gretting response... requesting to Upload");

                FileInputStream fis = null;

                try {
                    fis = new FileInputStream(uploadFile);
                } catch (FileNotFoundException ex) {
                    // Do exception handling
                }
                
                pw.write(UPLOAD_REQUEST + " " + uploadFile.length() +"\n");
                pw.flush();
                
                line = readLine(mySocket);
                
                if(!UPLOAD_OK.equals(line)) {
                	System.err.println("Did not get the upload ok from server: " + line);
                	return;
                }
                
                System.out.println("C - Received upload acceptance");
                
                byte[] buffer = new byte[64000];
                
                OutputStream out = mySocket.getOutputStream();
    			
    			while (true) {
    				int bytesRead = fis.read(buffer);
    				if(bytesRead == -1) break;
    				
    				System.out.println("C - Sending " + bytesRead + " bytes");
    				
    				out.write(buffer, 0, bytesRead);
    				out.flush();
    			}
    			
    			System.out.println("C - Sent the whole file");
    			
    			line = readLine(mySocket);
				
				if(!UPLOAD_FINISHED.equals(line)) {
					System.err.println("Did not receive download finished: " + line);
					return;
				}
				
			}
			// DOWNLOAD
			else {
				System.out.println("C - Got Greeting response.. requesting download");
				
				pw.write(DOWNLOAD_REQUEST + " " + fileToDownload + "\n");
				pw.flush();
				
				line = readLine(mySocket);
				
				if(line == null || !line.startsWith(DOWNLOAD_OK)) {
					System.err.println("Unable to download file: " + line);
				}
				
				System.out.println("C - Got download response");
				
				long fileLength = Long.parseLong(line.substring(DOWNLOAD_OK.length()).trim());
				
				File destination = new File("C:\\Users\\Janson\\workspace\\FileServer\\temp\\downloaded.mp3");
					
				FileOutputStream fileOut = new FileOutputStream(destination);
				
				byte[] buffer = new byte[64000];
				long totalBytesRead = 0;
				
				InputStream inFile = mySocket.getInputStream();
				
				while (true) {
					System.out.println("BYTES READ: " + totalBytesRead + "/" + fileLength);
					
					if(totalBytesRead == fileLength) break;
					if(totalBytesRead > fileLength) {
						System.err.println("TOO MANY BYTES READ: " + totalBytesRead + "/" + fileLength);
					}
					System.out.println("C - Waiting for part of file");
					
					int numBytesRead = inFile.read(buffer);
					if(numBytesRead == -1) break;
					
					System.out.println("C - Got " + numBytesRead + " bytes of the file");
					
					fileOut.write(buffer, 0, numBytesRead);
					fileOut.flush();
					
					totalBytesRead += numBytesRead;
				}
				
				if(totalBytesRead != fileLength) {
					System.err.println("Did not receive the whole file, only got: " + totalBytesRead + " bytes");
					return;
				}
				
				System.out.println("C - Got the whole file");
				
				pw.write(DOWNLOAD_FINISHED + "\n");
				pw.flush();
			}
			
			
			
			pw.write(GOODBYE + "\n");
			pw.flush();
			
			System.out.println("C - Sending Goodbye");
									
			mySocket.close();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		MyClient mc = new MyClient();
		mc.start();
	}

}
