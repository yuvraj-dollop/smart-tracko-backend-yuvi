package com.cico.exception;

public class ExpiredJwtException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	public ExpiredJwtException() {
        super();
    }
	public ExpiredJwtException(String message) {
        super(message);
    }

}
