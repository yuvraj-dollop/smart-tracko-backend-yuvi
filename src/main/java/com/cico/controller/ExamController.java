package com.cico.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cico.payload.AddExamRequest;
import com.cico.payload.ExamRequest;
import com.cico.payload.TestFilterRequest;
import com.cico.service.IExamService;
import com.cico.util.AppConstants;
import com.cico.util.ExamType;

@RestController
@RequestMapping("/exam")
@CrossOrigin("*")
public class ExamController {

	@Autowired
	private IExamService examService;

	// ==================== CHAPTER EXAM METHODS ====================
	// GET
	@GetMapping("/getChapterExamResult")
	public ResponseEntity<?> getChapterExamResult(@RequestParam(name = AppConstants.RESULT_ID) Integer id) {
		return examService.getChapterExamResult(id);
	}

	@GetMapping("/getALLChapterExamResultesByChapterIdApi")
	public ResponseEntity<?> getChapterExamResultes(@RequestParam(name = AppConstants.CHAPTER_ID) Integer chapterId) {
		return examService.getChapterExamResultByChaterId(chapterId);
	}

	@GetMapping("/checkExamCompleteOrNot")
	public ResponseEntity<?> checkExamCompletedOrNot(@RequestParam(name = AppConstants.CHAPTER_ID) Integer chapterId,
			@RequestParam("studentId") Integer studentId) {
		return examService.getChapterExamIsCompleteOrNot(chapterId, studentId);
	}

	@GetMapping("/getChapterExam")
	public ResponseEntity<?> getChapterExam(@RequestParam(name = AppConstants.CHAPTER_ID) Integer chapterId) {
		return examService.getChapterExam(chapterId);
	}

	// POST
	@PostMapping("/addChapterExam")
	public ResponseEntity<?> addChapterExamResult(@Valid @RequestBody ExamRequest chapterExamResult) {
		return this.examService.addChapterExamResult(chapterExamResult);
	}

	// PUT
	@PutMapping("/changeChapterExamStatus")
	public ResponseEntity<?> changeChapterExamStatus(@RequestParam(name = AppConstants.EXAM_ID) Integer examId) {
		return examService.changeChapterExamStatus(examId);
	}

	@PutMapping("/setChapterExamStartStatus")
	public ResponseEntity<?> setChapterExamStartStatus(
			@RequestParam(name = AppConstants.CHAPTER_ID) Integer chapterId) {
		return examService.setChapterExamStartStatus(chapterId);
	}

	// ==================== SUBJECT EXAM METHODS ====================
	// GET
	@GetMapping("/getALLSubjectExamResultesBySubjectId")
	public ResponseEntity<?> getSubjectExamResultes(@RequestParam(name = AppConstants.EXAM_ID) Integer examId) {
		return examService.getSubjectExamResultesBySubjectId(examId);
	}

	@GetMapping("/getSubjectExamResult")
	public ResponseEntity<?> getSubjectExamResult(@RequestParam(name = AppConstants.RESULT_ID) Integer resultId) {
		return examService.getSubjectExamResult(resultId);
	}

	@GetMapping("/getAllSubjectNormalAndScheduleExam")
	public ResponseEntity<?> getAllSubjectNormalAndScheduleExam(
			@RequestParam(name = AppConstants.SUBJECT_ID) Integer subjectId) {
		return examService.getAllSubjectNormalAndScheduleExam(subjectId);
	}

	@GetMapping("/getAllSubjectNormalAndScheduleExamForStudent")
	public ResponseEntity<?> getAllSubjectNormalAndScheduleExamForStudent(
			@RequestParam(name = AppConstants.STUDENT_ID) Integer studentId) {
		return examService.getAllSubjectNormalAndScheduleExamForStudent(studentId);
	}

	@GetMapping("/getSubjectExamCount")
	public ResponseEntity<?> getSubjectExamCount(@RequestParam(name = AppConstants.STUDENT_ID) Integer studentId) {
		return examService.getSubjectExamCount(studentId);
	}

	// POST
	@PostMapping("/addSubjectExam")
	public ResponseEntity<?> addSubjectExam(@Valid @RequestBody AddExamRequest request) {
		return examService.addSubjectExam(request);
	}

	@PostMapping("/addSubjectExamResult")
	public ResponseEntity<?> addSubjectExamResult(@Valid @RequestBody ExamRequest chapterExamResult) {
		return this.examService.addSubjectExamResult(chapterExamResult);
	}

	// PUT
	@PutMapping("/updateSubjectExam")
	public ResponseEntity<?> updateSubjectExam(@Valid @RequestBody AddExamRequest request) {
		return examService.updateSubjectExam(request);
	}

