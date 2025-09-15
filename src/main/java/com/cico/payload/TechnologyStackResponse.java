package com.cico.payload;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(value = Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnologyStackResponse {
	private Integer id;
	private String imageName;
	private String technologyName;
	private Boolean isDeleted;
	private LocalDateTime createdDate;
	private LocalDateTime updatedDate;

	public TechnologyStackResponse(Integer id, String imageName, String technologyName) {
		super();
		this.id = id;
		this.imageName = imageName;
		this.technologyName = technologyName;
	}

}
