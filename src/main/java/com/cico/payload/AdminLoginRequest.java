package com.cico.payload;

import javax.validation.constraints.NotBlank;

import com.cico.util.AppConstants;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminLoginRequest {

    @NotBlank(message = AppConstants.ADMIN_ID_REQUIRED)
    private String adminId;

    @NotBlank(message = AppConstants.PASSWORD_REQUIRED)
    private String password;
}
