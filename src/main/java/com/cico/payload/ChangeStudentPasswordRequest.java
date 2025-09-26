package com.cico.payload;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.cico.util.AppConstants;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeStudentPasswordRequest {
	@Pattern(regexp = AppConstants.PASSWORD_REGEX, message = AppConstants.INVALID_PASSWORD_FROMAT)
	@NotBlank(message = AppConstants.PASSWORD_REQUIRED)
	private String newPassword;
	@NotNull(message = AppConstants.STUDENT_ID_REQUIRED)
	private Integer studentId;
}
