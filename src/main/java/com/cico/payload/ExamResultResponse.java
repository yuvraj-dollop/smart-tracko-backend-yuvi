package com.cico.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamResultResponse {

	private Integer id;
	private Integer correcteQuestions;
	private Integer wrongQuestions;
	private Integer notSelectedQuestions;
	private String profilePic;
	private Integer studentId;
	private String studentName;
	private Integer scoreGet;
	public Integer totalQuestion;
	private Integer percentile;

	public ExamResultResponse(Integer id, Integer correcteQuestions, Integer wrongQuestions,
			Integer notSelectedQuestions, String profilePic, Integer studentId, String studentName, Integer scoreGet,
			Integer totalQuestion) {
		this.id = id;
		this.correcteQuestions = correcteQuestions;
		this.wrongQuestions = wrongQuestions;
		this.notSelectedQuestions = notSelectedQuestions;
		this.profilePic = profilePic;
		this.studentId = studentId;
		this.studentName = studentName;
		this.scoreGet = scoreGet;
		this.totalQuestion = totalQuestion;
	}

}
