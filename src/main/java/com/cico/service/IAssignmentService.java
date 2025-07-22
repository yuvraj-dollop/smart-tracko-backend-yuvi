package com.cico.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.cico.payload.AssignmentFilter;
import com.cico.payload.AssignmentQuestionRequest;
import com.cico.payload.AssignmentRequest;
import com.cico.payload.AssignmentSubmissionRequest;
import com.cico.payload.UpdateAssignmentQuestionRequest;
import com.cico.util.SubmissionStatus;

public interface IAssignmentService {

	ResponseEntity<?> getAssignment(Long id);

	ResponseEntity<?> createAssignment(AssignmentRequest assignmentRequest);

	// ResponseEntity<?> addQuestionInAssignment(AssignmentQuestionRequest
	// questionRequest);

	ResponseEntity<?> getAllAssignments();

	ResponseEntity<?> getAssignmentQuesById(Long questionId);

	ResponseEntity<?> submitAssignment(MultipartFile file, AssignmentSubmissionRequest readValue);

	ResponseEntity<?> getSubmitedAssignmetByStudentId(Integer studentId, Integer pageSise, Integer pageNumber,
			SubmissionStatus status);

	ResponseEntity<?> getAllSubmitedAssginments(Integer courseId, Integer subjectId, SubmissionStatus status,
			Integer pageSise, Integer pageNumber);

	ResponseEntity<?> updateSubmitedAssignmentStatus(Long submissionId, String status, String review);

	ResponseEntity<?> addQuestionInAssignment(String question, String videoUrl, List<MultipartFile> questionImages,
			Long assignmentId);

	ResponseEntity<?> deleteTaskQuestion(Long questionId);

	ResponseEntity<?> addAssignment(Long assignmentId, MultipartFile attachment);

	ResponseEntity<?> getAllSubmissionAssignmentTaskStatus();

	ResponseEntity<?> getOverAllAssignmentTaskStatus();

	ResponseEntity<?> getAllLockedAndUnlockedAssignment(Integer studentId);

	ResponseEntity<?> getAssignmentQuesSubmissionStatus(Long questionId, Integer studentId);

	ResponseEntity<?> getAllSubmissionAssignmentTaskStatusByCourseIdAndSubjectId(AssignmentFilter assignmentFilter);

	ResponseEntity<?> getSubmittedAssignmentBySubmissionId(Long submissionId);

	ResponseEntity<?> updateAssignmentQuestion(Long questionId, String question, String videoUrl,
			List<String> questionImages, List<MultipartFile> newImages);

	ResponseEntity<?> activateAssignment(Long id);

	ResponseEntity<?> getAllSubmittedAssignmentTask(Long assignmentId);

	ResponseEntity<?> deleteAttachment(Long assignmentId);

	ResponseEntity<?> addAttachment(Long assignmentId, MultipartFile file);

	ResponseEntity<?> isAssignmentTaskSubmittedByStudent(Long assignmentId);

	ResponseEntity<?> updateAssignment(AssignmentRequest assignmentRequest);

	ResponseEntity<?> getAllAssignments_new(AssignmentFilter assignmentFilter);

	ResponseEntity<?> changeAssignmentTaskStatus(Long id);

	ResponseEntity<?> activateAssigment(Long assignmentId);

	// .................. NEW METHODS ..............

	ResponseEntity<?> getAllLockedAndUnlockedAssignment(Integer studentId, Integer pageSise, Integer pageNumber);

	ResponseEntity<?> addQuestionInAssignment(AssignmentQuestionRequest assignmentQuestionRequest);

	ResponseEntity<?> updateAssignmentQuestion(UpdateAssignmentQuestionRequest updateAssignmentQuestionRequest);

	ResponseEntity<?> getCountOfAssignmentAndTask(Integer studentId);

}
