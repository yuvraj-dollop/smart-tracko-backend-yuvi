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

import com.cico.service.ITokenManagementService;
import com.cico.service.impl.CustomUserDetailsServiceImpl;
import com.cico.util.AppConstants;

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
		System.out.println("Request URL :: " + request.getRequestURL());
		
//		String clientSource = request.getHeader("X-Client-Source");
//		System.err.println("==========client or device type " + clientSource);
//		
//		request.getHeaderNames().asIterator().forEachRemaining(
//			    header -> System.out.println("Header: " + header + " => " + request.getHeader(header))
//			);
//		System.out.println("Request URL :: " + request.getRequestURL());
		
		if (token != null) {
			System.err.println(",,,,,,,,,,,,,,,,");

			if (token.startsWith("Bearer ")) {
				token = token.substring(7);
			}
			String uri = request.getRequestURI();
			 
			boolean isOtpFlow = uri.equals("/api/auth/v2/refresh-token") || uri.equals("/api/auth/v2/verify-otp")
					|| uri.equals("/api/auth/v2/resend-otp") || uri.equals("/api/auth/v2/reset-password");

			if (isOtpFlow||tokenManagementService.existsByToken(token)) {
				
//				String tokenPlatform = String.valueOf(util.getClaims(token).get("platform"));
//				if (tokenPlatform == null || clientSource == null || !tokenPlatform.equalsIgnoreCase(clientSource)) {
//					response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
//							"Token platform mismatch: token issued for '" + tokenPlatform + "', but used from '" + clientSource + "'");
//					return;
//				}

				String username = util.getUsername(token);
				if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
					UserDetails user = detailsServiceImpl.DataLoadByUsername(username, util.getRole(token));
					UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
							username, user.getPassword(), user.getAuthorities());
					authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authenticationToken);
				}
			} else {
				// ❌ Invalid token — respond with 401 Unauthorized
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token.");
				return;
			}
		}

		filterChain.doFilter(request, response);
	}
}
