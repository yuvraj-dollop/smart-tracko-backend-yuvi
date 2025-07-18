package com.cico.payload;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

import com.cico.util.AppConstants;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSubmissionRequest {

	@NotNull(message = AppConstants.TASK_ID_REQUIRED)
	private Long taskId;

	@NotNull(message = AppConstants.QUESTION_ID_REQUIRED)
	private Long questionId;

	@NotNull(message = AppConstants.STUDENT_ID_REQUIRED)
	private Integer studentId;

	@NotBlank(message = AppConstants.TASK_DESCRIPTION_REQUIRED)
	private String taskDescription;

	private String codeSubmission;

	private MultipartFile submittionFileName;
}
