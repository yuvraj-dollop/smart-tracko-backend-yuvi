package com.cico.controller;

import java.time.LocalDate;
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

import com.cico.exception.InvalidException;
import com.cico.payload.AssignmentFilter;
import com.cico.payload.AssignmentQuestionRequest;
import com.cico.payload.AssignmentRequest;
import com.cico.payload.AssignmentSubmissionRequest;
import com.cico.payload.PaginationRequest;
import com.cico.payload.UpdateAssignmentQuestionRequest;
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
	public ResponseEntity<?> addAssignment(@RequestParam(name = AppConstants.ASSIGNMENT_ID) Long assignmentId,
			@RequestParam(value = AppConstants.ATTACHMENET, required = false) MultipartFile attachment) {
		return this.service.addAssignment(assignmentId, attachment);
	}

	@GetMapping("/getAssignment")
	public ResponseEntity<?> getAssigment(@RequestParam(name = AppConstants.ASSIGNMENT_ID) Long id) {
		return service.getAssignment(id);
	}

	@GetMapping("/getAllAssignments")
	public ResponseEntity<?> getAllAssignments() {
		return service.getAllAssignments();
	}

	@PutMapping("/activateAssignment")
	public ResponseEntity<?> activateTask(@RequestParam(name = AppConstants.ID) Long id) {
		return service.activateAssignment(id);
	}

	// acitvate task by id
	@PutMapping("/changeAssignmentTaskStatus")
	public ResponseEntity<?> changeAssignmentTaskStatus(@RequestParam(name = AppConstants.ID) Long id) {
		return service.changeAssignmentTaskStatus(id);
	}

	// getAllAssignments_new
	@PostMapping("/getAllAssignments_new")
	public ResponseEntity<?> getAllAssignments_new(@Valid @RequestBody AssignmentFilter assignmentFilter) {
		return service.getAllAssignments_new(assignmentFilter);
	}

//	activateAssigment
	@PutMapping("/activateAssigment")
	public ResponseEntity<?> activateAssigment(@RequestParam(name = AppConstants.ID) Long assignmentId) {
		return service.activateAssigment(assignmentId);
	}

	// =============== QUESTION MANAGEMENT ===============
	@PostMapping("/addQuestionInAssignment")
	public ResponseEntity<?> addQuestionInAssignment(@Valid AssignmentQuestionRequest assignmentQuestionRequest,
			@RequestParam(value = "questionImages", required = false) List<MultipartFile> questionImages) {
		assignmentQuestionRequest.setQuestionImages(questionImages);
		return service.addQuestionInAssignment(assignmentQuestionRequest);
	}

	@GetMapping("/getAssignmentQuesById")
	public ResponseEntity<?> getAssignmentQuestion(@RequestParam(name = AppConstants.QUESTION_ID) Long questionId) {
		return service.getAssignmentQuesById(questionId);
	}

	@DeleteMapping("/deleteTaskQuestion")
	public ResponseEntity<?> deleteTaskQuestions(@RequestParam(name = AppConstants.QUESTION_ID) Long questionId) {
		return service.deleteTaskQuestion(questionId);
	}

