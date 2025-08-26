package com.cico.payload;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.cico.util.AppConstants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentFeesRequest {

	@NotNull(message = AppConstants.STUDENT_ID_REQUIRED)
	private Integer studentId;

	@NotNull(message = AppConstants.COURSE_ID_REQUIRED)
	private Integer courseId;

	@NotNull(message = AppConstants.FEES_REQUIRED)
	@DecimalMin(value = "0.0", inclusive = false, message = "Final fees must be greater than 0")
	private Double finalFees;

	@NotBlank(message = AppConstants.DATE_REQUIRED)
	private String date;

}
