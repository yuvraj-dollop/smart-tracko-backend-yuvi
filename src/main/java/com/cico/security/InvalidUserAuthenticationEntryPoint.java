package com.cico.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

@Component
public class InvalidUserAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		Exception exception = (Exception) request.getAttribute("exception");
		System.err.println("EXCEPTION ===> " + exception.getMessage());
		String message = "Authentication Failed";
		if (exception instanceof SignatureException) {
			message = "Authentication Invalid Token SignatureFailed";
		} else if (exception instanceof ExpiredJwtException) {
			message = "Token Expired";
		} else if (exception instanceof MalformedJwtException) {
			message = "Malformed JWT token.";
		} else if (exception instanceof UnsupportedJwtException) {
			message = "Unsupported JWT token.";
		} else {
			message = "Authentication Failed";
		}

		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
	}

}
