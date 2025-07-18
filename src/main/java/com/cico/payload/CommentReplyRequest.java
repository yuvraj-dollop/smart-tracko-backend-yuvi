package com.cico.payload;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

import com.cico.util.AppConstants;

import lombok.Data;

@Data
public class CommentReplyRequest {

	@NotNull(message = AppConstants.STUDENT_ID_REQUIRED)
	private Integer studentId;

	@NotNull(message = AppConstants.COMMENT_ID_REQUIRED)
	private Integer commentsId;

	@NotBlank(message = AppConstants.COMMENT_CONTENT_REQUIRED)
	private String content;

	private MultipartFile file;
}
