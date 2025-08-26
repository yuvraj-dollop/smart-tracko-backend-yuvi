package com.cico.payload;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class TestFilterRequest {

	@NotNull(message = "course ID cannot be null")
	private Integer courseId;
	private Integer subjectId;
	@NotNull(message = "Student ID cannot be null")
	private Integer studentId;
	private PaginationRequest paginationRequest;
	private String status;
}
