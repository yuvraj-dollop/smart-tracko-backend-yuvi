package com.cico.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.cico.exception.BadRequestException;
import com.cico.exception.InvalidCredentialsException;
import com.cico.exception.ResourceNotFoundException;
import com.cico.exception.UnauthorizeException;
import com.cico.model.Admin;
import com.cico.model.QrManage;
import com.cico.model.Student;
import com.cico.model.TokenManagement;
import com.cico.payload.AdminLoginRequest;
import com.cico.payload.ApiResponse;
import com.cico.payload.ForgetPasswordRequest;
import com.cico.payload.OtpRequest;
import com.cico.payload.RefreshTokenRequest;
import com.cico.payload.ResetPasswordRequest;
import com.cico.payload.StudentLoginRequest;
import com.cico.repository.AdminRepository;
import com.cico.repository.QrManageRepository;
import com.cico.repository.StudentRepository;
import com.cico.repository.TokenManagementRepository;
import com.cico.security.JwtUtil;
import com.cico.service.IAuthService;
import com.cico.service.IOtpService;
import com.cico.service.IQRService;
import com.cico.service.IStudentService;
import com.cico.service.ITokenManagementService;
import com.cico.util.AppConstants;
import com.cico.util.OtpType;
import com.cico.util.TokenType;

@Service
public class AuthSeriveImpl implements IAuthService {

	@Autowired
	private AdminRepository repo;

	@Autowired
	private StudentRepository studRepo;

//	@Autowired
//	private OtpRepository otpRepo;

	@Autowired
	private BCryptPasswordEncoder encoder;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private IOtpService otpService;
	@Autowired
	private ITokenManagementService tokenManagementService;
	@Autowired
	private IStudentService studentService;
	@Autowired
	private IQRService qrService;
	@Autowired
	private QrManageRepository qrManageRepository;

	@Override
	public ApiResponse studentLogin(StudentLoginRequest loginRequest) {
		String userIdOrEmail = loginRequest.getUserIdOrEmail();
		String password = loginRequest.getPassword();

		Optional<Student> optionalStudent = studRepo.findByUserIdOrEmailAndAndIsActive(userIdOrEmail, userIdOrEmail,
				true);

		if (optionalStudent.isEmpty()) {
			throw new ResourceNotFoundException("Student not found with given userId or email.");
		}

		Student student = optionalStudent.get();

		if (!encoder.matches(password, student.getPassword())) {
			throw new UnauthorizeException("Invalid email/userId or password.");
		}

		String token = jwtUtil.generateTokenForStudent(student.getStudentId().toString(), student.getUserId(),
				student.getDeviceId(), student.getRole(), TokenType.AUTH_TOKEN, OtpType.LOGIN, false);

		// Generate OTP
		String otp = otpService.generateOtp(student.getUserId(), OtpType.LOGIN);
		// emailService.sendOtpEmail(student.getEmail(), otp);

		Map<String, Object> res = new HashMap<>();
		res.put("authToken", token);
		res.put("otp", otp);

		return ApiResponse.builder().success(true).message("OTP sent to your registered email. Please verify.")
				.http(HttpStatus.OK).data(res).build();
	}

	@Override
	public ApiResponse adminLogin(AdminLoginRequest loginRequest) {
		String adminId = loginRequest.getAdminId();
		String password = loginRequest.getPassword();

		Optional<Admin> optionalAdmin = repo.findByAdminEmail(adminId);
		if (optionalAdmin.isEmpty() || !encoder.matches(password, optionalAdmin.get().getPassword())) {
			throw new InvalidCredentialsException(AppConstants.INVALID_CREDENTIALS);
		}

		Admin admin = optionalAdmin.get();

		String authToken = jwtUtil.generateTokenForAdmin(admin.getAdminEmail(), // subject
				TokenType.AUTH_TOKEN, OtpType.LOGIN,false);

		// Generate OTP
		String otp = otpService.generateOtp(admin.getAdminEmail(), OtpType.LOGIN);
		// emailService.sendOtpEmail(admin.getAdminEmail(), otp);

		Map<String, Object> data = new HashMap<>();
		data.put("authToken", authToken);
		data.put("otp", otp);

		return ApiResponse.builder().success(true).message("OTP sent. Please verify to continue.").http(HttpStatus.OK)
				.data(data).build();
	}

