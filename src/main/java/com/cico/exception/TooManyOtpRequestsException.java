package com.cico.exception;

public class TooManyOtpRequestsException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TooManyOtpRequestsException() {
        super();
    }

	public TooManyOtpRequestsException(String message) {
        super(message);
    }
}
