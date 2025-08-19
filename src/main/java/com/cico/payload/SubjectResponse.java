package com.cico.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@JsonInclude(value = Include.NON_NULL)
public class SubjectResponse {
	private Integer subjectId;
	private String subjectName;
	private Boolean isDeleted;
	private Boolean isActive;
	public Long chapterCount;
	public Long chapterCompleted;
	@Builder.Default
	private TechnologyStackResponse technologyStack = new TechnologyStackResponse();

	public SubjectResponse(Integer subjectId, String subjectName, Integer technologyId, String technologyName,
			String imageName) {
		System.err.println("tecnology id -----> " + technologyStack + " ---- " + technologyId);
		this.subjectId = subjectId;
		this.subjectName = subjectName;
		this.technologyStack = new TechnologyStackResponse(technologyId, technologyName, imageName);
	}

}
