package com.cs456.project.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileListManager {
	private List<FileListObject> files = new ArrayList<FileListObject>();
	private List<FileListObject> directories = new ArrayList<FileListObject>();
	
	
	public FileListManager(List<FileListObject> list) {
		for(FileListObject file : list) {
			if(file.isDirectory()) {
				directories.add(file);
			}
			else {
				files.add(file);
			}
		}
		
		Collections.sort(directories);
		Collections.sort(files);
	}
	
	public List<FileListObject> getDirectories() {
		return this.directories;
	}
	
	public List<FileListObject> getFiles() {
		return this.files;
	}
	
	public List<FileListObject> getAll(boolean directoriesFirst) {
		List<FileListObject> returnList = new ArrayList<FileListObject>();
		
		if(directoriesFirst) {
			returnList.addAll(directories);
			returnList.addAll(files);
		}
		else {
			returnList.addAll(files);
			returnList.addAll(directories);
		}
		
		return returnList;
		
	}
}
