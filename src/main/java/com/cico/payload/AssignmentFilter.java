package com.cico.payload;

import lombok.Data;

@Data
public class AssignmentFilter {
	private Integer courseId;
	private Integer subjectId;
	private PaginationRequest pageRequest;
	private Long assignmentId;
}
