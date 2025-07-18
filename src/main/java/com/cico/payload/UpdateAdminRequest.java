package com.cico.payload;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class UpdateAdminRequest {
	@NotNull(message = "Admin ID is required")
	private Integer adminId;

	@NotBlank(message = "Admin name is required")
	private String adminName;

	@NotBlank(message = "Admin email is required")
	@Email(message = "Invalid email format")
	private String adminEmail;

	@NotNull(message = "File is required")
	private MultipartFile file;
}
