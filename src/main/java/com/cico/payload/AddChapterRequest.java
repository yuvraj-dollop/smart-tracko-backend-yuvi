package com.cico.payload;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

import com.cico.util.AppConstants;

import lombok.Data;

@Data
public class AddChapterRequest {
	
	@NotNull(message = AppConstants.SUBJECT_ID_REQUIRED)
    private Integer subjectId;

    @NotBlank(message = AppConstants.CHAPTER_NAME_REQUIRED)
    private String chapterName;

    private MultipartFile image; // Optional image, no validation needed

}
