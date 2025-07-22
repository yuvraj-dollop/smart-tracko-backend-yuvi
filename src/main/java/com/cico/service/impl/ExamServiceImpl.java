package com.cico.service.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cico.exception.ResourceAlreadyExistException;
import com.cico.exception.ResourceNotFoundException;
import com.cico.kafkaServices.KafkaProducerService;
import com.cico.model.Chapter;
import com.cico.model.ChapterCompleted;
import com.cico.model.ChapterExamResult;
import com.cico.model.Course;
import com.cico.model.CourseExam;
import com.cico.model.CourseExamResult;
import com.cico.model.Exam;
import com.cico.model.Question;
import com.cico.model.Student;
import com.cico.model.Subject;
import com.cico.model.SubjectExam;
import com.cico.model.SubjectExamResult;
import com.cico.model.SubmittedExamQuestionHistory;
import com.cico.payload.AddExamRequest;
import com.cico.payload.ChapterExamResultResponse;
import com.cico.payload.CourseExamResponse;
import com.cico.payload.CourseExamResultResponse;
import com.cico.payload.ExamRequest;
import com.cico.payload.ExamResultResponse;
import com.cico.payload.NotificationInfo;
import com.cico.payload.PageResponse;
import com.cico.payload.PaginationRequest;
import com.cico.payload.QuestionResponse;
import com.cico.payload.SubjectExamResponse;
import com.cico.payload.TestFilterRequest;
import com.cico.payload.UpcomingExamResponse;
import com.cico.repository.ChapterCompletedRepository;
import com.cico.repository.ChapterExamResultRepo;
import com.cico.repository.ChapterRepository;
import com.cico.repository.CourseExamRepository;
import com.cico.repository.CourseExamResultRepository;
import com.cico.repository.CourseRepository;
import com.cico.repository.ExamRepo;
import com.cico.repository.QuestionRepo;
import com.cico.repository.StudentRepository;
import com.cico.repository.SubjectExamRepo;
import com.cico.repository.SubjectExamResultRepo;
import com.cico.repository.SubjectRepository;
import com.cico.repository.SubmittedExamHistoryRepo;
import com.cico.service.IExamService;
import com.cico.util.AppConstants;
import com.cico.util.ExamType;
import com.cico.util.NotificationConstant;

@Service
public class ExamServiceImpl implements IExamService {

	@Autowired
	private ExamRepo examRepo;

	@Autowired
	ChapterRepository chapterRepo;

	@Autowired
	private ChapterExamResultRepo chapterExamResultRepo;

	@Autowired
	private StudentRepository studentRepository;

	@Autowired
	private ChapterCompletedRepository chapterCompletedRepository;

	@Autowired
	private SubjectExamRepo subjectExamRepo;

	@Autowired
	private SubjectExamResultRepo subjectExamResultRepo;

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private SubjectServiceImpl subjectServiceImpl;

	@Autowired
	private KafkaProducerService kafkaProducerService;

	@Autowired
	private SubmittedExamHistoryRepo submittedExamHistoryRepo;

	@Autowired
	private CourseRepository courseRepository;

	@Autowired
	private CourseExamRepository courseExamRepo;

	@Autowired
	private CourseExamResultRepository courseExamResultRepo;

	@Autowired
	private QuestionRepo questionRepo;

