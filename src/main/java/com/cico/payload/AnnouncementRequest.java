package com.cico.payload;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import com.cico.util.AppConstants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnnouncementRequest {

    @NotBlank(message = AppConstants.TITLE_REQUIRED)
    @Size(max = 100, message = AppConstants.MAX_TITLE_LENGTH)
    private String title;

    @NotBlank(message = AppConstants.MESSAGE_REQUIRED)
    private String message;

    @NotEmpty(message = AppConstants.COURSE_ID_REQUIRED)
    private List<Integer> courseId;
}