package com.cico.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cico.payload.CompilerRequest;
import com.cico.payload.CompilerResponse;
import com.cico.service.ICompilerService;

@RestController
@RequestMapping("/compiler")
public class CompilerController {

	@Autowired
	private ICompilerService compilerService;

	@PostMapping("/compile")
	public ResponseEntity<CompilerResponse> compileCode(@RequestBody CompilerRequest request) {
		return new ResponseEntity<>(compilerService.compileCode(request), HttpStatus.OK);
	}

	@PostMapping("/v2/compile")
	public ResponseEntity<CompilerResponse> compileCodeNew(@RequestBody CompilerRequest request) {
		return new ResponseEntity<>(compilerService.compileCode(request), HttpStatus.OK);
	}
}
