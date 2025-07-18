package com.cico.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PayFeesRequest {
	
	@NotNull(message = "Fees ID is required")
    private Integer feesId;

    @NotNull(message = "Pay amount is required")
    private Double feesPayAmount;

    @NotBlank(message = "Pay date is required")
    private String payDate;

    @NotBlank(message = "Receipt number is required")
    private String recieptNo;

    @NotBlank(message = "Description is required")
    private String description;

}
