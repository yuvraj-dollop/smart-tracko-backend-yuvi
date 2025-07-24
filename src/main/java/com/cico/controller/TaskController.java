package com.cico.controller;

import java.util.List;

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
import org.springframework.web.multipart.MultipartFile;

import com.cico.model.AttachmentStatus;
import com.cico.payload.AddQuestionInTaskRequest;
import com.cico.payload.AddTaskQuestionAttachmentRequest;
import com.cico.payload.PaginationRequest;
import com.cico.payload.StudentTaskFilterRequest;
import com.cico.payload.TaskRequest;
import com.cico.payload.TaskSubmissionRequest;
import com.cico.payload.UpdateTaskQuestionRequest;
import com.cico.payload.UpdateTaskSubmissionStatusRequest;
import com.cico.service.ITaskService;
import com.cico.util.AppConstants;
import com.cico.util.SubmissionStatus;

@RequestMapping("/task")
@RestController
@CrossOrigin("*")
public class TaskController {

	@Autowired
	ITaskService taskService;

	@PostMapping("/createTask")
	private ResponseEntity<?> createTask(@RequestBody @Valid TaskRequest taskRequest) {
		return taskService.createTask(taskRequest);
	}

	@GetMapping("/getTaskById")
	public ResponseEntity<?> getTaskById(@RequestParam(name = AppConstants.TASK_ID) Long taskId) {
		return this.taskService.getTaskById(taskId);

	}

	@PostMapping("/getAllTaskOfStudent")
	public ResponseEntity<?> getAllTaskOfStudent(@RequestBody StudentTaskFilterRequest studentTaskFilterRequest) {
		return taskService.getAllTaskOfStudent(studentTaskFilterRequest);

	}

	@PostMapping("/getAllTasks")
	public ResponseEntity<?> getAllTasks(
			@RequestParam(name = AppConstants.COURSE_ID, required = false) Integer courseId,
			@RequestParam(name = AppConstants.SUBJECT_ID, required = false) Integer subjectId,
			@RequestParam("pageSize") Integer pageSise, @RequestParam("pageNumber") Integer pageNumber) {
		return taskService.getAllTasks(pageSise, pageNumber, courseId, subjectId);

	}

	// get all task of student
	@GetMapping("/getTaskQuestionsByTaskIdForStudent")
	public ResponseEntity<?> getTaskQuestionByTaskIdForStudent(@RequestParam(name = AppConstants.TASK_ID) Long taskId,
			@RequestParam("studentId") Integer studentId) {
		return taskService.getTaskQuestionsByTaskIdForStudent(taskId, studentId);
	}

	@PostMapping("/studentTaskSubmittion")
	public ResponseEntity<?> StudentTaskSubmittion(@RequestParam("taskId") Long taskId,
			@RequestParam("questionId") Long questionId, @RequestParam("studentId") Integer studentId,
			@RequestParam(name = "submittionFileName", required = false) MultipartFile file,
			@RequestParam("taskDescription") String taskDescription,
			@RequestParam(value = "codeSubmission", required = false) String codeSubmission) throws Exception {
		return taskService.studentTaskSubmittion(questionId, taskId, studentId, file, taskDescription, codeSubmission);

	}

	@PostMapping("/addQuestionInTask")
	public ResponseEntity<?> addQuestionInTask(@RequestParam("taskId") Long taskId,
			@RequestParam(value = "question", required = true) String question,
			@RequestParam(value = "videoUrl", required = false) String videoUrl,
			@RequestParam(value = "questionImages", required = false) List<MultipartFile> questionImages,
			@RequestParam(value = "attachment", required = false) MultipartFile attachment,
			@RequestParam(value = "status", required = false) AttachmentStatus status,
			@RequestParam(value = "title") String title,
			@RequestParam(value = "codeSubmisionStatus", required = false) Boolean codeSubmisionStatus) {
		return taskService.addQuestionInTask(title, question, videoUrl, questionImages, taskId, attachment, status,
				codeSubmisionStatus);
	}

	@PostMapping("/addTaskQuestionAttachment")
	public ResponseEntity<?> addTaskQuestionAttachment(@RequestParam("taskId") Long taskId,
			@RequestParam("questionId") Long questionId,
			@RequestParam(value = "attachment", required = false) MultipartFile attachment,
			@RequestParam("status") AttachmentStatus status) {
		return this.taskService.addTaskQuestionAttachment(taskId, attachment, questionId, status);
	}

	@DeleteMapping("/deleteTaskQuestion")
	public ResponseEntity<?> deleteTaskQuestions(@RequestParam(name = AppConstants.QUESTION_ID) Long questionId) {
		return taskService.deleteTaskQuestion(questionId);
	}

