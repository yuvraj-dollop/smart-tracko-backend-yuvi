package com.cico.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cico.exception.InvalidFileTypeException;
import com.cico.exception.ResourceAlreadyExistException;
import com.cico.exception.ResourceNotFoundException;
import com.cico.kafkaServices.KafkaProducerService;
import com.cico.model.Assignment;
import com.cico.model.AssignmentSubmission;
import com.cico.model.AssignmentTaskQuestion;
import com.cico.model.Student;
import com.cico.payload.AssignmentAndTaskSubmission;
import com.cico.payload.AssignmentFilter;
import com.cico.payload.AssignmentFilterResponse;
import com.cico.payload.AssignmentQuestionRequest;
import com.cico.payload.AssignmentRequest;
import com.cico.payload.AssignmentResponse;
import com.cico.payload.AssignmentSubmissionRequest;
import com.cico.payload.AssignmentSubmissionResponse;
import com.cico.payload.AssignmentTaskFilterReponse;
import com.cico.payload.AssignmentTaskSubmissionCounts;
import com.cico.payload.AssignmentTaskSubmissionSummary;
import com.cico.payload.CourseResponse;
import com.cico.payload.NotificationInfo;
import com.cico.payload.SubjectResponse;
import com.cico.payload.TaskQuestionResponse;
import com.cico.payload.TaskStatusSummary;
import com.cico.payload.UpdateAssignmentQuestionRequest;
import com.cico.repository.AssignmentRepository;
import com.cico.repository.AssignmentSubmissionRepository;
import com.cico.repository.AssignmentTaskQuestionRepository;
import com.cico.repository.CourseRepository;
import com.cico.repository.StudentRepository;
import com.cico.repository.SubjectRepository;
import com.cico.service.IAssignmentService;
import com.cico.service.ITaskService;
import com.cico.util.AppConstants;
import com.cico.util.NotificationConstant;
import com.cico.util.SubmissionStatus;

@Service
public class AssignmentServiceImpl implements IAssignmentService {

	private final LeaveServiceImpl leaveServiceImpl;

	@Autowired
	private AssignmentRepository assignmentRepository;

	@Autowired
	private CourseRepository courseRepo;

	@Autowired
	private SubjectRepository subjectRepo;

	@Autowired
	private FileServiceImpl fileServiceImpl;

	@Autowired
	private AssignmentTaskQuestionRepository assignmentTaskQuestionRepository;

	@Autowired
	private StudentRepository studentRepository;

	@Autowired
	private AssignmentSubmissionRepository submissionRepository;

	@Autowired
	private KafkaProducerService kafkaProducerService;

	@Autowired
	private ITaskService taskService;

	AssignmentServiceImpl(LeaveServiceImpl leaveServiceImpl) {
		this.leaveServiceImpl = leaveServiceImpl;
	}

	public Assignment checkIsPresent(Long id) {
		return assignmentRepository.findByIdAndIsDeleted(id, false)
				.orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
	}

