package com.cico.payload;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

	@NotBlank(message = "New password is required")
	private String newPassword;

	@NotBlank(message = "Confirm password is required")
	private String confirmPassword;
}
