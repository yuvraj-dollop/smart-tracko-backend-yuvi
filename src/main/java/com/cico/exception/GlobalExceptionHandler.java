package com.cico.exception;

import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cico.util.AppConstants;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<MyErrorResponse> showMYCustomError(ResourceNotFoundException rnfe) {
		return new ResponseEntity<MyErrorResponse>(
				new MyErrorResponse(new Date().toString(), "Resource Not Found", rnfe.getMessage()),
				HttpStatus.NOT_FOUND);

	}

	@ExceptionHandler(ResourceAlreadyExistException.class)
	public ResponseEntity<MyErrorResponse> showMYCustomError(ResourceAlreadyExistException rfe) {
		return new ResponseEntity<MyErrorResponse>(
				new MyErrorResponse(new Date().toString(), "Resource Is Already Exist", rfe.getMessage()),
				HttpStatus.FOUND);

	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<MyErrorResponse> showMYCustomError(InvalidCredentialsException ice) {
		return new ResponseEntity<MyErrorResponse>(
				new MyErrorResponse(new Date().toString(), "INVALID_CREDENTIALS", ice.getMessage()),
				HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(UnauthorizeException.class)
	public ResponseEntity<MyErrorResponse> showMYCustomError(UnauthorizeException ue) {
		return new ResponseEntity<MyErrorResponse>(
				new MyErrorResponse(new Date().toString(), AppConstants.UNAUTHORIZED, ue.getMessage()),
				HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<MyErrorResponse> showMYCustomError(BadRequestException ue) {
		return new ResponseEntity<MyErrorResponse>(
				new MyErrorResponse(new Date().toString(), AppConstants.UNAUTHORIZED, ue.getMessage()),
				HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ExpiredJwtException.class)
	public ResponseEntity<MyErrorResponse> showMYCustomError(ExpiredJwtException ex) {
		return new ResponseEntity<MyErrorResponse>(
				new MyErrorResponse(new Date().toString(), AppConstants.UNAUTHORIZED, ex.getMessage()),
				HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(TooManyOtpRequestsException.class)
	public ResponseEntity<MyErrorResponse> showMYCustomError(TooManyOtpRequestsException ex) {
		return new ResponseEntity<>(new MyErrorResponse(new Date().toString(), "TOO_MANY_REQUESTS", ex.getMessage()),
				HttpStatus.TOO_MANY_REQUESTS);
	}

	@ExceptionHandler(OtpVerificationLimitExceededException.class)
	public ResponseEntity<MyErrorResponse> showMYCustomError(OtpVerificationLimitExceededException ex) {
		return new ResponseEntity<>(new MyErrorResponse(new Date().toString(), "TOO_MANY_REQUESTS", ex.getMessage()),
				HttpStatus.TOO_MANY_REQUESTS);
	}

	@ExceptionHandler(InvalidFileTypeException.class)
	public ResponseEntity<MyErrorResponse> showMYCustomError(InvalidFileTypeException ue) {
		return new ResponseEntity<MyErrorResponse>(
				new MyErrorResponse(new Date().toString(), AppConstants.INVALID_FILE_TYPE, ue.getMessage()),
				HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(FileSizeExceededException.class)
	public ResponseEntity<MyErrorResponse> showMYCustomError(FileSizeExceededException ue) {
		return new ResponseEntity<MyErrorResponse>(new MyErrorResponse(new Date().toString(),
				"file size too large! File size exceeds allowed limit.", ue.getMessage()),
				HttpStatus.PAYLOAD_TOO_LARGE);
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<MyErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
	    String errorMessage = ex.getBindingResult()
	            .getFieldErrors()
	            .stream()
	            .map(error -> error.getField() + ": " + error.getDefaultMessage())
	            .findFirst()
	            .orElse("Validation failed");

	    return new ResponseEntity<>(
	            new MyErrorResponse(new Date().toString(), "VALIDATION_ERROR", errorMessage),
	            HttpStatus.BAD_REQUEST
	    );
	}


}
