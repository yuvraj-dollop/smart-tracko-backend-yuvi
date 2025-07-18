package com.cico.controller;

import java.util.List;

import javax.validation.Valid;

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
import org.springframework.web.multipart.MultipartFile;

import com.cico.payload.AssignmentFilter;
import com.cico.payload.AssignmentRequest;
import com.cico.payload.AssignmentSubmissionRequest;
import com.cico.service.IAssignmentService;
import com.cico.util.AppConstants;
import com.cico.util.SubmissionStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/assignment")
@CrossOrigin("*")
public class AssigmentController {

	@Autowired
	private IAssignmentService service;

	@Autowired
	private ObjectMapper objectMapper;

	// =============== ASSIGNMENT MANAGEMENT ===============
	@PostMapping("/createAssignment")
	public ResponseEntity<?> createAssignment(@RequestBody @Valid AssignmentRequest assignmentRequest) {
		return service.createAssignment(assignmentRequest);
	}

	@PostMapping("/addAssignment")
	public ResponseEntity<?> addAssignment(@RequestParam("assignmentId") Long assignmentId,
			@RequestParam(value = "attachment", required = false) MultipartFile attachment) {
		return this.service.addAssignment(assignmentId, attachment);
	}

	@GetMapping("/getAssignment")
	public ResponseEntity<?> getAssigment(@RequestParam("assignmentId") Long id) {
		return service.getAssignment(id);
	}

	@GetMapping("/getAllAssignments")
	public ResponseEntity<?> getAllAssignments() {
		return service.getAllAssignments();
	}

	@PutMapping("/activateAssignment")
	public ResponseEntity<?> activateTask(@RequestParam("id") Long id) {
		return service.activateAssignment(id);
	}

	// acitvate task by id
	@PutMapping("/changeAssignmentTaskStatus")
	public ResponseEntity<?> changeAssignmentTaskStatus(@RequestParam("id") Long id) {
		return service.changeAssignmentTaskStatus(id);
	}

	// getAllAssignments_new
	@PostMapping("/getAllAssignments_new")
	public ResponseEntity<?> getAllAssignments_new(@RequestBody AssignmentFilter assignmentFilter) {
		return service.getAllAssignments_new(assignmentFilter);
	}

//	activateAssigment

	@PutMapping("/activateAssigment")
	public ResponseEntity<?> activateAssigment(@RequestParam("assignmentId") Long assignmentId) {
		return service.activateAssigment(assignmentId);
	}

	// =============== QUESTION MANAGEMENT ===============
	@PostMapping("/addQuestionInAssignment")
	public ResponseEntity<?> addQuestionInAssignment(@RequestParam("assignmentId") Long assignmentId,
			@RequestParam("question") String question, @RequestParam("videoUrl") String videoUrl,
			@RequestParam(value = "questionImages", required = false) List<MultipartFile> questionImages) {
		return service.addQuestionInAssignment(question, videoUrl, questionImages, assignmentId);
	}

	@GetMapping("/getAssignmentQuesById")
	public ResponseEntity<?> getAssignmentQuestion(@RequestParam("questionId") Long questionId) {
		return service.getAssignmentQuesById(questionId);
	}

	@DeleteMapping("/deleteTaskQuestion")
	public ResponseEntity<?> deleteTaskQuestions(@RequestParam("questionId") Long questionId) {
		return service.deleteTaskQuestion(questionId);
	}

	@PutMapping("/updateAssignmentQuestion")
	public ResponseEntity<?> updateAssignmentQuestion(@RequestParam("questionId") Long questionId,
			@RequestParam("question") String question, @RequestParam("videoUrl") String videoUrl,
			@RequestParam(value = "questionImages", required = false) List<String> questionImages,
			@RequestParam(value = "newImages", required = false) List<MultipartFile> newImages) {
		return service.updateAssignmentQuestion(questionId, question, videoUrl, questionImages, newImages);
	}

	// =============== SUBMISSION MANAGEMENT ===============
	@PostMapping("/submitAssignment")
	public ResponseEntity<?> submitAssignmentByStudent(@RequestParam("file") MultipartFile file,
			@RequestParam("assignmentSubmissionRequest") String assignmentSubmissionRequest) throws Exception {
		AssignmentSubmissionRequest readValue = objectMapper.readValue(assignmentSubmissionRequest,
				AssignmentSubmissionRequest.class);
		return service.submitAssignment(file, readValue);
	}

	@PutMapping("/updateSubmitedAssignmentStatus")
	public ResponseEntity<?> updateSubmitedAssignmentStatus(@RequestParam("submissionId") Long submissionId,
			@RequestParam("status") String status, @RequestParam("review") String review) {
		return service.updateSubmitedAssignmentStatus(submissionId, status, review);
	}

	// =============== STUDENT-SPECIFIC ENDPOINTS ===============
	@GetMapping("/getSubmitedAssignmetByStudentId")
	public ResponseEntity<?> getSubmitedAssignmetByStudentId(@RequestParam("studentId") Integer studentId,
			@RequestParam(value = "pageSize") Integer pageSise, @RequestParam(value = "pageNumber") Integer pageNumber,
			@RequestParam(value = "status", defaultValue = "NOT_CHECKED_WITH_IT") SubmissionStatus status) {
		return service.getSubmitedAssignmetByStudentId(studentId, pageSise, pageNumber, status);
	}

