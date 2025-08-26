package com.cico.payload;

import javax.validation.constraints.NotNull;

import com.cico.util.AppConstants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentSubmissionRequest {

	@NotNull(message = AppConstants.ASSIGNMENT_ID_REQUIRED)
	private Long assignmentId;
	@NotNull(message = AppConstants.TASK_ID_REQUIRED)
	private Long taskId;
	@NotNull(message = AppConstants.STUDENT_ID_REQUIRED)
	private Integer studentId;
	@NotNull(message = AppConstants.DESCRIPTION_REQUIRED)
	private String description;
}
