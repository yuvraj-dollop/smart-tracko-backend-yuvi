package com.cico.service;

import com.cico.payload.AdminLoginRequest;
import com.cico.payload.ApiResponse;
import com.cico.payload.ForgetPasswordRequest;
import com.cico.payload.OtpRequest;
import com.cico.payload.RefreshTokenRequest;
import com.cico.payload.ResetPasswordRequest;
import com.cico.payload.StudentLoginRequest;

public interface IAuthService {

	public ApiResponse studentLogin(StudentLoginRequest loginRequest);

	public ApiResponse adminLogin(AdminLoginRequest loginRequest);

	public ApiResponse forgotPassword(ForgetPasswordRequest request);

	public ApiResponse resetPassword(ResetPasswordRequest request);

	public ApiResponse verifyOtp(OtpRequest otpRequest);

	public ApiResponse resendOtp();

	public ApiResponse logout();

	public ApiResponse refreshToken(RefreshTokenRequest refreshTokenRequest);

}
