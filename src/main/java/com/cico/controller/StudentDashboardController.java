package com.cico.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cico.payload.PaginationRequest;
import com.cico.service.IAssignmentService;
import com.cico.service.IExamService;
import com.cico.service.IStudentService;
import com.cico.service.ITaskService;
import com.cico.util.AppConstants;

@RequestMapping("/dashboard")
@RestController
@CrossOrigin("*")
public class StudentDashboardController {

	@Autowired
	private IStudentService studentService;
	@Autowired
	private IExamService examService;
	@Autowired
	private IAssignmentService assignmentService;
	@Autowired
	private ITaskService taskService;

	@GetMapping("/v2/getStudentCalenderData")
	public ResponseEntity<Map<String, Object>> getStudentCalenderData(@RequestParam(name = AppConstants.ID) Integer id,
			@RequestParam(name = AppConstants.MONTH) Integer month,
			@RequestParam(name = AppConstants.YEAR) Integer year) {
		Map<String, Object> response = studentService.getCalenderData(id, month, year);
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
	}

	@GetMapping("/v2/getTodayAttendance/{studentId}")
	public ResponseEntity<Map<String, Object>> getTodayAttendanceNew(@PathVariable Integer studentId) {
		Map<String, Object> todayAttendance = studentService.getTodayAttendance(studentId);
		return ResponseEntity.ok(todayAttendance);
	}

	@GetMapping("/v2/getAllUpcomingScheduleExams")
	public ResponseEntity<?> getAllUpcomingScheduleExams(
			@RequestParam(name = AppConstants.STUDENT_ID) Integer studentId,
			@RequestParam(name = AppConstants.START_DATE, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(name = AppConstants.END_DATE, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			@RequestParam(name = AppConstants.PAGE_SIZE) Integer pageSize,
			@RequestParam(name = AppConstants.PAGE_NUMBER) Integer pageNumber) {
		return examService.getAllUpcomingExams(studentId, startDate, endDate,
				PaginationRequest.builder().pageSize(pageSize).pageNumber(pageNumber).build());
	}

	@GetMapping("/v2/getCountOfAssignmentAndTask/{studentId}")
	public ResponseEntity<?> getCountOfAssignmentAndTask(
			@PathVariable(name = AppConstants.STUDENT_ID) Integer studentId) {
		Map<String, Long> response = new HashMap<>();
		response.put("TotakAssignment", assignmentService.countAssignmentOfStudent(studentId));
		response.put("TotakSubmittedAssignment", assignmentService.countSubmittedAssignmentByStudentId(studentId));
		response.put("TotakTask", taskService.countTaskOfStudent(studentId));
		response.put("TotakSubmittedTask", taskService.countSubmittedTasksByStudentId(studentId));

		return ResponseEntity.ok(response);
	}

}
