package com.cico.payload;

import java.time.LocalDateTime;
import java.util.List;

import com.cico.model.AttachmentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(value = Include.NON_NULL)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class TaskQuestionResponse {

	private Long questionId;

	private String question;

	private List<String> questionImages;

	private String videoUrl;
	private Boolean isActive;
	private Boolean isSubmitted;
	private String taskAttachment;
	private AttachmentStatus attachmentStatus;
	private LocalDateTime submissionDate;
	private String title;
	private Boolean codeSubmisionStatus;

	public TaskQuestionResponse(Long questionId, String question, List<String> questionImages, String videoUrl,String title) {
		super();
		this.questionId = questionId;
		this.question = question;
		this.questionImages = questionImages;
		this.videoUrl = videoUrl;
		this.title = title;

	}

	public TaskQuestionResponse(Long questionId, String question, Boolean isSubmitted, LocalDateTime submissionDate) {
		super();
		this.questionId = questionId;
		this.question = question;
		this.isSubmitted = isSubmitted;
		this.submissionDate = submissionDate;
	}

}
