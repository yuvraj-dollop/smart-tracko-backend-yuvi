package com.cico.payload;

import javax.validation.constraints.Min;
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
public class UpdateFeesPayRequest {

	@NotNull(message = AppConstants.PAY_ID_REQUIRED)
	private Integer payId;

	@NotNull(message = AppConstants.FEES_PAY_AMOUNT_REQUIRED)
//	@Min(value = 1, message = AppConstants.FEES_PAY_AMOUNT_MIN)
	private Double feesPayAmount;

	private String payDate;
}
