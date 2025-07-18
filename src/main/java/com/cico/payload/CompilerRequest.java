package com.cico.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompilerRequest {

	private String code;
	private String language;
	private String fileName;
}
