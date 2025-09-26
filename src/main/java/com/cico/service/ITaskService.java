package com.cico.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.cico.model.AttachmentStatus;
import com.cico.model.Task;
import com.cico.payload.AddQuestionInTaskRequest;
import com.cico.payload.AddTaskQuestionAttachmentRequest;
import com.cico.payload.StudentTaskFilterRequest;
import com.cico.payload.TaskFilterRequest;
import com.cico.payload.TaskRequest;
import com.cico.payload.TaskRequestNew;
import com.cico.payload.TaskSubmissionRequest;
import com.cico.payload.UpdateTaskQuestionRequest;
import com.cico.payload.UpdateTaskSubmissionStatusRequest;
import com.cico.util.SubmissionStatus;

public interface ITaskService {

	ResponseEntity<?> createTask(TaskRequest taskRequest);

	List<Task> getFilteredTasks(TaskFilterRequest taskFilter);

	ResponseEntity<?> getTaskById(Long taskId);

	ResponseEntity<?> studentTaskSubmittion(Long questionId, Long taskId, Integer studentId, MultipartFile file,
			String taskDescription, String codeSubmission);

	ResponseEntity<?> addQuestionInTask(String title, String question, String videoUrl,
			List<MultipartFile> questionImages, Long taskId, MultipartFile attachment, AttachmentStatus status,
			Boolean codeSubmisionStatus);

//	ResponseEntity<?> addTaskAttachment(Long taskId, MultipartFile attachment);

	ResponseEntity<?> deleteTaskQuestion(Long questionId);

	ResponseEntity<?> getAllSubmitedTasks(Integer courseId, Integer subjectId, SubmissionStatus status,
			Integer pageNumber, Integer pageSise);

	ResponseEntity<?> getSubmittedTaskQuestionForStudent(Integer studentId, Integer pageNumber, Integer pageSise,
			SubmissionStatus status);

	ResponseEntity<?> updateSubmitedTaskStatus(Long submissionId, String status, String review);

	ResponseEntity<?> getOverAllTaskStatusforBarChart();

	ResponseEntity<?> getAllTaskOfStudent(StudentTaskFilterRequest studentTaskFilterRequest);

	ResponseEntity<?> isTaskSubmitted(Long taskId, Integer studentId);

	ResponseEntity<?> getSubmissionTaskById(Long id);

	ResponseEntity<?> getTaskQuestion(Long questionId, Long taskId);

	ResponseEntity<?> updateTaskQuestion(Long questionId, String question, String videoUrl, List<String> questionImages,
			List<MultipartFile> newImages, Long taskId);

	ResponseEntity<?> getAllSubmissionTaskStatusByCourseIdAndSubjectId(Integer courseId, Integer subjectId,
			Integer pageNumber, Integer pageSize);

	ResponseEntity<?> deleteTaskQuestionAttachement(Long taskId, Long questionId);

	ResponseEntity<?> activateTask(Long id);

	ResponseEntity<?> getAllTaskSubmissionBYTaskId(Long taskId);

	ResponseEntity<?> isTaskSubmitted(Long taskId);

	ResponseEntity<?> isTaskQuestionSubmittedByStudentId(Long questionId, Integer studentId);

	ResponseEntity<?> getTaskQuestionsByTaskIdForStudent(Long taskId, Integer studentId);

	ResponseEntity<?> getTaskQuestionsByTaskId(Long taskId, Integer pageSise, Integer pageNumber);

	ResponseEntity<?> updateTaskQuestionStatus(Long taskId, Long questionId);

	ResponseEntity<?> isTaskQuestionSubmited(Long questionId, Long taskId);

	ResponseEntity<?> addTaskQuestionAttachment(Long taskId, MultipartFile attachment, Long questionId,
			AttachmentStatus status);

	ResponseEntity<?> getAllTasks(Integer pageSise, Integer pageNumber, Integer courseId, Integer subjectId);

	ResponseEntity<?> getAllTaskQuestionWithSubmissionCount(Long taskId);

	// .........................NEW METHOD'S ...............................

	ResponseEntity<?> studentTaskSubmittion(TaskSubmissionRequest submissionRequest);

	ResponseEntity<?> addQuestionInTask(AddQuestionInTaskRequest addQuestionInTaskRequest);

	ResponseEntity<?> addTaskQuestionAttachment(AddTaskQuestionAttachmentRequest attachmentRequest);

	ResponseEntity<?> updateSubmitedTaskStatus(UpdateTaskSubmissionStatusRequest statusRequest);

	ResponseEntity<?> updateTaskQuestion(UpdateTaskQuestionRequest taskQuestionRequest);

	public Long countSubmittedTasksByStudentId(Integer studentId);

	public Long countTaskOfStudent(Integer studentId);

	ResponseEntity<?> createTaskNew(TaskRequestNew taskRequest);
}
