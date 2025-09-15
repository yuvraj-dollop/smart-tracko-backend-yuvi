package com.cico.payload;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageResponse<T> {

	private List<T> response;
	private int page;
	private int size;
	private long totalElements;
	private int totalPages;
	private boolean last;
	private boolean first;

	public List<T> getResponse() {
		return response == null ? null : new ArrayList<>(this.response);
	}

	public void setResponse(List<T> response) {
		if (response == null) {
			this.response = null;
		} else {
			this.response = response;
		}
	}

	// ✅ Generic constructor (instead of DiscussionFormResponse)
	public PageResponse(List<T> responseList, int page, int size, long totalElements, int totalPages, boolean last) {
		this.response = responseList;
		this.page = page;
		this.size = size;
		this.totalElements = totalElements;
		this.totalPages = totalPages;
		this.last = last;
	}
}
