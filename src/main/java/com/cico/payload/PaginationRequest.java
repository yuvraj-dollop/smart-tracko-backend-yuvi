package com.cico.payload;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class PaginationRequest {

	@NotBlank(message = "Page size cannot be null")
	private Integer pageSize;
	@NotBlank(message = "Page number cannot be null")
	private Integer pageNumber;

	private String sortBy;
	private String sortDirection;
	private String searchQuery;

}