	@Override
	public ResponseEntity<?> getAssignment(Long id) {
		Assignment assignment = checkIsPresent(id);
		Map<String, Object> response = new HashMap<>();
		response.put(AppConstants.MESSAGE, AppConstants.DATA_FOUND);
		response.put("assignment", assignmentResponseFilter(assignment));

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createAssignment(AssignmentRequest assignmentRequest) {

		Optional<Assignment> obj = assignmentRepository.findByName(assignmentRequest.getTitle().trim());
		Map<String, Object> response = new HashMap<>();
		if (obj.isEmpty()) {
			Assignment assignment = new Assignment();
			assignment.setTitle(assignmentRequest.getTitle().trim());

			assignment.setCourse(courseRepo.findById(assignmentRequest.getCourseId()).get());

			if (assignmentRequest.getSubjectId() != null)
				assignment.setSubject(subjectRepo.findById(assignmentRequest.getSubjectId()).get());

			assignment.setCreatedDate(LocalDateTime.now());
			Assignment savedAssignment = assignmentRepository.save(assignment);
			response.put(AppConstants.MESSAGE, AppConstants.SUCCESS);
			response.put("assignmentId", savedAssignment.getId());

			return new ResponseEntity<>(response, HttpStatus.CREATED);
		} else {
			throw new ResourceAlreadyExistException("Assignmnet Already Present With This Title");
		}
	}

	@Override
	public ResponseEntity<?> getAllAssignments() {
		List<Assignment> assignments = assignmentRepository.findByIsDeletedFalse();
		Map<String, Object> response = new HashMap<>();
		response.put(AppConstants.MESSAGE, AppConstants.DATA_FOUND);

		List<AssignmentResponse> collect = assignments.parallelStream().filter(obj -> !obj.getIsDeleted())
				.map(this::assignmentResponseFilter).collect(Collectors.toList());
		return new ResponseEntity<>(collect, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getAssignmentQuesById(Long questionId) {
		Map<String, Object> response = new HashMap<>();
		AssignmentTaskQuestion assignmentTaskQuestion = assignmentTaskQuestionRepository.findByQuestionId(questionId)
				.orElseThrow(() -> new ResourceNotFoundException(AppConstants.NO_DATA_FOUND));

		AssignmentTaskFilterReponse obj = new AssignmentTaskFilterReponse();
		obj.setTitle(assignmentTaskQuestion.getTitle());
		obj.setQuestion(assignmentTaskQuestion.getQuestion());
		obj.setQuestionId(assignmentTaskQuestion.getQuestionId());
		obj.setVideoUrl(assignmentTaskQuestion.getVideoUrl());
		obj.setQuestionImages(assignmentTaskQuestion.getQuestionImages());

		// setting assignment attachament
		obj.setAssignmentAttachement(assignmentTaskQuestion.getAssignment().getTaskAttachment());
		response.put("question", obj);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> submitAssignment(MultipartFile file, AssignmentSubmissionRequest readValue) {
		System.err.println("AAAAAAAAAAAAAAAAA ==> " + readValue);
		Optional<AssignmentTaskQuestion> obj = assignmentTaskQuestionRepository.findByQuestionId(readValue.getTaskId());
//		boolean anyMatch = obj.get().getAssignmentSubmissions().parallelStream()
//				.anyMatch(obj2 -> obj2.getStudent().getStudentId() == readValue.getStudentId());

		boolean anyMatch = obj.get().getAssignmentSubmissions().stream()
				.anyMatch(obj2 -> obj2.getStudent().getStudentId().equals(readValue.getStudentId()));

		if (file != null && !file.isEmpty()) {
			String contentType = file.getContentType();
			String originalFilename = file.getOriginalFilename();
			Boolean isPdf = contentType.equals("application/pdf") || originalFilename.endsWith(".pdf");
			Boolean isZip = contentType.equals("application/zip")
					|| originalFilename.equalsIgnoreCase("application/x-zip-compressed")
					|| originalFilename.endsWith(".zip");
			if (!isPdf && !isZip) {
				throw new InvalidFileTypeException("Only PDF or ZIP files are allowed for submission.");
			}

		}
		System.err.println("ANY MATCH IF ASSIGNMENT ALREDY SUBMITTED ==> " + anyMatch);

		if (!anyMatch) {
			AssignmentSubmission submission = new AssignmentSubmission();
			submission.setStudent(studentRepository.findByStudentId(readValue.getStudentId()));
			submission.setDescription(readValue.getDescription());
			submission.setSubmissionDate(LocalDateTime.now());
			submission.setStatus(SubmissionStatus.Unreviewed);
			if (Objects.nonNull(file)) {
				String fileName = fileServiceImpl.uploadFileInFolder(file, AppConstants.TASK_AND_ASSIGNMENT_SUBMISSION);
				submission.setSubmitFile(fileName);
			}
			AssignmentSubmission save = submissionRepository.save(submission);
			obj.get().getAssignmentSubmissions().add(save);
			assignmentTaskQuestionRepository.save(obj.get());

			System.err.println("FOR DEBUG ===> " + save);
			// .....firebase notification .....//

//			NotificationInfo fcmIds = studentRepository.findFcmIdByStudentId(readValue.getStudentId());
//			String message = String.format("Your assignment has been successfully submitted. Thank you!");
//			fcmIds.setMessage(message);
//			fcmIds.setTitle("Submission updates!");
//			kafkaProducerService.sendNotification(NotificationConstant.COMMON_TOPIC, fcmIds.toString());
			// .....firebase notification .....//
			Map<String, Object> res = new HashMap<>();
			res.put("response", "Assignment Submitted");
			res.put("questionId", readValue.getTaskId());
			System.err.println("RESPONSE FOR DEBUG ==> " + res);
			return ResponseEntity.ok(res);
		} else {
			throw new ResourceAlreadyExistException("ALREADY THIS ASSIGNMENT TASK SUBMITED!!");
		}
	}

	@Override
	public ResponseEntity<?> getSubmitedAssignmetByStudentId(Integer studentId, Integer pageSise, Integer pageNumber,
			SubmissionStatus status) {
		Page<AssignmentSubmissionResponse> res = submissionRepository.getSubmitAssignmentByStudentId(studentId,
				PageRequest.of(pageNumber, pageSise), status);
		return new ResponseEntity<>(res, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<?> getAllSubmitedAssginments(Integer courseId, Integer subjectId, SubmissionStatus status,
			Integer pageSize, Integer pageNumber) {

		return new ResponseEntity<>(assignmentRepository.findAllAssignmentSubmissionWithCourseIdAndSubjectId(courseId,
				subjectId, status, PageRequest.of(pageNumber, pageSize)), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> updateSubmitedAssignmentStatus(Long submissionId, String status, String review) {

		AssignmentSubmission sub = submissionRepository.findById(submissionId)
				.orElseThrow(() -> new ResourceNotFoundException("Submission not found with this id!"));

		System.err.println("|" + sub.getStatus() + "|" + sub.getStatus().toString().equals(AppConstants.ACCEPTED));

		if (sub.getStatus().toString().equals("Rejected") || sub.getStatus().toString().equals(AppConstants.ACCEPTED)) {
			return new ResponseEntity<>(HttpStatus.OK);
		}

		// fetching assignment title and task Number
		Object[] details = assignmentRepository.fetchAssignmentNameAndTaskNumberByAssignmentSubmissionId(submissionId);
		String message = "";

		if (status.equals(SubmissionStatus.Reviewing.toString())) {
			submissionRepository.updateSubmitAssignmentStatus(submissionId, SubmissionStatus.Reviewing, review);
		} else if (status.equals(SubmissionStatus.Accepted.toString())) {
//			message = String.format("Your task %d of assignment %s has been accepted. Thank you for your submission.",
//					details[1], details[0]);
			submissionRepository.updateSubmitAssignmentStatus(submissionId, SubmissionStatus.Accepted, review);
		} else if (status.equals(SubmissionStatus.Rejected.toString())) {
//			message = String.format("Your task %d of assignment %s has been rejected.", details[1], details[0]);
			submissionRepository.updateSubmitAssignmentStatus(submissionId, SubmissionStatus.Rejected, review);
		}

		// .....firebase notification .....//
		NotificationInfo fcmIds = studentRepository.findFcmIdByStudentId(sub.getStudent().getStudentId());
		fcmIds.setTitle(String.format("%s submission updates!", "Assignent"));
		fcmIds.setMessage(message);

		kafkaProducerService.sendNotification(NotificationConstant.COMMON_TOPIC, fcmIds.toString());
		// .....firebase notification .....//

		AssignmentSubmissionResponse response = new AssignmentSubmissionResponse();
		response.setApplyForCourse(sub.getStudent().getApplyForCourse());
		response.setFullName(sub.getStudent().getFullName());
		response.setSubmissionDate(sub.getSubmissionDate());
		response.setStatus(status);
		response.setProfilePic(sub.getStudent().getProfilePic());
		response.setSubmitFile(sub.getSubmitFile());
		response.setDescription(sub.getDescription());
		response.setStatus(SubmissionStatus.valueOf(status).name());
		response.setReview(sub.getReview());

		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<?> addQuestionInAssignment(String question, String videoUrl,
			List<MultipartFile> questionImages, Long assignmentId) {

		Assignment assignment = checkIsPresent(assignmentId);
		AssignmentTaskQuestion assignmentTaskQuestion = new AssignmentTaskQuestion();
		assignmentTaskQuestion.setQuestion(question);
		assignmentTaskQuestion.setVideoUrl(videoUrl);
		assignmentTaskQuestion.setIsDeleted(false);
		assignmentTaskQuestion.setIsActive(true);
		assignmentTaskQuestion.setTaskNumber(assignment.getAssignmentQuestion().size() + 1);
		if (Objects.nonNull(questionImages)) {
			List<String> fileNames = questionImages.stream()
					.map(file -> fileServiceImpl.uploadFileInFolder(file, AppConstants.TASK_ASSIGNMENT_FILES))
					.collect(Collectors.toList());
			assignmentTaskQuestion.setQuestionImages(fileNames);
		}
		assignmentTaskQuestion.setAssignment(assignment);
		AssignmentTaskQuestion newQuestion = assignmentTaskQuestionRepository.save(assignmentTaskQuestion);
		assignment.getAssignmentQuestion().add(newQuestion);

		assignmentRepository.save(assignment);
		return new ResponseEntity<>(taskquestionResponseFilter(newQuestion), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> addAssignment(Long assignmentId, MultipartFile attachment) {

		Assignment assignment = checkIsPresent(assignmentId);
		if (Objects.nonNull(assignment) && Objects.nonNull(attachment)) {
			String fileName = fileServiceImpl.uploadFileInFolder(attachment, AppConstants.TASK_ASSIGNMENT_FILES);
			assignment.setTaskAttachment(fileName);
		}

		assignmentRepository.save(assignment);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getAllSubmissionAssignmentTaskStatus() {

		List<Object[]> list = assignmentRepository.getAllSubmissionAssignmentTaskStatus();
		List<AssignmentAndTaskSubmission> assignmentTaskStatusList = new ArrayList<>();

		for (Object[] objects : list) {
			AssignmentAndTaskSubmission assignmentTaskStatus = new AssignmentAndTaskSubmission();
			assignmentTaskStatus.setAssignmentId((long) objects[0]);
			assignmentTaskStatus.setAssignmentTitle((String) objects[1]);
			assignmentTaskStatus.setUnReviewed(Objects.nonNull(objects[2]) ? (long) objects[2] : 0);
			assignmentTaskStatus.setReviewed((long) objects[3]);
			assignmentTaskStatus.setTotalSubmitted((long) objects[4]);
			assignmentTaskStatus.setTaskCount((long) objects[5]);
			assignmentTaskStatus.setTaskId((Long) objects[6]);

			assignmentTaskStatusList.add(assignmentTaskStatus);
		}
		return ResponseEntity.ok(assignmentTaskStatusList);

	}

	@Override
	public ResponseEntity<?> getOverAllAssignmentTaskStatus() {
		TaskStatusSummary overAllAssignmentTaskStatus = assignmentRepository.getOverAllAssignmentTaskStatus();
		return ResponseEntity.ok(overAllAssignmentTaskStatus);
	}

	// @Override
	// public ResponseEntity<?> getAllLockedAndUnlockedAssignment(Integer studentId)
	// {

	// Map<String, Object> response = new HashMap<>();
	// List<Assignment> lockedAssignment = new ArrayList<>();
	// List<Assignment> unLockedAssignment = new ArrayList<>();

	// List<Assignment> allAssignment = assignmentRepository
	// .findAll(studentRepository.findById(studentId).get().getCourse().getCourseId());

	// if (allAssignment.isEmpty()) {
	// response.put(AppConstants.MESSAGE, AppConstants.ASSIGNMENT_NOT_FOUND);
	// return new ResponseEntity<>(response, HttpStatus.OK);
	// }

	// allAssignment = AllAssignmentTemp(allAssignment, studentId);

	// if (!allAssignment.isEmpty()) {
	// unLockedAssignment.add(allAssignment.get(0));
	// }

	// for (int i = 0; i < allAssignment.size(); i++) {
	// Assignment assignment = allAssignment.get(i);
	// List<AssignmentSubmission> submittedAssignment =
	// assignment.getAssignmentQuestion().parallelStream()
	// .flatMap(question -> question.getAssignmentSubmissions().stream()
	// .filter(submission -> submission.getStudent().getStudentId() == studentId))
	// .collect(Collectors.toList());

	// long taskCount = submittedAssignment.stream().filter(
	// submission -> List.of("Accepted", "Rejected",
	// "Reviewing").contains(submission.getStatus().name()))
	// .count();

	// if (taskCount == assignment.getAssignmentQuestion().size()) {
	// if (i < allAssignment.size() - 1) {
	// unLockedAssignment.add(allAssignment.get(i + 1));
	// }
	// } else {
	// for (int j = i + 1; j < allAssignment.size(); j++) {
	// lockedAssignment.add(allAssignment.get(j));
	// }
	// // lockedAssignment.add(assignment);
	// break;
	// }
	// }

	// int count = 0;
	// if (unLockedAssignment.size() == 1 && lockedAssignment.size() > 1) {

	// List<AssignmentSubmission> collect =
	// unLockedAssignment.get(0).getAssignmentQuestion().stream()
	// .flatMap(q -> q.getAssignmentSubmissions().stream()
	// .filter(q2 -> q2.getStudent().getStudentId() == studentId))
	// .collect(Collectors.toList());
	// if (collect.size() ==
	// unLockedAssignment.get(0).getAssignmentQuestion().size()) {
	// unLockedAssignment.add(allAssignment.remove(1));
	// }

	// } else if (allAssignment.size() > 2) {
	// int startIndex = Math.max(0, unLockedAssignment.size() - 2);
	// for (int i = startIndex; i < unLockedAssignment.size(); i++) {
	// List<AssignmentSubmission> collect =
	// unLockedAssignment.get(i).getAssignmentQuestion().stream()
	// .flatMap(q -> q.getAssignmentSubmissions().stream().filter(q2 -> {
	// if (q2.getStudent() != null) {
	// return q2.getStudent().getStudentId() == studentId;
	// } else {
	// return false;
	// }
	// })).collect(Collectors.toList());
	// boolean anyMatch = collect.stream()
	// .anyMatch(submission1 ->
	// "Unreviewed".equals(submission1.getStatus().name()));
	// if (collect.size() ==
	// unLockedAssignment.get(i).getAssignmentQuestion().size() && anyMatch) {
	// count += 1;
	// }
	// }
	// if (count == 1) {
	// if (lockedAssignment.size() > 0) {
	// unLockedAssignment.add(lockedAssignment.remove(0));
	// }
	// }
	// count = 0;

	// }

	// List<AssignmentFilterResponse> assignmentFilterResponses =
	// unLockedAssignment.stream().map(obj -> {
	// AssignmentFilterResponse res = new AssignmentFilterResponse();
	// res.setId(obj.getId());
	// res.setTitle(obj.getTitle());
	// res.setStatus(obj.getIsActive());
	// res.setTaskQuestion(obj.getAssignmentQuestion().stream()
	// .map(obj2 -> new AssignmentTaskFilterReponse(obj2, studentId,
	// obj2.getTaskNumber()))
	// .collect(Collectors.toList()));
	// res.setTotalTaskCompleted(
	// (int) obj.getAssignmentQuestion().stream().flatMap(obj2 ->
	// obj2.getAssignmentSubmissions().stream())
	// .filter(obj3 -> obj3.getStudent().getStudentId() == studentId).count());
	// return res;
	// }).collect(Collectors.toList());
	// response.put("unLockedAssignment", assignmentFilterResponses);
	// response.put("lockedAssignment", lockedAssignment.size());
	// return new ResponseEntity<>(response, HttpStatus.OK);
	// }
	@Override
	public ResponseEntity<?> getAllLockedAndUnlockedAssignment(Integer studentId) {
		Map<String, Object> response = new HashMap<>();
		List<Assignment> lockedAssignment = new ArrayList<>();
		List<Assignment> unLockedAssignment = new ArrayList<>();

		try {
			Optional<Student> optionalStudent = studentRepository.findById(studentId);
			if (optionalStudent.isEmpty()) {
				response.put(AppConstants.MESSAGE, "Student not found");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}

			Student student = optionalStudent.get();
			if (student.getCourse() == null) {
				response.put(AppConstants.MESSAGE, "Student course not found");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}

			List<Assignment> allAssignment = assignmentRepository.findAll(student.getCourse().getCourseId());

			if (allAssignment == null || allAssignment.isEmpty()) {
				response.put(AppConstants.MESSAGE, AppConstants.ASSIGNMENT_NOT_FOUND);
				return new ResponseEntity<>(response, HttpStatus.OK);
			}

			allAssignment = AllAssignmentTemp(allAssignment, studentId);

			if (!allAssignment.isEmpty()) {
				unLockedAssignment.add(allAssignment.get(0));
			}

			for (int i = 0; i < allAssignment.size(); i++) {
				Assignment assignment = allAssignment.get(i);
				if (assignment.getAssignmentQuestion() == null)
					continue;

				List<AssignmentSubmission> submittedAssignment = assignment.getAssignmentQuestion().parallelStream()
						.filter(Objects::nonNull).flatMap(question -> {
							if (question.getAssignmentSubmissions() == null)
								return Stream.empty();
							return question.getAssignmentSubmissions().stream()
									.filter(submission -> submission != null && submission.getStudent() != null
											&& submission.getStudent().getStudentId() == studentId);
						}).collect(Collectors.toList());

				long taskCount = submittedAssignment.stream()
						.filter(submission -> submission.getStatus() != null
								&& List.of("Accepted", "Rejected", "Reviewing").contains(submission.getStatus().name()))
						.count();

				if (assignment.getAssignmentQuestion().size() == taskCount) {
					if (i < allAssignment.size() - 1) {
						unLockedAssignment.add(allAssignment.get(i + 1));
					}
				} else {
					for (int j = i + 1; j < allAssignment.size(); j++) {
						lockedAssignment.add(allAssignment.get(j));
					}
					break;
				}
			}

			int count = 0;
			if (unLockedAssignment.size() == 1 && lockedAssignment.size() > 1 && allAssignment.size() > 1) {
				Assignment firstUnlocked = unLockedAssignment.get(0);
				if (firstUnlocked.getAssignmentQuestion() != null) {
					List<AssignmentSubmission> collect = firstUnlocked.getAssignmentQuestion().stream()
							.filter(Objects::nonNull).flatMap(q -> {
								if (q.getAssignmentSubmissions() == null)
									return Stream.empty();
								return q.getAssignmentSubmissions().stream().filter(q2 -> q2 != null
										&& q2.getStudent() != null && q2.getStudent().getStudentId() == studentId);
							}).collect(Collectors.toList());

					if (collect.size() == firstUnlocked.getAssignmentQuestion().size()) {
						if (allAssignment.size() > 1) {
							unLockedAssignment.add(allAssignment.get(1));
						}
					}
				}
			} else if (allAssignment.size() > 2) {
				int startIndex = Math.max(0, unLockedAssignment.size() - 2);
				for (int i = startIndex; i < unLockedAssignment.size(); i++) {
					Assignment unlocked = unLockedAssignment.get(i);
					if (unlocked.getAssignmentQuestion() == null)
						continue;

					List<AssignmentSubmission> collect = unlocked.getAssignmentQuestion().stream().flatMap(q -> {
						if (q.getAssignmentSubmissions() == null)
							return Stream.empty();
						return q.getAssignmentSubmissions().stream().filter(q2 -> q2 != null && q2.getStudent() != null
								&& q2.getStudent().getStudentId() == studentId);
					}).collect(Collectors.toList());

					boolean anyMatch = collect.stream().anyMatch(submission1 -> submission1.getStatus() != null
							&& "Unreviewed".equals(submission1.getStatus().name()));

					if (collect.size() == unlocked.getAssignmentQuestion().size() && anyMatch) {
						count++;
					}
				}

				if (count == 1 && !lockedAssignment.isEmpty()) {
					unLockedAssignment.add(lockedAssignment.remove(0));
				}
				count = 0;
			}

			List<AssignmentFilterResponse> assignmentFilterResponses = unLockedAssignment.stream().map(obj -> {
				AssignmentFilterResponse res = new AssignmentFilterResponse();
				res.setId(obj.getId());
				res.setTitle(obj.getTitle());
				res.setStatus(obj.getIsActive());

//				res.setTaskQuestion(obj.getAssignmentQuestion() == null ? new ArrayList<>()
//						: obj.getAssignmentQuestion().stream()
//								.map(obj2 -> new AssignmentTaskFilterReponse(obj2, studentId, obj2.getTaskNumber()))
//								.collect(Collectors.toList()));
				res.setTaskQuestion(obj.getAssignmentQuestion() == null ? new ArrayList<>()
						: obj.getAssignmentQuestion().stream().filter(q -> {
							// Always show active and not deleted
							if (Boolean.TRUE.equals(q.getIsActive()) && Boolean.FALSE.equals(q.getIsDeleted())) {
								return true;
							}

							// If question is inactive or deleted, check if student submitted it
							if ((Boolean.FALSE.equals(q.getIsActive()) || Boolean.TRUE.equals(q.getIsDeleted()))
									&& q.getAssignmentSubmissions() != null) {
								return q.getAssignmentSubmissions().stream()
										.anyMatch(sub -> sub != null && sub.getStudent() != null
												&& sub.getStudent().getStudentId().equals(studentId));
							}

							return false;
						}).map(q -> new AssignmentTaskFilterReponse(q, studentId, q.getTaskNumber()))
								.collect(Collectors.toList()));

				int totalTaskCompleted = obj
						.getAssignmentQuestion() == null
								? 0
								: (int) obj.getAssignmentQuestion().stream()
										.flatMap(obj2 -> obj2.getAssignmentSubmissions() == null ? Stream.empty()
												: obj2.getAssignmentSubmissions().stream()
														.filter(obj3 -> obj3 != null && obj3.getStudent() != null
																&& obj3.getStudent().getStudentId() == studentId))
										.count();

				res.setTotalTaskCompleted(totalTaskCompleted);
				return res;
			}).collect(Collectors.toList());

			response.put("unLockedAssignment", assignmentFilterResponses);
			response.put("lockedAssignment", lockedAssignment.size());
			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace(); // You can replace with a logger
			response.put(AppConstants.MESSAGE, "An error occurred while processing the assignments");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<?> getAssignmentQuesSubmissionStatus(Long questionId, Integer studentId) {

		boolean status = assignmentTaskQuestionRepository.checkTaskSubmissionExistence(questionId, studentId);

		Map<String, Object> response = new HashMap<>();

		response.put(AppConstants.STATUS, status);
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<?> getAllSubmissionAssignmentTaskStatusByCourseIdAndSubjectId(
			AssignmentFilter assignmentFilter) {

		Long assignmentId = assignmentFilter.getAssignmentId();
		Integer pageSize = assignmentFilter.getPageRequest().getPageSize();
		Integer pageNumber = assignmentFilter.getPageRequest().getPageNumber();

		if (Objects.isNull(pageSize) || Objects.isNull(pageNumber)) {
			pageSize = 10;
			pageNumber = 0;
		}

		Page<AssignmentAndTaskSubmission> res = assignmentRepository
				.findAllAssignmentStatusWithAssignmentId(assignmentId, PageRequest.of(pageNumber, pageSize));

		return ResponseEntity.ok(res);
	}

	public List<AssignmentTaskSubmissionSummary> convert(List<Object[]> results) {

		Map<Long, AssignmentTaskSubmissionSummary> assignmentMap = new HashMap<>();
		for (Object[] row : results) {
			AssignmentTaskSubmissionSummary assignmentDTO = assignmentMap.computeIfAbsent((Long) row[5], id -> {
				AssignmentTaskSubmissionSummary newAssignment = new AssignmentTaskSubmissionSummary();
				newAssignment.setAssignmentId((Long) row[5]);
				newAssignment.setDescription("");
				newAssignment.setStatus((boolean) row[7]);
				newAssignment.setAssignmentTitle((String) row[6]);
				return newAssignment;
			});
			AssignmentTaskSubmissionCounts questionDTO = new AssignmentTaskSubmissionCounts();
			questionDTO.setTotalSubmitted((Long) row[0]);
			questionDTO.setUnReviewed((Long) row[1]);
			questionDTO.setReveiwed((Long) row[2]);
			questionDTO.setTaskNumber((Long) row[8]);
			questionDTO.setTaskId((Long) row[10]);
			assignmentDTO.getTask().add(questionDTO);

		}

		return new ArrayList<>(assignmentMap.values());
	}

	@Override
	public ResponseEntity<?> deleteTaskQuestion(Long questionId) {
		assignmentTaskQuestionRepository.deleteQuestionByIdAndId(questionId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	public List<Assignment> AllAssignmentTemp(List<Assignment> list, Integer studentId) {
		list = list.stream().filter(obj -> checkAssignmentSubmission(obj, studentId)).collect(Collectors.toList());
		list.forEach(obj -> {
			obj.setAssignmentQuestion(obj.getAssignmentQuestion().parallelStream().filter(obj1 -> !obj1.getIsDeleted())
					.collect(Collectors.toList()));
		});
		for (Assignment assignment : list) {
			System.err.println(" ----------------- Assignment => " + assignment.getTitle());
		}
		return list;
	}

	public boolean checkAssignmentSubmission(Assignment assignment, Integer studentId) {

		if (assignment.getIsDeleted() || !assignment.getIsActive()) {
			return assignment.getAssignmentQuestion().stream()
					.flatMap(q -> q.getAssignmentSubmissions().stream()
							.filter(q2 -> q2.getStudent().getStudentId() == studentId))
					.anyMatch(obj2 -> obj2.getStudent().getStudentId() == studentId);

		} else {
			return true;
		}

	}

	public AssignmentSubmissionResponse assignmentSubmissionResponse(AssignmentSubmission response) {

		AssignmentSubmissionResponse response2 = new AssignmentSubmissionResponse();

		response2.setApplyForCourse(response.getStudent().getApplyForCourse());
		response2.setFullName(response.getStudent().getFullName());
		response2.setSubmissionDate(response.getSubmissionDate());
		response2.setStatus(response.getStatus().toString());
		response2.setProfilePic(response.getStudent().getProfilePic());
		// response2.setTitle(response.getTitle());
		// response2.setAssignmentTitle("");
		return response2;
	}

	@Override
	public ResponseEntity<?> getSubmittedAssignmentBySubmissionId(Long submissionId) {

		Map<String, Object> res = new HashMap<>();
		Optional<AssignmentSubmission> submission = submissionRepository.findBySubmissionId(submissionId);
		if (submission != null) {
			AssignmentSubmission sub = submission.get();
			AssignmentSubmissionResponse response = new AssignmentSubmissionResponse();
			response.setApplyForCourse(sub.getStudent().getApplyForCourse());
			response.setFullName(sub.getStudent().getFullName());
			response.setSubmissionDate(sub.getSubmissionDate());
			response.setStatus(sub.getStatus().toString());
			response.setProfilePic(sub.getStudent().getProfilePic());
			// response.setTitle(sub.getTitle());
			response.setSubmitFile(sub.getSubmitFile());
			response.setDescription(sub.getDescription());
			response.setStatus(sub.getStatus().toString());
			response.setReview(sub.getReview());

			return new ResponseEntity<>(response, HttpStatus.OK);
		} else {
			res.put(AppConstants.STATUS, AppConstants.NO_DATA_FOUND);
			return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
		}

	}

	@Override
	public ResponseEntity<?> updateAssignmentQuestion(Long questionId, String question, String videoUrl,
			List<String> questionImages, List<MultipartFile> newImages) {
		Map<String, Object> response = new HashMap<>();
		AssignmentTaskQuestion assignmentTaskQuestion = assignmentTaskQuestionRepository.findByQuestionId(questionId)
				.orElseThrow(() -> new ResourceNotFoundException(AppConstants.NO_DATA_FOUND));

		assignmentTaskQuestion.setQuestion(question);
		assignmentTaskQuestion.setVideoUrl(videoUrl);
		if (Objects.isNull(questionImages)) {
			assignmentTaskQuestion.setQuestionImages(new ArrayList<String>());
		} else {
			assignmentTaskQuestion.setQuestionImages(questionImages);
		}
		if (Objects.nonNull(newImages) && newImages.size() > 0) {

			List<String> fileNames = newImages.stream()
					.map(file -> fileServiceImpl.uploadFileInFolder(file, AppConstants.TASK_ASSIGNMENT_FILES))
					.collect(Collectors.toList());
			assignmentTaskQuestion.getQuestionImages().addAll(fileNames);
		}

		AssignmentTaskQuestion save = assignmentTaskQuestionRepository.save(assignmentTaskQuestion);

		response.put(AppConstants.MESSAGE, AppConstants.UPDATE_SUCCESSFULLY);
		response.put("id", assignmentTaskQuestion.getAssignment().getId());
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	public AssignmentResponse assignmentResponseFilter(Assignment a) {

		AssignmentResponse res = new AssignmentResponse();
		CourseResponse cr = new CourseResponse();
		SubjectResponse sr = new SubjectResponse();

		res.setId(a.getId());
		res.setTaskAttachment(a.getTaskAttachment());
		res.setTitle(a.getTitle());
		res.setCreatedDate(a.getCreatedDate());

		cr.setCourseName(a.getCourse().getCourseName());
		cr.setCourseId(a.getCourse().getCourseId());

		sr.setSubjectId(a.getSubject().getSubjectId());
		sr.setSubjectName(a.getSubject().getSubjectName());

		res.setAssignmentQuestion(a.getAssignmentQuestion().parallelStream().filter(obj -> !obj.getIsDeleted())
				.map(this::taskquestionResponseFilter).collect(Collectors.toList()));
		res.setCourse(cr);
		res.setSubject(sr);

		return res;

	}

	public TaskQuestionResponse taskquestionResponseFilter(AssignmentTaskQuestion obj) {
		TaskQuestionResponse taskQuestionResponse = new TaskQuestionResponse(obj.getQuestionId(), obj.getQuestion(),
				obj.getQuestionImages(), obj.getVideoUrl(), obj.getTitle());

		taskQuestionResponse.setIsActive(obj.getIsActive());

		return taskQuestionResponse;
	}

	@Override
	public ResponseEntity<?> activateAssignment(Long id) {

		Map<String, Object> res = new HashMap<>();
		Assignment assigment = checkIsPresent(id);
		if (!assigment.getIsActive()) {
			if (assigment.getAssignmentQuestion().size() == 0) {
				res.put("message", "Please add some quesiton in assignment");
				return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
			}
		}

		assigment.setIsActive(!assigment.getIsActive());

		if (assigment.getIsActive()) {

//			// .... firebase notification ....//
//			// fetching all the fcmId
//			// sending message via kafka to firebase PUSH NOTIFICATION
//			List<NotificationInfo> fcmIds = studentRepository
//					.findAllFcmIdByCourseId(assigment.getCourse().getCourseId());
//			String message = String.format("A new assignment %s has been assigned. Please review and get started.",
//					assigment.getTitle());
//
//			List<NotificationInfo> newlist = fcmIds.stream().parallel().map(obj1 -> {
//				obj1.setMessage(message);
//				obj1.setTitle("New assignment assign");
//				return obj1;
//			}).toList();
//
//			kafkaProducerService.sendNotification(NotificationConstant.ASSIGNMENT_TOPIC, newlist.toString());
//			// .... firebase notification ....//
		}
		res.put(AppConstants.STATUS, assignmentRepository.save(assigment).getIsActive());
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getAllSubmittedAssignmentTask(Long assignmentId) {
		List<AssignmentSubmissionResponse> res = assignmentRepository.getAllSubmittedAssignmentTask(assignmentId);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> deleteAttachment(Long assignmentId) {
		Assignment assignment = assignmentRepository.findById(assignmentId)
				.orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
		assignment.setTaskAttachment("");
		assignmentRepository.save(assignment);
		return new ResponseEntity<>(HttpStatus.OK);

	}

	@Override
	public ResponseEntity<?> addAttachment(Long assignmentId, MultipartFile file) {

		Assignment assignment = assignmentRepository.findById(assignmentId)
				.orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

		if (file != null) {
			String fileName = fileServiceImpl.uploadFileInFolder(file, AppConstants.ATTENDANCE_IMAGES);
			assignment.setTaskAttachment(fileName);
		}

		assignmentRepository.save(assignment);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> isAssignmentTaskSubmittedByStudent(Long assignmentId) {
		Assignment assignment = assignmentRepository.findById(assignmentId)
				.orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

		Map<String, Object> response = new HashMap<>();
		boolean hasSubmission = false;

		// Fetch all questions for the assignment
		List<AssignmentTaskQuestion> questions = assignment.getAssignmentQuestion().stream()
				.filter(q -> !q.getIsDeleted()).collect(Collectors.toList());

		for (AssignmentTaskQuestion question : questions) {
			if (question.getAssignmentSubmissions() != null && !question.getAssignmentSubmissions().isEmpty()) {
				hasSubmission = true;
				break;
			}
		}

		response.put("status", hasSubmission);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> updateAssignment(AssignmentRequest assignmentRequest) {
		Map<String, Object> response = new HashMap<>();
		Assignment assignment = assignmentRepository.findById(assignmentRequest.getAssignmentId())
				.orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

		if (assignmentRequest.getTitle() != null && !assignmentRequest.getTitle().trim().isEmpty()) {
			Optional<Assignment> existingAssignment = assignmentRepository
					.findByName(assignmentRequest.getTitle().trim());
			if (existingAssignment.isPresent() && !existingAssignment.get().getId().equals(assignment.getId())) {
				throw new ResourceAlreadyExistException("Assignment with this title already exists.");
			}
			assignment.setTitle(assignmentRequest.getTitle().trim());
		}

		if (assignmentRequest.getCourseId() != null) {
			assignment.setCourse(courseRepo.findById(assignmentRequest.getCourseId()).get());
		}

		if (assignmentRequest.getSubjectId() != null) {
			assignment.setSubject(subjectRepo.findById(assignmentRequest.getSubjectId()).get());
		}

		Assignment updatedAssignment = assignmentRepository.save(assignment);
		response.put(AppConstants.MESSAGE, AppConstants.UPDATE_SUCCESSFULLY);
		response.put("assignment", assignmentResponseFilter(updatedAssignment));

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getAllAssignments_new(AssignmentFilter assignmentFilter) {
		Map<String, Object> response = new HashMap<>();

		PageRequest of = PageRequest.of(assignmentFilter.getPageRequest().getPageNumber(),
				assignmentFilter.getPageRequest().getPageSize());

		Page<Assignment> assignments = assignmentRepository.findAllByCourseIdAndSubjectIdAndIsDeletedFalse(
				assignmentFilter.getCourseId(), assignmentFilter.getSubjectId(), of);
//		if (assignments.isEmpty()) {
//			response.put(AppConstants.MESSAGE, AppConstants.NO_DATA_FOUND);
//			return new ResponseEntity<>(response, HttpStatus.OK);
//		}

		Page<AssignmentResponse> map = assignments.map(this::filterAssingnmentDetailsOnly);
		response.put(AppConstants.MESSAGE, AppConstants.DATA_FOUND);
		response.put("assignments", map);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	public AssignmentResponse filterAssingnmentDetailsOnly(Assignment assignment) {

		AssignmentResponse res = new AssignmentResponse();
		res.setTotalTaskCount(assignment.getAssignmentQuestion().size());
		res.setId(assignment.getId());
		res.setTitle(assignment.getTitle());
		res.setIsActive(assignment.getIsActive());
		return res;
	}

	@Override
	public ResponseEntity<?> changeAssignmentTaskStatus(Long id) {
		AssignmentTaskQuestion taskQuestion = assignmentTaskQuestionRepository.findByQuestionIdAndIsDeleted(id, false)
				.orElseThrow(() -> new ResourceNotFoundException("Assignment task not found"));

		taskQuestion.setIsActive(!taskQuestion.getIsActive());
		AssignmentTaskQuestion updatedTask = assignmentTaskQuestionRepository.save(taskQuestion);
		Map<String, Object> response = new HashMap<>();
		response.put(AppConstants.MESSAGE, AppConstants.UPDATE_SUCCESSFULLY);
		response.put("status", updatedTask.getIsActive());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> activateAssigment(Long assignmentId) {
		Map<String, Object> response = new HashMap<>();
		Assignment assignment = assignmentRepository.findById(assignmentId)
				.orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

		if (assignment.getAssignmentQuestion().isEmpty()) {
			response.put(AppConstants.MESSAGE, "Please add some questions to the assignment before activating it");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		assignment.setIsActive(!assignment.getIsActive());
		assignmentRepository.save(assignment);
		response.put(AppConstants.MESSAGE, "Assignment status changed successfully");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	// ......................... NEW METHOD'S ............................

	@Override
	public ResponseEntity<?> getAllLockedAndUnlockedAssignment(Integer studentId, Integer pageSize,
			Integer pageNumber) {
		Map<String, Object> response = new HashMap<>();
		List<Assignment> lockedAssignment = new ArrayList<>();
		List<Assignment> unLockedAssignment = new ArrayList<>();

		try {
			Optional<Student> optionalStudent = studentRepository.findById(studentId);
			if (optionalStudent.isEmpty()) {
				response.put(AppConstants.MESSAGE, AppConstants.STUDENT_NOT_FOUND);
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}

			Student student = optionalStudent.get();
			if (student.getCourse() == null) {
				response.put(AppConstants.MESSAGE, AppConstants.COURSE_NOT_FOUND);
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}

			List<Assignment> allAssignment = assignmentRepository.findAll(student.getCourse().getCourseId());

			if (allAssignment == null || allAssignment.isEmpty()) {
				response.put(AppConstants.MESSAGE, AppConstants.ASSIGNMENT_NOT_FOUND);
				return new ResponseEntity<>(response, HttpStatus.OK);
			}

			allAssignment = AllAssignmentTemp(allAssignment, studentId);

			if (!allAssignment.isEmpty()) {
				unLockedAssignment.add(allAssignment.get(0));
			}

			for (int i = 0; i < allAssignment.size(); i++) {
				Assignment assignment = allAssignment.get(i);
				if (assignment.getAssignmentQuestion() == null)
					continue;

				List<AssignmentSubmission> submittedAssignment = getSubmittedAssignments(assignment, studentId);

				long taskCount = submittedAssignment.stream()
						.filter(submission -> submission.getStatus() != null
								&& List.of("Accepted", "Rejected", "Reviewing").contains(submission.getStatus().name()))
						.count();

				if (assignment.getAssignmentQuestion().size() == taskCount) {
					if (i < allAssignment.size() - 1) {
						unLockedAssignment.add(allAssignment.get(i + 1));
					}
				} else {
					for (int j = i + 1; j < allAssignment.size(); j++) {
						lockedAssignment.add(allAssignment.get(j));
					}
					break;
				}
			}

			handleUnlockConditions(allAssignment, unLockedAssignment, lockedAssignment, studentId);

			List<AssignmentFilterResponse> assignmentFilterResponses = new ArrayList<>();

			for (Assignment assignment : unLockedAssignment) {
				assignmentFilterResponses.add(mapToAssignmentResponse(assignment, studentId, false));

			}

			for (Assignment assignment : lockedAssignment) {
				assignmentFilterResponses.add(mapToAssignmentResponse(assignment, studentId, true));
			}

			// Add manual pagination
			int start = Math.min(pageNumber * pageSize, assignmentFilterResponses.size());
			int end = Math.min(start + pageSize, assignmentFilterResponses.size());
			List<AssignmentFilterResponse> paginatedList = assignmentFilterResponses.subList(start, end);

			// Wrap paginated list as a Page
			Pageable pageable = PageRequest.of(pageNumber, pageSize);
			Page<AssignmentFilterResponse> pageResult = new PageImpl<>(paginatedList, pageable,
					assignmentFilterResponses.size());

			// Prepare response map
			response.put("assignments", pageResult);

			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (Exception e) {
//			e.printStackTrace(); // You can replace with a logger
			response.put(AppConstants.MESSAGE, "An error occurred while processing the assignments");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private AssignmentFilterResponse mapToAssignmentResponse(Assignment obj, Integer studentId, Boolean isLocked) {
		AssignmentFilterResponse res = new AssignmentFilterResponse();
		res.setId(obj.getId());
		res.setTitle(obj.getTitle());
		res.setStatus(obj.getIsActive());

		List<AssignmentTaskFilterReponse> questions = obj.getAssignmentQuestion() == null ? new ArrayList<>()
				: obj.getAssignmentQuestion().stream().filter(q -> {
					if (Boolean.TRUE.equals(q.getIsActive()) && Boolean.FALSE.equals(q.getIsDeleted())) {
						return true;
					}
					if ((Boolean.FALSE.equals(q.getIsActive()) || Boolean.TRUE.equals(q.getIsDeleted()))
							&& q.getAssignmentSubmissions() != null) {
						return q.getAssignmentSubmissions().stream().anyMatch(sub -> sub != null
								&& sub.getStudent() != null && sub.getStudent().getStudentId().equals(studentId));
					}
					return false;
				}).map(q -> new AssignmentTaskFilterReponse(q, studentId, q.getTaskNumber()))
						.collect(Collectors.toList());

		res.setTaskQuestion(questions);

		int totalTaskCompleted = obj.getAssignmentQuestion() == null ? 0
				: (int) obj.getAssignmentQuestion().stream().flatMap(obj2 -> obj2.getAssignmentSubmissions() == null
						? Stream.empty()
						: obj2.getAssignmentSubmissions().stream().filter(obj3 -> obj3 != null
								&& obj3.getStudent() != null && obj3.getStudent().getStudentId().equals(studentId)))
						.count();

		res.setTotalTaskCompleted(totalTaskCompleted);
		res.setIsLocked(isLocked);
		return res;
	}

	private List<AssignmentSubmission> getSubmittedAssignments(Assignment assignment, Integer studentId) {
		return assignment.getAssignmentQuestion().stream().filter(Objects::nonNull)
				.flatMap(q -> q.getAssignmentSubmissions() == null ? Stream.empty()
						: q.getAssignmentSubmissions().stream()
								.filter(sub -> sub != null && sub.getStudent() != null
										&& sub.getStudent().getStudentId().equals(studentId)))
				.collect(Collectors.toList());
	}

	private void handleUnlockConditions(List<Assignment> allAssignment, List<Assignment> unLockedAssignment,
			List<Assignment> lockedAssignment, Integer studentId) {

		// Loop through unlocked assignments
		for (int i = 0; i < unLockedAssignment.size(); i++) {
			Assignment current = unLockedAssignment.get(i);

			if (current.getAssignmentQuestion() == null)
				continue;

			List<AssignmentSubmission> submissions = getSubmittedAssignments(current, studentId);

			boolean allSubmitted = submissions.size() == current.getAssignmentQuestion().size();

			// If current assignment is fully submitted, try to unlock the next one
			if (allSubmitted && unLockedAssignment.size() < allAssignment.size()) {
				int nextIndex = allAssignment.indexOf(current) + 1;

				if (nextIndex < allAssignment.size()) {
					Assignment nextAssignment = allAssignment.get(nextIndex);

					if (lockedAssignment.remove(nextAssignment)) {
						unLockedAssignment.add(nextAssignment);
					}
				}
			}
		}
	}

	@Override
	public ResponseEntity<?> addQuestionInAssignment(AssignmentQuestionRequest request) {

		Assignment assignment = checkIsPresent(request.getAssignmentId());
		AssignmentTaskQuestion assignmentTaskQuestion = new AssignmentTaskQuestion();
		assignmentTaskQuestion.setTitle(request.getTitle());
		assignmentTaskQuestion.setQuestion(request.getQuestion());
		assignmentTaskQuestion.setVideoUrl(request.getVideoUrl());
		assignmentTaskQuestion.setIsDeleted(false);
		assignmentTaskQuestion.setIsActive(true);
		assignmentTaskQuestion.setTaskNumber(assignment.getAssignmentQuestion().size() + 1);
		if (Objects.nonNull(request.getQuestionImages())) {
			List<String> fileNames = request.getQuestionImages().stream()
					.map(file -> fileServiceImpl.uploadFileInFolder(file, AppConstants.TASK_ASSIGNMENT_FILES))
					.collect(Collectors.toList());
			assignmentTaskQuestion.setQuestionImages(fileNames);
		}
		assignmentTaskQuestion.setAssignment(assignment);
		AssignmentTaskQuestion newQuestion = assignmentTaskQuestionRepository.save(assignmentTaskQuestion);
		assignment.getAssignmentQuestion().add(newQuestion);

		assignmentRepository.save(assignment);
		return new ResponseEntity<>(taskquestionResponseFilter(newQuestion), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> updateAssignmentQuestion(UpdateAssignmentQuestionRequest request) {
		Map<String, Object> response = new HashMap<>();
		AssignmentTaskQuestion assignmentTaskQuestion = assignmentTaskQuestionRepository
				.findByQuestionId(request.getQuestionId())
				.orElseThrow(() -> new ResourceNotFoundException(AppConstants.NO_DATA_FOUND));

		assignmentTaskQuestion.setTitle(request.getTitle());
		assignmentTaskQuestion.setQuestion(request.getQuestion());
		assignmentTaskQuestion.setVideoUrl(request.getVideoUrl());
		if (Objects.isNull(request.getQuestionImages())) {
			assignmentTaskQuestion.setQuestionImages(new ArrayList<String>());
		} else {
			assignmentTaskQuestion.setQuestionImages(request.getQuestionImages());
		}
		if (Objects.nonNull(request.getNewImages()) && request.getNewImages().size() > 0) {

			List<String> fileNames = request.getNewImages().stream()
					.map(file -> fileServiceImpl.uploadFileInFolder(file, AppConstants.TASK_ASSIGNMENT_FILES))
					.collect(Collectors.toList());
			assignmentTaskQuestion.getQuestionImages().addAll(fileNames);
		}

		assignmentTaskQuestionRepository.save(assignmentTaskQuestion);

		response.put(AppConstants.MESSAGE, AppConstants.UPDATE_SUCCESSFULLY);
		response.put("id", assignmentTaskQuestion.getAssignment().getId());
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@Override
	public Long countSubmittedAssignmentByStudentId(Integer studentId) {
		Student student = studentRepository.findById(studentId).orElseThrow(() -> {
			throw new ResourceNotFoundException("Student not found");
		});

		if (student.getCourse() == null) {
			throw new ResourceNotFoundException("Student course not found");
		}
		return assignmentRepository.countByCourseIdAndIsDeletedFalse(student.getCourse().getCourseId());
	}

	@Override
	public Long countAssignmentOfStudent(Integer studentId) {
		return submissionRepository.countSubmittedAssignmentsByStudentId(studentId);
	}

	@Override
	public ResponseEntity<?> getAssignmentQuestionDetails(Long questionId, Integer studentId) {
		Map<String, Object> response = new HashMap<>();
		AssignmentTaskQuestion assignmentTaskQuestion = assignmentTaskQuestionRepository.findByQuestionId(questionId)
				.orElseThrow(() -> new ResourceNotFoundException(AppConstants.NO_DATA_FOUND));

		AssignmentTaskFilterReponse obj = new AssignmentTaskFilterReponse();
		obj.setTitle(assignmentTaskQuestion.getTitle());
		obj.setQuestion(assignmentTaskQuestion.getQuestion());
		obj.setQuestionId(assignmentTaskQuestion.getQuestionId());
		obj.setVideoUrl(assignmentTaskQuestion.getVideoUrl());
		obj.setQuestionImages(assignmentTaskQuestion.getQuestionImages());

		// setting assignment attachament
		obj.setAssignmentAttachement(assignmentTaskQuestion.getAssignment().getTaskAttachment());
		response.put("question", obj);
		response.put("assignmentId", assignmentTaskQuestion.getAssignment().getId());

		AssignmentSubmissionResponse submittedAssignment = submissionRepository
				.getSubmitAssignmentQuestionByStudentId(studentId, questionId, null);
		System.err.println(submittedAssignment + " <<<-----------------");
		if (submittedAssignment != null) {
			submittedAssignment
					.setOriginalFileName(extractOriginalFileNameFromUrl(submittedAssignment.getSubmitFile()));
			System.err.println("extractOriginalFileNameFromUrl(submittedAssignment.getSubmitFile()) => "
					+ extractOriginalFileNameFromUrl(submittedAssignment.getSubmitFile()));
			response.put("submittedAssignment", submittedAssignment);
		}

		return ResponseEntity.ok(response);
	}

	private String extractOriginalFileNameFromUrl(String url) {
		if (url == null || url.isBlank() || !url.contains("/")) {
			return null;
		}

		// Get the last segment (file name) after last '/'
		String fileName = url.substring(url.lastIndexOf("/") + 1); // e.g., 29a9af80-xyz_Submit My File.zip

		int underscoreIndex = fileName.indexOf("_");
		int dotIndex = fileName.lastIndexOf(".");

		// Ensure underscore is before dot (correct format) and both exist
		if (underscoreIndex != -1 && dotIndex != -1 && underscoreIndex < dotIndex) {
			String originalFileName = fileName.substring(underscoreIndex + 1, dotIndex); // e.g., Submit My File
			return originalFileName.trim(); // Removes any extra spaces if present
		}

		// Fallback: return default name
		return "SubmittedFile";
	}

	@Override
	public ResponseEntity<?> getOverAllAssignmentTaskStatusNew(LocalDate startDate, LocalDate endDate) {
		TaskStatusSummary overAllAssignmentTaskStatus = assignmentRepository
				.getOverAllAssignmentTaskStatusBetweenDates(startDate, endDate);
		return ResponseEntity.ok(overAllAssignmentTaskStatus);
	}
}
