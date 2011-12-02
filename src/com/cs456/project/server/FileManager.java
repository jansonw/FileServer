package com.cs456.project.server;

import java.util.HashMap;

public class FileManager {
	private static FileManager singleton = null;
	HashMap<String, FileData> files = new HashMap<String, FileData>();
	
	public static synchronized FileManager getInstance() {
		if(singleton == null) {
			singleton = new FileManager();
		}
		
		return singleton;
	}
	
	private FileManager() {}
	
	public synchronized boolean incrementFileInUse(String filePath) {
		FileData file = files.get(filePath);
		
		
		if(file == null) {
			file = new FileData();
		}
		
		if(file.isMarkedForDeletion()) {
			return false;
		}
		
		file.incrementNumDownloaders();
		files.put(filePath, file);
		
		return true;
	}
	
	public synchronized boolean decrementFileInUse(String filePath) {
		FileData file = files.get(filePath);
		
		file.decrementNumDownloaders();		
		files.put(filePath, file);
		
		return file.getNumDownloaders() == 0 && file.isMarkedForDeletion();
	}
	
	public synchronized boolean markForDeletion(String filePath) {
		FileData file = files.get(filePath);
		
		if(file == null) {
			file = new FileData();
		}
		
		file.markForDeletion();
		files.put(filePath, file);
		
		return file.getNumDownloaders() == 0;
	}
}

class FileData {
	private boolean markedForDelete = false;
	private int numDownloaders = 0;
	
	public FileData() {}
	
	public boolean isMarkedForDeletion() {
		return this.markedForDelete;
	}
	
	public void markForDeletion() {
		this.markedForDelete = true;
	}
	
	public int getNumDownloaders() {
		return this.numDownloaders;
	}
	
	public void incrementNumDownloaders() {
		this.numDownloaders++;
	}
	
	public void decrementNumDownloaders() {
		this.numDownloaders--;
	}
}
