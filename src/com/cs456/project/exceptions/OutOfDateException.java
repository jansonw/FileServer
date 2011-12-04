package com.cs456.project.exceptions;

@SuppressWarnings("serial")
public class OutOfDateException extends Exception {
	public OutOfDateException(String message) {
		super(message);
	}
}
