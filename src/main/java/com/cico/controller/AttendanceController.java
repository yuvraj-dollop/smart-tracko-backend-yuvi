package com.cico.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cico.service.IStudentService;
import com.cico.util.AppConstants;

@RequestMapping("/attendance")
@RestController
@CrossOrigin("*")
public class AttendanceController {
	@Autowired
	private IStudentService studentService;

	@GetMapping("/v2/getStudentCalenderData")
	public ResponseEntity<Map<String, Object>> getCalenderDataNew(@RequestParam(AppConstants.STUDENT_ID) Integer id,
			@RequestParam(AppConstants.MONTH) Integer month, @RequestParam(AppConstants.YEAR) Integer year) {
		Map<String, Object> response = studentService.getCalenderDataNew(id, month, year);
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
	}

	@GetMapping("/v2/getAttendanceAnalytics")
	public ResponseEntity<Map<String, Object>> getAttendanceAnalytics(
			@RequestParam(AppConstants.STUDENT_ID) Integer id) {
		Map<String, Object> response = studentService.getAttendanceAnalytics(id);
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
	}

	// for recent attendance (attendance history)
	@GetMapping("v2/getStudentCheckInCheckOutHistory")
	public ResponseEntity<?> getStudentCheckInCheckOutHistory(
			@RequestParam(name = AppConstants.START_DATE, required = false) String startDate,
			@RequestParam(AppConstants.END_DATE) String endDate,
			@RequestParam(name = AppConstants.PAGE_NUMBER, defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
			@RequestParam(name = AppConstants.PAGE_SIZE, defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
			@RequestParam(name = AppConstants.TYPE, required = false) String type) {

		return studentService.getStudentCheckInCheckOutHistoryNew(startDate, endDate, pageNumber, pageSize, type);
	}
}