//	@PutMapping("/updateAssignmentQuestion")
//	public ResponseEntity<?> updateAssignmentQuestion(@RequestParam("questionId") Long questionId,
//			@RequestParam("question") String question, @RequestParam("videoUrl") String videoUrl,
//			@RequestParam(value = "questionImages", required = false) List<String> questionImages,
//			@RequestParam(value = "newImages", required = false) List<MultipartFile> newImages) {
//		return service.updateAssignmentQuestion(questionId, question, videoUrl, questionImages, newImages);
//	}

	@PutMapping("/updateAssignmentQuestion")
	public ResponseEntity<?> updateAssignmentQuestion(
			@Valid UpdateAssignmentQuestionRequest updateAssignmentQuestionRequest,
			@RequestParam(value = "newImages", required = false) List<String> questionImages,
			@RequestParam(value = "newImages", required = false) List<MultipartFile> newImages) {
		updateAssignmentQuestionRequest.setQuestionImages(questionImages);
		updateAssignmentQuestionRequest.setNewImages(newImages);
		return service.updateAssignmentQuestion(updateAssignmentQuestionRequest);
	}

	// =============== SUBMISSION MANAGEMENT ===============
	@PostMapping("/submitAssignment")
	public ResponseEntity<?> submitAssignmentByStudent(@RequestParam(name = AppConstants.FILE) MultipartFile file,
			@RequestParam("assignmentSubmissionRequest") String assignmentSubmissionRequest) throws Exception {
		AssignmentSubmissionRequest readValue = objectMapper.readValue(assignmentSubmissionRequest,
				AssignmentSubmissionRequest.class);
		return service.submitAssignment(file, readValue);
	}

	@PutMapping("/updateSubmitedAssignmentStatus")
	public ResponseEntity<?> updateSubmitedAssignmentStatus(
			@RequestParam(name = AppConstants.SUBMISSION_ID) Long submissionId, @RequestParam("status") String status,
			@RequestParam("review") String review) {
		return service.updateSubmitedAssignmentStatus(submissionId, status, review);
	}

	// =============== STUDENT-SPECIFIC ENDPOINTS ===============
	@GetMapping("/getSubmitedAssignmetByStudentId")
	public ResponseEntity<?> getSubmitedAssignmetByStudentId(
			@RequestParam(name = AppConstants.STUDENT_ID) Integer studentId,
			@RequestParam(value = "pageSize") Integer pageSise, @RequestParam(value = "pageNumber") Integer pageNumber,
			@RequestParam(value = AppConstants.STATUS, defaultValue = "NOT_CHECKED_WITH_IT") SubmissionStatus status) {
		return service.getSubmitedAssignmetByStudentId(studentId, pageSise, pageNumber, status);
	}

	@GetMapping("/getAllLockedAndUnlockedAssignment")
	public ResponseEntity<?> getAllLockedAndUnlockedAssignment(
			@RequestParam(name = AppConstants.STUDENT_ID) Integer studentId) {
		return service.getAllLockedAndUnlockedAssignment(studentId);
	}

	@GetMapping("/getAssignmentQuesSubmissionStatus")
	public ResponseEntity<?> getAssignmentQuesSubmissionStatus(
			@RequestParam(name = AppConstants.QUESTION_ID) Long questionId,
			@RequestParam(name = AppConstants.STUDENT_ID) Integer studentId) {
		return service.getAssignmentQuesSubmissionStatus(questionId, studentId);
	}

	@GetMapping("/isAssignmentTaskSubmittedByStudent")
	public ResponseEntity<?> isAssignmentTaskSubmittedByStudent(
			@RequestParam(name = AppConstants.ASSIGNMENT_ID) Long assignmentId) {
		return service.isAssignmentTaskSubmittedByStudent(assignmentId);
	}

	// =============== ADMIN-SPECIFIC ENDPOINTS ===============
	@GetMapping("/getAllSubmitedAssginments")
	public ResponseEntity<?> getAllSubmitedAssginments(
			@RequestParam(value = "courseId", required = false) Integer courseId,
			@RequestParam(value = "subjectId", required = false) Integer subjectId,
			@RequestParam(value = AppConstants.STATUS, required = false) SubmissionStatus status,
			@RequestParam("pageSize") Integer pageSise, @RequestParam("pageNumber") Integer pageNumber) {
		return service.getAllSubmitedAssginments(courseId, subjectId, status, pageSise, pageNumber);
	}

	@PostMapping("/getAllSubmissionAssignmentTaskStatusByAssignmentId")
	public ResponseEntity<?> getAllSubmissionAssignmentTaskStatusByCourseId(
			@Valid @RequestBody AssignmentFilter assignmentFilter) {
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
	public ResponseEntity<?> getSubmittedAssignmentBySubmissionId(
			@RequestParam(name = AppConstants.SUBMISSION_ID) Long submissionId) {
		return service.getSubmittedAssignmentBySubmissionId(submissionId);
	}

	@GetMapping("/getAllSubmittedAssignmentTask")
	public ResponseEntity<?> getAllSubmittedAssignmentTask(
			@RequestParam(name = AppConstants.ASSIGNMENT_ID) Long assignmentId) {
		return service.getAllSubmittedAssignmentTask(assignmentId);
	}

	// update assignment
	@PutMapping("/updateAssignment")
	public ResponseEntity<?> updateAssignment(@RequestBody @Valid AssignmentRequest assignmentRequest) {
		return service.updateAssignment(assignmentRequest);
	}

	// =============== ATTACHMENT MANAGEMENT ===============
	@PostMapping("/addAttachment")
	public ResponseEntity<?> addAttachment(@RequestParam(name = AppConstants.ASSIGNMENT_ID) Long assignmentId,
			@RequestParam(value = AppConstants.FILE, required = false) MultipartFile file) {
		return service.addAttachment(assignmentId, file);
	}

	@DeleteMapping("/deleteAttachment")
	public ResponseEntity<?> deleteAttachment(@RequestParam(name = AppConstants.ASSIGNMENT_ID) Long assignmentId) {
		return service.deleteAttachment(assignmentId);
	}

	// ........................... NEW API's ........................

	// ............... GET METHOD .................
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

	@GetMapping("/v2/getAssignmentQuesSubmissionStatus")
	public ResponseEntity<?> getAssignmentQuesSubmissionStatusNew(
			@RequestParam(name = AppConstants.QUESTION_ID) Long questionId,
			@RequestParam(name = AppConstants.STUDENT_ID) Integer studentId) {
		return service.getAssignmentQuesSubmissionStatus(questionId, studentId);
	}

	@GetMapping("/v2/getAssignmentQuesById")
	public ResponseEntity<?> getAssignmentQuestionNew(@RequestParam(name = AppConstants.QUESTION_ID) Long questionId) {
		return service.getAssignmentQuesById(questionId);
	}

	// FOR STUDENT - RETURN QUESTION DETAILS, SUBMISSION DETAILS, STATUS AND ADMIN
	// RESPONSE FOR THE ASSIGNMENT ALSO
	@GetMapping("/v2/getAssignmentQuestionDetails")
	public ResponseEntity<?> getAssignmentQuestionDetails(
			@RequestParam(name = AppConstants.QUESTION_ID) Long questionId,
			@RequestParam(name = AppConstants.STUDENT_ID) Integer studentId) {
		return service.getAssignmentQuestionDetails(questionId, studentId);
	}

	// ............... POST METHOD ..................

	@PostMapping("/v2/submitAssignment")
	public ResponseEntity<?> submitAssignmentByStudentNew(@RequestParam(name = AppConstants.FILE) MultipartFile file,
			@Valid AssignmentSubmissionRequest assignmentSubmissionRequest) throws Exception {
		return service.submitAssignment(file, assignmentSubmissionRequest);
	}

	@GetMapping("v2/getOverAllAssignmentTaskStatus")
	public ResponseEntity<?> getOverAllAssignmentTaskStatusNew(@RequestParam("startDate") String startDate,
			@RequestParam("endDate") String endDate) {
		LocalDate startLocalDate = LocalDate.parse(startDate);
		LocalDate endLocalDate = LocalDate.parse(endDate); // <-- fixed (was parsing startDate earlier)

		if (endLocalDate.isBefore(startLocalDate)) {
			throw new InvalidException("End date cannot be before start date");
		}

		if (startLocalDate.isAfter(endLocalDate)) {
			throw new InvalidException("Start date cannot be after end date");
		}
		if (startLocalDate.isAfter(LocalDate.now()) || endLocalDate.isAfter(LocalDate.now())) {
			throw new InvalidException("Start date or end date cannot be in the future");
		}
		return service.getOverAllAssignmentTaskStatusNew(startLocalDate, endLocalDate);
	}

	// getAllAssignments_new
	@GetMapping("/v2/getAllAssignments_new")
	public ResponseEntity<?> getAllAssignments_newV2(@RequestParam(required = false) Integer courseId,
			@RequestParam(required = false) Integer subjectId, @RequestParam(required = false) Long assignmentId,
			@RequestParam(value = AppConstants.PAGE_SIZE, required = false, defaultValue = "0") int page,
			@RequestParam(value = AppConstants.PAGE_NUMBER, required = false, defaultValue = "10") int size) {
		AssignmentFilter filter = new AssignmentFilter();
		filter.setCourseId(courseId);
		filter.setSubjectId(subjectId);
		filter.setAssignmentId(assignmentId);
		filter.setPageRequest(PaginationRequest.builder().pageNumber(page).pageSize(size).build());

		return service.getAllAssignments_new(filter);
	}

	@GetMapping("/v2/getAllSubmitedAssginments")
	public ResponseEntity<?> getAllSubmitedAssginmentsnNew(
			@RequestParam(value = "courseId", required = false) Integer courseId,
			@RequestParam(value = "subjectId", required = false) Integer subjectId,
			@RequestParam(value = AppConstants.STATUS, required = false) SubmissionStatus status,
			@RequestParam("pageSize") Integer pageSise, @RequestParam("pageNumber") Integer pageNumber) {
		return service.getAllSubmitedAssginments(courseId, subjectId, status, pageSise, pageNumber);
	}

	@PostMapping("/v2/getAllSubmissionAssignmentTaskStatusByAssignmentId")
	public ResponseEntity<?> getAllSubmissionAssignmentTaskStatusByCourseIdNew(
			@Valid @RequestBody AssignmentFilter assignmentFilter) {
		return service.getAllSubmissionAssignmentTaskStatusByCourseIdAndSubjectId(assignmentFilter);
	}

	@PutMapping("/v2/changeAssignmentTaskStatus")
	public ResponseEntity<?> changeAssignmentTaskStatusNew(@RequestParam(name = AppConstants.ID) Long id) {
		return service.changeAssignmentTaskStatus(id);
	}

	@PutMapping("/v2/activateAssigment")
	public ResponseEntity<?> activateAssigmentNew(@RequestParam(name = AppConstants.ID) Long assignmentId) {
		return service.activateAssigment(assignmentId);
	}

	@PostMapping("/v2/createAssignment")
	public ResponseEntity<?> createAssignmentNew(@RequestBody @Valid AssignmentRequest assignmentRequest) {
		return service.createAssignment(assignmentRequest);
	}

	@GetMapping("/v2/isAssignmentTaskSubmittedByStudent")
	public ResponseEntity<?> isAssignmentTaskSubmittedByStudentNew(
			@RequestParam(name = AppConstants.ASSIGNMENT_ID) Long assignmentId) {
		return service.isAssignmentTaskSubmittedByStudent(assignmentId);
	}

	@PostMapping("/v2/addQuestionInAssignment")
	public ResponseEntity<?> addQuestionInAssignmentNew(@Valid AssignmentQuestionRequest assignmentQuestionRequest,
			@RequestParam(value = "questionImages", required = false) List<MultipartFile> questionImages) {
		assignmentQuestionRequest.setQuestionImages(questionImages);
		return service.addQuestionInAssignment(assignmentQuestionRequest);
	}

	@PostMapping("/v2/addAttachment")
	public ResponseEntity<?> addAttachmentNew(@RequestParam(name = AppConstants.ASSIGNMENT_ID) Long assignmentId,
			@RequestParam(value = AppConstants.FILE, required = false) MultipartFile file) {
		return service.addAttachment(assignmentId, file);
	}

	@PutMapping("/v2/updateAssignmentQuestion")
	public ResponseEntity<?> updateAssignmentQuestionNew(
			@Valid UpdateAssignmentQuestionRequest updateAssignmentQuestionRequest,
			@RequestParam(value = "newImages", required = false) List<String> questionImages,
			@RequestParam(value = "newImages", required = false) List<MultipartFile> newImages) {
		updateAssignmentQuestionRequest.setQuestionImages(questionImages);
		updateAssignmentQuestionRequest.setNewImages(newImages);
		return service.updateAssignmentQuestion(updateAssignmentQuestionRequest);
	}

	// update assignment
	@PutMapping("/v2/updateAssignment")
	public ResponseEntity<?> updateAssignmentNew(@RequestBody @Valid AssignmentRequest assignmentRequest) {
		return service.updateAssignment(assignmentRequest);
	}

	@GetMapping("/v2/getOverAllAssignmentTask")
	public ResponseEntity<?> getOverAllAssignmentTask() {
		return service.getOverAllAssignmentTaskStatus();
	}

	@DeleteMapping("/v2/deleteTaskQuestion")
	public ResponseEntity<?> deleteTaskQuestionsNew(@RequestParam(name = AppConstants.QUESTION_ID) Long questionId) {
		return service.deleteTaskQuestion(questionId);
	}

	@DeleteMapping("/v2/deleteAttachment")
	public ResponseEntity<?> deleteAttachmentNew(@RequestParam(name = AppConstants.ASSIGNMENT_ID) Long assignmentId) {
		return service.deleteAttachment(assignmentId);
	}

}