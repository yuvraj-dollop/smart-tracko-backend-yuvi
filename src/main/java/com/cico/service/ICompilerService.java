package com.cico.service;

import com.cico.payload.CompilerRequest;
import com.cico.payload.CompilerResponse;

public interface ICompilerService {

	public CompilerResponse compileCode(CompilerRequest compilerRequest);
}