	@PutMapping("/changeSubjectExamStatus")
	public ResponseEntity<?> changeSubjectExamStatus(@RequestParam(name = AppConstants.EXAM_ID) Integer examId) {
		return examService.changeSubjectExamStatus(examId);
	}

	@PutMapping("/setSubjectExamStartStatus")
	public ResponseEntity<?> setSubjectExamStartStatus(@RequestParam(name = AppConstants.EXAM_ID) Integer examId) {
		return examService.setSubjectExamStartStatus(examId);
	}

	// DELETE
	@DeleteMapping("/deleteSubjectExam")
	public ResponseEntity<?> deleteSubjectExam(@RequestParam(name = AppConstants.EXAM_ID) Integer examId) {
		return examService.deleteSubjectExam(examId);
	}

	// ==================== COURSE EXAM METHODS ====================
	// GET
	@GetMapping("/getCourseExamResult")
	public ResponseEntity<?> getCourseExamResult(@RequestParam(name = AppConstants.RESULT_ID) Integer resultId) {
		return examService.getCourseExamResult(resultId);
	}

	// POST
	@PostMapping("/addCourseExamResult")
	public ResponseEntity<?> addCourseExamResult(@Valid @RequestBody ExamRequest courseExamResult) {
		return this.examService.addCourseExamResult(courseExamResult);
	}

	@PostMapping("/getAllCourseExamsByExamTypes")
	public ResponseEntity<?> filterCourseSubjectTest(@RequestParam(name = AppConstants.EXAM_TYPE) ExamType examType,
			@Valid @RequestBody TestFilterRequest request) {
		return examService.filterCourseSubjectTest(examType, request);
	}

	// PUT
	@PutMapping("/setCourseExamStartStatus")
	public ResponseEntity<?> setCourseExamStartStatus(@RequestParam(name = AppConstants.EXAM_ID) Integer examId) {
		return examService.setCourseExamStartStatus(examId);
	}

	// ==================== GENERAL EXAM METHODS ====================
	// PUT
	@PutMapping("/deleteExamById")
	public ResponseEntity<?> deleteExamById(@RequestParam(name = AppConstants.EXAM_ID) Integer examId) {
		return examService.deleteExamById(examId);
	}

	// ......................... NEW API'S ..............................

	@GetMapping("/v2/checkExamCompleteOrNot")
	public ResponseEntity<?> checkExamCompletedOrNotNew(@RequestParam(name = AppConstants.CHAPTER_ID) Integer chapterId,
			@RequestParam(name = AppConstants.STUDENT_ID) Integer studentId) {
		return examService.getChapterExamIsCompleteOrNot(chapterId, studentId);
	}

	@GetMapping("/v2/getChapterExamResult")
	public ResponseEntity<?> getChapterExamResultNew(@RequestParam(name = AppConstants.RESULT_ID) Integer resultId) {
		return examService.getChapterExamResultNew(resultId);
	}

	@PutMapping("/v2/setChapterExamStartStatus/{chapterId}")
	public ResponseEntity<?> setChapterExamStartStatusNew(
			@PathVariable(name = AppConstants.CHAPTER_ID) Integer chapterId) {
		return examService.setChapterExamStartStatus(chapterId);
	}

	@GetMapping("/v2/getChapterExam")
	public ResponseEntity<?> getChapterExamNew(@RequestParam(name = AppConstants.CHAPTER_ID) Integer chapterId) {
		return examService.getChapterExamNew(chapterId);
	}

	// POST
	@PostMapping("/v2/addChapterExam")
	public ResponseEntity<?> addChapterExamResultNew(@Valid @RequestBody ExamRequest chapterExamResult) {
		System.err.println(" ------------- >chapterExamResult => " + chapterExamResult);
		return this.examService.addChapterExamResult(chapterExamResult);
	}

//................................................... New ......................................................
	@GetMapping("/v2/getPerformanceDataOfStudent")
	public ResponseEntity<?> getResultCountsDataOfStudent(
			@RequestParam(name = AppConstants.STUDENT_ID) Integer studentId) {

		return examService.getOverallResultOfStudentByCourse(studentId);
	}

	@GetMapping("/v2/getperformanceDataMonthaly")
	public ResponseEntity<?> getperformanceDataMonthaly(Integer studentId) {
		return examService.getperformanceDataMonthaly(studentId);
	}

	@GetMapping("/v2/getAllSubjectPerformanceData")
	public ResponseEntity<?> getAllSubjectPerformanceData(Integer studentId) {
		return examService.getAllSubjectPerformanceData(studentId);
	}

	@GetMapping("/v2/getAllTestperformanceDataOfStudent")
	public ResponseEntity<?> getAllTestperformanceDataOfStudent(Integer studentId) {
		return examService.getAllTestperformanceDataOfStudent(studentId);
	}
}