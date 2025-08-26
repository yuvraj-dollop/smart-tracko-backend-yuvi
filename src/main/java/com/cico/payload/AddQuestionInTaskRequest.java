package com.cico.payload;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

import com.cico.model.AttachmentStatus;
import com.cico.util.AppConstants;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddQuestionInTaskRequest {

	@NotNull(message = AppConstants.TASK_ID_REQUIRED)
	private Long taskId;

	@NotBlank(message = AppConstants.QUESTION_REQUIRED)
	private String question;

	private String videoUrl;

	private List<MultipartFile> questionImages;

	private MultipartFile attachment;

	private AttachmentStatus status;

	@NotBlank(message = AppConstants.TITLE_REQUIRED)
	private String title;

	private Boolean codeSubmisionStatus;
}
