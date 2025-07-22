package com.cico.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**").allowedOrigins("*");
		registry.addMapping("/**").allowedOrigins("*") // or specify your frontend URL
				.allowedMethods("*").allowedHeaders("*").exposedHeaders("X-Server-Time");
	}

}