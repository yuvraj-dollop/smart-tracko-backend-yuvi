package com.cico.payload;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentFilterResponse {
	private Long id;
	private String title;
	private List<AssignmentTaskFilterReponse> taskQuestion;
	private Integer totalTaskCompleted;
	private Boolean status;
	private Boolean isLocked;
}
