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
import com.cico.kafkaServices.KafkaProducerService;
import com.cico.model.Admin;
import com.cico.model.Student;
import com.cico.model.TokenManagement;
import com.cico.payload.AdminLoginRequest;
import com.cico.payload.ApiResponse;
import com.cico.payload.ForgetPasswordRequest;
import com.cico.payload.OtpRequest;
import com.cico.payload.ResetPasswordRequest;
import com.cico.payload.StudentLoginRequest;
import com.cico.repository.AdminRepository;
import com.cico.repository.StudentRepository;
import com.cico.security.JwtUtil;
import com.cico.service.IAuthService;
import com.cico.service.IOtpService;
import com.cico.service.ITokenManagementService;
import com.cico.util.AppConstants;
import com.cico.util.EmailService;
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
	private KafkaProducerService kafkaProducerService;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private IOtpService otpService;
	@Autowired
	private ITokenManagementService tokenManagementService;

	@Autowired
	private EmailService emailService;

	@Override
	public ApiResponse studentLogin(StudentLoginRequest loginRequest) {
		String userIdOrEmail = loginRequest.getUserIdOrEmail();
		String password = loginRequest.getPassword();

		Optional<Student> optionalStudent = studRepo.findByUserIdOrEmailAndAndIsActive(userIdOrEmail, userIdOrEmail,
				true);

		if (optionalStudent.isEmpty()) {
			throw new ResourceNotFoundException(AppConstants.USER_NOT_FOUND);
		}

		Student student = optionalStudent.get();

		if (!encoder.matches(password, student.getPassword())) {
			throw new UnauthorizeException(AppConstants.INVALID_CREDENTIALS);
		}

		String token = jwtUtil.generateTokenForStudent(student.getStudentId().toString(), student.getUserId(),
				student.getDeviceId(), student.getRole(), TokenType.AUTH_TOKEN, OtpType.LOGIN, false);

		// Generate OTP
		String otp = otpService.generateOtp(student.getUserId(), OtpType.LOGIN);

		// Send OTP email using template

//				Map<String, String> placeholders = new HashMap<>();
//				placeholders.put("userName", student.getFullName());
//				placeholders.put("otp", otp);
//		        emailService.sendEmail(student.getEmail(), TemplateType.OTP_LOGIN, placeholders);

		Map<String, Object> res = new HashMap<>();
		res.put("authToken", token);
		res.put("otp", otp);

		return ApiResponse.builder().success(true).message(AppConstants.OTP_SENT_FOR_LOGIN).http(HttpStatus.OK)
				.data(res).build();
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
				TokenType.AUTH_TOKEN, OtpType.LOGIN, false);

		// Generate OTP
		String otp = otpService.generateOtp(admin.getAdminEmail(), OtpType.LOGIN);

		// Send OTP email using template

//		Map<String, String> placeholders = new HashMap<>();
//		placeholders.put("userName", admin.getAdminName());
//		placeholders.put("otp", otp);
//		
//		emailService.sendEmail(admin.getAdminEmail(), TemplateType.OTP_LOGIN, placeholders);

		// =======================kafka=======================//
