package com.cico.payload;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.cico.util.AppConstants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentRequest {

    private Long assignmentId; // Optional for update

    @NotBlank(message = AppConstants.TITLE_REQUIRED)
//    @Size(min = 3, max = 100, message = AppConstants.TITLE_SIZE_VALIDATION)
    private String title;

    @NotNull(message = AppConstants.COURSE_ID_REQUIRED)
    private Integer courseId;

    @NotNull(message =AppConstants.SUBJECT_ID_REQUIRED)
    private Integer subjectId;
}
