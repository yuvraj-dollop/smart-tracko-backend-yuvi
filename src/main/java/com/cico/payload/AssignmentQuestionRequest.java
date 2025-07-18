package com.cico.payload;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

import com.cico.util.AppConstants;

import lombok.Data;

@Data
public class AssignmentQuestionRequest {

	@NotNull(message = AppConstants.ASSIGNMENT_ID_REQUIRED)
	private Long assignmentId;

	@NotBlank(message = AppConstants.QUESTION_REQUIRED)
	private String question;

	@NotBlank(message = AppConstants.VIDEO_URL_REQUIRED)
	private String videoUrl;

	private List<MultipartFile> questionImages;

}
