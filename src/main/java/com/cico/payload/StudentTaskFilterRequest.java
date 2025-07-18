package com.cico.payload;

import javax.validation.constraints.NotNull;

import com.cico.util.AppConstants;

import lombok.Data;

@Data
public class StudentTaskFilterRequest {
	
	@NotNull(message = AppConstants.STUDENT_ID_REQUIRED)
	private Integer studentId; // Use Long for nullability
	
	private String status;
	
	@NotNull(message = AppConstants.PAGINATION_REQUEST_REQUIRED)
	private PaginationRequest pageRequest;
}
