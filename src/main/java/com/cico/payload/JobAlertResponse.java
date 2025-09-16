package com.cico.payload;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.cico.util.JobType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class JobAlertResponse implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

//	private Integer jobId;
//	private String type;
//	private String  jobTitle;
//	private String jobDescription;
//	private String companyName;
//	private String experienceRequired;
//	private String technicalSkills;
//	private TechnologyStack technologyStack;
//	private String jobPackage;
	
	private Integer jobId;
	private String jobTitle;
	private String jobDescription;
	private String companyName;
	private String experienceRequired;
	private String technicalSkills;
	private String jobPackage;
	private Boolean isActive;
	private String technologyStackName;
	private JobType type;
	private LocalDateTime createdDate;
	private LocalDateTime updatedDate;

}