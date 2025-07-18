package com.cico.payload;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.cico.util.AppConstants;

import lombok.Data;

@Data
public class AddChapterContentRequest {

	@NotNull(message = AppConstants.CHAPTER_ID_REQUIRED)
	private Integer chapterId;

	@NotBlank(message = AppConstants.CHAPTER_CONTENT_TITLE_REQUIRED)
	private String title;

	@NotBlank(message = AppConstants.CHAPTER_CONTENT_SUBTITLE_REQUIRED)
	private String subTitle;

	@NotBlank(message = AppConstants.CHAPTER_CONTENT_BODY_REQUIRED)
	private String content;

}
