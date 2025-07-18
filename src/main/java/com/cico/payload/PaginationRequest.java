package com.cico.payload;

import javax.validation.constraints.NotBlank;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaginationRequest {

	@NotBlank(message = "Page size cannot be null")
	private Integer pageSize;
	@NotBlank(message = "Page number cannot be null")
	private Integer pageNumber;

	private String sortBy;
	private String sortDirection;
	private String searchQuery;

}
