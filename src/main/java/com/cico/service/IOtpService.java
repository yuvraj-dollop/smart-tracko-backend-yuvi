package com.cico.service;

import com.cico.util.OtpType;

public interface IOtpService {
	
	String generateOtp(String email, OtpType otpType);

	boolean validateOtp(String email, String otp, OtpType otpType);

//	void removeOtp(String email);

}