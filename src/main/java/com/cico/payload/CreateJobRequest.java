package com.cico.payload;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.cico.util.AppConstants;
import com.cico.util.JobType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateJobRequest {

    @NotBlank(message = AppConstants.JOB_TITLE_REQUIRED)
    private String jobTitle;

    @NotBlank(message =AppConstants.JOB_DESCRIPTION_REQUIRED)
    private String jobDescription;

    @NotBlank(message = AppConstants.COMPANY_NAME_REQUIRED)
    private String companyName;

    @NotBlank(message = AppConstants.EXPERIENCE_REQUIRED_MANDATORY)
    private String experienceRequired;

    @NotBlank(message = AppConstants.TECHNICAL_SKILL_REQUIRED)
    private String technicalSkills;

    @NotBlank(message =AppConstants.JOB_PACKAGE_REQUIRED)
    private String jobPackage;

    @NotNull(message = AppConstants.JOB_TYPE_REQUIRED)
    private JobType type;

    @NotNull(message = AppConstants.TECHNOLOGY_STACK_REQUIRED)
    private Integer technologyStackId;

}
