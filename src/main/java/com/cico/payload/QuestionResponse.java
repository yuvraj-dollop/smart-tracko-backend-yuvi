package com.cico.payload;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(value = Include.NON_NULL)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponse {
	private Integer questionId;
	private String questionContent;

	// OLD
	private String option1;
	private String option2;
	private String option3;
	private String option4;
	private String correctOption;
	private String selectedOption;// select by student

	// NEW ADD
	private List<String> option;
	private Integer correctOpt;
	private Integer selectedOpt;// select by student

	private String questionImage;
	private Boolean isSelected;
}
