package com.cico.exception;

public class FileSizeExceededException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public FileSizeExceededException() {
		super();
	}

	public FileSizeExceededException(String message) {
		super(message);
	}

}
