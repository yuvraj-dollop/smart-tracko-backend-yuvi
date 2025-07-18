package com.cico.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
	public ResponseEntity<?> getChapterExamResult(@RequestParam("resultId") Integer id) {
		return examService.getChapterExamResult(id);
	}

	@GetMapping("/getALLChapterExamResultesByChapterIdApi")
	public ResponseEntity<?> getChapterExamResultes(@RequestParam("chapterId") Integer chapterId) {
		return examService.getChapterExamResultByChaterId(chapterId);
	}

	@GetMapping("/checkExamCompleteOrNot")
	public ResponseEntity<?> checkExamCompletedOrNot(@RequestParam("chapterId") Integer chapterId,
			@RequestParam("studentId") Integer studentId) {
		return examService.getChapterExamIsCompleteOrNot(chapterId, studentId);
	}

	@GetMapping("/getChapterExam")
	public ResponseEntity<?> getChapterExam(@RequestParam("chapterId") Integer chapterId) {
		return examService.getChapterExam(chapterId);
	}

	// POST
	@PostMapping("/addChapterExam")
	public ResponseEntity<?> addChapterExamResult(@RequestBody ExamRequest chapterExamResult) {
		return this.examService.addChapterExamResult(chapterExamResult);
	}

	// PUT
	@PutMapping("/changeChapterExamStatus")
	public ResponseEntity<?> changeChapterExamStatus(@RequestParam Integer examId) {
		return examService.changeChapterExamStatus(examId);
	}

	@PutMapping("/setChapterExamStartStatus")
	public ResponseEntity<?> setChapterExamStartStatus(@RequestParam Integer chapterId) {
		return examService.setChapterExamStartStatus(chapterId);
	}

	// ==================== SUBJECT EXAM METHODS ====================
	// GET
	@GetMapping("/getALLSubjectExamResultesBySubjectId")
	public ResponseEntity<?> getSubjectExamResultes(@RequestParam("examId") Integer examId) {
		return examService.getSubjectExamResultesBySubjectId(examId);
	}

	@GetMapping("/getSubjectExamResult")
	public ResponseEntity<?> getSubjectExamResult(@RequestParam("resultId") Integer resultId) {
		return examService.getSubjectExamResult(resultId);
	}

	@GetMapping("/getAllSubjectNormalAndScheduleExam")
	public ResponseEntity<?> getAllSubjectNormalAndScheduleExam(@RequestParam("subjectId") Integer subjectId) {
		return examService.getAllSubjectNormalAndScheduleExam(subjectId);
	}

	@GetMapping("/getAllSubjectNormalAndScheduleExamForStudent")
	public ResponseEntity<?> getAllSubjectNormalAndScheduleExamForStudent(
			@RequestParam("studentId") Integer studentId) {
		return examService.getAllSubjectNormalAndScheduleExamForStudent(studentId);
	}

	@GetMapping("/getSubjectExamCount")
	public ResponseEntity<?> getSubjectExamCount(@RequestParam("studentId") Integer studentId) {
		return examService.getSubjectExamCount(studentId);
	}

	// POST
	@PostMapping("/addSubjectExam")
	public ResponseEntity<?> addSubjectExam(@RequestBody AddExamRequest request) {
		return examService.addSubjectExam(request);
	}

	@PostMapping("/addSubjectExamResult")
	public ResponseEntity<?> addSubjectExamResult(@RequestBody ExamRequest chapterExamResult) {
		return this.examService.addSubjectExamResult(chapterExamResult);
	}

	// PUT
	@PutMapping("/updateSubjectExam")
	public ResponseEntity<?> updateSubjectExam(@RequestBody AddExamRequest request) {
		return examService.updateSubjectExam(request);
	}

	@PutMapping("/changeSubjectExamStatus")
	public ResponseEntity<?> changeSubjectExamStatus(@RequestParam Integer examId) {
		return examService.changeSubjectExamStatus(examId);
	}

	@PutMapping("/setSubjectExamStartStatus")
	public ResponseEntity<?> setSubjectExamStartStatus(@RequestParam Integer examId) {
		return examService.setSubjectExamStartStatus(examId);
	}

	// DELETE
	@DeleteMapping("/deleteSubjectExam")
	public ResponseEntity<?> deleteSubjectExam(@RequestParam("examId") Integer examId) {
		return examService.deleteSubjectExam(examId);
	}

	// ==================== COURSE EXAM METHODS ====================
	// GET
	@GetMapping("/getCourseExamResult")
	public ResponseEntity<?> getCourseExamResult(@RequestParam("resultId") Integer resultId) {
		return examService.getCourseExamResult(resultId);
	}

	// POST
	@PostMapping("/addCourseExamResult")
	public ResponseEntity<?> addCourseExamResult(@RequestBody ExamRequest courseExamResult) {
		return this.examService.addCourseExamResult(courseExamResult);
	}

	@PostMapping("/getAllCourseExamsByExamTypes")
	public ResponseEntity<?> filterCourseSubjectTest(@RequestParam("examType") ExamType examType,
			@RequestBody TestFilterRequest request) {
		return examService.filterCourseSubjectTest(examType, request);
	}

	// PUT
	@PutMapping("/setCourseExamStartStatus")
	public ResponseEntity<?> setCourseExamStartStatus(@RequestParam Integer examId) {
		return examService.setCourseExamStartStatus(examId);
	}

	// ==================== GENERAL EXAM METHODS ====================
	// PUT
	@PutMapping("/deleteExamById")
	public ResponseEntity<?> deleteExamById(@RequestParam("examId") Integer examId) {
		return examService.deleteExamById(examId);
	}
}