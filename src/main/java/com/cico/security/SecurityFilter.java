package com.cico.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cico.exception.ExpiredJwtException;
import com.cico.exception.UnauthorizeException;
import com.cico.service.ITokenManagementService;
import com.cico.service.impl.CustomUserDetailsServiceImpl;
import com.cico.util.AppConstants;
import com.cico.util.TokenType;

@Component
public class SecurityFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtil util;

	@Autowired
	private CustomUserDetailsServiceImpl detailsServiceImpl;

	@Autowired
	private ITokenManagementService tokenManagementService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String token = request.getHeader(AppConstants.AUTHORIZATION);
		String uri = request.getRequestURI();
		System.out.println("Request URL :: " + uri);

		if (token != null) {
			if (token.startsWith("Bearer ")) {
				token = token.substring(7);
			}
			
//			if (util.isTokenExpired(token)) {
//				System.err.println("............. TOKEN EXPIRED ................");
//				tokenManagementService.deleteToken(token);
//				throw new ExpiredJwtException("TOKEN EXPIRED");
//			}
			
//			String tokenType = (String) util.getHeader(token, "tokenType");
//			if(tokenType.equals(TokenType.REFRESH_TOKEN.name()) && !uri.equals("api/auth/v2/refresh-token"))
//			{
//				System.err.println("............. INVALID TOKEN YOU CAN'T CALL API VIA REFRESH TOKEN ..........");
////				throw new UnauthorizeException("Invalid Token.");
//				throw new UnauthorizeException("Invalid Token.");
//			}

			boolean isOtpFlow = uri.equals("/api/auth/v2/refresh-token") || uri.equals("/api/auth/v2/verify-otp")
					|| uri.equals("/api/auth/v2/resend-otp") || uri.equals("/api/auth/v2/reset-password");

//			if (isOtpFlow || tokenManagementService.existsByToken(token)) {

			String username = util.getUsername(token);
			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				UserDetails user = detailsServiceImpl.DataLoadByUsername(username, util.getRole(token));
				UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
						username, user.getPassword(), user.getAuthorities());
				authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authenticationToken);
			}
//			}
//		else {
//				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token.");
//				return;
//			}
		}

		filterChain.doFilter(request, response);
	}
}
