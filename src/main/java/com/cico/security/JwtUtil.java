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
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtil {
	@Value("${secret}")
	private String secret;

	@Autowired
	private HttpServletRequest httpServletRequest;

	private String generateToken(Map<String, Object> claims, String subject) {

		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(
						new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(4 * 24 * 60 * 60 * 1000)))
				.setIssuer("CICO").signWith(SignatureAlgorithm.HS256, secret).compact();
//		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
//				.setExpiration(
//						new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1)))
//				.setIssuer("CICO").signWith(SignatureAlgorithm.HS256, secret).compact();
	}

	public String generateTokenForStudent(String studentId, String subject, String deviceId, String role) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("Role", role);
		claims.put("StudentId", studentId);
		claims.put("deviceId", deviceId);
		claims.put("tokenType", TokenType.ACCESS_TOKEN);
		return generateToken(claims, subject);
	}

	public String generateTokenForAdmin(String adminId) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("Role", "ADMIN");
		claims.put("adminId", adminId);
		return generateToken(claims, adminId);
	}

	// ===================for web
	private String generateToken(Map<String, Object> claims, String subject, TokenType tokenType) {
		long expirationMillis;

		if (tokenType == TokenType.AUTH_TOKEN) {
			expirationMillis = TimeUnit.MINUTES.toMillis(5); // 05 minutes
		} else if (tokenType == TokenType.ACCESS_TOKEN) {
//			expirationMillis = TimeUnit.MINUTES.toMillis(3); // 3 minutes
			expirationMillis = TimeUnit.DAYS.toMillis(1); // 1 days
		} else {
//			expirationMillis = TimeUnit.MINUTES.toMillis(10); // 10 minutes
			expirationMillis = TimeUnit.DAYS.toMillis(7); // 7 days
		}

		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + expirationMillis)).setIssuer("CICO")
				.signWith(SignatureAlgorithm.HS256, secret).compact();
	}

	// ===================for web
	public String generateTokenForStudent(String studentId, String subject, String deviceId, String role,
			TokenType tokenType, OtpType optType, Boolean isOtpVerified) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("Role", role);
		claims.put("StudentId", studentId);
		claims.put("deviceId", deviceId);
		claims.put("tokenType", tokenType);
		claims.put("otpType", optType);
		claims.put("isOtpVerified", isOtpVerified);
		return generateToken(claims, subject, tokenType);
	}

	// ===================for web
	public String generateTokenForAdmin(String adminId, TokenType tokenType, OtpType optType,Boolean isOtpVerified) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("Role", "ADMIN");
		claims.put("adminId", adminId);
		claims.put("tokenType", tokenType);
		claims.put("otpType", optType);
		claims.put("isOtpVerified", isOtpVerified);
		return generateToken(claims, adminId, tokenType);
	}

	// ================== For Web
	public String generateRefreshToken(String subject, String role, TokenType tokenType) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("Role", role);
		claims.put("tokenType", tokenType);
		return generateToken(claims, subject, tokenType);
	}

	public String getToken() {
		String header = httpServletRequest.getHeader("Authorization");
		System.out.println("header" + header);
		if (header == null || !header.startsWith("Bearer ")) {
			System.err.println("Exception not handled");
//			throw new ExpiredJwtException("Authorization header is missing or invalid");
		}
		return header.substring(7);
	}

	public Claims getClaims(String token) {
		System.err.println("token in util " + token);
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

//	public Boolean isTokenExpired(String token) {
//		return extractExpiration(token).before(new Date());
//	}
	public boolean isTokenExpired(String token) {
		try {
			return extractExpiration(token).before(new Date());
		} catch (ExpiredJwtException e) {
			return true; // Token is expired
		} catch (Exception e) {
			// Optional: you can return true for invalid tokens or handle separately
			return true;
		}
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
