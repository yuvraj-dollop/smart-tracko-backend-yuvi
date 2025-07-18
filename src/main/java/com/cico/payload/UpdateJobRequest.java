package com.cico.payload;

import javax.validation.constraints.NotNull;

import com.cico.util.AppConstants;
import com.cico.util.JobType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateJobRequest {

    @NotNull(message = AppConstants.JOB_ID_REQUIRED)
    private Integer jobId;

    private String jobTitle;

    private String jobDescription;

    private String companyName;

    private String experienceRequired;

    private String technicalSkills;

    private String jobPackage;

    private JobType type; // Optional: convert to enum in service

    private Integer technologyStackId;

    // Getters and setters
}
