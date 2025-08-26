package com.cico.payload;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewsEventsResponse {
	private String shortDescription;
	private String briefDescription;
	private String image;
	private String title;
	private LocalDateTime createdDate;
	private Boolean isActive;
	private Boolean isDeleted;
}