	@Override
	public ResponseEntity<?> addChapterExamResult(ExamRequest chapterExamResult) {
		Student student = studentRepository.findById(chapterExamResult.getStudentId())
				.orElseThrow(() -> new ResourceNotFoundException(AppConstants.STUDENT_NOT_FOUND));
		Chapter chapter = chapterRepo.findById(chapterExamResult.getChapterId()).get();

		Optional<ChapterExamResult> findByChapterAndStudent = chapterExamResultRepo.findByChapterAndStudent(chapter,
				student);
		if (findByChapterAndStudent.isPresent())
			throw new ResourceAlreadyExistException("You have already submitted this test");

		ChapterExamResult examResult = new ChapterExamResult();
		Map<Integer, String> review = chapterExamResult.getReview();
		int correct = 0;
		int inCorrect = 0;
		examResult.setChapter(chapter);
		examResult.setStudent(student);

		List<Question> questions = chapter.getExam().getQuestions();
		questions = questions.stream().filter(obj -> !obj.getIsDeleted()).collect(Collectors.toList());

		for (Question q : questions) {
			Integer id = q.getQuestionId();
			String correctOption = q.getCorrectOption();

			if (Objects.nonNull(review)) {
				String reviewAns = review.get(id);
				if (Objects.nonNull(reviewAns)) {
					if (review.get(id).equals(correctOption)) {
						correct++;
					} else {
						inCorrect++;
					}
				}
			}
		}
		examResult.setReview(review);
		examResult.setCorrecteQuestions(correct);
		examResult.setWrongQuestions(inCorrect);
		examResult.setNotSelectedQuestions(questions.size() - (correct + inCorrect));
		examResult.setScoreGet(correct - inCorrect);
		examResult.setTotalQuestion(questions.size());
		ChapterExamResult save = chapterExamResultRepo.save(examResult);

		ChapterCompleted chapterCompleted = new ChapterCompleted();
		chapterCompleted.setChapterId(chapterExamResult.getChapterId());
		chapterCompleted.setStudentId(chapterExamResult.getStudentId());
		chapterCompleted.setSubjectId(chapterExamResult.getSubjectId());
		chapterCompletedRepository.save(chapterCompleted);

		ExamResultResponse res = new ExamResultResponse();
		res.setCorrecteQuestions(save.getCorrecteQuestions());
		res.setNotSelectedQuestions(save.getNotSelectedQuestions());
		res.setScoreGet(save.getScoreGet());
		res.setWrongQuestions(save.getWrongQuestions());
		res.setId(save.getId());
		res.setTotalQuestion(save.getTotalQuestion());
		res.setStudentId(save.getStudent().getStudentId());
		res.setStudentName(save.getStudent().getFullName());
		res.setProfilePic(save.getStudent().getProfilePic());

		List<ChapterExamResult> allResults = chapterExamResultRepo.findAllById(chapter.getChapterId());

		int total = allResults.size();
		int lowerScores = 0;

		for (ChapterExamResult result : allResults) {
			if (result.getScoreGet() <= save.getScoreGet()) {
				lowerScores++;
			}
		}

		System.err.println("TOTAL ====> " + total);
		int percentile = 0;
		if (total == 1) {
			percentile = 100;
		} else {
			percentile = (int) Math.round(((double) lowerScores / total) * 100);
		}

		res.setPercentile(percentile);

		// .....firebase notification .....//

		NotificationInfo fcmIds = studentRepository.findFcmIdByStudentId(student.getStudentId());
		String message = String.format("Congratulations! You have successfully completed your exam. Well done!");
		fcmIds.setMessage(message);
		fcmIds.setTitle("Exam Completed!");
		kafkaProducerService.sendNotification(NotificationConstant.COMMON_TOPIC, fcmIds.toString());
		// .....firebase notification .....//

		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	public Exam checkChapterExamIsPresent(Integer examId) {
		Exam exam = examRepo.findById(examId).orElseThrow(() -> new ResourceNotFoundException("Exam not Found!!"));
		exam.setQuestions(exam.getQuestions().stream().filter(obj -> !obj.getIsDeleted() && obj.getIsActive())
				.collect(Collectors.toList()));
		return exam;
	}

	@Override
	public ResponseEntity<?> addSubjectExamResult(ExamRequest request) {
		Map<String, String> response = new HashMap<>();

		Student student = studentRepository.findById(request.getStudentId()).get();
		Subject subject = subjectRepository.findById(request.getSubjectId()).get();
		SubjectExam subjectExam = subjectExamRepo.findById(request.getExamId())
				.orElseThrow(() -> new ResourceNotFoundException(AppConstants.EXAM_NOT_FOUND));

		if (subjectExam.getExamType() == ExamType.SCHEDULEEXAM) {
			LocalDateTime scheduledDateTime = LocalDateTime.of(subjectExam.getScheduleTestDate(),
					subjectExam.getExamStartTime());
			LocalDateTime examEndTime = scheduledDateTime.plus(subjectExam.getExamTimer() + 1, ChronoUnit.MINUTES);
			LocalDateTime now = LocalDateTime.now();

			if (!now.isBefore(examEndTime) && !now.isAfter(scheduledDateTime)) {
				System.out.println("Exam submission is not allowed at this time.");
				response.put(AppConstants.MESSAGE, AppConstants.SORRY_EXAM_END);
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
		}
		Optional<SubjectExamResult> result = subjectExamResultRepo.findByExamIdAndStudentId(request.getExamId(),
				student);
		if (result.isPresent())
			throw new ResourceAlreadyExistException("You have already submitted this test");

		SubjectExamResult examResult = new SubjectExamResult();
		Map<Integer, String> review = request.getReview();
		int correct = 0;
		int inCorrect = 0;
		examResult.setSubjectExamId(request.getExamId());
		examResult.setStudent(student);

		List<Question> questions = subject.getQuestions().stream()
				.filter(obj -> request.getQuestionList().contains(obj.getQuestionId())).collect(Collectors.toList());

		subject.getChapters().stream().forEach(temp -> {
			temp.getExam().getQuestions().stream().forEach(temp1 -> {
				if (request.getQuestionList().contains(temp1.getQuestionId())) {
					questions.add(temp1);
				}
			});
		});

		for (Question q : questions) {
			Integer id = q.getQuestionId();
			String correctOption = q.getCorrectOption();

			if (Objects.nonNull(review)) {
				String reviewAns = review.get(id);
				if (Objects.nonNull(reviewAns)) {
					if (review.get(id).equals(correctOption)) {
						correct++;
					} else {
						inCorrect++;
					}
				}
			}
		}

		examResult.setSubject(subject);
		examResult.setSubjectExamId(request.getExamId());
		examResult.setRandomQuestoinList(request.getQuestionList());
		examResult.setReview(review);
		examResult.setCorrecteQuestions(correct);
		examResult.setWrongQuestions(inCorrect);
		examResult.setNotSelectedQuestions(questions.size() - (correct + inCorrect));
		examResult.setScoreGet(correct - inCorrect);
		examResult.setTotalQuestion(questions.size());
		SubjectExamResult save = subjectExamResultRepo.save(examResult);

		subjectExam.getResults().add(save);
		subjectExamRepo.save(subjectExam);

		ExamResultResponse res = new ExamResultResponse();
		res.setCorrecteQuestions(save.getCorrecteQuestions());
		res.setId(save.getNotSelectedQuestions());
		res.setScoreGet(save.getScoreGet());
		res.setWrongQuestions(save.getWrongQuestions());
		res.setId(save.getId());

		// .....firebase notification .....//

//		NotificationInfo fcmIds = studentRepository.findFcmIdByStudentId(student.getStudentId());
//		String message = String.format("Congratulations! You have successfully completed your exam. Well done!");
//		fcmIds.setMessage(message);
//		fcmIds.setTitle("Exam Completed!");
//		kafkaProducerService.sendNotification(NotificationConstant.COMMON_TOPIC, fcmIds.toString());
		// .....firebase notification .....//

		// Save submitted exam question history
		List<SubmittedExamQuestionHistory> submittedHistoryList = new ArrayList<>();
		for (Integer question : request.getQuestionList()) {
			SubmittedExamQuestionHistory history = new SubmittedExamQuestionHistory();
			history.setStudent(student);
			history.setQuestionId(question);
			history.setExamId(request.getExamId());
			history.setSubjectId(request.getSubjectId());
			submittedHistoryList.add(history);
		}
		submittedExamHistoryRepo.saveAll(submittedHistoryList);

		return new ResponseEntity<>(res, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<?> getChapterExamResult(Integer id) {

		Map<String, Object> response = new HashMap<>();

		ChapterExamResult examResult = chapterExamResultRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException(AppConstants.NO_DATA_FOUND));
		ChapterExamResultResponse chapterExamResultResponse = new ChapterExamResultResponse();

		chapterExamResultResponse.setCorrecteQuestions(examResult.getCorrecteQuestions());
		chapterExamResultResponse.setId(examResult.getId());
		chapterExamResultResponse.setNotSelectedQuestions(examResult.getNotSelectedQuestions());
		chapterExamResultResponse.setReview(examResult.getReview());
		chapterExamResultResponse.setWrongQuestions(examResult.getWrongQuestions());
		chapterExamResultResponse.setTotalQuestion(examResult.getTotalQuestion());
		chapterExamResultResponse.setScoreGet(examResult.getScoreGet());
		chapterExamResultResponse
				.setSelectedQuestions((examResult.getTotalQuestion() - examResult.getNotSelectedQuestions()));
		List<QuestionResponse> questions = examResult.getChapter().getExam().getQuestions().stream()
				.map(obj -> questionFilter(obj)).collect(Collectors.toList());

		response.put("examResult", chapterExamResultResponse);
		response.put("questions", questions);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getSubjectExamResult(Integer resultId) {

		Map<String, Object> response = new HashMap<>();

		Optional<SubjectExamResult> result = subjectExamResultRepo.findById(resultId);

		if (result.isEmpty()) {
			response.put(AppConstants.MESSAGE, AppConstants.NO_RESUTL_FOUND);
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		}

		List<QuestionResponse> questiosList = questionRepo
				.findAllByIdAndIsDeletedFalse(result.get().getRandomQuestoinList()).stream()
				.map(obj -> questionFilter(obj)).collect(Collectors.toList());

		response.put("examResult", chapterExamResultResponseFilter(result.get()));
		response.put("questions", questiosList);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	public ChapterExamResultResponse chapterExamResultResponseFilter(SubjectExamResult examResult) {
		ChapterExamResultResponse chapterExamResultResponse = new ChapterExamResultResponse();

		chapterExamResultResponse.setCorrecteQuestions(examResult.getCorrecteQuestions());
		chapterExamResultResponse.setId(examResult.getId());
		chapterExamResultResponse.setNotSelectedQuestions(examResult.getNotSelectedQuestions());
		chapterExamResultResponse.setReview(examResult.getReview());
		chapterExamResultResponse.setWrongQuestions(examResult.getWrongQuestions());
		chapterExamResultResponse.setTotalQuestion(examResult.getTotalQuestion());
		chapterExamResultResponse.setScoreGet(examResult.getScoreGet());

		return chapterExamResultResponse;

	}

	public QuestionResponse questionFilter(Question question) {
		QuestionResponse questionResponse = new QuestionResponse();
		questionResponse.setCorrectOption(question.getCorrectOption());
		questionResponse.setOption1(question.getOption1());
		questionResponse.setOption2(question.getOption2());
		questionResponse.setOption3(question.getOption3());
		questionResponse.setOption4(question.getOption4());
		questionResponse.setSelectedOption(question.getSelectedOption());
		questionResponse.setQuestionId(question.getQuestionId());
		questionResponse.setQuestionContent(question.getQuestionContent());
		questionResponse.setQuestionImage(question.getQuestionImage());

		return questionResponse;
	}

	@Override
	public ResponseEntity<?> getChapterExamIsCompleteOrNot(Integer chapterId, Integer studentId) {
		Map<String, Object> response = new HashMap<>();

		// Fetch Chapter
		Optional<Chapter> chapterOpt = chapterRepo.findByChapterIdAndIsDeleted(chapterId, false);
		if (chapterOpt.isEmpty()) {
			response.put(AppConstants.MESSAGE, AppConstants.EXAM_NOT_FOUND);
			response.put(AppConstants.STATUS, false);
			return ResponseEntity.ok(response);
		}
		Chapter chapter = chapterOpt.get();

		// Check if Exam exists
		if (chapter.getExam() == null) {
			response.put(AppConstants.MESSAGE, AppConstants.EXAM_NOT_FOUND);
			response.put(AppConstants.STATUS, false);
			return ResponseEntity.ok(response);
		}

		// Fetch Student
		Student student = studentRepository.findByStudentId(studentId);
		if (student == null) {
			response.put(AppConstants.MESSAGE, "Student not found");
			response.put(AppConstants.STATUS, false);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		// Fetch Exam Result
		Optional<ChapterExamResult> examResultOpt = chapterExamResultRepo.findByChapterAndStudent(chapter, student);

		if (examResultOpt.isPresent()) {
			response.put(AppConstants.MESSAGE, AppConstants.DATA_FOUND);
			response.put("resultId", examResultOpt.get().getId());
			response.put(AppConstants.STATUS, true);
		} else if (!chapter.getExam().getQuestions().isEmpty()) {
			response.put(AppConstants.MESSAGE, "takeATest");
			response.put("isExamCompleted", Boolean.FALSE);
		} else {
			response.put(AppConstants.MESSAGE, "Exam has no questions");
		}

		return ResponseEntity.ok(response);
	}

	@Override
	public ResponseEntity<?> getChapterExamResultByChaterId(Integer chapterId) {
		Map<String, Object> response = new HashMap<>();
		List<ExamResultResponse> findAllById = chapterExamResultRepo.findAllStudentResultWithChapterId(chapterId);
		if (Objects.nonNull(findAllById)) {
			response.put("examResult", findAllById);
		} else {
			response.put(AppConstants.MESSAGE, AppConstants.NO_DATA_FOUND);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getSubjectExamResultesBySubjectId(Integer examId) {

		Map<String, Object> response = new HashMap<>();
		List<ExamResultResponse> list = subjectExamRepo.findAllStudentResultWithExamId(examId);
		if (Objects.nonNull(list)) {
			response.put("examResult", list);
		} else {
			response.put(AppConstants.MESSAGE, AppConstants.NO_DATA_FOUND);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	public SubjectExam checkSubjectExamIsPresent(Integer examId) {
		SubjectExam exam = subjectExamRepo.findById(examId)
				.orElseThrow(() -> new ResourceNotFoundException("Exam not Found!!"));
		return exam;
	}

	public LocalDateTime changeIntoLocalDateTime(LocalDate date, LocalTime time) {

		LocalDate scheduleTestDate = LocalDate.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
		LocalTime examStartTime = LocalTime.of(time.getHour(), time.getMinute());
		return scheduleTestDate.atTime(examStartTime);
	}

	@Override
	public ResponseEntity<?> addSubjectExam(AddExamRequest request) {

		Map<String, Object> response = new HashMap<>();

		Subject subject = subjectServiceImpl.checkSubjectIsPresent(request.getSubjectId());
		SubjectExam exam = new SubjectExam();

		Optional<SubjectExam> isExamExist = subject.getExams().stream()
				.filter(obj -> obj.getExamName().equals(request.getExamName().trim())).findFirst();

		// checking exam existance with the name;
		boolean contains = isExamExist.isPresent() && subject.getExams().contains(isExamExist.get());

		if (contains)
			throw new ResourceAlreadyExistException(AppConstants.EXAM_ALREADY_PRESENT_WITH_THIS_NAME);

		// schedule exam case
		if (request.getScheduleTestDate() != null) {
			// checking the date must not be before or equals to current date time
			LocalDateTime scheduledDateTime = changeIntoLocalDateTime(request.getScheduleTestDate(),
					request.getExamStartTime());

			LocalDateTime currentDateTime = LocalDateTime.now();
			if (scheduledDateTime.isBefore(currentDateTime) || scheduledDateTime.isEqual(currentDateTime)) {
				response.put(AppConstants.MESSAGE, "Exam date and time must be in the future");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}

			// ensuring!. checking exam time not under previous exam duration time
			SubjectExam latestExam = subjectExamRepo.findLatestExam();

			if (latestExam != null && subject.getExams().contains(latestExam)) {

				LocalDateTime actuallatestExamTime = changeIntoLocalDateTime(latestExam.getScheduleTestDate(),
						latestExam.getExamStartTime());
				LocalDateTime latestExamTimeWithDuration = changeIntoLocalDateTime(latestExam.getScheduleTestDate(),
						latestExam.getExamStartTime().plusMinutes(latestExam.getExamTimer()));

				LocalDateTime requestDateTime = changeIntoLocalDateTime(request.getScheduleTestDate(),
						request.getExamStartTime());

				if (requestDateTime.isAfter(actuallatestExamTime)
						&& requestDateTime.isBefore(latestExamTimeWithDuration)) {
					Duration duration = Duration.between(requestDateTime, latestExamTimeWithDuration);

					long hours = duration.toHours();
					long minutes = duration.toMinutes() % 60;

					String message = "";
					if (hours != 0)
						message = String.format(
								"Please add the exam after %d hours and %d minutes from request date and time. An another exam is already scheduled during this time. or Add after this time",
								hours, minutes);
					else
						message = String.format(
								"Please add the exam after  %d minutes  from request date and time. An another exam is already scheduled during this time. or Add after this time",
								minutes);

					response.put(AppConstants.MESSAGE, message);
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				}

			}

			exam.setScheduleTestDate(request.getScheduleTestDate());
			exam.setExamStartTime(request.getExamStartTime());
			exam.setExamType(ExamType.SCHEDULEEXAM);

		} else {
			exam.setExamType(ExamType.NORMALEXAM);
		}

		exam.setPassingMarks(request.getPassingMarks());
		exam.setExamImage(subject.getTechnologyStack().getImageName());
		exam.setExamName(request.getExamName().trim());
		exam.setTotalQuestionForTest(request.getTotalQuestionForTest());
		exam.setExamTimer(request.getExamTimer());
		exam.setCreatedDate(LocalDateTime.now());
		exam.setUpdatedDate(LocalDateTime.now());
		SubjectExam savedExam = subjectExamRepo.save(exam);
		subject.getExams().add(savedExam);
		subjectRepository.save(subject);

		response.put(AppConstants.SUBJECT_EXAM, subjectExamResponseFilter(savedExam));
		response.put(AppConstants.MESSAGE, AppConstants.EXAM_ADDED_SUCCESSFULLY);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> updateSubjectExam(AddExamRequest request) {

		Map<String, Object> response = new HashMap<>();

		SubjectExam exam = checkSubjectExamIsPresent(request.getExamId());
		Optional<SubjectExam> isExamExist = subjectExamRepo.findByExamName(request.getExamName().trim());
		if (isExamExist.isPresent()) {
			if (!exam.getExamName().trim().equals(isExamExist.get().getExamName()) && isExamExist.isPresent())
				throw new ResourceAlreadyExistException(AppConstants.EXAM_ALREADY_PRESENT_WITH_THIS_NAME);
		}
		if (exam.getResults().size() != 0 || exam.getIsStart()) {
			response.put(AppConstants.MESSAGE, "Cannot update exam: It is either completed or currently live");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		if (exam.getExamType().equals(ExamType.SCHEDULEEXAM)) {

			if (request.getScheduleTestDate() != null)
				exam.setScheduleTestDate(request.getScheduleTestDate());
			if (request.getExamStartTime() != null)
				exam.setExamStartTime(request.getExamStartTime());

			// checking the date must be after or equals to current date time

			LocalDateTime scheduledDateTime = changeIntoLocalDateTime(request.getScheduleTestDate(),
					request.getExamStartTime());
			LocalDateTime currentDateTime = LocalDateTime.now();
			if (scheduledDateTime.isBefore(currentDateTime) || scheduledDateTime.isEqual(currentDateTime)) {
				response.put(AppConstants.MESSAGE, "Exam date and time cannot be in the past");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}

			// ensuring!. checking exam time not under previous exam duration time

			Subject subject = subjectServiceImpl.checkSubjectIsPresent(request.getSubjectId());
			SubjectExam latestExam = subjectExamRepo.findLatestExam();

			if (latestExam != null && subject.getExams().contains(latestExam)) {

				LocalDateTime actuallatestExamTime = changeIntoLocalDateTime(latestExam.getScheduleTestDate(),
						latestExam.getExamStartTime());
				LocalDateTime latestExamTimeWithDuration = changeIntoLocalDateTime(latestExam.getScheduleTestDate(),
						latestExam.getExamStartTime().plusMinutes(latestExam.getExamTimer()));

				LocalDateTime requestDateTime = changeIntoLocalDateTime(request.getScheduleTestDate(),
						request.getExamStartTime());

				if (requestDateTime.isAfter(actuallatestExamTime)
						&& requestDateTime.isBefore(latestExamTimeWithDuration)) {
					Duration duration = Duration.between(requestDateTime, latestExamTimeWithDuration);

					long hours = duration.toHours();
					long minutes = duration.toMinutes() % 60;

					String message = "";
					if (hours != 0)
						message = String.format(
								"Please add the exam after %d hours and %d minutes from request date and time. An another exam is already scheduled during this time. or Add after this time",
								hours, minutes);
					else
						message = String.format(
								"Please add the exam after  %d minutes  from request date and time. An another exam is already scheduled during this time. or Add after this time",
								minutes);

					response.put(AppConstants.MESSAGE, message);
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				}

			}

		}

		if (request.getPassingMarks() != null)
			exam.setPassingMarks(request.getPassingMarks());
		if (request.getExamName() != null)
			exam.setExamName(request.getExamName());
		if (request.getExamTimer() != null)
			exam.setExamTimer(request.getExamTimer());
		if (request.getTotalQuestionForTest() != null)
			exam.setTotalQuestionForTest(request.getTotalQuestionForTest());

		exam.setUpdatedDate(LocalDateTime.now());

		SubjectExam examRes = subjectExamRepo.save(exam);
		response.put(AppConstants.EXAM, subjectExamResponseFilter(examRes));

		response.put(AppConstants.MESSAGE, AppConstants.UPDATE_SUCCESSFULLY);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> deleteSubjectExam(Integer examId) {

		Map<String, Object> response = new HashMap<>();
		SubjectExam exam = checkSubjectExamIsPresent(examId);
		if (!exam.getIsStart()) {
			subjectExamRepo.delete(exam);
			response.put(AppConstants.MESSAGE, AppConstants.DELETE_SUCCESS);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} else {
			response.put(AppConstants.MESSAGE, "Can't delete this exam");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

	}

	// for admin use
	@Override
	public ResponseEntity<?> getAllSubjectNormalAndScheduleExam(Integer subjectId) {
		Subject subject = subjectRepository.findById(subjectId)
				.orElseThrow(() -> new ResourceNotFoundException("subject not found!!"));

		Map<String, Object> response = new HashMap<>();
		List<SubjectExamResponse> normalExam = new ArrayList<>();
		List<SubjectExamResponse> scheduleExam = new ArrayList<>();
		subject.getExams().stream().forEach(obj -> {
			if (obj.getExamType().equals(ExamType.SCHEDULEEXAM)) {
				SubjectExamResponse res = subjectExamResponseFilter(obj);
				scheduleExam.add(res);
			} else {
				SubjectExamResponse res = subjectExamResponseFilter(obj);
				normalExam.add(res);
			}
		});

		response.put(AppConstants.NORMAL_EXAM, normalExam);
		response.put(AppConstants.SCHEDULE_EXAM, scheduleExam);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	public SubjectExamResponse subjectExamResponseFilter(SubjectExam e) {
		SubjectExamResponse res = new SubjectExamResponse();
		res.setExamId(e.getExamId());
		res.setExamImage(e.getExamImage());
		res.setExamTimer(e.getExamTimer());
		res.setTotalQuestionForTest(e.getTotalQuestionForTest());
		res.setPassingMarks(e.getPassingMarks());
		res.setExamName(e.getExamName());
		res.setExamType(e.getExamType());
		res.setScheduleTestDate(e.getScheduleTestDate());
		res.setExamStartTime(e.getExamStartTime());
		res.setIsActive(e.getIsActive());
		res.setIsStart(e.getIsStart());
		return res;
	}

	// for student use
//	@Override
	public ResponseEntity<?> getAllSubjectNormalAndScheduleExamForStudent(Integer studentId) {
//		studentRepository.findById(studentId).orElseThrow(() -> new ResourceNotFoundException("student not found !! "));
//
//		Map<String, Object> response = new HashMap<>();
//		List<SubjectExamResponse> allSubjectExam = new ArrayList<>();
//
//		allSubjectExam = subjectRepository.getAllSubjectExam(studentId);
//		List<SubjectExamResponse> normalExam = new ArrayList<>();
//		List<SubjectExamResponse> scheduleExam = new ArrayList<>();
//		allSubjectExam.stream().forEach(obj -> {
//			if (obj.getExamType().equals(ExamType.SCHEDULEEXAM)) {
//				LocalDateTime scheduledDateTime = LocalDateTime.of(obj.getScheduleTestDate(), obj.getExamStartTime());
//				LocalDateTime examEndTime = scheduledDateTime.plus(AppConstants.EXTRA_EXAM_TIME, ChronoUnit.MINUTES);
//				LocalDateTime now = LocalDateTime.now();
//
//				if (now.isBefore(examEndTime)) {
//					obj.setIsExamEnd(false); // Exam is not ended
//					obj.setExtraTime(1);
//				} else {
//					obj.setIsExamEnd(true);
//				}
//				scheduleExam.add(obj);
//			} else {
//				normalExam.add(obj);
//			}
//		});
//		response.put(AppConstants.NORMAL_EXAM, normalExam);
//		response.put(AppConstants.SCHEDULE_EXAM, scheduleExam);
//		response.put(AppConstants.MESSAGE, AppConstants.SUCCESS);
//		return new ResponseEntity<>(response, HttpStatus.OK);
		return null;
	}

	@Override
	public ResponseEntity<?> changeSubjectExamStatus(Integer examId) {
		Map<String, Object> response = new HashMap<>();
		SubjectExam exam = checkSubjectExamIsPresent(examId);
		if (!exam.getIsStart()) {
			exam.setIsActive(!exam.getIsActive());
			subjectExamRepo.save(exam);

			// .....firebase notification .....//

			List<NotificationInfo> fcmIds = studentRepository.findAllFcmIdByExamId(examId);
			String message = String
					.format("An exam has been scheduled. Please check the details and prepare accordingly.");

			List<NotificationInfo> newlist = fcmIds.parallelStream().map(obj -> {
				obj.setMessage(message);
				obj.setTitle("Exam Scheduled!");
				return obj;
			}).toList();
			kafkaProducerService.sendNotification(NotificationConstant.COMMON_TOPIC, newlist.toString());

			// .....firebase notification .....//

			response.put("isActive", exam.getIsActive());
			return new ResponseEntity<>(response, HttpStatus.OK);
		} else {
			response.put(AppConstants.MESSAGE, "Can't inactive this exam!. Exam is scheduled or live");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> setSubjectExamStartStatus(Integer examId) {

		SubjectExam exam = checkSubjectExamIsPresent(examId);
		exam.setIsStart(true);
		subjectExamRepo.save(exam);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	public ResponseEntity<?> getSubjectExamCount(Integer studentId) {

		Map<String, Object> response = new HashMap<>();
		Long normalExamCount = subjectExamRepo.fetchSubjectExamCount(ExamType.NORMALEXAM, studentId);
		Long scheduleExamCount = subjectExamRepo.fetchSubjectExamCount(ExamType.SCHEDULEEXAM, studentId);
		Long totalNormalExamCount = subjectExamRepo.fetchTotalExamCount(studentId, ExamType.NORMALEXAM);
		Long totalScheduleExamCount = subjectExamRepo.fetchTotalExamCount(studentId, ExamType.SCHEDULEEXAM);
		response.put("normalExamCount", normalExamCount);
		response.put("scheduleExamCount", scheduleExamCount);
		response.put("totalNormalCount", totalNormalExamCount);
		response.put("totalScheduleExamCount", totalScheduleExamCount);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	// ******************************************

	@Override
	public ResponseEntity<?> setChapterExamStartStatus(Integer chapterId) {

		Map<String, Object> response = new HashMap<>();
		Optional<Chapter> chapter = chapterRepo.findByChapterIdAndIsDeleted(chapterId, false);
		if (chapter.isPresent()) {

			Optional<Exam> exam = examRepo.findByExamIdAndIsDeleted(chapter.get().getExam().getExamId(), false);
			if (exam.isPresent()) {
				exam.get().setIsStarted(true);
				examRepo.save(exam.get());
				return new ResponseEntity<>(HttpStatus.OK);
			}
		}
		response.put(AppConstants.MESSAGE, "Chapter not found");
		return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);

	}

	@Override
	public ResponseEntity<?> changeChapterExamStatus(Integer examId) {

		Optional<Exam> exam = examRepo.findByExamIdAndIsDeleted(examId, false);
		Map<String, Object> response = new HashMap<>();
		if (exam.isPresent()) {
			if (!exam.get().getIsStarted()) {

				if (exam.get().getQuestions().size() == 0) {
					response.put(AppConstants.MESSAGE, "Can't active this exam!. Add some questions here");
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				}

				exam.get().setIsActive(!exam.get().getIsActive());
				examRepo.save(exam.get());

				response.put("isActive", exam.get().getIsActive());
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else {
				response.put(AppConstants.MESSAGE, "Can't inactive this exam!. Exam is scheduled");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
		}
		return new ResponseEntity<>("NOT FOUND", HttpStatus.NOT_FOUND);
	}

	@Override
	public ResponseEntity<?> getChapterExam(Integer chapterId) {

		Map<String, Object> response = new HashMap<>();
		Optional<Chapter> chapter = chapterRepo.findByChapterIdAndIsDeleted(chapterId, false);
		if (chapter.isPresent()) {
			response.put("testQuestions", chapter.get().getExam().getQuestions().parallelStream()
					.filter(obj -> !obj.getIsDeleted()).map(this::questionFilterWithoudCorrectOprion));
			response.put("examTimer", chapter.get().getExam().getExamTimer());
			response.put("chapterId", chapterId);

			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		response.put(AppConstants.MESSAGE, "Chapter not found");
		return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
	}

	public QuestionResponse questionFilterWithoudCorrectOprion(Question question) {
		QuestionResponse questionResponse = new QuestionResponse();
		questionResponse.setOption1(question.getOption1());
		questionResponse.setOption2(question.getOption2());
		questionResponse.setOption3(question.getOption3());
		questionResponse.setOption4(question.getOption4());
		questionResponse.setQuestionId(question.getQuestionId());
		questionResponse.setQuestionContent(question.getQuestionContent());
		questionResponse.setQuestionImage(question.getQuestionImage());
		return questionResponse;
	}

	// *********************************COURSE EXAM
	// CODE*********************************//

	@Override
	public ResponseEntity<?> addCourseExamResult(ExamRequest request) {
		Map<String, String> response = new HashMap<>();

		// 1. Validate student and exam exist
		Student student = studentRepository.findById(request.getStudentId())
				.orElseThrow(() -> new ResourceNotFoundException("Student not found"));

		CourseExam courseExam = courseExamRepo.findById(request.getExamId())
				.orElseThrow(() -> new ResourceNotFoundException(AppConstants.EXAM_NOT_FOUND));

		// 2. Check for scheduled exam time constraints
		if (courseExam.getExamType() == ExamType.SCHEDULEEXAM) {
			LocalDateTime scheduledDateTime = LocalDateTime.of(courseExam.getScheduleTestDate(),
					courseExam.getExamStartTime());
			LocalDateTime examEndTime = scheduledDateTime.plus(courseExam.getExamTimer() + 1, ChronoUnit.MINUTES);
			LocalDateTime now = LocalDateTime.now();

			if (!now.isBefore(examEndTime) && !now.isAfter(scheduledDateTime)) {
				response.put(AppConstants.MESSAGE, AppConstants.SORRY_EXAM_END);
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
		}

		// 3. Check if student already submitted this exam
		Optional<CourseExamResult> existingResult = courseExamResultRepo.findByCourseExamAndStudent(courseExam,
				student);
		if (existingResult.isPresent()) {
			throw new ResourceAlreadyExistException("You have already submitted this test");
		}

		// 4. Get all questions from all subjects in the course
		List<Question> questions = new ArrayList<>();
		Course course = courseExam.getCourse();

		for (Subject subject : course.getSubjects()) {
			// Add subject's direct questions
			questions.addAll(subject.getQuestions().stream()
					.filter(q -> request.getQuestionList().contains(q.getQuestionId())).collect(Collectors.toList()));

			// Add questions from subject's chapters
			subject.getChapters().forEach(chapter -> {
				chapter.getExam().getQuestions().forEach(chapterQuestion -> {
					if (request.getQuestionList().contains(chapterQuestion.getQuestionId())) {
						questions.add(chapterQuestion);
					}
				});
			});
		}

		// 5. Calculate results
		CourseExamResult examResult = new CourseExamResult();
		Map<Integer, String> review = request.getReview();
		int correct = 0;
		int incorrect = 0;

		for (Question q : questions) {
			Integer id = q.getQuestionId();
			String correctOption = q.getCorrectOption();

			if (Objects.nonNull(review)) {
				String reviewAns = review.get(id);
				if (Objects.nonNull(reviewAns)) {
					if (reviewAns.equals(correctOption)) {
						correct++;
					} else {
						incorrect++;
					}
				}
			}
		}

		// 6. Set exam result properties
		examResult.setStudent(student);
		examResult.setCourseExam(courseExam);
		examResult.setRandomQuestoinList(request.getQuestionList());
		examResult.setReview(review);
		examResult.setCorrecteQuestions(correct);
		examResult.setWrongQuestions(incorrect);
		examResult.setNotSelectedQuestions(questions.size() - (correct + incorrect));
		examResult.setScoreGet(correct - incorrect);
		examResult.setTotalQuestion(questions.size());
		examResult.setCourseExam(courseExam); // 7. Save the result
		CourseExamResult savedResult = courseExamResultRepo.save(examResult);

		// 8. Prepare response
		ExamResultResponse res = new ExamResultResponse();
		res.setCorrecteQuestions(savedResult.getCorrecteQuestions());
		res.setNotSelectedQuestions(savedResult.getNotSelectedQuestions());
		res.setScoreGet(savedResult.getScoreGet());
		res.setWrongQuestions(savedResult.getWrongQuestions());
		res.setId(savedResult.getId());

		// 9. Save submitted exam question history
		List<SubmittedExamQuestionHistory> submittedHistoryList = new ArrayList<>();
		for (Integer questionId : request.getQuestionList()) {
			SubmittedExamQuestionHistory history = new SubmittedExamQuestionHistory();
			history.setStudent(student);
			history.setQuestionId(questionId);
			history.setExamId(request.getExamId());
			history.setCourseId(course.getCourseId()); // Store course ID instead of subject ID
			submittedHistoryList.add(history);
		}
		submittedExamHistoryRepo.saveAll(submittedHistoryList);

		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getCourseExamResult(Integer resultId) {

		Map<String, Object> response = new HashMap<>();
		CourseExamResult examResult = courseExamResultRepo.findById(resultId)
				.orElseThrow(() -> new ResourceNotFoundException(AppConstants.NO_DATA_FOUND));
		List<QuestionResponse> questiosList = questionRepo
				.findAllByIdAndIsDeletedFalse(examResult.getRandomQuestoinList()).stream()
				.map(obj -> questionFilter(obj)).collect(Collectors.toList());
		response.put("examResult", courseExamResultResponseFilter(examResult));
		response.put("questions", questiosList);
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	public CourseExamResultResponse courseExamResultResponseFilter(CourseExamResult examResult) {
		CourseExamResultResponse courseExamResultResponse = new CourseExamResultResponse();
		courseExamResultResponse.setCorrecteQuestions(examResult.getCorrecteQuestions());
		courseExamResultResponse.setId(examResult.getId());
		courseExamResultResponse.setNotSelectedQuestions(examResult.getNotSelectedQuestions());
		courseExamResultResponse.setReview(examResult.getReview());
		courseExamResultResponse.setWrongQuestions(examResult.getWrongQuestions());
		courseExamResultResponse.setTotalQuestion(examResult.getTotalQuestion());
		courseExamResultResponse.setScoreGet(examResult.getScoreGet());
		return courseExamResultResponse;

	}

	@Override
	public ResponseEntity<?> getCourseExamResultsByExamId(Integer examId) {

		Map<String, Object> response = new HashMap<>();
		List<ExamResultResponse> list = courseExamResultRepo.findAllResutls(examId);
		if (Objects.nonNull(list)) {
			response.put("examResult", list);
		} else {
			response.put(AppConstants.MESSAGE, AppConstants.NO_DATA_FOUND);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	public CourseExam checkCourseExamIsPresent(Integer examId) {
		return courseExamRepo.findById(examId).orElseThrow(() -> new ResourceNotFoundException("Exam not Found!!"));

	}

	public ResponseEntity<?> addCourseExam(AddExamRequest request) {
		Map<String, Object> response = new HashMap<>();

		// 1. Validate course exists
		Course course = courseRepository.findById(request.getCourseId())
				.orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + request.getCourseId()));

		// 2. Check for duplicate exam name in the same course
		boolean examExists = courseExamRepo.existsByExamNameAndCourseId(request.getExamName().trim(),
				request.getCourseId());

		if (examExists) {
			throw new ResourceAlreadyExistException("Exam with this name already exists in the course");
		}

		// 3. Create new exam
		CourseExam exam = new CourseExam();
		exam.setExamName(request.getExamName().trim());
		exam.setPassingMarks(request.getPassingMarks());
		exam.setTotalQuestionForTest(request.getTotalQuestionForTest());
		exam.setExamTimer(request.getExamTimer());
		exam.setCourse(course);

		// Set technology stack image from course
		if (course.getTechnologyStack() != null) {
			exam.setExamImage(course.getTechnologyStack().getImageName());
		}

		// 4. Handle scheduled exam specific logic
		if (request.getScheduleTestDate() != null) {
			LocalDateTime scheduledDateTime = LocalDateTime.of(request.getScheduleTestDate(),
					request.getExamStartTime());

			// Validate future date
			if (scheduledDateTime.isBefore(LocalDateTime.now())) {
				response.put(AppConstants.MESSAGE, "Exam schedule must be in the future");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}

			// Check for scheduling conflicts
			List<CourseExam> conflictingExams = courseExamRepo.findScheduledExamsByCourse(request.getCourseId(),
					request.getScheduleTestDate(), request.getExamStartTime(), request.getExamTimer());

			if (!conflictingExams.isEmpty()) {
				CourseExam conflictingExam = conflictingExams.get(0);
				LocalDateTime conflictEnd = LocalDateTime
						.of(conflictingExam.getScheduleTestDate(), conflictingExam.getExamStartTime())
						.plusMinutes(conflictingExam.getExamTimer());

				Duration remaining = Duration.between(scheduledDateTime, conflictEnd);
				String message = String.format(
						"There's a scheduling conflict with '%s'. Please schedule after %d hours and %d minutes.",
						conflictingExam.getExamName(), remaining.toHours(), remaining.toMinutesPart());

				response.put(AppConstants.MESSAGE, message);
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}

			exam.setScheduleTestDate(request.getScheduleTestDate());
			exam.setExamStartTime(request.getExamStartTime());
			exam.setExamType(ExamType.SCHEDULEEXAM);
		} else {
			exam.setExamType(ExamType.NORMALEXAM);
		}

		// 5. Set audit fields
		exam.setCreatedDate(LocalDateTime.now());
		exam.setUpdatedDate(LocalDateTime.now());
		exam.setIsActive(true);
		exam.setIsDeleted(false);

		// 6. Save the exam
		CourseExam savedExam = courseExamRepo.save(exam);

		// 7. Prepare response
		response.put("courseExam", mapCourseExamToResponse(savedExam));
		response.put(AppConstants.MESSAGE, "Course exam created successfully");
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	// Helper method to map entity to response DTO
	private Map<String, Object> mapCourseExamToResponse(CourseExam exam) {
		Map<String, Object> response = new HashMap<>();
		response.put("examId", exam.getExamId());
		response.put("examName", exam.getExamName());
		response.put("examType", exam.getExamType());
		response.put("examTimer", exam.getExamTimer());
		response.put("passingMarks", exam.getPassingMarks());
		response.put("totalQuestionForTest", exam.getTotalQuestionForTest());
		response.put("scheduleTestDate", exam.getScheduleTestDate());
		response.put("examStartTime", exam.getExamStartTime());
		response.put("createdDate", exam.getCreatedDate());
		response.put("courseId", exam.getCourse().getCourseId());
		response.put("isActive", exam.getIsActive());
		response.put("examImage", exam.getExamImage());
		return response;
	}

	@Override
	public ResponseEntity<?> setCourseExamStartStatus(Integer examId) {

		CourseExam exam = checkCourseExamIsPresent(examId);
		exam.setIsStart(true);
		courseExamRepo.save(exam);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> changeCourseExamStatus(Integer examId) {
		Map<String, Object> response = new HashMap<>();
		CourseExam exam = checkCourseExamIsPresent(examId);
		if (!exam.getIsStart()) {
			exam.setIsActive(!exam.getIsActive());
			courseExamRepo.save(exam);

			// .....firebase notification .....//

			List<NotificationInfo> fcmIds = studentRepository.findAllFcmIdByExamId(examId);
			String message = String
					.format("An exam has been scheduled. Please check the details and prepare accordingly.");

			List<NotificationInfo> newlist = fcmIds.parallelStream().map(obj -> {
				obj.setMessage(message);
				obj.setTitle("Exam Scheduled!");
				return obj;
			}).toList();
			kafkaProducerService.sendNotification(NotificationConstant.COMMON_TOPIC, newlist.toString());

			// .....firebase notification .....//

			response.put("isActive", exam.getIsActive());
			return new ResponseEntity<>(response, HttpStatus.OK);
		} else {
			response.put(AppConstants.MESSAGE, "Can't inactive this exam!. Exam is scheduled or live");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
	}

	// for student use
	@Override
	public ResponseEntity<?> getAllCourseNormalAndScheduleExamForStudent(Integer studentId) {
		studentRepository.findById(studentId).orElseThrow(() -> new ResourceNotFoundException("student not found !! "));

		Map<String, Object> response = new HashMap<>();
		List<CourseExamResponse> allCourseExam = new ArrayList<>();
		// Fetching all course exams for the student
		allCourseExam = courseExamRepo.findAllCourseExam(studentId);

		List<CourseExamResponse> normalExam = new ArrayList<>();
		List<CourseExamResponse> scheduleExam = new ArrayList<>();
		allCourseExam.stream().forEach(obj -> {
			if (obj.getExamType().equals(ExamType.SCHEDULEEXAM)) {
				LocalDateTime scheduledDateTime = LocalDateTime.of(obj.getScheduleTestDate(), obj.getExamStartTime());
				LocalDateTime examEndTime = scheduledDateTime.plus(AppConstants.EXTRA_EXAM_TIME, ChronoUnit.MINUTES);
				LocalDateTime now = LocalDateTime.now();

				if (now.isBefore(examEndTime)) {
					obj.setIsExamEnd(false); // Exam is not ended
					obj.setExtraTime(1);
				} else {
					obj.setIsExamEnd(true);
				}
				scheduleExam.add(obj);
			} else {
				normalExam.add(obj);
			}
		});
		response.put(AppConstants.NORMAL_EXAM, normalExam);
		response.put(AppConstants.SCHEDULE_EXAM, scheduleExam);
		response.put(AppConstants.MESSAGE, AppConstants.SUCCESS);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getAllCourseNormalAndScheduleExam(Integer couseId) {
		courseRepository.findById(couseId).orElseThrow(() -> new ResourceNotFoundException("course not found!!"));

		Map<String, Object> response = new HashMap<>();
		List<CourseExamResponse> normalExam = new ArrayList<>();
		List<CourseExamResponse> scheduleExam = new ArrayList<>();

		List<CourseExam> exams = courseExamRepo.findAllByCourse_CourseId(couseId);

		exams.stream().forEach(obj -> {
			if (obj.getExamType().equals(ExamType.SCHEDULEEXAM)) {
				CourseExamResponse res = courseExamResponseFilter(obj);
				scheduleExam.add(res);
			} else {
				CourseExamResponse res = courseExamResponseFilter(obj);
				normalExam.add(res);
			}
		});

		response.put(AppConstants.NORMAL_EXAM, normalExam);
		response.put(AppConstants.SCHEDULE_EXAM, scheduleExam);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	public CourseExamResponse courseExamResponseFilter(CourseExam e) {
		CourseExamResponse res = new CourseExamResponse();
		res.setExamId(e.getExamId());
		res.setExamImage(e.getExamImage());
		res.setExamTimer(e.getExamTimer());
		res.setTotalQuestionForTest(e.getTotalQuestionForTest());
		res.setPassingMarks(e.getPassingMarks());
		res.setExamName(e.getExamName());
		res.setExamType(e.getExamType());
		res.setScheduleTestDate(e.getScheduleTestDate());
		res.setExamStartTime(e.getExamStartTime());
		res.setIsActive(e.getIsActive());
		res.setIsStart(e.getIsStart());
		return res;
	}

	@Override
	public ResponseEntity<?> deleteExamById(Integer examId) {
		Map<String, Object> response = new HashMap<>();
		CourseExam exam = checkCourseExamIsPresent(examId);
		if (!exam.getIsStart()) {
			exam.setIsDeleted(true);
			exam.setIsActive(false);
			courseExamRepo.save(exam);
			response.put(AppConstants.MESSAGE, AppConstants.DELETE_SUCCESS);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} else {
			response.put(AppConstants.MESSAGE, "Can't delete this exam");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> updateCourseExam(AddExamRequest request) {
		Map<String, Object> response = new HashMap<>();

		// 1. Validate exam exists
		CourseExam exam = courseExamRepo.findById(request.getExamId())
				.orElseThrow(() -> new ResourceNotFoundException("Course exam not found"));

		// 2. Check for duplicate exam name in the same course
		Optional<CourseExam> isExamExist = courseExamRepo.findByCourseIdAndCourseName(request.getExamName().trim(),
				exam.getCourse().getCourseId());

		if (isExamExist.isPresent() && !exam.getExamId().equals(isExamExist.get().getExamId())) {
			throw new ResourceAlreadyExistException("Exam with this name already exists in the course");
		}

		Long resultSize = courseExamResultRepo.findByCourseExam(exam);

		// 3. Check if exam can be updated (not started or completed)
		if (resultSize > 0 || exam.getIsStart()) {
			response.put(AppConstants.MESSAGE, "Cannot update exam: It is either completed or currently live");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		// 4. Handle scheduled exam specific updates
		if (exam.getExamType().equals(ExamType.SCHEDULEEXAM)) {
			// Update schedule date and time if provided
			if (request.getScheduleTestDate() != null) {
				exam.setScheduleTestDate(request.getScheduleTestDate());
			}
			if (request.getExamStartTime() != null) {
				exam.setExamStartTime(request.getExamStartTime());
			}

			// Validate new schedule is in future
			LocalDateTime scheduledDateTime = changeIntoLocalDateTime(exam.getScheduleTestDate(),
					exam.getExamStartTime());
			LocalDateTime currentDateTime = LocalDateTime.now();

			if (scheduledDateTime.isBefore(currentDateTime) || scheduledDateTime.isEqual(currentDateTime)) {
				response.put(AppConstants.MESSAGE, "Exam date and time cannot be in the past");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}

			// Check for scheduling conflicts with other exams in the same course
			List<CourseExam> conflictingExams = courseExamRepo.findScheduledExamsByCourse(
					exam.getCourse().getCourseId(), exam.getScheduleTestDate(), exam.getExamStartTime(),
					exam.getExamTimer());

			if (!conflictingExams.isEmpty() && !conflictingExams.get(0).getExamId().equals(exam.getExamId())) {
				CourseExam conflictingExam = conflictingExams.get(0);
				LocalDateTime conflictEnd = LocalDateTime
						.of(conflictingExam.getScheduleTestDate(), conflictingExam.getExamStartTime())
						.plusMinutes(conflictingExam.getExamTimer());

				Duration remaining = Duration.between(scheduledDateTime, conflictEnd);
				String message = String.format(
						"There's a scheduling conflict with '%s'. Please schedule after %d hours and %d minutes.",
						conflictingExam.getExamName(), remaining.toHours(), remaining.toMinutesPart());

				response.put(AppConstants.MESSAGE, message);
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
		}

		// 5. Update exam properties
		if (request.getPassingMarks() != null) {
			exam.setPassingMarks(request.getPassingMarks());
		}
		if (request.getExamName() != null) {
			exam.setExamName(request.getExamName().trim());
		}
		if (request.getExamTimer() != null) {
			exam.setExamTimer(request.getExamTimer());
		}
		if (request.getTotalQuestionForTest() != null) {
			exam.setTotalQuestionForTest(request.getTotalQuestionForTest());
		}

		exam.setUpdatedDate(LocalDateTime.now());

		// 6. Save updated exam
		CourseExam updatedExam = courseExamRepo.save(exam);

		// 7. Prepare response
		response.put(AppConstants.EXAM, courseExamResponseFilter(updatedExam));
		response.put(AppConstants.MESSAGE, AppConstants.UPDATE_SUCCESSFULLY);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getCourseExamResultesBySubjectId(Integer examId) {
		// TODO Auto-generated method stub
		return null;
	}

	public ResponseEntity<?> filterCourseSubjectTest(ExamType examType, TestFilterRequest request) {
		// Early validation
		Integer courseId = request.getCourseId();
		Integer subjectId = request.getSubjectId();
		Integer studentId = request.getStudentId();

		if (courseId == null || studentId == null) {
			throw new ResourceNotFoundException("Course or Student ID cannot be null");
		}

		// Validate entities
		validateEntities(courseId, subjectId, studentId);

		// Prepare response
		Map<String, Object> response = new HashMap<>();
		response.put(AppConstants.MESSAGE, AppConstants.SUCCESS);
		// Create page request once
		PageRequest pageRequest = PageRequest.of(request.getPaginationRequest().getPageNumber(),
				request.getPaginationRequest().getPageSize());

		if (subjectId != null) {
			processSubjectExams(studentId, pageRequest, response, examType, request.getStatus());
		} else {
			processCourseExams(studentId, courseId, pageRequest, response, examType, request.getStatus());
		}

		return ResponseEntity.ok(response);
	}

	private void validateEntities(Integer courseId, Integer subjectId, Integer studentId) {
		if (subjectId != null && !subjectRepository.existsById(subjectId)) {
			throw new ResourceNotFoundException("Subject not found with id: " + subjectId);
		}

		if (!courseRepository.existsById(courseId)) {
			throw new ResourceNotFoundException("Course not found with id: " + courseId);
		}

		if (!studentRepository.existsById(studentId)) {
			throw new ResourceNotFoundException("Student not found with id: " + studentId);
		}

		if (!studentRepository.validateStudentCourse(courseId, studentId)) {
			throw new ResourceNotFoundException("Course not found for the student with id: " + studentId);
		}
	}

	private void processSubjectExams(Integer studentId, PageRequest pageRequest, Map<String, Object> response,
			ExamType examType, String status) {
		Page<SubjectExamResponse> exams = subjectRepository.getAllSubjectExam(examType, studentId, status, pageRequest);

		if (examType.equals(ExamType.SCHEDULEEXAM)) {
			exams = exams.map(this::processScheduleExam);
		}
		response.put(AppConstants.EXAM, exams);
	}

	private void processCourseExams(Integer studentId, Integer courseId, PageRequest pageRequest,
			Map<String, Object> response, ExamType examType, String status) {
		Page<CourseExamResponse> exams = courseExamRepo.findCourseExams(examType, courseId, studentId, status,
				pageRequest);

		if (examType.equals(ExamType.SCHEDULEEXAM)) {
			exams = exams.map(this::processScheduleExam);
		}
		response.put(AppConstants.EXAM, exams);
	}

	private SubjectExamResponse processScheduleExam(SubjectExamResponse exam) {
		LocalDateTime scheduledDateTime = LocalDateTime.of(exam.getScheduleTestDate(), exam.getExamStartTime());
		LocalDateTime examEndTime = scheduledDateTime.plus(AppConstants.EXTRA_EXAM_TIME, ChronoUnit.MINUTES);
		boolean isExamEnded = !LocalDateTime.now().isBefore(examEndTime);
		exam.setIsExamEnd(isExamEnded);
		if (!isExamEnded) {
			exam.setExtraTime(1);
		}
		return exam;
	}

	private CourseExamResponse processScheduleExam(CourseExamResponse exam) {
		LocalDateTime scheduledDateTime = LocalDateTime.of(exam.getScheduleTestDate(), exam.getExamStartTime());
		LocalDateTime examEndTime = scheduledDateTime.plus(AppConstants.EXTRA_EXAM_TIME, ChronoUnit.MINUTES);
		boolean isExamEnded = !LocalDateTime.now().isBefore(examEndTime);
		exam.setIsExamEnd(isExamEnded);
		if (!isExamEnded) {
			exam.setExtraTime(1);
		}
		return exam;
	}

	// ....................... NEW METHOD'S .........................

	public ResponseEntity<?> getAllUpcomingExams(Integer studentId, PaginationRequest request) {
		Student student = studentRepository.findById(studentId)
				.orElseThrow(() -> new ResourceNotFoundException(AppConstants.STUDENT_NOT_FOUND));

		// Fetch data
		List<SubjectExamResponse> subjectExams = subjectRepository.findUpcomingSubjectExams(ExamType.SCHEDULEEXAM,
				studentId);
		List<CourseExamResponse> courseExams = courseExamRepo.findUpcomingCourseExams(ExamType.SCHEDULEEXAM,
				student.getCourse().getCourseId(), studentId);

		// Convert both to UpcomingExamResponse
		List<UpcomingExamResponse> allExams = new ArrayList<>();

		for (SubjectExamResponse s : subjectExams) {
			UpcomingExamResponse response = new UpcomingExamResponse();
			response.setExamId(s.getExamId());
			response.setExamName(s.getExamName());
			response.setExamImage(s.getExamImage());
			response.setExamTimer(s.getExamTimer());
			response.setTotalQuestionForTest(s.getTotalQuestionForTest());
			response.setPassingMarks(s.getPassingMarks());
			response.setScoreGet(s.getScoreGet());
			response.setScheduleTestDate(s.getScheduleTestDate());
			response.setExamStartTime(s.getExamStartTime());
			response.setIsStart(s.getIsStart());
			response.setExamType(s.getExamType());
			response.setResultId(s.getResultId());
			response.setStatus(s.getStatus());
			response.setSubjectId(s.getSubjectId());
			response.setExamFrom("SUBJECT");
			allExams.add(response);
		}

		for (CourseExamResponse c : courseExams) {
			UpcomingExamResponse response = new UpcomingExamResponse();
			response.setExamId(c.getExamId());
			response.setExamName(c.getExamName());
			response.setExamImage(c.getExamImage());
			response.setExamTimer(c.getExamTimer());
			response.setTotalQuestionForTest(c.getTotalQuestionForTest());
			response.setPassingMarks(c.getPassingMarks());
			response.setScoreGet(c.getScoreGet());
			response.setScheduleTestDate(c.getScheduleTestDate());
			response.setExamStartTime(c.getExamStartTime());
			response.setIsStart(c.getIsStart());
			response.setExamType(c.getExamType());
			response.setResultId(c.getResultId());
			response.setStatus(c.getStatus());
			response.setCourseId(c.getCourseId());
			response.setExamFrom("COURSE");
			allExams.add(response);
		}

		// Sort by schedule date and time
		allExams.sort(Comparator
				.comparing(UpcomingExamResponse::getScheduleTestDate, Comparator.nullsLast(Comparator.naturalOrder()))
				.thenComparing(UpcomingExamResponse::getExamStartTime,
						Comparator.nullsLast(Comparator.naturalOrder())));

		// Manual Pagination
		int page = request.getPageNumber();
		int size = request.getPageSize();
		int totalElements = allExams.size();
		int totalPages = (int) Math.ceil((double) totalElements / size);
		int startIndex = page * size;
		int endIndex = Math.min(startIndex + size, totalElements);

		List<UpcomingExamResponse> paginatedList = startIndex >= totalElements ? Collections.emptyList()
				: allExams.subList(startIndex, endIndex);

		PageResponse<UpcomingExamResponse> pageResponse = new PageResponse<>();
		pageResponse.setResponse(paginatedList);
		pageResponse.setPage(page);
		pageResponse.setSize(size);
		pageResponse.setTotalElements(totalElements);
		pageResponse.setTotalPages(totalPages);
		pageResponse.setLast(page >= totalPages - 1);

		return ResponseEntity.ok(pageResponse);
	}

	@Override
	public ResponseEntity<?> getChapterExamNew(Integer chapterId) {

		Map<String, Object> response = new HashMap<>();
		Optional<Chapter> chapter = chapterRepo.findByChapterIdAndIsDeleted(chapterId, false);
		if (chapter.isPresent()) {
			response.put("testQuestions", chapter.get().getExam().getQuestions().parallelStream()
					.filter(obj -> !obj.getIsDeleted()).map(this::questionFilterWithoudCorrectOprionNew));
			response.put("examTimer", chapter.get().getExam().getExamTimer());
			response.put("chapterId", chapterId);
			response.put("subjectId", chapterRepo.findSubjectIdByChapterId(chapterId));
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		response.put(AppConstants.MESSAGE, "Chapter not found");
		return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
	}

	public QuestionResponse questionFilterWithoudCorrectOprionNew(Question question) {
		List<String> options = new ArrayList<>();
		options.add(question.getOption1());
		options.add(question.getOption2());
		options.add(question.getOption3());
		options.add(question.getOption4());
		QuestionResponse questionResponse = new QuestionResponse();
		questionResponse.setOption(options);
		questionResponse.setQuestionId(question.getQuestionId());
		questionResponse.setQuestionContent(question.getQuestionContent());
		questionResponse.setQuestionImage(question.getQuestionImage());
		return questionResponse;
	}

	@Override
	public ResponseEntity<?> getChapterExamResultNew(Integer id) {

		Map<String, Object> response = new HashMap<>();

		ChapterExamResult examResult = chapterExamResultRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException(AppConstants.NO_DATA_FOUND));
		ChapterExamResultResponse chapterExamResultResponse = new ChapterExamResultResponse();

		Map<Integer, String> review = examResult.getReview();
		chapterExamResultResponse.setCorrecteQuestions(examResult.getCorrecteQuestions());
		chapterExamResultResponse.setId(examResult.getId());
		chapterExamResultResponse.setNotSelectedQuestions(examResult.getNotSelectedQuestions());
		chapterExamResultResponse.setReview(review);
		chapterExamResultResponse.setWrongQuestions(examResult.getWrongQuestions());
		chapterExamResultResponse.setTotalQuestion(examResult.getTotalQuestion());
		chapterExamResultResponse.setScoreGet(examResult.getScoreGet());
		chapterExamResultResponse
				.setSelectedQuestions((examResult.getTotalQuestion() - examResult.getNotSelectedQuestions()));

		List<Question> questions = examResult.getChapter().getExam().getQuestions();
		// Set selectedOption temporarily in each question
		for (Question q : questions) {
			String selected = review.get(q.getQuestionId());
			System.err.println(
					"SELECTED ==> " + selected + " , QUESTION ID ==> " + q.getQuestionId() + ", REVIEW ==> " + review);
			if (selected != null) {
				q.setSelectedOption(selected); // Assuming setter exists
			}
		}

		// Now pass to mapper
		List<QuestionResponse> questionResponses = questions.stream().map(this::questionFilterNew)
				.collect(Collectors.toList());
		response.put("examResult", chapterExamResultResponse);

		response.put("questions", questionResponses);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	public QuestionResponse questionFilterNew(Question question) {
		System.err.println("question.getSelectedOption() => " + question.getSelectedOption());
		List<String> options = new ArrayList<>();
		options.add(question.getOption1());
		options.add(question.getOption2());
		options.add(question.getOption3());
		options.add(question.getOption4());
		QuestionResponse questionResponse = new QuestionResponse();
		questionResponse.setOption(options);
		questionResponse.setQuestionId(question.getQuestionId());
		questionResponse.setQuestionContent(question.getQuestionContent());
		questionResponse.setQuestionImage(question.getQuestionImage());

		// correctOpt Index Set
		String correct = question.getCorrectOption() != null ? question.getCorrectOption().trim() : null;
		if (correct != null) {
			for (int i = 0; i < options.size(); i++) {
				if (options.get(i).trim().equalsIgnoreCase(correct)) {
					questionResponse.setCorrectOpt(i);
					break;
				}
			}
		}

		// selectedOpt Index Set
		String selected = question.getSelectedOption() != null ? question.getSelectedOption().trim() : null;
		if (selected != null) {
			for (int i = 0; i < options.size(); i++) {
				if (options.get(i).trim().equalsIgnoreCase(selected)) {
					questionResponse.setSelectedOpt(i);
					break;
				}
			}
		}
		return questionResponse;
	}

}
