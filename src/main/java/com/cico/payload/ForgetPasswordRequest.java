package com.cico.payload;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgetPasswordRequest {
	@NotBlank(message = "User ID or Email is required")
	private String userIdOrEmail;
}
