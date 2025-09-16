package com.cico.payload;

import java.time.LocalDate;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.cico.model.Course;
import com.cico.util.AppConstants;

import lombok.Data;

@Data
public class StudentRequest {

	@NotBlank(message = AppConstants.FULL_NAME_REQUIRED)
	private String fullName;

	@NotNull(message = AppConstants.DOB_REQUIRED)
	private LocalDate dob;

	@NotBlank(message = AppConstants.FATHERS_NAME_REQUIRED)
	private String fathersName;

	private String fathersOccupation;

	@NotBlank(message = AppConstants.CONTACT_FATHER_REQUIRED)
	@Pattern(regexp = "^[6-9]\\d{9}$", message = AppConstants.CONTACT_FATHER_INVALID)
	private String contactFather;

	private String mothersName;

	@Pattern(regexp = "^[6-9]\\d{9}$", message = AppConstants.CONTACT_MOTHER_INVALID)
	private String contactMother;

	@NotBlank(message = AppConstants.MOBILE_REQUIRED)
	@Pattern(regexp = "^[6-9]\\d{9}$", message = AppConstants.MOBILE_INVALID)
	private String mobile;

	private String languageKnown;

	@NotBlank(message = AppConstants.EMAIL_REQUIRED)
	@Email(message = AppConstants.EMAIL_INVALID)
	private String email;

	@NotNull(message = AppConstants.COURSE_REQUIRED)
	private Course course;

	@NotNull(message = AppConstants.JOIN_DATE_REQUIRED)
	private LocalDate joinDate;

	private String currentCourse;
	private String currentSem;

	@NotBlank(message = AppConstants.COLLEGE_REQUIRED)
	private String college;

	private String localAddress;

	@NotBlank(message = AppConstants.PERMANENT_ADDRESS_REQUIRED)
	private String parmanentAddress;
}