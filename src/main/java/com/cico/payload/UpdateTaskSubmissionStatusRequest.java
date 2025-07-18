package com.cico.payload;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.cico.util.AppConstants;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTaskSubmissionStatusRequest {

    @NotNull(message = AppConstants.SUBMISSION_ID_REQUIRED)
    private Long submissionId;

    @NotBlank(message = AppConstants.SUBMISSION_STATUS_REQUIRED)
    private String status;

    @NotBlank(message = AppConstants.REVIEW_REQUIRED)
    private String review;
}
