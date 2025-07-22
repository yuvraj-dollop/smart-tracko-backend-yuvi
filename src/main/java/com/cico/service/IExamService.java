package com.cico.service;

import org.springframework.http.ResponseEntity;

import com.cico.payload.AddExamRequest;
import com.cico.payload.ExamRequest;
import com.cico.payload.PaginationRequest;
import com.cico.payload.TestFilterRequest;
import com.cico.util.ExamType;

public interface IExamService {

	// ==================== CHAPTER EXAM METHODS ====================
	// Create
	ResponseEntity<?> addChapterExamResult(ExamRequest chapterExamResult);

	// Read
	ResponseEntity<?> getChapterExamResult(Integer id);

	ResponseEntity<?> getChapterExamResultByChaterId(Integer chapterId);

	ResponseEntity<?> getChapterExamIsCompleteOrNot(Integer chapterId, Integer studentId);

	ResponseEntity<?> getChapterExam(Integer chapterId);

	// Update
	ResponseEntity<?> changeChapterExamStatus(Integer examId);

	ResponseEntity<?> setChapterExamStartStatus(Integer chapterId);

	// ==================== SUBJECT EXAM METHODS ====================
	// Create
	ResponseEntity<?> addSubjectExam(AddExamRequest request);

	ResponseEntity<?> addSubjectExamResult(ExamRequest request);

	// Read
	ResponseEntity<?> getSubjectExamResult(Integer resultId);

	ResponseEntity<?> getSubjectExamResultesBySubjectId(Integer examId);

	ResponseEntity<?> getAllSubjectNormalAndScheduleExam(Integer subjectId);

	ResponseEntity<?> getAllSubjectNormalAndScheduleExamForStudent(Integer studentId);

	ResponseEntity<?> getSubjectExamCount(Integer studentId);

	// Update
	ResponseEntity<?> updateSubjectExam(AddExamRequest request);

	ResponseEntity<?> changeSubjectExamStatus(Integer examId);

	ResponseEntity<?> setSubjectExamStartStatus(Integer examId);

	// Delete
	ResponseEntity<?> deleteSubjectExam(Integer examId);

	// ==================== COURSE EXAM METHODS ====================
	// Create
	ResponseEntity<?> addCourseExam(AddExamRequest request);

	ResponseEntity<?> addCourseExamResult(ExamRequest request);

	// Read
	ResponseEntity<?> getCourseExamResult(Integer resultId);

	ResponseEntity<?> getCourseExamResultesBySubjectId(Integer examId);

	ResponseEntity<?> getCourseExamResultsByExamId(Integer examId);

	ResponseEntity<?> getAllCourseNormalAndScheduleExam(Integer couseId);

	ResponseEntity<?> getAllCourseNormalAndScheduleExamForStudent(Integer studentId);

	ResponseEntity<?> filterCourseSubjectTest(ExamType examType, TestFilterRequest request);

	// Update
	ResponseEntity<?> updateCourseExam(AddExamRequest request);

	ResponseEntity<?> changeCourseExamStatus(Integer examId);

	ResponseEntity<?> setCourseExamStartStatus(Integer examId);

	// ==================== GENERAL EXAM METHODS ====================
	ResponseEntity<?> deleteExamById(Integer examId);
	
	
	
	
	// .................. NEW METHOD'S ................................
	
	
	public ResponseEntity<?> getAllUpcomingExams(Integer studentId, PaginationRequest request);

	public ResponseEntity<?> getChapterExamNew(Integer chapterId);

	public ResponseEntity<?> getChapterExamResultNew(Integer id);
}
