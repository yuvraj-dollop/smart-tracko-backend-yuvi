package com.cico.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

	@PostMapping("/v2/execute")
	public ResponseEntity<CompilerResponse> execute(@RequestBody CompilerRequest request) {
		return ResponseEntity.ok(compilerService.execute(request));
	}

	@GetMapping("/v2/getLanguages")
	public ResponseEntity<List<Map<String, Object>>> getRuntimes() {
		return ResponseEntity.ok(compilerService.getRuntimes());
	}

}
