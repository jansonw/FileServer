package com.cs456.project.common;

import java.io.Serializable;

public class FileListObject implements Serializable {
	private String displayName;
	private boolean isDirectory;

	public FileListObject(String filePath, String rootDirectory) {		
		String temp = filePath.substring(rootDirectory.length(), filePath.length());
		
		if(temp.contains("\\")) {
			isDirectory = true;
			displayName = temp.substring(0, temp.indexOf("\\"));
		}
		else {
			isDirectory = false;
			displayName = temp;
		}
	}

	public boolean isDirectory() {
		return isDirectory;
	}
	
	public String getDisplayName() {
		return this.displayName;
	}
	
}
