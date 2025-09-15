package com.cico.payload;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateChapterContentRequest {

	@NotBlank(message = "Title is required")
	private String title;

	@NotBlank(message = "Sub-title is required")
	private String subTitle;

	@NotBlank(message = "Content is required")
	private String content;

	@NotNull(message = "Content ID is required")
	private Integer contentId;
}