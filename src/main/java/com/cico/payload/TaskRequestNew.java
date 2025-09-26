package com.cico.payload;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.cico.util.AppConstants;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskRequestNew {
	@NotBlank(message = AppConstants.TASK_NAME_REQUIRED)
	private String taskName;

	@NotNull(message = AppConstants.COURSE_NOT_NULL)
	private Integer courseId;

	@NotNull(message = AppConstants.SUJECT_NOT_NULL)
	private Integer subjectId;

	// Optional field; no validation
	private String attachmentStatus;
}
