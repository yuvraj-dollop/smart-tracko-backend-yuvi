package com.cico.payload;

import java.time.LocalDate;
import java.time.LocalTime;

import com.cico.util.ExamType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(value = Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class CourseExamResponse {

	private String courseName;
	private Integer examId;
	private Integer courseId;
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

	public CourseExamResponse(String examName, Integer examId, String examImage, Integer examTimer,
			Integer passingMarks, Integer scoreGet, LocalDate scheduleTestDate, Integer totalQuestionForTest,
			ExamType examType, Integer resultId, Integer courseId, LocalTime examStartTime, Boolean isStart) {
		this.examName = examName;
		this.examId = examId;
		this.examImage = examImage;
		this.examTimer = examTimer;
		this.passingMarks = passingMarks;
		this.scoreGet = scoreGet;
		this.scheduleTestDate = scheduleTestDate;
		this.totalQuestionForTest = totalQuestionForTest;
		this.examType = examType;
		this.resultId = resultId;
		this.courseId = courseId;
		this.examStartTime = examStartTime;
		this.isStart = isStart;
		this.status = status;

// Optional defaults
		this.isActive = true;
		this.isExamEnd = false;
		this.extraTime = 0;
		this.totalExamQuestion = totalQuestionForTest;
	}

}