//		EmailRequest emailPayload = new EmailRequest();
//		emailPayload.setToEmail(admin.getAdminEmail());
//		emailPayload.setTemplateType(TemplateType.OTP_LOGIN);
//		emailPayload.setPlaceholders(Map.of(
//		    "userName", admin.getAdminName(),
//		    "otp", otp
//		));
//		try {
//			kafkaProducerService.sendEmailEvent(emailPayload);
//		} catch (JsonProcessingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		Map<String, Object> data = new HashMap<>();
		data.put("authToken", authToken);
		data.put("otp", otp);

		return ApiResponse.builder().success(true).message(AppConstants.OTP_SENT).http(HttpStatus.OK).data(data)
				.build();
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
			token = jwtUtil.generateTokenForAdmin(email, TokenType.AUTH_TOKEN, OtpType.FORGET_PASSWORD, false);
		} else
			throw new ResourceNotFoundException(AppConstants.USER_NOT_FOUND);

		otp = otpService.generateOtp(email, OtpType.FORGET_PASSWORD);

		Map<String, Object> res = new HashMap<>();
		res.put("authToken", token);
		res.put("otp", otp);

		return ApiResponse.builder().success(true).message(AppConstants.OTP_SENT_FOR_RESET_PASSWORD).http(HttpStatus.OK)
				.data(res).build();
	}

	@Override
	public ApiResponse resetPassword(ResetPasswordRequest request) {
		String token = jwtUtil.getToken();
		String email = jwtUtil.getUsername(token);

		TokenType tokenType = TokenType.valueOf(jwtUtil.getHeader(token, "tokenType").toString());
		OtpType otpType = OtpType.valueOf(jwtUtil.getHeader(token, "otpType").toString());

		if (tokenType != TokenType.AUTH_TOKEN || otpType != OtpType.FORGET_PASSWORD) {
			throw new UnauthorizeException(AppConstants.INVALID_TOKEN_TYPE_FOR_RESET_PASSWORD);
		}
		Boolean isOtpVerified = (Boolean) jwtUtil.getHeader(token, "isOtpVerified");

		if (Boolean.FALSE.equals(isOtpVerified)) {
			throw new UnauthorizeException(AppConstants.OTP_NOT_VERIFIED);
		}

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
			throw new ResourceNotFoundException(AppConstants.USER_NOT_FOUND);
		}

		return ApiResponse.builder().success(true).message(AppConstants.PASSWORD_RESET_SUCCESSFULLY).http(HttpStatus.OK)
				.build();
	}

	@Override
	public ApiResponse resendOtp() {
		String oldToken = jwtUtil.getToken();
		String emailOrUserId = jwtUtil.getUsername(oldToken);

		TokenType tokenType = TokenType.valueOf(jwtUtil.getHeader(oldToken, "tokenType").toString());
		OtpType otpType = OtpType.valueOf(jwtUtil.getHeader(oldToken, "otpType").toString());

		String role = String.valueOf(jwtUtil.getHeader(oldToken, "Role"));

		if (tokenType != TokenType.AUTH_TOKEN) {
			throw new UnauthorizeException(AppConstants.AUTH_TOKEN_REQUIRED);
		}

		if (otpType != OtpType.LOGIN && otpType != OtpType.FORGET_PASSWORD) {
			throw new BadRequestException(AppConstants.RESEND_OTP_NOT_ALLOWED_FOR_THIS_TYPE);
		}

		String newOtp = otpService.generateOtp(emailOrUserId, otpType);
		String newToken;

		if ("STUDENT".equalsIgnoreCase(role)) {
			Student student = studRepo.findByUserId(emailOrUserId);
			if (student == null) {
				throw new ResourceNotFoundException(AppConstants.STUDENT_NOT_EXISTS_MESSAGE);
			}

			newToken = jwtUtil.generateTokenForStudent(student.getStudentId().toString(), student.getUserId(),
					student.getDeviceId(), student.getRole(), TokenType.AUTH_TOKEN, otpType, false // Not yet verified
			);
		} else if ("ADMIN".equalsIgnoreCase(role)) {
			Admin admin = repo.findByAdminEmail(emailOrUserId)
					.orElseThrow(() -> new ResourceNotFoundException(AppConstants.ADMIN_NOT_FOUND));

			newToken = jwtUtil.generateTokenForAdmin(admin.getAdminEmail(), TokenType.AUTH_TOKEN, otpType, false);
		} else {
			throw new UnauthorizeException(AppConstants.INVALID_ROLE_TYPE);
		}

		Map<String, Object> data = new HashMap<>();
		data.put("authToken", newToken);
		data.put("otp", newOtp); // Optional: only show in dev

		return ApiResponse.builder().success(true).message(AppConstants.OTP_SENT).http(HttpStatus.OK).data(data)
				.build();
	}

	@Override
	public ApiResponse verifyOtp(OtpRequest otpRequest) {
		String jwtToken = jwtUtil.getToken();
		if (jwtToken == null) {
			throw new UnauthorizeException(AppConstants.MISSING_TOKEN);
		}

		if (jwtUtil.isTokenExpired(jwtToken)) {
			throw new UnauthorizeException("Token expired");
		}

		String userId = jwtUtil.getUsername(jwtToken);
		String role = String.valueOf(jwtUtil.getHeader(jwtToken, "Role"));
		String tokenTypeStr = String.valueOf(jwtUtil.getHeader(jwtToken, "tokenType"));
		String otpTypeStr = String.valueOf(jwtUtil.getHeader(jwtToken, "otpType"));

		TokenType tokenType = TokenType.valueOf(tokenTypeStr);
		OtpType otpType = OtpType.valueOf(otpTypeStr);

		if (tokenType != TokenType.AUTH_TOKEN || !(otpType == OtpType.LOGIN || otpType == OtpType.FORGET_PASSWORD)) {
			throw new UnauthorizeException(AppConstants.INVALID_TOKEN_TYPE_FOR_VERIFICATION);
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
				throw new BadRequestException(AppConstants.INVALID_OR_EXPIRED_OTP);
			}

			String token;
			TokenType tokenToIssue = (otpType == OtpType.FORGET_PASSWORD) ? TokenType.AUTH_TOKEN
					: TokenType.ACCESS_TOKEN;
			token = jwtUtil.generateTokenForStudent(student.getStudentId().toString(), student.getUserId(),
					student.getDeviceId(), student.getRole(), tokenToIssue, otpType, true);

			res.put(tokenToIssue == TokenType.AUTH_TOKEN ? "authToken" : "accessToken", token);
			res.put("student", student);
			tokenManagementService.save(new TokenManagement(null, token));

		} else if ("ADMIN".equalsIgnoreCase(role)) {
			Admin admin = repo.findByAdminEmail(userId)
					.orElseThrow(() -> new ResourceNotFoundException(AppConstants.ADMIN_NOT_FOUND));

			isValid = otpService.validateOtp(userId, otpRequest.getOtp(), otpType);
			if (!isValid) {
				throw new BadRequestException(AppConstants.INVALID_OR_EXPIRED_OTP);
			}

			String token;
			TokenType tokenToIssue = (otpType == OtpType.FORGET_PASSWORD) ? TokenType.AUTH_TOKEN
					: TokenType.ACCESS_TOKEN;
			token = jwtUtil.generateTokenForAdmin(admin.getAdminEmail(), tokenToIssue, otpType, true);

			res.put(tokenToIssue == TokenType.AUTH_TOKEN ? "authToken" : "accessToken", token);
			res.put("admin", admin);
			tokenManagementService.save(new TokenManagement(null, token));

		} else {
			throw new UnauthorizeException(AppConstants.INVALID_ROLE_TYPE);
		}

		return ApiResponse.builder().success(true).message(AppConstants.OTP_VERIFIED_SUCCESSFULLY).http(HttpStatus.OK)
				.data(res).build();
	}

	@Override
	public ApiResponse logout() {
//		String token = jwtUtil.getToken();
//
//		if (token == null) {
//			throw new UnauthorizeException(AppConstants.MISSING_TOKEN);
//		}
//
//		//tokenManagementService.deleteToken(tokenManagementService.getTokenByToken(token));
//
//		return ApiResponse.builder().success(true).message(AppConstants.LOGOUT_SUCCESSFULL).http(HttpStatus.OK).build();
   return null;
	}

}