	@GetMapping("/getAllLockedAndUnlockedAssignment")
	public ResponseEntity<?> getAllLockedAndUnlockedAssignment(@RequestParam("studentId") Integer studentId) {
		return service.getAllLockedAndUnlockedAssignment(studentId);
	}

	@GetMapping("/getAssignmentQuesSubmissionStatus")
	public ResponseEntity<?> getAssignmentQuesSubmissionStatus(@RequestParam("questionId") Long questionId,
			@RequestParam("studentId") Integer studentId) {
		return service.getAssignmentQuesSubmissionStatus(questionId, studentId);
	}

	@GetMapping("/isAssignmentTaskSubmittedByStudent")
	public ResponseEntity<?> isAssignmentTaskSubmittedByStudent(@RequestParam("assignmentId") Long assignmentId) {
		return service.isAssignmentTaskSubmittedByStudent(assignmentId);
	}

	// =============== ADMIN-SPECIFIC ENDPOINTS ===============
	@GetMapping("/getAllSubmitedAssginments")
	public ResponseEntity<?> getAllSubmitedAssginments(
			@RequestParam(value = "courseId", required = false) Integer courseId,
			@RequestParam(value = "subjectId", required = false) Integer subjectId,
			@RequestParam(value = "status", required = false) SubmissionStatus status,
			@RequestParam("pageSize") Integer pageSise, @RequestParam("pageNumber") Integer pageNumber) {
		return service.getAllSubmitedAssginments(courseId, subjectId, status, pageSise, pageNumber);
	}

	@PostMapping("/getAllSubmissionAssignmentTaskStatusByAssignmentId")
	public ResponseEntity<?> getAllSubmissionAssignmentTaskStatusByCourseId(
			@RequestBody AssignmentFilter assignmentFilter) {
		return service.getAllSubmissionAssignmentTaskStatusByCourseIdAndSubjectId(assignmentFilter);
	}

	// =============== REPORTING/STATUS ENDPOINTS ===============
	@GetMapping("/getAllSubmissionAssignmentTaskStatus")
	public ResponseEntity<?> getAllSubmissionAssignmentTaskStatus() {
		return service.getAllSubmissionAssignmentTaskStatus();
	}

	@GetMapping("/getOverAllAssignmentTaskStatus")
	public ResponseEntity<?> getOverAllAssignmentTaskStatus() {
		return service.getOverAllAssignmentTaskStatus();
	}

	@GetMapping("/getSubmittedAssignmentBySubmissionId")
	public ResponseEntity<?> getSubmittedAssignmentBySubmissionId(@RequestParam("submissionId") Long submissionId) {
		return service.getSubmittedAssignmentBySubmissionId(submissionId);
	}

	@GetMapping("/getAllSubmittedAssignmentTask")
	public ResponseEntity<?> getAllSubmittedAssignmentTask(@RequestParam("assignmentId") Long assignmentId) {
		return service.getAllSubmittedAssignmentTask(assignmentId);
	}

	// update assignment
	@PutMapping("/updateAssignment")
	public ResponseEntity<?> updateAssignment(@RequestBody @Valid AssignmentRequest assignmentRequest) {
		return service.updateAssignment(assignmentRequest);
	}

	// =============== ATTACHMENT MANAGEMENT ===============
	@PostMapping("/addAttachment")
	public ResponseEntity<?> addAttachment(@RequestParam("assignmentId") Long assignmentId,
			@RequestParam(value = "file", required = false) MultipartFile file) {
		return service.addAttachment(assignmentId, file);
	}

	@DeleteMapping("/deleteAttachment")
	public ResponseEntity<?> deleteAttachment(@RequestParam("assignmentId") Long assignmentId) {
		return service.deleteAttachment(assignmentId);
	}
	
	// ........................... NEW API's ........................

		@GetMapping("/v2/getSubmitedAssignmetByStudentId")
		public ResponseEntity<?> getSubmitedAssignmetByStudentIdNew(
				@RequestParam(name = AppConstants.STUDENT_ID) Integer studentId,
				@RequestParam(value = AppConstants.PAGE_SIZE) Integer pageSize,
				@RequestParam(value = AppConstants.PAGE_NUMBER) Integer pageNumber,
				@RequestParam(value = AppConstants.STATUS, defaultValue = "NOT_CHECKED_WITH_IT") SubmissionStatus status) {
			return service.getSubmitedAssignmetByStudentId(studentId, pageSize, pageNumber, status);
		}

		@GetMapping("/v2/getAllLockedAndUnlockedAssignment")
		public ResponseEntity<?> getAllLockedAndUnlockedAssignmentNew(
				@RequestParam(name = AppConstants.STUDENT_ID) Integer studentId,
				@RequestParam(value = AppConstants.PAGE_SIZE) Integer pageSize,
				@RequestParam(value = AppConstants.PAGE_NUMBER) Integer pageNumber) {
			return service.getAllLockedAndUnlockedAssignment(studentId, pageSize, pageNumber);
		}
}