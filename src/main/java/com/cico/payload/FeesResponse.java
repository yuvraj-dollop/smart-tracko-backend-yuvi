package com.cico.payload;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeesResponse {

	private Integer feesId;
	private Integer studentId;
	private String fullName;
	private String mobile;
	private LocalDate dob;
	private String email;
	private String college;
	private String profilePic;
	private String currentCourse;

	private Integer courseId;
	private String courseName;
	private String courseFees;
	private Double finalFees;
	private Double feesPaid;
	private Double remainingFees;
	private LocalDate date;
	private LocalDate createdDate;
	private LocalDate updatedDate;
	@Builder.Default
	private Boolean isCompleted = false;
}
