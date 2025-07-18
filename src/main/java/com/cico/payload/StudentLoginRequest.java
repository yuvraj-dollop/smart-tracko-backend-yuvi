package com.cico.payload;

import javax.validation.constraints.NotBlank;

import com.cico.util.AppConstants;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentLoginRequest {

	@NotBlank(message = AppConstants.USERNAME_OR_EMAIL_REQUIRED)
	private String userIdOrEmail;
	
	@NotBlank(message = AppConstants.PASSWORD_REQUIRED)
	private String password;
}
