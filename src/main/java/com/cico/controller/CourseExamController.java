package com.cico.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cico.payload.AddExamRequest;
import com.cico.payload.ExamRequest;
import com.cico.service.IExamService;
import com.cico.util.AppConstants;

@RestController
@RequestMapping("/courseExam")
@CrossOrigin("*")
public class CourseExamController {

	@Autowired
	private IExamService examService;

	// =============== EXAM RESULT OPERATIONS ===============
	@PostMapping("/addCourseExamResult")
	public ResponseEntity<?> addCourseExamResult(@Valid @RequestBody ExamRequest request) {
		return examService.addCourseExamResult(request);
	}

	@GetMapping("/getCourseExamResult")
	public ResponseEntity<?> getCourseExamResult(@RequestParam(name = AppConstants.RESULT_ID) Integer resultId) {
		return examService.getCourseExamResult(resultId);
	}

	@GetMapping("/getCourseExamResultsBExamId")
	public ResponseEntity<?> getCourseExamResultsByExamId(@RequestParam(name = AppConstants.EXAM_ID) Integer examId) {
		return examService.getCourseExamResultsByExamId(examId);
	}

	// =============== EXAM MANAGEMENT ===============
	// changes addCourseExam() to addCourseExamNew()
	@PostMapping("/addCourseExam")
	public ResponseEntity<?> addCourseExam(@Valid @RequestBody AddExamRequest request) {
		return examService.addCourseExamNew(request);
	}

	@PutMapping("/updateCourseExam")
	public ResponseEntity<?> updateCourseExam(@Valid @RequestBody AddExamRequest request) {
		return examService.updateCourseExam(request);
	}

	@PutMapping("/deleteExamById")
	public ResponseEntity<?> deleteExamById(@RequestParam(name = AppConstants.EXAM_ID) Integer examId) {
		return examService.deleteExamById(examId);
	}

	// =============== EXAM STATUS OPERATIONS ===============
	@PutMapping("/setCourseExamStartStatus")
	public ResponseEntity<?> setCourseExamStartStatus(@RequestParam(name = AppConstants.EXAM_ID) Integer examId) {
		return examService.setCourseExamStartStatus(examId);
	}

	@PutMapping("/changeCourseExamStatus")
	public ResponseEntity<?> changeCourseExamStatus(@RequestParam(name = AppConstants.EXAM_ID) Integer examId) {
		return examService.changeCourseExamStatus(examId);
	}

	// =============== EXAM RETRIEVAL ===============
	@GetMapping("/getAllCourseNormalAndScheduleExamForStudent")
	public ResponseEntity<?> getAllCourseNormalAndScheduleExamForStudent(
			@RequestParam(name = AppConstants.STUDENT_ID) Integer studentId) {
		return examService.getAllCourseNormalAndScheduleExamForStudent(studentId);
	}

	@GetMapping("/getAllCourseNormalAndScheduleExam")
	public ResponseEntity<?> getAllCourseNormalAndScheduleExam(
			@RequestParam(name = AppConstants.COURSE_ID) Integer courseId) {
		return examService.getAllCourseNormalAndScheduleExam(courseId);
	}

	// ==============================New Apis ======================================
	@GetMapping("/v2/getAllCourseNormalAndScheduleExamForStudent")
	public ResponseEntity<?> getAllCourseNormalAndScheduleExamForStudentNew(
			@RequestParam(name = AppConstants.STUDENT_ID) Integer studentId) {
		return examService.getAllCourseNormalAndScheduleExamForStudent(studentId);
	}

	@GetMapping("/v2/getAllCourseNormalAndScheduleExam")
	public ResponseEntity<?> getAllCourseNormalAndScheduleExamNew(
			@RequestParam(name = AppConstants.COURSE_ID) Integer courseId) {
		return examService.getAllCourseNormalAndScheduleExam(courseId);
	}

	@PostMapping("/v2/addCourseExam")
	public ResponseEntity<?> addCourseExamNew(@Valid @RequestBody AddExamRequest request) {
		return examService.addCourseExamNew(request);
	}

	@PutMapping("/v2/deleteExamById")
	public ResponseEntity<?> deleteExamByIdNew(@RequestParam(name = AppConstants.EXAM_ID) Integer examId) {
		return examService.deleteExamById(examId);
	}

	@PutMapping("/v2/updateCourseExam")
	public ResponseEntity<?> updateCourseExamNew(@Valid @RequestBody AddExamRequest request) {
		return examService.updateCourseExam(request);
	}

}