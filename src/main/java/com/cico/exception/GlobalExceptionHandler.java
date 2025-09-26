package com.cico.exception;

import java.sql.Timestamp;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cico.util.AppConstants;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<MyErrorResponse> showMyCustomError(ResourceNotFoundException rnfe) {
		return new ResponseEntity<MyErrorResponse>(
				new MyErrorResponse(new Date().toString(), "Resource Not Found", rnfe.getMessage()),
				HttpStatus.NOT_FOUND);

	}

	@ExceptionHandler(ResourceAlreadyExistException.class)
	public ResponseEntity<MyErrorResponse> showMyCustomError(ResourceAlreadyExistException rfe) {
		return new ResponseEntity<MyErrorResponse>(
				new MyErrorResponse(new Date().toString(), "Resource Is Already Exist", rfe.getMessage()),
				HttpStatus.FOUND);

	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<MyErrorResponse> showMyCustomError(InvalidCredentialsException ice) {
		return new ResponseEntity<MyErrorResponse>(
				new MyErrorResponse(new Date().toString(), "INVALID_CREDENTIALS", ice.getMessage()),
				HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(UnauthorizeException.class)
	public ResponseEntity<MyErrorResponse> showMyCustomError(UnauthorizeException ue) {
		return new ResponseEntity<MyErrorResponse>(
				new MyErrorResponse(new Date().toString(), AppConstants.UNAUTHORIZED, ue.getMessage()),
				HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<MyErrorResponse> showMyCustomError(BadRequestException ue) {
		return new ResponseEntity<MyErrorResponse>(
				new MyErrorResponse(new Date().toString(), AppConstants.UNAUTHORIZED, ue.getMessage()),
				HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ExpiredJwtException.class)
	public ResponseEntity<MyErrorResponse> showMyCustomError(ExpiredJwtException ex) {
		return new ResponseEntity<MyErrorResponse>(
				new MyErrorResponse(new Date().toString(), AppConstants.UNAUTHORIZED, ex.getMessage()),
				HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(TooManyOtpRequestsException.class)
	public ResponseEntity<MyErrorResponse> showMyCustomError(TooManyOtpRequestsException ex) {
		return new ResponseEntity<>(new MyErrorResponse(new Date().toString(), "TOO_MANY_REQUESTS", ex.getMessage()),
				HttpStatus.TOO_MANY_REQUESTS);
	}

	@ExceptionHandler(OtpVerificationLimitExceededException.class)
	public ResponseEntity<MyErrorResponse> showMyCustomError(OtpVerificationLimitExceededException ex) {
		return new ResponseEntity<>(new MyErrorResponse(new Date().toString(), "TOO_MANY_REQUESTS", ex.getMessage()),
				HttpStatus.TOO_MANY_REQUESTS);
	}

	@ExceptionHandler(InvalidFileTypeException.class)
	public ResponseEntity<MyErrorResponse> showMyCustomError(InvalidFileTypeException ue) {
		return new ResponseEntity<MyErrorResponse>(
				new MyErrorResponse(new Date().toString(), AppConstants.INVALID_FILE_TYPE, ue.getMessage()),
				HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(InvalidException.class)
	public ResponseEntity<MyErrorResponse> showMyCustomError(InvalidException ue) {
		return new ResponseEntity<MyErrorResponse>(
				new MyErrorResponse(new Date().toString(), AppConstants.INVALID_DATA, ue.getMessage()),
				HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(FileSizeExceededException.class)
	public ResponseEntity<MyErrorResponse> showMyCustomError(FileSizeExceededException ue) {
		return new ResponseEntity<MyErrorResponse>(new MyErrorResponse(new Date().toString(),
				"file size too large! File size exceeds allowed limit.", ue.getMessage()),
				HttpStatus.PAYLOAD_TOO_LARGE);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<MyErrorResponse> showMyCustomError(MethodArgumentNotValidException ex) {
		String errorMessage = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage()).findFirst()
				.orElse("Validation failed");

		return new ResponseEntity<>(new MyErrorResponse(new Date().toString(), "VALIDATION_ERROR", errorMessage),
				HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(DateTimeParseException.class)
	public ResponseEntity<?> handleDateTimeParseException(DateTimeParseException ex) {
		return ResponseEntity.badRequest().body(Map.of("message", "Invalid date format. Please use yyyy-MM-dd",
				"status", HttpStatus.BAD_REQUEST, "date", new Timestamp(System.currentTimeMillis())));
	}
//	@ExceptionHandler(RequestRejectedException.class)
//	public ResponseEntity<String> handleRequestRejected(RequestRejectedException ex) {
//		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request: " + ex.getMessage());
//	}
}
