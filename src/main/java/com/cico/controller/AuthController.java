package com.cico.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cico.payload.AdminLoginRequest;
import com.cico.payload.ApiResponse;
import com.cico.payload.ForgetPasswordRequest;
import com.cico.payload.OtpRequest;
import com.cico.payload.ResetPasswordRequest;
import com.cico.payload.StudentLoginRequest;
import com.cico.service.IAuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private IAuthService authService;

    // STUDENT LOGIN (generates auth token + OTP)
    @PostMapping("/v2/student/login")
    public ResponseEntity<ApiResponse> studentLogin(@Valid @RequestBody StudentLoginRequest request) {
        ApiResponse response = authService.studentLogin(request);
        return ResponseEntity.status(response.getHttp()).body(response);
    }

    // ADMIN LOGIN (generates auth token + OTP)
    @PostMapping("/v2/admin/login")
    public ResponseEntity<ApiResponse> adminLogin(@Valid @RequestBody AdminLoginRequest request) {
        ApiResponse response = authService.adminLogin(request);
        return ResponseEntity.status(response.getHttp()).body(response);
    }
    
    //for both, admin and student
    @PostMapping("/v2/forget-password")
    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody ForgetPasswordRequest request) {
        ApiResponse response = authService.forgotPassword(request);
        return ResponseEntity.status(response.getHttp()).body(response);
    }
    
  //for both, admin and student after forgot password
    @PostMapping("/v2/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        ApiResponse response = authService.resetPassword(request);
        return ResponseEntity.status(response.getHttp()).body(response);
    }

    // OTP VERIFICATION (requires auth token in Authorization header)
    @PostMapping("/v2/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtp(@Valid @RequestBody OtpRequest request) {
        ApiResponse response = authService.verifyOtp(request);
        return ResponseEntity.status(response.getHttp()).body(response);
    }

    // RESEND OTP (rate-limited)
    @PostMapping("/v2/resend-otp")
    public ResponseEntity<ApiResponse> resendOtp() {
        ApiResponse response = authService.resendOtp();
        return ResponseEntity.status(response.getHttp()).body(response);
    }
    
    @DeleteMapping("/v2/logout")
	public ResponseEntity<ApiResponse> logout() {
		ApiResponse response = authService.logout();
		return ResponseEntity.status(response.getHttp()).body(response);
	}
}
