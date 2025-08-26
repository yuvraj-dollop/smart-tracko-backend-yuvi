package com.cico.payload;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.cico.util.AppConstants;

import lombok.Data;

@Data
public class UpdateChapterRequest {
	  @NotNull(message = AppConstants.CHAPTER_ID_REQUIRED)
	    private Integer chapterId;

	    @NotBlank(message = AppConstants.CHAPTER_NAME_REQUIRED)
	    private String chapterName;

	    @NotNull(message = AppConstants.SUBJECT_ID_REQUIRED)
	    private Integer subjectId;

}
