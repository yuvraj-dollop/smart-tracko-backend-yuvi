package com.cico.payload;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.cico.util.AppConstants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LeaveRequest {

	@NotNull(message = AppConstants.LEAVE_TYPE_REQUIRED)
	private Integer leaveTypeId;

	@NotBlank(message = AppConstants.LEAVE_START_DATE_REQUIRED)
	private String leaveStartDate;

	@NotBlank(message = AppConstants.LEAVE_END_DATE_REQUIRED)
	private String leaveEndDate;

	@NotBlank(message = AppConstants.LEAVE_REASON_REQUIRED)
	private String leaveReason;

	@NotBlank(message = AppConstants.LEAVE_DAY_TYPE_REQUIRED)
	private String leaveDayType;

	private String halfDayType;
}
