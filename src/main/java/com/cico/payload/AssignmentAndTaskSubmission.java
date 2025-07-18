package com.cico.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(value = Include.NON_NULL)
@NoArgsConstructor

public class AssignmentAndTaskSubmission {

	private Long assignmentId;
	private String taskTitle;
	private Long taskId;
	private Long totalSubmitted;
	private Long unReviewed;
	private boolean status;
	private Long reviewed ;
	private Long taskCount;
	private String assignmentTitle;
	private String description;
	private Long taskNumber;
	private Long questionId;
	private Boolean isActive;

	public AssignmentAndTaskSubmission(Long questionId, String questionPreview, Long totalSubmissions,
			Long unreviewedCount, Long reviewedCount,Boolean isActive) {
		super();
		this.taskId	 = questionId;
		this.taskTitle = questionPreview;
		this.totalSubmitted = totalSubmissions;
		this.unReviewed = unreviewedCount;
		this.reviewed = reviewedCount;
		this.status = isActive;
	}

	public AssignmentAndTaskSubmission(Long taskId, Long totalSubmitted, Long unReveiwed, Long reveiwed,
			String taskTitle, Boolean status) {
		super();
		this.taskId = taskId;
		this.totalSubmitted = totalSubmitted;
		this.unReviewed = unReveiwed;
		this.reviewed = reveiwed;
		this.taskTitle = taskTitle;
		this.status = status;
	}

	public AssignmentAndTaskSubmission(Long taskId, Long totalSubmitted, Long unReveiwed, Long reveiwed, Long taskCount,
			String taskTitle, Long assignmentId, String assignmentTitle, Boolean status, Long taskNumber) {
		super();
		this.taskId = taskId;
		this.totalSubmitted = totalSubmitted;
		this.unReviewed = unReveiwed;
		this.reviewed  = reveiwed;
		this.taskTitle = taskTitle;
		this.taskCount = taskCount;
		this.assignmentTitle = assignmentTitle;
		this.assignmentId = assignmentId;
		this.status = status;
		this.taskNumber = taskNumber;
	}

	
	// here task is  questionID
	public AssignmentAndTaskSubmission(String taskTitle, Long taskId, Long totalSubmitted, Long unReveiwed,
			Long reveiwed,Boolean isActive) {
		super();
		this.taskTitle = taskTitle;
		this.questionId = taskId;
		this.totalSubmitted = totalSubmitted;
		this.unReviewed = unReveiwed;
		this.reviewed = reveiwed;
		this.isActive = isActive;
	}
	public AssignmentAndTaskSubmission(String taskTitle, Long taskId, Long totalSubmitted, Long unReveiwed,
			Long reveiwed) {
		super();
		this.taskTitle = taskTitle;
		this.questionId = taskId;
		this.totalSubmitted = totalSubmitted;
		this.unReviewed = unReveiwed;
		this.reviewed = reveiwed;
		
	}

}
