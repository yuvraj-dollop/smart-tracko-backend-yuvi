package com.cico.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cico.model.Question;
import com.cico.service.IQuestionService;

@RestController
@RequestMapping("/question")
@CrossOrigin("*")
public class QuestionController {

	@Autowired
	IQuestionService questionService;

	// ==================== CHAPTER QUESTION METHODS ====================
	// POST
	@PostMapping("/addQuestionToChapter")
	public ResponseEntity<Question> addQuestionToChapterExam(@RequestParam("chapterId") Integer chapterId,
			@RequestParam("questionContent") String questionContent, @RequestParam("option1") String option1,
			@RequestParam("option2") String option2, @RequestParam("option3") String option3,
			@RequestParam("option4") String option4,
			@RequestParam(name = "image", required = false) MultipartFile image,
			@RequestParam("correctOption") String correctOption) {
		Question question = questionService.addQuestionToChapterExam(chapterId, questionContent, option1, option2,
				option3, option4, image, correctOption);
		return new ResponseEntity<>(question, HttpStatus.OK);
	}

	// GET
	@GetMapping("/getAllQuestionByChapterId")
	public ResponseEntity<List<Question>> getAllQuestionById(@RequestParam("chapterId") Integer chapterId) {
		List<Question> question = questionService.getAllQuestionByChapterId(chapterId);
		return ResponseEntity.ok(question);
	}

	// BULK UPLOAD
	@PostMapping("/upload-csv-chapter-question")
	public ResponseEntity<?> uploaodChapterBulkQuestion(@RequestParam("file") MultipartFile file,
			@RequestParam("chapterId") Integer chapterId) {
		return questionService.uploaodChapterBulkQuestion(file, chapterId);
	}

	// ==================== SUBJECT QUESTION METHODS ====================
	// POST
	@PostMapping("/addQuestionToSubject")
	public ResponseEntity<Question> addQuestionToSubjectExam(@RequestParam("subjectId") Integer subjectId,
			@RequestParam("questionContent") String questionContent, @RequestParam("option1") String option1,
			@RequestParam("option2") String option2, @RequestParam("option3") String option3,
			@RequestParam("option4") String option4,
			@RequestParam(name = "image", required = false) MultipartFile image,
			@RequestParam("correctOption") String correctOption) {
		Question question = questionService.addQuestionToSubjectExam(subjectId, questionContent, option1, option2,
				option3, option4, image, correctOption);
		return new ResponseEntity<>(question, HttpStatus.OK);
	}

	// GET
	@GetMapping("/getAllSubjectQuestionBySubjectId")
	public ResponseEntity<Map<String, Object>> getAllSubjectQuestionBySubjectId(
			@RequestParam("subjectId") Integer subjectId) {
		Map<String, Object> res = questionService.getAllSubjectQuestionBySubjectId(subjectId);
		return ResponseEntity.ok(res);
	}

	@GetMapping("/getAllSubjectQuestionBySubjectIdWithPagination")
	public ResponseEntity<?> getAllSubjectQuestionBySubjectIdWithPagination(
			@RequestParam("subjectId") Integer subjectId, @RequestParam(value = "pageSize") Integer pageSise,
			@RequestParam(value = "pageNumber") Integer pageNumber) {
		return questionService.getAllSubjectQuestionBySubjectIdWithPagination(subjectId, pageSise, pageNumber);
	}

	@GetMapping("/getAllSubjectQuestionForTest")
	public ResponseEntity<?> getAllSubjectQuestionForTest(@RequestParam("examId") Integer examId,
			@RequestParam("studentId") Integer studentId) {
		return questionService.getAllSubjectQuestionForTest(examId, studentId);
	}

	// BULK UPLOAD
	@PostMapping("/upload-csv-subject-question")
	public ResponseEntity<?> uploadSubjectBulkQuestion(@RequestParam("file") MultipartFile file,
			@RequestParam("subjectId") Integer subjectId) {
		return questionService.uploadSubjectBulkQuestion(file, subjectId);
	}

	// ==================== COURSE QUESTION METHODS ====================
	// GET
	@GetMapping("/getAllCourseQuestionForTest")
	public ResponseEntity<?> getAllCourseQuestionForTest(@RequestParam("examId") Integer examId,
			@RequestParam("studentId") Integer studentId) {
		return questionService.getAllCourseQuestionForTest(examId, studentId);
	}

	// ==================== GENERAL QUESTION METHODS ====================
	// GET
	@GetMapping("/getQuestionById")
	public ResponseEntity<Question> getQuestionById(@RequestParam("questionId") Integer questionId) {
		Question question = questionService.getQuestionById(questionId);
		return ResponseEntity.ok(question);
	}

	// PUT
	@PutMapping("/updateQuestionById")
	public ResponseEntity<?> updateQuestion(@RequestParam("questionContent") String questionContent,
			@RequestParam("option1") String option1, @RequestParam("option2") String option2,
			@RequestParam("option3") String option3, @RequestParam("option4") String option4,
			@RequestParam("questionId") Integer questionId, @RequestParam("correctOption") String correctOption,
			@RequestParam(name = "image", required = false) MultipartFile image, @RequestParam("examId") Integer examId,
			@RequestParam("type") Integer type) {
		return questionService.updateQuestion(questionId, questionContent, option1, option2, option3, option4,
				correctOption, image, examId, type);
	}

	@PutMapping("/deleteQuestionById")
	public ResponseEntity<?> deleteQuestion(@RequestParam("questionId") Integer questionId) {
		questionService.deleteQuestion(questionId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PutMapping("/updateQuestionStatus")
	public ResponseEntity<?> updateQuestionStatus(@RequestParam("questionId") Integer questionId) {
		questionService.updateQuestionStatus(questionId);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}