	@GetMapping("/getSubmittedTaskQuestionForStudent")
	public ResponseEntity<?> getSubmitedTaskForStudent(@RequestParam(name = AppConstants.STUDENT_ID) Integer studentId,
			@RequestParam("pageSize") Integer pageSise, @RequestParam("pageNumber") Integer pageNumber,
			@RequestParam(value = AppConstants.STATUS, required = false) SubmissionStatus status) {
		return taskService.getSubmittedTaskQuestionForStudent(studentId, pageNumber, pageSise, status);
	}

	@GetMapping("/getAllSubmitedTask")
	public ResponseEntity<?> getAllSubmitedTasks(
			@RequestParam(value = AppConstants.COURSE_ID, required = false) Integer courseId,
			@RequestParam(value = AppConstants.SUBJECT_ID, required = false) Integer subjectId,
			@RequestParam(value = AppConstants.STATUS, required = false) SubmissionStatus status,
			@RequestParam("pageSize") Integer pageSize, @RequestParam("pageNumber") Integer pageNumber) {

		return taskService.getAllSubmitedTasks(courseId, subjectId, status, pageNumber, pageSize);
	}

	@PutMapping("/updateSubmitedAssignmentStatus")
	public ResponseEntity<?> updateSubmitedAssignmentStatus(@RequestParam("submissionId") Long submissionId,
			@RequestParam("status") String status, @RequestParam("review") String review) {
		return taskService.updateSubmitedTaskStatus(submissionId, status, review);
	}

	@GetMapping("/getOverAllTaskStatusforBarChart")
	public ResponseEntity<?> getOverAllTaskStatusforBarChart() {
		return taskService.getOverAllTaskStatusforBarChart();
	}

	@GetMapping("/getSubmissionTaskById")
	public ResponseEntity<?> getSubmissionTaskById(@RequestParam(value = AppConstants.ID) Long id) {
		return taskService.getSubmissionTaskById(id);
	}

	// for admin use
	@GetMapping("/getTaskQuestion")
	public ResponseEntity<?> getTaskQuestion(@RequestParam(name = AppConstants.QUESTION_ID) long questionId,
			@RequestParam(name = AppConstants.TASK_ID) Long taskId) {
		return taskService.getTaskQuestion(questionId, taskId);
	}

	@GetMapping("/getAllSubmissionTaskStatusByCourseIdAndSubjectId")
	public ResponseEntity<?> getAllSubmissionTaskStatusByCourseIdAndSubjectId(
			@RequestParam(name = AppConstants.COURSE_ID) Integer courseId,
			@RequestParam(name = AppConstants.SUBJECT_ID) Integer subjectId, @RequestParam("pageSize") Integer pageSise,
			@RequestParam("pageNumber") Integer pageNumber) {
		return taskService.getAllSubmissionTaskStatusByCourseIdAndSubjectId(courseId, subjectId, pageNumber, pageSise);
	}

	@PutMapping("/updateTaskQuestion")
	public ResponseEntity<?> updateTaskQuestion(@RequestParam("questionId") Long questionId,
			@RequestParam("question") String question, @RequestParam("videoUrl") String videoUrl,
			@RequestParam(value = "questionImages", required = false) List<String> questionImages,
			@RequestParam(value = "newImages", required = false) List<MultipartFile> newImages,
			@RequestParam("taskId") Long taskId) {
		return taskService.updateTaskQuestion(questionId, question, videoUrl, questionImages, newImages, taskId);
	}

	@DeleteMapping("/deleteTaskQuestionAttachement")
	public ResponseEntity<?> deleteTaskQuestionAttachement(@RequestParam(name = AppConstants.TASK_ID) Long taskId,
			@RequestParam(name = AppConstants.QUESTION_ID) Long questionId) {
		return taskService.deleteTaskQuestionAttachement(taskId, questionId);
	}

	@PutMapping("/activateTask")
	public ResponseEntity<?> activateTask(@RequestParam(name = AppConstants.ID) Long id) {
		return taskService.activateTask(id);
	}

	// update task question status
	@PutMapping("/updateTaskQuestionStatus")
	public ResponseEntity<?> updateTaskQuestionStatus(@RequestParam(name = AppConstants.QUESTION_ID) Long questionId,
			@RequestParam(name = AppConstants.TASK_ID) Long taskId) {
		return taskService.updateTaskQuestionStatus(taskId, questionId);
	}

	@GetMapping("/getAllTaskSubmissionByTaskId")
	public ResponseEntity<?> getAllTaskSubmissionBYTaskId(@RequestParam(name = AppConstants.TASK_ID) Long taskId) {
		return taskService.getAllTaskSubmissionBYTaskId(taskId);
	}

