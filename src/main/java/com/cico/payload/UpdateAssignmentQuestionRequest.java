package com.cico.payload;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

import com.cico.util.AppConstants;

import lombok.Data;

@Data
public class UpdateAssignmentQuestionRequest {

	@NotNull(message = AppConstants.QUESTION_ID_REQUIRED)
	private Long questionId;

	@NotBlank(message = AppConstants.QUESTION_REQUIRED)
	private String question;

//	@NotBlank(message = AppConstants.VIDEO_URL_REQUIRED)
	private String videoUrl;
		
	@NotBlank(message = AppConstants.TITLE_REQUIRED)
	private String title;

	private List<String> questionImages; // existing image URLs

	private List<MultipartFile> newImages; // new images to be uploaded

}
