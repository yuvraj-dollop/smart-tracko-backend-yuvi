package com.cico.payload;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class TestFilterRequest {

	@NotBlank(message = "course ID cannot be null")
	private Integer courseId;
	private Integer subjectId;
	@NotBlank(message = "Student ID cannot be null")
	private Integer studentId;
	private PaginationRequest paginationRequest;
	private String status;
}
