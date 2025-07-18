package com.cico.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(value = Include.NON_NULL)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterContentResponse {

	private Integer id;
	private String title;
	private String subTitle;
	private String content;
	private Boolean isDeleted = false;
	private String  chapterName;
}
