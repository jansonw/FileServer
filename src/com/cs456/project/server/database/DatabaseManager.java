package com.cs456.project.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.cs456.project.common.FileListObject;
import com.cs456.project.common.FileWrapper;
import com.cs456.project.exceptions.RequestExecutionException;

public class DatabaseManager {
	private static DatabaseManager singleton = null;
	private Connection connection = null;
	private static Logger logger = Logger.getLogger(DatabaseManager.class);
	
	public static synchronized DatabaseManager getInstance() {
		if(singleton == null) {
			try {
				singleton = new DatabaseManager();
			} catch (ClassNotFoundException e) {
				logger.error("Unable to find the ojdbc driver", e);
				singleton = null;
			} catch (SQLException e) {
				logger.error("An error occurred while establishing a connection to the database", e);
				singleton = null;
			}
		}
		
		return singleton;
	}
	
	private DatabaseManager() throws ClassNotFoundException, SQLException {
		// Load the JDBC driver
		String driverName = "oracle.jdbc.driver.OracleDriver";
		Class.forName(driverName);
		
		// Create a connection to the database
		String serverName = "localhost";
		String portNumber = "1521";
		String sid = "xe";
		String url = "jdbc:oracle:thin:@" + serverName + ":" + portNumber
				+ ":" + sid;
		String username = "janson";
		String password = "159753";
		connection = DriverManager.getConnection(url, username, password);
	}
	
	public synchronized void registerUser(String username, String password) throws SQLException, RequestExecutionException {
		ResultSet rs = executeQuery("Select * from Users where upper(username)=upper('" + username + "')");
		
		if(rs.next()) {
			throw new RequestExecutionException("The username: " + username + " is already taken.");
		}
		
		executeQuery("Insert into Users values (upper('" + username + "'), '" + password + "', '0', 'N')");
	}
	
	public synchronized void passwordChange(String username, String oldPassword, String newPassword) throws SQLException, RequestExecutionException {
		ResultSet rs = executeQuery("Select * from Users where upper(username)=upper('" + username + "') and password='" + oldPassword + "'");
		
		if(!rs.next()) {
			throw new RequestExecutionException("The password supplied for username: " + username + " is incorrect!");
		}
		
		executeQuery("Update Users set password='" + newPassword + "' where upper(username)=upper('" + username + "')");
	}
	
	public synchronized void addFile(FileWrapper wrapper) throws SQLException, RequestExecutionException {
		ResultSet rs = executeQuery("Select * from Files where upper(file_path)=upper('" + wrapper.getFilePath() + "')");
		
		if(rs.next()) {
			throw new RequestExecutionException("The file_path: " + wrapper.getFilePath() + " already exists.");
		}
		
		executeQuery("Insert into Files values ('" 
				+ wrapper.getFilePath() + "', upper('"
				+ wrapper.getOwner() + "'), '"
				+ FileWrapper.booleanToChar(wrapper.isShared()) + "', '"
				+ FileWrapper.booleanToChar(wrapper.isComplete()) + "', "
				+ "'N')");
	}
	
	public synchronized void deleteFile(String filePath) throws SQLException, RequestExecutionException {
		ResultSet rs = executeQuery("Select * from Files where upper(file_path)=upper('" + filePath + "')");
		
		if(!rs.next()) {
			throw new RequestExecutionException("The file_path: " + filePath + " does not exist.");
		}
		
		executeQuery("Delete from Files where upper(file_path)=upper('" + filePath + "')");
	}
	
	public synchronized void updateFile(String lookupFilePath, FileWrapper wrapper) throws SQLException, RequestExecutionException {
		ResultSet rs = executeQuery("Select * from Files where upper(file_path)=upper('" + lookupFilePath + "')");
		
		if(!rs.next()) {
			throw new RequestExecutionException("The file_path: " + lookupFilePath + " does not exist.");
		}
		
		executeQuery("Update Files " + "set file_path='" + wrapper.getFilePath() 
				+ "', shared='" + FileWrapper.booleanToChar(wrapper.isShared())
				+ "', complete='" + FileWrapper.booleanToChar(wrapper.isComplete()) 
				+ "', marked_for_deletion='" + FileWrapper.booleanToChar(wrapper.isMarkedForDeletion())
				+ "' where upper(file_path)=upper('" + lookupFilePath + "')");
	}
	
	public synchronized void updatePermissions(FileWrapper wrapper) throws SQLException, RequestExecutionException {
		ResultSet rs = executeQuery("Select * from Files where upper(file_path)=upper('" + wrapper.getFilePath() + "') and owner=upper('" + wrapper.getOwner() + "') and complete='Y' and marked_for_deletion='N'");
		
		if(!rs.next()) {
			throw new RequestExecutionException("The file_path: " + wrapper.getFilePath() + " does not exist for owner: " + wrapper.getOwner().toUpperCase() + " that is complete");
		}
		
		executeQuery("Update Files " + "set shared='" + FileWrapper.booleanToChar(wrapper.isShared())
				+ "' where upper(file_path)=upper('" +  wrapper.getFilePath() + "') and owner=upper('" + wrapper.getOwner() + "') and complete='Y' and marked_for_deletion='N'");
	}
	
	public synchronized FileWrapper getFile(String filePath, boolean showDeleted) throws SQLException {
		String query = "Select * from Files where upper(file_path)=upper('" + filePath + "')";
		
		if(!showDeleted) {
			query += " and marked_for_deletion='N'";
		}
		
		ResultSet rs = executeQuery(query);
		
		if(!rs.next()) {
			return null;
		}
		
		String owner = rs.getString("owner");
		boolean isShared = FileWrapper.charToBoolean(rs.getString("shared"));
		boolean isComplete = FileWrapper.charToBoolean(rs.getString("complete"));
		boolean isMarkedForDeletion = FileWrapper.charToBoolean(rs.getString("marked_for_deletion"));
		
		
		return new FileWrapper(filePath, owner, isShared, isComplete, isMarkedForDeletion);
	}
	
	public synchronized Set<FileListObject> getFileList(String rootPath, String owner) throws SQLException {
		ResultSet rs = executeQuery("Select file_path from Files where " +
				"upper(file_path) LIKE upper('" + rootPath + "%') " +
				"and (owner=upper('" + owner + "') or (shared='Y' and complete='Y')) " +
				"and marked_for_deletion='N'");
		
		Set<FileListObject> fileList = new HashSet<FileListObject>();
		
		while(rs.next()) {
			fileList.add(new FileListObject(rs.getString("file_path"), rootPath));
		}

		return fileList;
	}
	
	public synchronized ResultSet executeQuery(String query) throws SQLException {
		return connection.createStatement().executeQuery(query);			
	}
}
