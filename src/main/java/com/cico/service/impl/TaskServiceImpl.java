package com.cico.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cico.exception.ResourceAlreadyExistException;
import com.cico.exception.ResourceNotFoundException;
import com.cico.kafkaServices.KafkaProducerService;
import com.cico.model.AttachmentStatus;
import com.cico.model.Course;
import com.cico.model.Subject;
import com.cico.model.Task;
import com.cico.model.TaskQuestion;
import com.cico.model.TaskSubmission;
import com.cico.payload.AssignmentAndTaskSubmission;
import com.cico.payload.AssignmentSubmissionResponse;
import com.cico.payload.CourseResponse;
import com.cico.payload.StudentTaskFilterRequest;
import com.cico.payload.SubjectResponse;
import com.cico.payload.TaskFilterRequest;
import com.cico.payload.TaskQuestionResponse;
import com.cico.payload.TaskQuestionSubmissionResponse;
import com.cico.payload.TaskRequest;
import com.cico.payload.TaskResponse;
import com.cico.payload.TaskStatusSummary;
import com.cico.payload.TaskSubmissionResponse;
import com.cico.repository.CourseRepository;
import com.cico.repository.StudentRepository;
import com.cico.repository.SubjectRepository;
import com.cico.repository.TaskQuestionRepository;
import com.cico.repository.TaskRepo;
import com.cico.repository.TaskSubmissionRepository;
import com.cico.service.ITaskService;
import com.cico.util.AppConstants;
import com.cico.util.SubmissionStatus;

@Service
public class TaskServiceImpl implements ITaskService {

	@Autowired
	TaskRepo taskRepo;

	@Autowired
	FileServiceImpl fileService;

	@Autowired
	CourseServiceImpl courseService;

	@Autowired
	SubjectServiceImpl subjectService;

	@Autowired
	SubjectRepository subjectRepo;

	@Autowired
	private TaskQuestionRepository taskQuestionRepository;
	@Autowired
	private StudentRepository studentRepository;

	@Autowired
	private TaskSubmissionRepository taskSubmissionRepository;
	@Autowired
	private CourseRepository courseRepository;

	@Autowired
	private KafkaProducerService kafkaProducerService;

