package com.cico.payload;

import java.time.LocalDate;
import java.time.LocalTime;

import com.cico.util.ExamType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(value = Include.NON_NULL)
@NoArgsConstructor
public class SubjectExamResponse {

	private String subjectName;
	private Integer examId;
	private Integer subjectId;
	private String examImage;
	private Boolean isActive;
	private Integer examTimer;
	private Integer totalQuestionForTest;
	private Integer passingMarks;
	private Integer scoreGet;
	private LocalDate scheduleTestDate;
	private Integer totalExamQuestion;
	private String examName;
	private ExamType examType;
	private Integer resultId;
	private LocalTime examStartTime;
	private Boolean isExamEnd;
	private Integer extraTime;
	private Boolean isStart;
	private String status;
	private Integer attempted;
	private Integer right;
	private Integer wrong;
	private Integer notAttempted;

	public SubjectExamResponse(String examName, String examImage, Integer examId, String imageName, Integer examTimer,
			Integer passingMarks, Integer scoreGet, LocalDate scheduleTestDate, Integer totalQuestionForTest,
			ExamType examType, Integer resultId, Integer subjectId, LocalTime examStartTime, Boolean isStart,
			String status) {
		super();

		this.examId = examId;

		this.examImage = examImage;
		// this.isActive = isActive;
		this.examTimer = examTimer;
		this.totalQuestionForTest = totalQuestionForTest;
		this.passingMarks = passingMarks;
		this.scoreGet = scoreGet;
		this.scheduleTestDate = scheduleTestDate;
		this.examType = examType;
		this.resultId = resultId;
		this.examName = examName;
		this.examTimer = examTimer;
		this.subjectId = subjectId;
		this.examStartTime = examStartTime;
		this.isStart = isStart;
		this.status = status;
	}

}