	@Override
	public ApiResponse forgotPassword(ForgetPasswordRequest request) {
		String input = request.getUserIdOrEmail();

		Optional<Student> studentOpt = studRepo.findByUserIdOrEmailAndAndIsActive(input, input, true);
		Optional<Admin> adminOpt = repo.findByAdminEmail(input);

		String email;
		String role;
		String token;
		String otp;

		if (studentOpt.isPresent()) {
			Student student = studentOpt.get();
			email = student.getEmail();
			role = student.getRole();
			token = jwtUtil.generateTokenForStudent(student.getStudentId().toString(), student.getUserId(),
					student.getDeviceId(), role, TokenType.AUTH_TOKEN, OtpType.FORGET_PASSWORD, false);
		} else if (adminOpt.isPresent()) {
			Admin admin = adminOpt.get();
			email = admin.getAdminEmail();
			role = "ADMIN";
			token = jwtUtil.generateTokenForAdmin(email, TokenType.AUTH_TOKEN, OtpType.FORGET_PASSWORD,false);
		} else
			throw new ResourceNotFoundException("User not Found");

		otp = otpService.generateOtp(email, OtpType.FORGET_PASSWORD);

		Map<String, Object> res = new HashMap<>();
		res.put("authToken", token);
		res.put("otp", otp);

		return ApiResponse.builder().success(true).message("OTP sent to your registered email for password reset.")
				.http(HttpStatus.OK).data(res).build();
	}

	@Override
	public ApiResponse resetPassword(ResetPasswordRequest request) {
		String token = jwtUtil.getToken();
		String email = jwtUtil.getUsername(token);

		TokenType tokenType = TokenType.valueOf(jwtUtil.getHeader(token, "tokenType").toString());
		OtpType otpType = OtpType.valueOf(jwtUtil.getHeader(token, "otpType").toString());

		if (tokenType != TokenType.AUTH_TOKEN || otpType != OtpType.FORGET_PASSWORD) {
			throw new UnauthorizeException("Invalid token type for resetting password.");
		}
		Boolean isOtpVerified = (Boolean) jwtUtil.getHeader(token, "isOtpVerified");
		if (!isOtpVerified)
			throw new UnauthorizeException("OTP is not verified");

		if (!request.getNewPassword().equals(request.getConfirmPassword())) {
			throw new BadRequestException("Passwords do not match.");
		}
		System.err.println("EMAIL ==> " + email);

		Student student = studRepo.findByUserId(email);
		Optional<Admin> adminOpt = repo.findByAdminEmail(email);

		if (student != null) {
			student.setPassword(encoder.encode(request.getNewPassword()));
			studRepo.save(student);
		} else if (adminOpt.isPresent()) {
			Admin admin = adminOpt.get();
			admin.setPassword(encoder.encode(request.getNewPassword()));
			repo.save(admin);
		} else {
			return ApiResponse.builder().success(false).message("User not found.").http(HttpStatus.NOT_FOUND).build();
		}

		return ApiResponse.builder().success(true).message("Password reset successful. Please login again.")
				.http(HttpStatus.OK).build();
	}

