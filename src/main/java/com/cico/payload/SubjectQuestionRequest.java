package com.cico.payload;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectQuestionRequest {

	@NotNull(message = "Subject ID is required")
	private Integer subjectId;

	@NotBlank(message = "Question content is required")
	private String questionContent;

	@NotBlank(message = "Option 1 is required")
	private String option1;

	@NotBlank(message = "Option 2 is required")
	private String option2;

	@NotBlank(message = "Option 3 is required")
	private String option3;

	@NotBlank(message = "Option 4 is required")
	private String option4;

	@NotBlank(message = "Correct option is required")
	private String correctOption;
}
