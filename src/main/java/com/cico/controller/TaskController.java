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

import com.cico.model.AttachmentStatus;
import com.cico.payload.StudentTaskFilterRequest;
import com.cico.payload.TaskRequest;
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
	public ResponseEntity<?> getTaskById(@RequestParam("taskId") Long taskId) {
		return this.taskService.getTaskById(taskId);

	}

	@PostMapping("/getAllTaskOfStudent")
	public ResponseEntity<?> getAllTaskOfStudent(@RequestBody StudentTaskFilterRequest studentTaskFilterRequest) {
		return taskService.getAllTaskOfStudent(studentTaskFilterRequest);

	}

	@PostMapping("/getAllTasks")
	public ResponseEntity<?> getAllTasks(@RequestParam(name = "courseId", required = false) Integer courseId,
			@RequestParam(name = "subjectId", required = false) Integer subjectId,
			@RequestParam("pageSize") Integer pageSise, @RequestParam("pageNumber") Integer pageNumber) {
		return taskService.getAllTasks(pageSise, pageNumber, courseId, subjectId);

	}

	// get all task of student
	@GetMapping("/getTaskQuestionsByTaskIdForStudent")
	public ResponseEntity<?> getTaskQuestionByTaskIdForStudent(@RequestParam("taskId") Long taskId,
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
	public ResponseEntity<?> deleteTaskQuestions(@RequestParam("questionId") Long questionId) {
		return taskService.deleteTaskQuestion(questionId);
	}

	@GetMapping("/getSubmittedTaskQuestionForStudent")
	public ResponseEntity<?> getSubmitedTaskForStudent(@RequestParam("studentId") Integer studentId,
			@RequestParam("pageSize") Integer pageSise, @RequestParam("pageNumber") Integer pageNumber,
			@RequestParam(value = "status", required = false) SubmissionStatus status) {
		return taskService.getSubmittedTaskQuestionForStudent(studentId, pageNumber, pageSise, status);
	}

	@GetMapping("/getAllSubmitedTask")
	public ResponseEntity<?> getAllSubmitedTasks(@RequestParam(value = "courseId", required = false) Integer courseId,
			@RequestParam(value = "subjectId", required = false) Integer subjectId,
			@RequestParam(value = "status", required = false) SubmissionStatus status,
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
	public ResponseEntity<?> getSubmissionTaskById(@RequestParam("id") Long id) {
		return taskService.getSubmissionTaskById(id);
	}

	// for admin use
	@GetMapping("/getTaskQuestion")
	public ResponseEntity<?> getTaskQuestion(@RequestParam("questionId") long questionId,
			@RequestParam("taskId") Long taskId) {
		return taskService.getTaskQuestion(questionId, taskId);
	}

	@GetMapping("/getAllSubmissionTaskStatusByCourseIdAndSubjectId")
	public ResponseEntity<?> getAllSubmissionTaskStatusByCourseIdAndSubjectId(
			@RequestParam("courseId") Integer courseId, @RequestParam("subjectId") Integer subjectId,
			@RequestParam("pageSize") Integer pageSise, @RequestParam("pageNumber") Integer pageNumber) {
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
	public ResponseEntity<?> deleteTaskQuestionAttachement(@RequestParam("taskId") Long taskId,
			@RequestParam("questionId") Long questionId) {
		return taskService.deleteTaskQuestionAttachement(taskId, questionId);
	}

	@PutMapping("/activateTask")
	public ResponseEntity<?> activateTask(@RequestParam("id") Long id) {
		return taskService.activateTask(id);
	}

	// update task question status
	@PutMapping("/updateTaskQuestionStatus")
	public ResponseEntity<?> updateTaskQuestionStatus(@RequestParam("questionId") Long questionId,
			@RequestParam("taskId") Long taskId) {
		return taskService.updateTaskQuestionStatus(taskId, questionId);
	}

	@GetMapping("/getAllTaskSubmissionByTaskId")
	public ResponseEntity<?> getAllTaskSubmissionBYTaskId(@RequestParam("taskId") Long taskId) {
		return taskService.getAllTaskSubmissionBYTaskId(taskId);
	}

	@GetMapping("/isTaskSubmittedByStudent")
	public ResponseEntity<?> isTaskSubmittedByStudent(@RequestParam("questionId") Long questionId,
			@RequestParam("studentId") Integer studentId) {
		return taskService.isTaskQuestionSubmittedByStudentId(questionId, studentId);
	}

	// check if task submission exists or not
	@GetMapping("/isTaskQuestionSubmited")
	public ResponseEntity<?> isTaskQuestionSubmited(@RequestParam("questionId") Long questionId,
			@RequestParam("taskId") Long taskId) {
		return taskService.isTaskQuestionSubmited(questionId, taskId);
	}

	// get all task question by task id
	@GetMapping("/getTaskQuestionsByTaskId")
	public ResponseEntity<?> getTaskQuestionsByTaskId(@RequestParam("taskId") Long taskId,
			@RequestParam("pageSize") Integer pageSise, @RequestParam("pageNumber") Integer pageNumber) {
		return taskService.getTaskQuestionsByTaskId(taskId, pageSise, pageNumber);
	}

	@GetMapping("/getAllTaskQuestionWithSubmissionCount")
	public ResponseEntity<?> getAllTaskQuestionWithSubmissionCount(@RequestParam("taskId") Long taskId) {
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

		@PostMapping("/v2/getAllTaskOfStudent")
		public ResponseEntity<?> getAllTaskOfStudentNew(
				@RequestBody @Valid StudentTaskFilterRequest studentTaskFilterRequest) {
			return taskService.getAllTaskOfStudent(studentTaskFilterRequest);

		}

//		@PostMapping("/v2/studentTaskSubmittion")
//		public ResponseEntity<?> StudentTaskSubmittion(@Valid @RequestBody TaskSubmissionRequest submissionRequest)
//				throws Exception {
//			return taskService.studentTaskSubmittion(submissionRequest);
//		}
}
