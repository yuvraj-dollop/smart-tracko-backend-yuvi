package com.cico.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cico.util.OtpType;
import com.cico.util.TokenType;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtil {
	@Value("${secret}")
	private String secret;

	@Autowired
	private HttpServletRequest httpServletRequest;

	// ======================= For APP ===================================
	private String generateToken(Map<String, Object> claims, String subject) {
		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1))) // 1 days
				.setIssuer("CICO").signWith(SignatureAlgorithm.HS256, secret).compact();
	}

	public String generateTokenForStudent(String studentId, String subject, String deviceId, String role) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("Role", role);
		claims.put("StudentId", studentId);
		claims.put("deviceId", deviceId);
		claims.put("platform", "mobile"); // platform claim for app
		return generateToken(claims, subject);
	}

	public String generateTokenForAdmin(String adminId) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("Role", "ADMIN");
		claims.put("adminId", adminId);
		claims.put("platform", "mobile"); // platform claim for app
		return generateToken(claims, adminId);
	}

	// ======================= For WEB ===================================
	private String generateToken(Map<String, Object> claims, String subject, TokenType tokenType) {
		long expirationMillis;

		if (tokenType == TokenType.AUTH_TOKEN) {
			expirationMillis = TimeUnit.MINUTES.toMillis(10); // 10 minutes
		} else {
			expirationMillis = TimeUnit.DAYS.toMillis(1); // 1 day
		}

		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + expirationMillis)).setIssuer("CICO")
				.signWith(SignatureAlgorithm.HS256, secret).compact();
	}

	public String generateTokenForStudent(String studentId, String subject, String deviceId, String role,
			TokenType tokenType, OtpType otpType, Boolean isOtpVerified) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("Role", role);
		claims.put("StudentId", studentId);
		claims.put("deviceId", deviceId);
		claims.put("tokenType", tokenType);
		claims.put("otpType", otpType);
		claims.put("platform", "web"); // platform claim for web
		claims.put("isOtpVerified", isOtpVerified);
		return generateToken(claims, subject, tokenType);
	}

	public String generateTokenForAdmin(String adminId, TokenType tokenType, OtpType otpType, Boolean isOtpVerified) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("Role", "ADMIN");
		claims.put("adminId", adminId);
		claims.put("tokenType", tokenType);
		claims.put("otpType", otpType);
		claims.put("platform", "web"); // platform claim for web
		claims.put("isOtpVerified", isOtpVerified);
		return generateToken(claims, adminId, tokenType);
	}

	// ==================== Utility ====================
	public String getToken() {
		String header = httpServletRequest.getHeader("Authorization");
		if (header == null || !header.startsWith("Bearer ")) {
			return null;
		}
		return header.substring(7);
	}

	public Claims getClaims(String token) {

		if (token.startsWith("Bearer "))
			token = token.substring(7);
		return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();

	}

	public String getUsername(String token) {
		return getClaims(token).getSubject();
	}

	public Object getHeader(String token, String key) {
		return getClaims(token).get(key);
	}

	public Boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	public Date extractExpiration(String token) {
		return getClaims(token).getExpiration();
	}

	public Boolean validateToken(String token, String userId) {
		final String username = getUsername(token);
		return (username.equals(userId) && !isTokenExpired(token));
	}

	public String getRole(String token) {
		return getClaims(token).get("Role").toString();
	}
}
