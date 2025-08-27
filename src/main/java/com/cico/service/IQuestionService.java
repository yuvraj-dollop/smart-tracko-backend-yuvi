package com.cico.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.cico.model.Question;

public interface IQuestionService {

	// ==================== CHAPTER QUESTION METHODS ====================
	// Create
	Question addQuestionToChapterExam(Integer chapterId, String questionContent, String option1, String option2,
			String option3, String option4, MultipartFile image, String correctOption);

	// Read
	List<Question> getAllQuestionByChapterId(Integer chapterId);

	// Update
	ResponseEntity<?> updateQuestion(Integer questionId, String questionContent, String option1, String option2,
			String option3, String option4, String correctOption, MultipartFile image, Integer examId, Integer type);

	void updateQuestionStatus(Integer questionId);

	// Delete
	void deleteQuestion(Integer questionId);

	// Bulk Operations
	ResponseEntity<?> uploaodChapterBulkQuestion(MultipartFile file, Integer chapterId);

	// ==================== SUBJECT QUESTION METHODS ====================
	// Create
	Question addQuestionToSubjectExam(Integer subjectId, String questionContent, String option1, String option2,
			String option3, String option4, MultipartFile image, String correctOption);

	// Read
	Map<String, Object> getAllSubjectQuestionBySubjectId(Integer subjectId);

	ResponseEntity<?> getAllSubjectQuestionBySubjectIdWithPagination(Integer subjectId, Integer pageSise,
			Integer pageNumber);

	ResponseEntity<?> getAllSubjectQuestionForTest(Integer examId, Integer studentId);

	// Bulk Operations
	ResponseEntity<?> uploadSubjectBulkQuestion(MultipartFile file, Integer subjectId);

	// ==================== COURSE QUESTION METHODS ====================
	// Read
	ResponseEntity<?> getAllCourseQuestionForTest(Integer examId, Integer studentId);

	// ==================== GENERAL QUESTION METHODS ====================
	// Read
	Question getQuestionById(Integer questionId);

//	====================== NEW METHIDS =========================

	ResponseEntity<?> getAllSubjectQuestionForTestNew(Integer examId, Integer studentId);

	ResponseEntity<?> getAllCourseQuestionForTestNew(Integer examId, Integer studentId);
}