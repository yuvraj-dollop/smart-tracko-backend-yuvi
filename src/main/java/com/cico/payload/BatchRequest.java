package com.cico.payload;

import java.time.LocalDate;
import java.time.LocalTime;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.cico.util.AppConstants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchRequest {

    @NotNull(message = AppConstants.COURSE_ID_REQUIRED)
    private Integer courseId;

    @NotBlank(message = AppConstants.BATCH_NAME_REQUIRED)
//    @Size(min = 3, max = 100, message = "Batch name must be between 3 and 100 characters")
    private String batchName;

    @NotNull(message = AppConstants.BATCH_START_DATE_REQUIRED)
    @FutureOrPresent(message = AppConstants.FUTURE_START_DATE)
    private LocalDate batchStartDate;

    @NotNull(message = AppConstants.BATCH_TIMING_REQUIRED)
    private LocalTime batchTiming;

    @NotBlank(message =AppConstants.BATCH_DETAIL_REQUIRED)
    @Size(max = 500, message = AppConstants.BATCH_DETAIL_MAX_SIZE)
    private String batchDetails;

    @NotNull(message = AppConstants.SUBJECT_ID_REQUIRED)
    private Integer subjectId;
}
