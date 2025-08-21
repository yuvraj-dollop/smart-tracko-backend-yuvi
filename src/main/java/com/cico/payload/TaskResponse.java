package com.cico.payload;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(value = Include.NON_NULL)
public class TaskResponse {
	private Long taskId;
	private String taskName;
	private CourseResponse course;
	private SubjectResponse subject;
	private List<TaskQuestionResponse> taskQuestion;
	private String taskAttachment;
	private String attachmentStatus;
	private LocalDateTime createdDate;
	private Boolean isCompleted;
	private Long totalTask;
	private Boolean isActive;
	private LocalDateTime dateTime;

	public TaskResponse(Long taskId, String taskName, Boolean isCompleted) {
		super();
		this.taskId = taskId;
		this.taskName = taskName;
		this.isCompleted = isCompleted;
	}

	public TaskResponse(Long taskId, String taskName, Long totalTask, Boolean isActive) {
		super();
		this.taskId = taskId;
		this.taskName = taskName;
		this.totalTask = totalTask;
		this.isActive = isActive;
	}

	public TaskResponse(Long taskId, String taskName, Boolean isCompleted, LocalDateTime createdDate) {
		this.taskId = taskId;
		this.taskName = taskName;
		this.isCompleted = isCompleted;
		this.createdDate = createdDate;
	}

}
