package com.cico.model;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class FeesRequest {

    @NotNull(message = "Student ID is required")
    private Integer studentId;

    @NotNull(message = "Course ID is required")
    private Integer courseId;

    @NotNull(message = "Final fees is required")
    private Double finalFees;

    @NotNull(message = "Date is required")
    private String date;
}
