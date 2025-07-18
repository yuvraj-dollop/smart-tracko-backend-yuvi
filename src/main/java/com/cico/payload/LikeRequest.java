package com.cico.payload;

import javax.validation.constraints.NotNull;

import com.cico.util.AppConstants;

import lombok.Data;

@Data
public class LikeRequest {

	@NotNull(message = AppConstants.STUDENT_ID_REQUIRED)
	private Integer studentId;

	@NotNull(message = AppConstants.DISCUSSION_FORM_ID_REQUIRED)
	private Integer discussionFormId;

}
