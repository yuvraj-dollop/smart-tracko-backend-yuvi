package com.cico.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cico.model.Batch;
import com.cico.payload.ApiResponse;
import com.cico.payload.BatchRequest;
import com.cico.payload.BatchResponse;
import com.cico.service.IBatchService;
import com.cico.util.AppConstants;

@RestController
@RequestMapping("/batch")
@CrossOrigin("*")
public class BatchController {

	@Autowired
	IBatchService batchService;

	@PostMapping("/createBatch")
	public ResponseEntity<?> createBatch(@RequestBody @Valid BatchRequest request) {
		System.out.println(request);
		ApiResponse createBatch = batchService.createBatch(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(createBatch);

	}

	@PutMapping("/updateBatch")
	public ResponseEntity<?> updateBatch(@RequestBody Batch batch) {

		ApiResponse updateBatch = batchService.updateBatch(batch);
		return ResponseEntity.ok(updateBatch);

	}

	@PutMapping("/deleteBatch/{batchId}")
	public ResponseEntity<ApiResponse> deleteBatch(@PathVariable(name = AppConstants.BATCH_ID) Integer batchId) {
		ApiResponse response = batchService.deleteBatch(batchId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/getBatchById/{batchId}")
	public ResponseEntity<?> getBatchById(@PathVariable(name = AppConstants.BATCH_ID) Integer batchId) {
		BatchResponse batchResponse = batchService.getBatchById(batchId);
		return ResponseEntity.ok(batchResponse);

	}

	@GetMapping("/getAllBatches")
	public ResponseEntity<?> getAllBatches(@RequestParam(name = AppConstants.STUDENT_ID) Integer studentId) {
		List<BatchResponse> batches = batchService.getAllBatches(studentId);
		return ResponseEntity.ok(batches);

	}

	@GetMapping("/getUpcomingBatches")
	public ResponseEntity<List<Batch>> getUpcomingBatches() {
		List<Batch> batches = batchService.getUpcomingBatches();
		return ResponseEntity.status(HttpStatus.OK).body(batches);

	}

	@PutMapping("/updateBatchStatus/{batchId}")
	public ResponseEntity<ApiResponse> updateBatchStatus(@PathVariable(name = AppConstants.BATCH_ID) Integer batchId) {
		ApiResponse response = batchService.updateBatchStatus(batchId);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);

	}

	@GetMapping("/firstUpcomingBatchOfCurrentCourse/{courseName}")
	public ResponseEntity<?> firstUpcomingBatchOfCurrentCourse(@PathVariable String courseName) {
		return new ResponseEntity<>(batchService.getFirstUpcomingBatchOfCurrentCourse(courseName), HttpStatus.OK);
	}
	
	//================================= New Methods =====================================================

	@GetMapping("/v2/getUpcomingBatches")
	public ResponseEntity<List<BatchResponse>> getUpcomingBatchesNew() {
		List<BatchResponse> batches = batchService.getUpcomingBatchesNew();
		return ResponseEntity.status(HttpStatus.OK).body(batches);

	}

}