	@Override
	public ApiResponse resendOtp() {
		String token = jwtUtil.getToken();
		String email = jwtUtil.getUsername(token);

		TokenType tokenType = TokenType.valueOf(jwtUtil.getHeader(token, "tokenType").toString());
		OtpType otpType = OtpType.valueOf(jwtUtil.getHeader(token, "otpType").toString());

		if (tokenType != TokenType.AUTH_TOKEN) {
			throw new UnauthorizeException("Only AUTH_TOKENs can be used for resending OTP.");
		}

		if (otpType != OtpType.LOGIN && otpType != OtpType.FORGET_PASSWORD) {
			throw new BadRequestException("OTP resend not allowed for this OTP type.");
		}

		String newOtp = otpService.generateOtp(email, otpType);

		Map<String, Object> data = new HashMap<>();
		data.put("otp", newOtp); // for dev/testing only

		return ApiResponse.builder().success(true).message("New OTP has been sent.").http(HttpStatus.OK).data(data)
				.build();
	}

//	@Override
//	public ApiResponse verifyOtp(OtpRequest otpRequest) {
//		String token = jwtUtil.getToken();
//		if (token == null) {
//			throw new UnauthorizeException("Missing token");
//		}
//
//		String userId = jwtUtil.getUsername(token);
//		String role = String.valueOf(jwtUtil.getHeader(token, "Role"));
//		String tokenTypeStr = String.valueOf(jwtUtil.getHeader(token, "tokenType"));
//		String otpTypeStr = String.valueOf(jwtUtil.getHeader(token, "otpType"));
//
//		TokenType tokenType = TokenType.valueOf(tokenTypeStr);
//		OtpType otpType = OtpType.valueOf(otpTypeStr);
//
//		if (tokenType != TokenType.AUTH_TOKEN || !(otpType == OtpType.LOGIN || otpType == OtpType.FORGET_PASSWORD)) {
//			throw new UnauthorizeException("Invalid token type or OTP type for verification.");
//		}
//
//		boolean isValid;
//		String otpIdentifier;
//		Map<String, Object> res = new HashMap<>();
//
//		if ("STUDENT".equalsIgnoreCase(role)) {
//			Student student = studRepo.findByUserId(userId);
//			if (student == null) {
//				throw new ResourceNotFoundException(AppConstants.STUDENT_NOT_EXISTS_MESSAGE);
//			}
//
//			otpIdentifier = (otpType == OtpType.FORGET_PASSWORD) ? student.getEmail() : student.getUserId();
//			isValid = otpService.validateOtp(otpIdentifier, otpRequest.getOtp(), otpType);
//			if (!isValid) {
//				throw new BadRequestException("Invalid or expired OTP.");
//			}
//
//			String accessToken = jwtUtil.generateTokenForStudent(student.getStudentId().toString(), student.getUserId(),
//					student.getDeviceId(), student.getRole(), TokenType.ACCESS_TOKEN, otpType, true);
//			String refreshToken = jwtUtil.generateRefreshToken(student.getUserId(), student.getRole(),
//					TokenType.REFRESH_TOKEN);
//
//			res.put("accessToken", accessToken);
//			res.put("refreshToken", refreshToken);
//			res.put("student", student);
//			tokenManagementService.save(new TokenManagement(null, accessToken));
//
//		} else if ("ADMIN".equalsIgnoreCase(role)) {
//			Admin admin = repo.findByAdminEmail(userId)
//					.orElseThrow(() -> new ResourceNotFoundException("Admin not found."));
//
//			isValid = otpService.validateOtp(userId, otpRequest.getOtp(), otpType);
//			if (!isValid) {
//				throw new BadRequestException("Invalid or expired OTP.");
//			}
//
//			String accessToken = jwtUtil.generateTokenForAdmin(admin.getAdminEmail(), TokenType.ACCESS_TOKEN, otpType);
//			String refreshToken = jwtUtil.generateRefreshToken(admin.getAdminEmail(), "ADMIN", TokenType.REFRESH_TOKEN);
//
//			res.put("accessToken", accessToken);
//			res.put("refreshToken", refreshToken);
//			res.put("admin", admin);
//			tokenManagementService.save(new TokenManagement(null, accessToken));
//
//		} else {
//			throw new UnauthorizeException("Invalid role in token.");
//		}
//
//		return ApiResponse.builder().success(true).message("OTP verified successfully").http(HttpStatus.OK).data(res)
//				.build();
//	}
	
	
	
