package com.cico.payload;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

import com.cico.util.AppConstants;

import lombok.Data;

@Data
public class CreateCommentRequest {

	@NotNull(message = AppConstants.STUDENT_ID_REQUIRED)
	private Integer studentId;

	@NotBlank(message = AppConstants.CONTENT_REQUIRED)
	private String content;

	@NotNull(message = AppConstants.DISCUSSION_FORM_ID_REQUIRED)
	private Integer discussionFormId;

	private MultipartFile file;
}
