package com.cico.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(value = Include.NON_NULL)
public class CompilerResponse {
	private String stdout;
	private String stderr;
	private Integer exitCode;
	private boolean timedOut;
	private long durationMs;

	private String response;
	private String type; // success || error
}
