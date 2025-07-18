package com.cico.payload;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.cico.model.Course;
import com.cico.model.Subject;
import com.cico.util.AppConstants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {

    @NotBlank(message = AppConstants.TASK_NAME_REQUIRED)
    private String taskName;

    @NotNull(message =AppConstants.COURSE_NOT_NULL)
    private Course course;

    @NotNull(message = AppConstants.SUJECT_NOT_NULL)
    private Subject subject;

    // Optional field; no validation
    private String attachmentStatus;
}