	@Override
	public ApiResponse verifyOtp(OtpRequest otpRequest) {
	    String jwtToken = jwtUtil.getToken();
	    if (jwtToken == null) {
	        throw new UnauthorizeException("Missing token");
	    }
 
	    String userId = jwtUtil.getUsername(jwtToken);
	    String role = String.valueOf(jwtUtil.getHeader(jwtToken, "Role"));
	    String tokenTypeStr = String.valueOf(jwtUtil.getHeader(jwtToken, "tokenType"));
	    String otpTypeStr = String.valueOf(jwtUtil.getHeader(jwtToken, "otpType"));
 
	    TokenType tokenType = TokenType.valueOf(tokenTypeStr);
	    OtpType otpType = OtpType.valueOf(otpTypeStr);
 
	    if (tokenType != TokenType.AUTH_TOKEN || !(otpType == OtpType.LOGIN || otpType == OtpType.FORGET_PASSWORD)) {
	        throw new UnauthorizeException("Invalid token type or OTP type for verification.");
	    }
 
	    boolean isValid;
	    String otpIdentifier;
	    Map<String, Object> res = new HashMap<>();
 
	    if ("STUDENT".equalsIgnoreCase(role)) {
	        Student student = studRepo.findByUserId(userId);
	        if (student == null) {
	            throw new ResourceNotFoundException(AppConstants.STUDENT_NOT_EXISTS_MESSAGE);
	        }
 
	        otpIdentifier = (otpType == OtpType.FORGET_PASSWORD) ? student.getEmail() : student.getUserId();
	        isValid = otpService.validateOtp(otpIdentifier, otpRequest.getOtp(), otpType);
	        if (!isValid) {
	            throw new BadRequestException("Invalid or expired OTP.");
	        }
 
	        String token;
	        TokenType tokenToIssue = (otpType == OtpType.FORGET_PASSWORD) ? TokenType.AUTH_TOKEN : TokenType.ACCESS_TOKEN;
	        token = jwtUtil.generateTokenForStudent(
	            student.getStudentId().toString(), student.getUserId(), student.getDeviceId(), student.getRole(),
	            tokenToIssue, otpType, true
	        );
 
	        res.put(tokenToIssue == TokenType.AUTH_TOKEN ? "authToken" : "accessToken", token);
	        res.put("student", student);
	        tokenManagementService.save(new TokenManagement(null, token));
 
	    } else if ("ADMIN".equalsIgnoreCase(role)) {
	        Admin admin = repo.findByAdminEmail(userId)
	                .orElseThrow(() -> new ResourceNotFoundException("Admin not found."));
 
	        isValid = otpService.validateOtp(userId, otpRequest.getOtp(), otpType);
	        if (!isValid) {
	            throw new BadRequestException("Invalid or expired OTP.");
	        }
 
	        String token;
	        TokenType tokenToIssue = (otpType == OtpType.FORGET_PASSWORD) ? TokenType.AUTH_TOKEN : TokenType.ACCESS_TOKEN;
	        token = jwtUtil.generateTokenForAdmin(admin.getAdminEmail(), tokenToIssue, otpType,true);
 
	        res.put(tokenToIssue == TokenType.AUTH_TOKEN ? "authToken" : "accessToken", token);
	        res.put("admin", admin);
	        tokenManagementService.save(new TokenManagement(null, token));
 
	    } else {
	        throw new UnauthorizeException("Invalid role in token.");
	    }
 
	    return ApiResponse.builder()
	            .success(true)
	            .message("OTP verified successfully")
	            .http(HttpStatus.OK)
	            .data(res)
	            .build();
	}

	@Override
	public ApiResponse logout() {
		String token = jwtUtil.getToken();

		if (token == null) {
			throw new UnauthorizeException("Authorization token missing");
		}

		String username = jwtUtil.getUsername(token);
		QrManage findByUserId = qrManageRepository.findByUserId(username);
		if (findByUserId != null) {
			;
			qrService.jobEnd(findByUserId.getUuid(), "LOGOUT");
			qrManageRepository.delete(findByUserId);
			tokenManagementService.deleteToken(token);
		}
		tokenManagementService.deleteToken(token);

		return ApiResponse.builder().success(true).message("Logout successful").http(HttpStatus.OK).build();
	}

	@Override
	public ApiResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
		String refreshToken = refreshTokenRequest.getRefreshToken();
		String username = jwtUtil.getUsername(refreshToken);
		String role = (String) jwtUtil.getHeader(refreshToken, "Role");
		TokenType tokenType = TokenType.valueOf(jwtUtil.getHeader(refreshToken, "tokenType").toString());

		if (tokenType != TokenType.REFRESH_TOKEN) {
			throw new UnauthorizeException("Invalid token type. Must be REFRESH_TOKEN.");
		}

		if (!jwtUtil.validateToken(refreshToken, username)) {
			throw new UnauthorizeException("Invalid or expired refresh token.");
		}

		Map<String, Object> res = new HashMap<>();

		if ("STUDENT".equalsIgnoreCase(role)) {
			Student student = studentService.getStudentByUserId(username);
			if (student == null)
				new ResourceNotFoundException(AppConstants.STUDENT_NOT_EXISTS_MESSAGE);
			String newAccessToken = jwtUtil.generateTokenForStudent(student.getStudentId().toString(),
					student.getUserId(), student.getDeviceId(), student.getRole(), TokenType.ACCESS_TOKEN, null, true);
			res.put("accessToken", newAccessToken);
			tokenManagementService.save(new TokenManagement(null, newAccessToken));
		} else if ("ADMIN".equalsIgnoreCase(role)) {
			Admin admin = repo.findByAdminEmail(username)
					.orElseThrow(() -> new ResourceNotFoundException(AppConstants.ADMIN_NOT_FOUND));
			String newAccessToken = jwtUtil.generateTokenForAdmin(admin.getAdminEmail(), TokenType.ACCESS_TOKEN, null,true);
			res.put("accessToken", newAccessToken);
			tokenManagementService.save(new TokenManagement(null, newAccessToken));
		} else {
			throw new UnauthorizeException("Invalid role in token.");
		}

		return ApiResponse.builder().success(true).message(AppConstants.NEW_TOKEN_GENERATED).http(HttpStatus.OK)
				.data(res).build();
	}

}
