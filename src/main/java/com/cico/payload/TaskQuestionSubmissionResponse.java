package com.cico.payload;

import java.time.LocalDateTime;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.cico.util.SubmissionStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskQuestionSubmissionResponse {

	private Long taskId;
	private Long questionId;
	private String taskDescription;
	private String submittionFileName;
	private LocalDateTime submissionDate;
	@Enumerated(EnumType.STRING)
	private SubmissionStatus status;
	private String review;
	private String taskName;
	private Integer taskNumber;
	
	public TaskQuestionSubmissionResponse(
		    long taskId,               // <- primitive
		    long questionId,           // <- primitive
		    String taskDescription,
		    String submittionFileName,
		    LocalDateTime submissionDate,
		    SubmissionStatus status,
		    String review,
		    String taskName,
		    Integer taskNumber             // <- primitive
		) {
		    this.taskId = taskId;
		    this.questionId = questionId;
		    this.taskDescription = taskDescription;
		    this.submittionFileName = submittionFileName;
		    this.submissionDate = submissionDate;
		    this.status = status;
		    this.review = review;
		    this.taskName = taskName;
		    this.taskNumber = taskNumber;
		}

	
	
}
