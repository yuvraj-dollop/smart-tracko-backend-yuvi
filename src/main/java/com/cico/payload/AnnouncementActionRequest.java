package com.cico.payload;

import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.cico.util.AppConstants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementActionRequest {

	@NotNull(message = AppConstants.STUDENT_ID_REQUIRED)
	@Min(value = 1, message = AppConstants.STUDENT_ID_POSITIVE)
	private Integer studentId;

	@NotEmpty(message = AppConstants.ANNOUNCEMENT_IDS_REQUIRED)
	private List<@NotNull(message = AppConstants.ANNOUNCEMENT_ID_NOT_NULL) Long> announcementIds;
}
