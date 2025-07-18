package com.cico.payload;

import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

import com.cico.util.AppConstants;

import lombok.Data;

@Data
public class UpdateTaskQuestionRequest {

	@NotNull(message = AppConstants.QUESTION_ID_REQUIRED)
	private Long questionId;

	@NotNull(message = AppConstants.TASK_ID_REQUIRED)
	private Long taskId;

	@NotEmpty(message = AppConstants.QUESTION_REQUIRED)
	private String question;

	private String videoUrl;

	private List<String> questionImages; // Existing image URLs

	private List<MultipartFile> newImages; // New images to be uploaded
}
