package com.cico.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestPerformanceResponse {
	private Integer examId;
	private String examName;
	private String examType; // COURSE or SUBJECT
	private String examDate; // Keep as String if you’re formatting dates
	private Integer totalQuestions;
	private Integer obtainedMarks;
	private Double percentage;
	private String examTimer;
	private String scheduleTestTime;
	private String status; // PASS / FAIL
}
