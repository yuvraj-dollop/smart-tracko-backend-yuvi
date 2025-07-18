package com.cico.payload;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.cico.util.AppConstants;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpRequest {

    // Will be extracted from token, not sent by client
    private String usernameOrEmail;

    @NotBlank(message = AppConstants.OTP_REQUIRED)
    @Pattern(regexp = "^[0-9]{6}$", message = AppConstants.OTP_LENGHT_VALIDATION)
    private String otp;
}
