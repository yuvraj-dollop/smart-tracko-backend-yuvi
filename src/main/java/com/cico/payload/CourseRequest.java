package com.cico.payload;

import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.cico.util.AppConstants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseRequest {

	private Integer courseId; // Optional for create, used for update

	@NotBlank(message = AppConstants.COURSE_NAME_REQUIRED)
	@Size(min = 2, max = 100, message = "Course name must be between 2 and 100 characters")
	private String courseName;

	@NotBlank(message = AppConstants.COURSE_FEES_REQUIRED)
//    @Pattern(regexp = "\\d+(\\.\\d{1,2})?", message = "Course fees must be a valid decimal number")
	private String courseFees;

	@NotBlank(message = AppConstants.DURATION_REQUIRED)
	@Min(value = 1, message = AppConstants.DURATION_POSITIVE_REQUIRED)
	private String duration;

	@NotBlank(message = AppConstants.SHORT_DESCRIPTION_REQUIRED)
	@Size(max = 255, message = AppConstants.SHORT_DESCRIPTION_MAX_SIZE)
	private String sortDescription;

	@NotNull(message = AppConstants.TECHNOLOGY_STACK_ID_REQUIRED)
	private Integer technologyStack;

	@NotEmpty(message = AppConstants.MIN_SUBJECT_ID)
	private List<@NotNull(message = AppConstants.SUBJECT_ID_NOT_NULL) Integer> subjectIds;

	@NotNull(message = AppConstants.STARTER_COURSE_NOT_DETERMINED)
	private Boolean isStarterCourse;
}
