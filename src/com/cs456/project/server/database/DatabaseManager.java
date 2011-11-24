package com.cs456.project.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

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
		ResultSet rs = executeQuery("Select * from Users where username='" + username + "'");
		
		if(rs.next()) {
			throw new RequestExecutionException("The username: " + username + " is already taken.  Please try registering with a different username");
		}
		
		executeQuery("Insert into Users values (" + username + ", " + password + "0, F");
	}
	
	public synchronized ResultSet executeQuery(String query) throws SQLException {
		return connection.createStatement().executeQuery(query);			
	}
}
