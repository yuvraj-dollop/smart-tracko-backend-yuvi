package com.cico.payload;

import javax.validation.constraints.NotBlank;

import com.cico.util.AppConstants;

import lombok.Data;

@Data
public class RefreshTokenRequest {
	
	@NotBlank(message = AppConstants.REFRESH_TOKEN_REQUIRED)
	private String refreshToken;
//	@NotBlank(message = AppConstants.ACCESS_TOKEN_REQUIRED)
//	private String accessToken;

}
