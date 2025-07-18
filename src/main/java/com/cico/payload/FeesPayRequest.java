package com.cico.payload;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.cico.util.AppConstants;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeesPayRequest {

	@NotNull(message = AppConstants.FEES_ID_REQUIRED)
	private Integer feesId;

	@NotNull(message = AppConstants.FEES_AMOUNT_REQUIRED)
	@Positive(message = AppConstants.FEES_AMOUNT_POSITIVE)
	private Double feesPayAmount;

	@NotBlank(message = AppConstants.PAY_DATE_REQUIRED)
	private String payDate;

	@NotBlank(message = AppConstants.RECEIPT_NO_REQUIRED)
	private String recieptNo;

	@NotBlank(message = AppConstants.DESCRIPTION_REQUIRED)
	private String description;
}
