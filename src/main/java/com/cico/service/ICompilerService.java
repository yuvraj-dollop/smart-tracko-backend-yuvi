package com.cico.service;

import java.util.List;
import java.util.Map;

import com.cico.payload.CompilerRequest;
import com.cico.payload.CompilerResponse;

public interface ICompilerService {

	public CompilerResponse compileCode(CompilerRequest compilerRequest);

	List<Map<String, Object>> getRuntimes();

	CompilerResponse execute(CompilerRequest request);
}
