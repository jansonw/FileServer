package com.cs456.project.common;

public class FileWrapper {
	private String filePath = null;
	private String owner = null;
	private boolean isShared = false;
	private boolean isComplete = true;
	private boolean markedForDeletion = false;
	
	public FileWrapper(String filePath, String owner, boolean isShared, boolean isComplete, boolean markedForDeletion) {
		this.filePath = filePath;
		this.owner = owner;
		this.isShared = isShared;
		this.isComplete = isComplete;
		this.markedForDeletion = markedForDeletion;
	}
	
	public String getFilePath() {
		return filePath;
	}
	
	public String getOwner() {
		return owner;
	}
	
	public boolean isShared() {
		return isShared;
	}
	
	public boolean isComplete() {
		return this.isComplete;
	}
	
	public boolean isMarkedForDeletion() {
		return markedForDeletion;
	}
	
	public static boolean charToBoolean(String str) {
		return ("Y".equals(str)) ? true : false;
	}
	
	public static String booleanToChar(boolean bool) {
		return (bool) ? "Y" : "N";
	}
}