	@Override
	public ResponseEntity<?> createTask(TaskRequest taskRequest) {
		if (taskRepo.findByTaskName(taskRequest.getTaskName().trim()) != null)
			throw new ResourceAlreadyExistException("Task already exist");

		Map<String, Object> response = new HashMap<>();
		Task task = new Task();
		task.setCourse(taskRequest.getCourse());
		task.setSubject(taskRequest.getSubject());
		task.setTaskName(taskRequest.getTaskName().trim());
		task.setCreatedDate(LocalDateTime.now());
		task.setUpdatedDate(LocalDateTime.now());
		Task newTask = taskRepo.save(task);
		response.put(AppConstants.MESSAGE, AppConstants.CREATE_SUCCESS);
		response.put("taskId", newTask.getTaskId());

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Override
	public List<Task> getFilteredTasks(TaskFilterRequest taskFilter) {
		Example<Task> example = null;

		Course course = courseRepository.findByCourseIdAndIsDeleted(taskFilter.getCourseId(), false);
		Subject subject = subjectRepo.findById(taskFilter.getSubjectId()).get();

		Task task = new Task();
		task.setCourse(course);
		task.setSubject(subject);
		task.setIsDeleted(taskFilter.getStatus());
		example = Example.of(task);
		taskRepo.findAll(example);
		return null;

	}

	@Override
	public ResponseEntity<?> getTaskById(Long taskId) {
		Task task = taskRepo.findByTaskIdAndIsDeletedFalse(taskId)
				.orElseThrow(() -> new ResourceNotFoundException("TASK NOT FOUND WITH THIS ID"));
		Map<String, Object> response = new HashMap<>();

		response.put(AppConstants.MESSAGE, AppConstants.DATA_FOUND);
		response.put("task", taskReponseFilter(task));
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	public TaskQuestionResponse taskquestionResponseFilter(TaskQuestion obj) {

		return TaskQuestionResponse.builder().isActive(obj.getIsActive()).questionId(obj.getQuestionId())
				.question(obj.getQuestion()).videoUrl(obj.getVideoUrl()).taskAttachment(obj.getTaskAttachment())
				.attachmentStatus(obj.getAttachmentStatus()).title(obj.getTitle())
				.codeSubmisionStatus(obj.getCodeSubmisionStatus()).questionImages(obj.getQuestionImages()).build();

	}

	public TaskResponse taskReponseFilter(Task task) {

		TaskResponse res = new TaskResponse();
		CourseResponse cr = new CourseResponse();
		SubjectResponse sr = new SubjectResponse();

		res.setTaskId(task.getTaskId());
		res.setTaskName(task.getTaskName());
		res.setTaskQuestion(task.getTaskQuestion().parallelStream().filter(obj -> !obj.getIsDeleted())
				.map(this::taskquestionResponseFilter).collect(Collectors.toList()));

//		res.setAttachmentStatus(task.getAttachmentStatus());
//		res.setTaskAttachment(task.getTaskAttachment());
		cr.setCourseId(task.getCourse().getCourseId());
		cr.setCourseName(task.getCourse().getCourseName());

		sr.setSubjectId(task.getSubject().getSubjectId());
		sr.setSubjectName(task.getSubject().getSubjectName());
		res.setCreatedDate(task.getCreatedDate());

		res.setSubject(sr);
		res.setCourse(cr);
		return res;
	}

	@Override
	public ResponseEntity<?> studentTaskSubmittion(Long questionId, Long taskId, Integer studentId, MultipartFile file,
			String taskDescription, String codeSubmission) {

		// check task is present or not

		Task task = taskRepo.findByTaskIdAndIsDeleted(taskId, false)
				.orElseThrow(() -> new ResourceNotFoundException("Task not found with this ID"));

		// check question exist in task or not
		TaskQuestion taskQuestion = taskQuestionRepository.findById(questionId)
				.orElseThrow(() -> new ResourceNotFoundException("Task question not found with this ID"));

		// check student exist or not
		if (!studentRepository.existsById(studentId)) {
			throw new ResourceNotFoundException("Student not found with this ID");
		}

		Map<String, Object> response = new HashMap<>();

		// check submission already exists
		Optional<TaskSubmission> obj = taskSubmissionRepository.findByQuestionIdAndStudentId(questionId, studentId);
		if (obj.isEmpty()) {
			TaskSubmission submittion = new TaskSubmission();
			submittion.setStudent(studentRepository.findByStudentId(studentId));

			if (taskQuestion.getAttachmentStatus().equals(AttachmentStatus.REQUIRED)) {
				if (Objects.isNull(file) || file.isEmpty()) {
					throw new ResourceNotFoundException("File is required for this task question");
				} else {
					String f = fileService.uploadFileInFolder(file, "zip");
					submittion.setSubmittionFileName(f);
				}
			} else if (taskQuestion.getAttachmentStatus().equals(AttachmentStatus.OPTIONAL)) {
				if (Objects.nonNull(file) && !file.isEmpty()) {
					String f = fileService.uploadFileInFolder(file, "zip");
					submittion.setSubmittionFileName(f);
				}
			} else {
				submittion.setSubmittionFileName(null);
			}

			submittion.setStatus(SubmissionStatus.Unreviewed);
			submittion.setSubmissionDate(LocalDateTime.now());
			submittion.setTaskDescription(taskDescription);
			submittion.setTask(task); // find task question by questionId
			submittion.setQuestion(taskQuestion);

			if (taskQuestion.getCodeSubmisionStatus() != null && taskQuestion.getCodeSubmisionStatus()) {
				if (Objects.isNull(codeSubmission) || codeSubmission.isEmpty()) {
					throw new ResourceNotFoundException("Code submission is required for this task question");
				}
			} else {
				if (Objects.nonNull(codeSubmission) && !codeSubmission.isEmpty()) {
					throw new ResourceAlreadyExistException("Code submission is not allowed for this task question");
				}
			}

			submittion.setCodeSubmission(codeSubmission);
			TaskSubmission taskSubmission = taskSubmissionRepository.save(submittion);

			response.put(AppConstants.MESSAGE, AppConstants.TASK_SUBMITTED_SUCCESSFULLY);

			return new ResponseEntity<>(response, HttpStatus.OK);

//			// firebase notification 
//			if (Objects.nonNull(object)) {
//				// .....firebase notification .....//
//				NotificationInfo fcmIds = studentRepository.findFcmIdByStudentId(studentId);
//				String message = String.format("Your task has been successfully submitted. Thank you!");
//				fcmIds.setMessage(message);
//				fcmIds.setTitle("Submission updates!");
//				kafkaProducerService.sendNotification(NotificationConstant.COMMON_TOPIC, fcmIds.toString());
//				// .....firebase notification .....//
//
//				return new ResponseEntity<>(HttpStatus.OK);
//			} else
//				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

		} else {
			throw new ResourceAlreadyExistException("ALREADY TASK SUBMITED");
		}
	}

	@Override
	public ResponseEntity<?> addQuestionInTask(String title, String question, String videoUrl,
			List<MultipartFile> questionImages, Long taskId, MultipartFile attachment, AttachmentStatus status,
			Boolean codeSubmisionStatus) {
		Optional<Task> taskOptional = taskRepo.findByTaskIdAndIsDeleted(taskId, false);

		if (taskOptional.isEmpty()) {
			throw new ResourceNotFoundException("Task not found with this ID");
		}

		TaskQuestion taskQuestion = new TaskQuestion();
		taskQuestion.setQuestion(question);
		taskQuestion.setVideoUrl(videoUrl);

		if (Objects.nonNull(questionImages)) {
			questionImages.forEach((t) -> {
				String fileName = fileService.uploadFileInFolder(t, "question");
				taskQuestion.getQuestionImages().add(fileName);
			});
		}
		if (Objects.nonNull(attachment)) {
			String fileName = fileService.uploadFileInFolder(attachment, AppConstants.TASK_ASSIGNMENT_FILES);
			taskQuestion.setTaskAttachment(fileName);

		}

		if (Objects.isNull(status)) {
			status = AttachmentStatus.REQUIRED;
		}
		taskQuestion.setAttachmentStatus(status);

		Task task = taskOptional.get();
		List<TaskQuestion> existingQuestions = taskOptional.get().getTaskQuestion();

		taskQuestion.setTaskNumber(existingQuestions.size() + 1);

		taskQuestion.setCodeSubmisionStatus(codeSubmisionStatus);
		taskQuestion.setIsDeleted(false);

		taskQuestion.setTitle(title.trim());
		taskQuestion.setTask(taskOptional.get());
		TaskQuestion newTaskQuestion = taskQuestionRepository.save(taskQuestion);

		existingQuestions.add(newTaskQuestion);

		taskRepo.save(task);
		return new ResponseEntity<>(taskquestionResponseFilter(newTaskQuestion), HttpStatus.OK);

	}

//	@Override
//	public ResponseEntity<?> addTaskAttachment(Long taskId, MultipartFile attachment) {
//		Optional<Task> task = taskRepo.findByTaskIdAndIsDeleted(taskId, false);
//
//		if (task.isPresent()) {
//			if (Objects.nonNull(attachment)) {
//				String fileName = fileService.uploadFileInFolder(attachment, AppConstants.TASK_ASSIGNMENT_FILES);
//				task.get().setTaskAttachment(fileName);
//				taskRepo.save(task.get());
//			}
//			return new ResponseEntity<>(HttpStatus.OK);
//		}
//		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//	}

	@Override
	public ResponseEntity<?> deleteTaskQuestion(Long questionId) {
		taskQuestionRepository.deleteTaskQuestion(questionId);
		Map<String, Object> response = new HashMap<>();
		response.put(AppConstants.MESSAGE, AppConstants.DELETE_SUCCESS);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getAllSubmitedTasks(Integer courseId, Integer subjectId, SubmissionStatus status,
			Integer pageNumber, Integer pageSize) {
		Page<AssignmentSubmissionResponse> res = taskRepo.findAllSubmissionTaskWithCourseIdAndSubjectId(courseId,
				subjectId, status, PageRequest.of(pageNumber, pageSize));
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getSubmittedTaskQuestionForStudent(Integer studentId, Integer pageNumber, Integer pageSize,
			SubmissionStatus status) {
		Page<TaskQuestionSubmissionResponse> res = taskSubmissionRepository.getSubmitedTaskForStudent(studentId, status,
				PageRequest.of(pageNumber, pageSize));
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> updateSubmitedTaskStatus(Long submissionId, String status, String review) {

		Optional<TaskSubmission> res = taskSubmissionRepository.findBySubmissionId(submissionId);
		if (res.isEmpty())
			throw new ResourceNotFoundException("Submission not found");
		String message = "";

		TaskSubmissionResponse response = new TaskSubmissionResponse();

		Optional<String> taskName = taskSubmissionRepository.fetchTaskNameByTaskSubmissionId(submissionId);

		// fetch task submission by id

		TaskSubmission updateSubmitTaskStatus = new TaskSubmission();

		if (status.equals(SubmissionStatus.Reviewing.toString())) {
			taskSubmissionRepository.updateSubmitTaskStatus(submissionId, SubmissionStatus.Reviewing, review);
		} else if (status.equals(SubmissionStatus.Accepted.toString())) {
			String.format("Your %s task has been accepted. Thank you for your submission.", taskName.get());
			taskSubmissionRepository.updateSubmitTaskStatus(submissionId, SubmissionStatus.Accepted, review);
		} else if (status.equals(SubmissionStatus.Rejected.toString())) {
			String.format("Your %s task has been rejected.", taskName.get());
			taskSubmissionRepository.updateSubmitTaskStatus(submissionId, SubmissionStatus.Rejected, review);
		}

		response.setFullName(res.get().getStudent().getFullName());
		response.setId(res.get().getId());
		response.setProfilePic(res.get().getStudent().getProfilePic());
		response.setReview(res.get().getReview());
		response.setStatus(SubmissionStatus.valueOf(status).name());
		response.setSubmissionDate(res.get().getSubmissionDate());
		response.setSubmittionFileName(res.get().getSubmittionFileName());
		response.setSubmittionFileName(res.get().getSubmittionFileName());

		// firebase notification

//		if (taskName.isPresent()) {
//			// fetching all the fcmId
//			// sending message via kafka to firebase
//			NotificationInfo fcmIds = studentRepository.findFcmIdByStudentId(res.get().getStudent().getStudentId());
//			fcmIds.setMessage(message);
//			fcmIds.setTitle("Submission updates!");
//			kafkaProducerService.sendNotification(NotificationConstant.TASK_STATUS_TOPIC, fcmIds.toString());
//		}
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<?> getOverAllTaskStatusforBarChart() {
		TaskStatusSummary overAllTaskQuestionStatus = taskRepo.getOverAllTaskQuestionStatus();
		return new ResponseEntity<>(overAllTaskQuestionStatus, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getAllTaskOfStudent(StudentTaskFilterRequest request) {

		Map<String, Object> response = new HashMap<>();
		PageRequest pagerequets = PageRequest.of(request.getPageRequest().getPageNumber(),
				request.getPageRequest().getPageSize(), Sort.by(Direction.DESC, "createdDate"));
		Page<TaskResponse> allTaskOfStudent = taskRepo.getAllTaskOfStudent(request.getStudentId(), request.getStatus(),
				pagerequets);
		response.put(AppConstants.MESSAGE, AppConstants.DATA_FOUND);
		response.put("allTask", allTaskOfStudent);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	public TaskResponse taskResponseFilter(Task task, Integer studentId) {

		Map<String, Object> res = (Map<String, Object>) isTaskSubmitted(task.getTaskId(), studentId).getBody();

		return new TaskResponse(task.getTaskId(), task.getTaskName(), (Boolean) res.get("status"));
	}

	@Override
	public ResponseEntity<?> isTaskSubmitted(Long taskId, Integer studentId) {
		TaskSubmission submission = taskSubmissionRepository.findByTaskIdAndStudentId(taskId, studentId);
		Map<String, Object> res = new HashMap<>();

		res.put("status", Objects.nonNull(submission) ? true : false);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

//	public List<Task> filterTasks(List<Task> list) {
//
//		List<Task> list2 = list.parallelStream().filter(o -> !o.getIsDeleted()).collect(Collectors.toList());
//		return list2.parallelStream().filter(obj -> {
//			obj.setTaskQuestion(
//					obj.getTaskQuestion().parallelStream().filter(o -> !o.getIsDeleted()).collect(Collectors.toList()));
//			return obj != null;
//		}).collect(Collectors.toList());
//	}
//

	public Boolean checkSubmission(Long taskId, Integer studentId) {
		Map<String, Object> res = (Map<String, Object>) isTaskSubmitted(taskId, studentId).getBody();
		return (Boolean) res.get("status");
	}

	public Task filterTask(Task task) {
		task.setTaskQuestion(task.getTaskQuestion().parallelStream().filter(obj -> !obj.getIsDeleted())
				.collect(Collectors.toList()));
		return task;
	}

	public TaskSubmissionResponse taskSubmissionResponse(TaskSubmission submission) {

		TaskSubmissionResponse response = new TaskSubmissionResponse();
		response.setId(submission.getId());
		response.setReview(submission.getReview());
		response.setStatus(submission.getStatus().toString());
		response.setTaskDescription(submission.getTaskDescription());
		response.setTaskName(submission.getTaskName());
		// response.setTaskId(submission.getTaskId());
		response.setSubmittionFileName(submission.getSubmittionFileName());
		response.setSubmissionDate(submission.getSubmissionDate());
		response.setProfilePic(submission.getStudent().getProfilePic());
		response.setStudentId(submission.getStudent().getStudentId());
		response.setFullName(submission.getStudent().getFullName());
		response.setApplyForCoure(submission.getStudent().getApplyForCourse());
		return response;
	}

	@Override
	public ResponseEntity<?> getSubmissionTaskById(Long id) {
		Map<String, Object> response = new HashMap<>();
		Optional<TaskSubmission> submission = taskSubmissionRepository.findById(id);
		if (Objects.nonNull(submission)) {
			TaskSubmissionResponse res = new TaskSubmissionResponse();
			res.setFullName(submission.get().getStudent().getFullName());
			res.setId(submission.get().getId());
			res.setReview(submission.get().getReview());
			res.setStatus((submission.get().getStatus()).toString());
			res.setProfilePic(submission.get().getStudent().getProfilePic());
			res.setSubmittionFileName(submission.get().getSubmittionFileName());
			res.setSubmissionDate(submission.get().getSubmissionDate());
			res.setSubmittionFileName(submission.get().getSubmittionFileName());
			response.put("submission", res);
			response.put(AppConstants.MESSAGE, AppConstants.DATA_FOUND);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} else {
			response.put(AppConstants.MESSAGE, AppConstants.NO_DATA_FOUND);
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		}

	}

	@Override
	public ResponseEntity<?> getTaskQuestion(Long questionId, Long taskId) {

		Optional<TaskQuestion> question = taskQuestionRepository.findByQuestionIdAndTaskId(questionId, taskId);
		Map<String, Object> response = new HashMap<>();
		if (Objects.nonNull(question)) {
			response.put(AppConstants.MESSAGE, AppConstants.DATA_FOUND);
			response.put("question", taskquestionResponseFilter(question.get()));
			return new ResponseEntity<>(response, HttpStatus.OK);
		} else {
			response.put(AppConstants.MESSAGE, AppConstants.NO_DATA_FOUND);
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public ResponseEntity<?> getAllSubmissionTaskStatusByCourseIdAndSubjectId(Integer courseId, Integer subjectId,
			Integer pageNumber, Integer pageSize) {

		Map<String, Object> response = new HashMap<>();

		Page<AssignmentAndTaskSubmission> resp = taskRepo.findAllTaskStatusWithCourseIdAndSubjectId(courseId, subjectId,
				PageRequest.of(pageNumber, pageSize, Sort.by(Direction.ASC, "createdDate")));
		response.put("data", resp);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	// changed
	@Override
	public ResponseEntity<?> updateTaskQuestion(Long questionId, String question, String videoUrl,
			List<String> questionImages, List<MultipartFile> newImages, Long taskId) {

		Map<String, Object> response = new HashMap<>();

		TaskQuestion taskQuestion = taskQuestionRepository.findByQuestionId(questionId)
				.orElseThrow(() -> new ResourceNotFoundException(AppConstants.NO_DATA_FOUND));

		Task task = taskRepo.findByTaskIdAndIsDeleted(taskId, false)
				.orElseThrow(() -> new ResourceNotFoundException("Task not found"));

		taskQuestion.setQuestion(question);
		taskQuestion.setVideoUrl(videoUrl);
		if (Objects.isNull(questionImages)) {
			taskQuestion.setQuestionImages(new ArrayList<String>());
		} else {
			taskQuestion.setQuestionImages(questionImages);
		}
		if (Objects.nonNull(newImages) && newImages.size() > 0) {
			List<String> fileNames = newImages.parallelStream()
					.map(file -> fileService.uploadFileInFolder(file, AppConstants.TASK_ASSIGNMENT_FILES))
					.collect(Collectors.toList());
			taskQuestion.getQuestionImages().addAll(fileNames);
		}
		task.setUpdatedDate(LocalDateTime.now());

		// Check if the task has any submissions
		Boolean existsByQuestionId = taskSubmissionRepository.submissionExistsByQuestionId(questionId);

		if (!existsByQuestionId) {
			TaskQuestion save = taskQuestionRepository.save(taskQuestion);
			response.put(AppConstants.MESSAGE, AppConstants.UPDATE_SUCCESSFULLY);
			response.put("question", save);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} else {
			throw new RuntimeException("Task already has submissions, cannot update the question.");
		}
	}

	@Override
	public ResponseEntity<?> deleteTaskQuestionAttachement(Long taskId, Long questionId) {

		TaskQuestion question = taskQuestionRepository.findByQuestionIdAndTaskId(questionId, taskId)
				.orElseThrow(() -> new ResourceNotFoundException("Task question not found with this ID"));

		if (Objects.nonNull(question.getTaskAttachment())) {
			question.setTaskAttachment(null);
			taskQuestionRepository.save(question);
			Map<String, Object> response = new HashMap<>();
			response.put(AppConstants.MESSAGE, AppConstants.DELETE_SUCCESS);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} else {
			throw new ResourceNotFoundException("No attachment found for this task question");
		}

	}

	@Override
	public ResponseEntity<?> activateTask(Long id) {
		Map<String, Object> res = new HashMap<>();

		Task task = taskRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task not found"));

		if (!task.getIsActive()) {
			long taskCount = task.getTaskQuestion().stream().filter(obj -> !obj.getIsDeleted()).count();
			if (taskCount == 0) {
				res.put("message", "Please add some quesiton in task");
				return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
			}
		}
		task.setIsActive(!task.getIsActive());

//		if (task.getIsActive()) {
//			// fetching all the fcmId
//			// sending message via kafka to firebase
//			List<NotificationInfo> fcmIds = studentRepository.findAllFcmIdByCourseId(task.getCourse().getCourseId());
//			String message = String.format("A new task %s has been assigned. Please review and get started.",
//					task.getTaskName());
//
//			List<NotificationInfo> newlist = fcmIds.stream().parallel().map(obj -> {
//				obj.setMessage(message);
//				obj.setTitle("New task assign");
//				return obj;
//			}).toList();
//			kafkaProducerService.sendNotification(NotificationConstant.TASK_TOPIC, newlist.toString());
//		}

		res.put(AppConstants.STATUS, taskRepo.save(task).getIsActive());
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getAllTaskSubmissionBYTaskId(Long taskId) {
		List<AssignmentSubmissionResponse> res = taskRepo.getAllTaskSubmissionBYTaskId(taskId);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> isTaskSubmitted(Long taskId) {

//		Map<String, Object> res = new HashMap<>();
//		Task task = taskRepo.findByTaskIdAndIsDeletedFalse(taskId)
//				.orElseThrow(() -> new ResourceNotFoundException("Task not found with this ID"));
//		List<TaskSubmission> submissions = task.getAssignmentSubmissions();
//		if (submissions != null && !submissions.isEmpty()) {
//			res.put("status", true);
//			res.put("submissionId", submissions.get(0).getId()); // Assuming you want the first submission ID
//			return new ResponseEntity<>(res, HttpStatus.OK);
//		}
//		res.put("status", false);
//		res.put("submissionId", null);
//		return new ResponseEntity<>(res, HttpStatus.OK);
		return null;

	}

	@Override
	public ResponseEntity<?> isTaskQuestionSubmittedByStudentId(Long questionId, Integer studentId) {

		Map<String, Object> res = new HashMap<>();

		Boolean isSubmittedTask = taskSubmissionRepository.submissionExistByQuestionIdAndStudentId(questionId,
				studentId);

		res.put("status", isSubmittedTask);

		return new ResponseEntity<>(res, HttpStatus.OK);

	}

	// for student use
	@Override
	public ResponseEntity<?> getTaskQuestionsByTaskIdForStudent(Long taskId, Integer studentId) {

		if (!taskRepo.existsById(taskId)) {
			throw new ResourceNotFoundException("Task not found with this ID");
		}

		boolean isStudentPresent = studentRepository.existsById(studentId);

		if (!isStudentPresent) {
			throw new ResourceNotFoundException("Student not found with this ID");
		}

		// Fetching task questions for the given taskId
		List<TaskQuestionResponse> questions = taskQuestionRepository.getAllTaskQuestionOfStudent(studentId, taskId);

		Map<String, Object> response = new HashMap<>();
		response.put("taskQuestions", questions);
		response.put(AppConstants.MESSAGE, questions.isEmpty() ? AppConstants.NO_DATA_FOUND : AppConstants.DATA_FOUND);
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	// for admin use
	@Override
	public ResponseEntity<?> getTaskQuestionsByTaskId(Long taskId, Integer pageSize, Integer pageNumber) {

		// Check if the task exists

//		Optional<Task> task = taskRepo.findById(taskId);
//
//		boolean existsById = taskRepo.existsById(taskId);
//		if (!existsById) {
//			throw new ResourceNotFoundException("Task not found with this ID");
//		}
//
//		Map<String, Object> response = new HashMap<>();
//
//		taskQuestionRepository.findAllByTaskId(taskId, PageRequest.of(pageNumber, pageSize));
//
//		response.put(AppConstants.MESSAGE, AppConstants.DATA_FOUND);
//		response.put("taskQuestions", taskQuestions);
//		return new ResponseEntity<>(response, HttpStatus.OK);

		return null;
	}

	@Override
	public ResponseEntity<?> updateTaskQuestionStatus(Long taskId, Long questionId) {
		TaskQuestion taskQuestion = taskQuestionRepository.findByQuestionIdAndTaskId(questionId, taskId)
				.orElseThrow(() -> new ResourceNotFoundException("Task question not found with this ID"));

		taskQuestion.setIsActive(!taskQuestion.getIsActive());
		taskQuestionRepository.save(taskQuestion);

		Map<String, Object> response = new HashMap<>();
		response.put(AppConstants.MESSAGE, AppConstants.UPDATE_SUCCESSFULLY);
		response.put("question", taskquestionResponseFilter(taskQuestion));
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> isTaskQuestionSubmited(Long questionId, Long taskId) {

		Map<String, Object> res = new HashMap<>();
		taskQuestionRepository.findByQuestionIdAndTaskId(questionId, taskId)
				.orElseThrow(() -> new ResourceNotFoundException("Task question not found with this ID"));

		Boolean isSubmitted = taskSubmissionRepository.submissionExistByQuestionIdAndTaskId(questionId, taskId);
		res.put("status", isSubmitted);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> addTaskQuestionAttachment(Long taskId, MultipartFile attachment, Long questionId,
			AttachmentStatus status) {
		TaskQuestion taskQuestion = taskQuestionRepository.findByQuestionIdAndTaskId(questionId, taskId)
				.orElseThrow(() -> new ResourceNotFoundException("Task question not found with this ID"));
		if (Objects.nonNull(attachment)) {

			String fileName = fileService.uploadFileInFolder(attachment, AppConstants.TASK_ASSIGNMENT_FILES);
			taskQuestion.setTaskAttachment(fileName);
			taskQuestion.setAttachmentStatus(status);
			taskQuestionRepository.save(taskQuestion);
			Map<String, Object> response = new HashMap<>();
			response.put(AppConstants.MESSAGE, AppConstants.ATTACHMENT_ADDED_SUCCESSFULLY);
			response.put("question", taskquestionResponseFilter(taskQuestion));
			return new ResponseEntity<>(response, HttpStatus.OK);
		} else {
			throw new ResourceNotFoundException("Attachment not found");
		}

	}

	@Override
	public ResponseEntity<?> getAllTasks(Integer pageSise, Integer pageNumber, Integer courseId, Integer subjectId) {

		PageRequest pageRequest = PageRequest.of(pageNumber, pageSise, Sort.by(Direction.DESC, "createdDate"));
		Page<TaskResponse> tasks = taskRepo.findAllByIsDeletedFalse(pageRequest, courseId, subjectId);

		Map<String, Object> response = new HashMap<>();
		response.put(AppConstants.MESSAGE, AppConstants.DATA_FOUND);
		response.put("tasks", tasks);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getAllTaskQuestionWithSubmissionCount(Long taskId) {

		List<AssignmentAndTaskSubmission> questions = taskRepo.getAllTaskQuestionWithSubmissionCountByTaskId(taskId);

		Map<String, Object> response = new HashMap<>();
		response.put(AppConstants.MESSAGE, AppConstants.DATA_FOUND);
		response.put("taskQuestions", questions);
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

}
