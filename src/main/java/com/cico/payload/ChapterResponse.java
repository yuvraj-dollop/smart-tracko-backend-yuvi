package com.cico.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(value = Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterResponse {

	private String chapterName;
	private Integer chapterId;
	private String chapterImage;
	private Integer subjectId;
	private String subjectName;
	private Integer scoreGet;

}
