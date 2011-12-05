package com.cs456.client;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A class containing a static method to return a stringified list of files
 * located withing a provided directoy
 * 
 * @author Janson
 * 
 */
public class FileListing {

	/**
	 * Returns a string containing sorted newline separated files located when
	 * doing a recursive file listing using the provided directory as the root
	 * 
	 * @param directory
	 *            The base directory from which the file listing should be run
	 * @return A sorted string representation of all files found using the
	 *         provided directory as the root
	 */
	public static String getSortedFileListString(File directory) {

		// Get a list of all of the files recursively located on in the provided
		// directory
		List<File> files = getUnsortedFileList(directory);

		// Sort these files for easy viewing
		Collections.sort(files);

		String returnString = "";

		// Create a string representation of the file listing, with each file
		// separated by a newline
		for (File file : files) {
			returnString += file.getPath() + "\n";
		}

		// Return the string minus the last newline character!
		return returnString.substring(0, returnString.length() - 1);
	}

	/**
	 * Will return an unsorted list containing all files found using the
	 * provided directory as the root
	 * 
	 * @param directory
	 *            The base directory from which the file listing should be run
	 * @return An unsorted list of all files found using the provided directory
	 *         as the root
	 */
	public static List<File> getUnsortedFileList(File directory) {
		List<File> fileList = new ArrayList<File>();

		// Get the files and directories located at the level of the provided
		// directory
		File[] fileAndDirList = directory.listFiles();

		// Loop through every file and directory found
		for (int i = 0; i < fileAndDirList.length; i++) {

			// Store the current file we are working with
			File curFile = fileAndDirList[i];

			// We don't want to look for files starting with a period
			if (curFile.getName().startsWith(".")) {
				continue;
			}

			// If the current file is a file, we will add it to the list of
			// files found
			if (curFile.isFile()) {
				fileList.add(curFile);
			}
			// Otherwise we will recurse and obtain all of the files found to
			// the file listing
			else {
				List<File> nextList = getUnsortedFileList(curFile); // Recurse!
				fileList.addAll(nextList);
			}
		}

		return fileList;
	}

}
