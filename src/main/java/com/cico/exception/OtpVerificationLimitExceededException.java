package com.cico.exception;

public class OtpVerificationLimitExceededException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public OtpVerificationLimitExceededException() {
        super();
    }
	
	public OtpVerificationLimitExceededException(String message) {
        super(message);
    }
}