	@GetMapping("/isTaskSubmittedByStudent")
	public ResponseEntity<?> isTaskSubmittedByStudent(@RequestParam(name = AppConstants.QUESTION_ID) Long questionId,
			@RequestParam(name = AppConstants.STUDENT_ID) Integer studentId) {
		return taskService.isTaskQuestionSubmittedByStudentId(questionId, studentId);
	}

	// check if task submission exists or not
	@GetMapping("/isTaskQuestionSubmited")
	public ResponseEntity<?> isTaskQuestionSubmited(@RequestParam(name = AppConstants.QUESTION_ID) Long questionId,
			@RequestParam(name = AppConstants.TASK_ID) Long taskId) {
		return taskService.isTaskQuestionSubmited(questionId, taskId);
	}

	// get all task question by task id
	@GetMapping("/getTaskQuestionsByTaskId")
	public ResponseEntity<?> getTaskQuestionsByTaskId(@RequestParam(name = AppConstants.TASK_ID) Long taskId,
			@RequestParam("pageSize") Integer pageSise, @RequestParam("pageNumber") Integer pageNumber) {
		return taskService.getTaskQuestionsByTaskId(taskId, pageSise, pageNumber);
	}

	@GetMapping("/getAllTaskQuestionWithSubmissionCount")
	public ResponseEntity<?> getAllTaskQuestionWithSubmissionCount(
			@RequestParam(name = AppConstants.TASK_ID) Long taskId) {
		return taskService.getAllTaskQuestionWithSubmissionCount(taskId);
	}

	// .................. NEW API'S ...........................

	@GetMapping("/v2/getSubmittedTaskQuestionForStudent")
	public ResponseEntity<?> getSubmitedTaskForStudentNew(
			@RequestParam(name = AppConstants.STUDENT_ID) Integer studentId,
			@RequestParam(name = AppConstants.PAGE_SIZE) Integer pageSise,
			@RequestParam(name = AppConstants.PAGE_NUMBER) Integer pageNumber,
			@RequestParam(value = AppConstants.STATUS, required = false) SubmissionStatus status) {
		return taskService.getSubmittedTaskQuestionForStudent(studentId, pageNumber, pageSise, status);
	}

	@PostMapping("/v2/getAllTaskOfStudent/{studentId}")
	public ResponseEntity<?> getAllTaskOfStudentNew(@PathVariable(name = AppConstants.STUDENT_ID) Integer studentId,
			@RequestParam(name = AppConstants.PAGE_SIZE) Integer pageSize,
			@RequestParam(name = AppConstants.PAGE_NUMBER) Integer pageNumber,
			@RequestParam(name = AppConstants.STATUS, required = false) String status) {
		StudentTaskFilterRequest studentTaskFilterRequest = StudentTaskFilterRequest.builder().studentId(studentId)
				.pageRequest(PaginationRequest.builder().pageSize(pageSize).pageNumber(pageNumber).build())
				.status(status).build();
		return taskService.getAllTaskOfStudent(studentTaskFilterRequest);

	}

	@PostMapping("/v2/studentTaskSubmittion")
	public ResponseEntity<?> StudentTaskSubmittion(@Valid TaskSubmissionRequest submissionRequest)
			throws Exception {
		return taskService.studentTaskSubmittion(submissionRequest);
	}

	@PostMapping("/v2/addQuestionInTask")
	public ResponseEntity<?> addQuestionInTask(@Valid @RequestBody AddQuestionInTaskRequest addQuestionInTaskRequest) {
		return taskService.addQuestionInTask(addQuestionInTaskRequest);
	}

	@PostMapping("/v2/addTaskQuestionAttachment")
	public ResponseEntity<?> addTaskQuestionAttachment(
			@Valid @RequestBody AddTaskQuestionAttachmentRequest attachmentRequest) {
		return this.taskService.addTaskQuestionAttachment(attachmentRequest);
	}

	@PutMapping("/v2/updateSubmitedAssignmentStatus")
	public ResponseEntity<?> updateSubmitedAssignmentStatus(
			@Valid @RequestBody UpdateTaskSubmissionStatusRequest statusRequest) {
		return taskService.updateSubmitedTaskStatus(statusRequest);
	}

	@PutMapping("/v2/updateTaskQuestion")
	public ResponseEntity<?> updateTaskQuestion(@Valid @RequestBody UpdateTaskQuestionRequest taskQuestionRequest) {
		return taskService.updateTaskQuestion(taskQuestionRequest);

	}

}
