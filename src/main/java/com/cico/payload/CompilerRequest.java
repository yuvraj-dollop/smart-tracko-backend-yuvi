package com.cico.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompilerRequest {
	private String version; // e.g. "3.10.0"
	private String input;
	private String code;
	private String language;
	private String fileName;
}
