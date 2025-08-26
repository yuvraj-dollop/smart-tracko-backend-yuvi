package com.cico.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cico.exception.BadRequestException;
import com.cico.exception.ResourceAlreadyExistException;
import com.cico.exception.ResourceNotFoundException;
import com.cico.model.Chapter;
import com.cico.model.CourseExam;
import com.cico.model.Exam;
import com.cico.model.Question;
import com.cico.model.Subject;
import com.cico.model.SubjectExam;
import com.cico.payload.QuestionResponse;
import com.cico.repository.ChapterRepository;
import com.cico.repository.CourseExamRepository;
import com.cico.repository.ExamRepo;
import com.cico.repository.QuestionRepo;
import com.cico.repository.SubjectExamRepo;
import com.cico.repository.SubjectRepository;
import com.cico.repository.SubmittedExamHistoryRepo;
import com.cico.service.IChapterService;
import com.cico.service.IFileService;
import com.cico.service.IQuestionService;
import com.cico.util.AppConstants;

@Service
public class QuestionServiceImpl implements IQuestionService {

	@Autowired
	private QuestionRepo questionRepo;

	@Autowired
	private IFileService fileService;

	@Autowired
	private ExamRepo examRepo;

	@Autowired
	private CourseExamRepository courseExamRepository;
	@Autowired
	private ChapterRepository chapterRepository;

	@Autowired
	private IChapterService ichapterService;

	@Autowired
	private ChapterServiceImpl chapterServiceImpl;

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private SubjectExamRepo subjectExamRepo;

	@Autowired
	private ExamServiceImpl examServiceImpl;

	@Autowired
	private SubjectServiceImpl subjectServiceImpl;

	@Autowired
	private SubmittedExamHistoryRepo submittedExamHistoryRepo;

	@Override
	public Question addQuestionToChapterExam(Integer chapterId, String questionContent, String option1, String option2,
			String option3, String option4, MultipartFile image, String correctOption) {
		Question questionObj = questionRepo.findByQuestionContentAndIsDeleted(questionContent.trim(), false);
		Optional<Chapter> chapter = chapterRepository.findById(chapterId);
		if (!chapter.isPresent()) {
			throw new ResourceNotFoundException(AppConstants.CHAPTER_NOT_FOUND);
		}

//		if (Objects.nonNull(questionObj) && chapter.get().getExam().getQuestions().contains(questionObj))
//			throw new ResourceAlreadyExistException(AppConstants.QUESTION_ALREDY_EXISTS);

		// UPDATE
		Optional<Question> existing = questionRepo.findByContentAndExam(questionContent.trim(),
				chapter.get().getExam().getExamId());
		if (existing.isPresent()) {
			throw new ResourceAlreadyExistException(AppConstants.QUESTION_ALREDY_EXISTS);
		}

		// Check Options Are Not Duplicate
		this.validateUniqueOptions(option1, option2, option3, option4);

		questionObj = new Question();
		questionObj.setQuestionContent(questionContent.trim());
		questionObj.setOption1(option1.trim());
		questionObj.setOption2(option2.trim());
		questionObj.setOption3(option3.trim());
		questionObj.setOption4(option4.trim());
		questionObj.setCorrectOption(correctOption.trim());
		if (image != null) {
			questionObj.setQuestionImage(image.getOriginalFilename());
			String file = fileService.uploadFileInFolder(image, AppConstants.SUBJECT_AND_CHAPTER_IMAGES);
			questionObj.setQuestionImage(file);
		}

		Question save = questionRepo.save(questionObj);
		Exam exam = chapter.get().getExam();
		exam.getQuestions().add(save);
		exam.setScore(exam.getQuestions().size());
		exam.setExamTimer(exam.getQuestions().size());
		examRepo.save(exam);
		return save;
	}

	@Override
	public Question addQuestionToSubjectExam(Integer subjectId, String questionContent, String option1, String option2,
			String option3, String option4, MultipartFile image, String correctOption) {
		Subject subject = subjectServiceImpl.checkSubjectIsPresent(subjectId);
		Question questionObj = questionRepo.findByQuestionContentAndIsDeleted(questionContent.trim(), false);
		if (Objects.nonNull(questionObj))
			throw new ResourceAlreadyExistException(AppConstants.QUESTION_ALREDY_EXISTS);

		// Check Options Are Not Duplicate
		this.validateUniqueOptions(option1, option2, option3, option4);

		questionObj = new Question();
		questionObj.setQuestionContent(questionContent.trim());
		questionObj.setOption1(option1.trim());
		questionObj.setOption2(option2.trim());
		questionObj.setOption3(option3.trim());
		questionObj.setOption4(option4.trim());
		questionObj.setCorrectOption(correctOption.trim());
		if (image != null) {
			questionObj.setQuestionImage(image.getOriginalFilename());
			String file = fileService.uploadFileInFolder(image, AppConstants.SUBJECT_AND_CHAPTER_IMAGES);
			questionObj.setQuestionImage(file);
		}

		Question save = questionRepo.save(questionObj);

		subject.getQuestions().add(save);
		subjectRepository.save(subject);

		return save;
	}

	@Override
	public ResponseEntity<?> updateQuestion(Integer questionId, String questionContent, String option1, String option2,
			String option3, String option4, String correctOption, MultipartFile image, Integer examId, Integer type) {

		Map<String, Object> response = new HashMap<>();

		// Check Options Are Not Duplicate
		this.validateUniqueOptions(option1, option2, option3, option4);
		// check question is present or not
		Question question = questionRepo.findByQuestionIdAndIsDeleted(questionId, false)
				.orElseThrow(() -> new ResourceNotFoundException(AppConstants.QUESTION_NOT_FOUND));

		if (question.getIsSelected()) {
			response.put(AppConstants.MESSAGE, "Update failed: Already selected for exams");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		// check exam is present or not
		// type 1 for checking chapter exam question
		// type 2 for checking subject exam question
		if (type == 1) {

			Optional<Exam> exam = examRepo.findByExamIdAndIsDeleted(examId, false);
			if (exam.isEmpty())
				throw new ResourceNotFoundException(AppConstants.EXAM_NOT_FOUND);

			if (exam.get().getIsStarted() || exam.get().getIsActive()) {
				response.put(AppConstants.MESSAGE,
						"Can't update the question exam are activated or question is already selected in subjecte exam");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}

			Question questionObj = questionRepo.findByQuestionContentAndIsDeleted(questionContent.trim(), false);

			if (Objects.nonNull(questionObj) && exam.get().getQuestions().contains(questionObj)
					&& questionObj.getQuestionId() != question.getQuestionId()) {
				throw new ResourceAlreadyExistException("Question already exist");
			}
		} else if (type == 2) {
			Optional<Subject> subject = subjectRepository.findBySubjectIdAndIsDeleted(examId);

			if (subject.isEmpty()) {
				throw new ResourceNotFoundException(AppConstants.SUBJECT_NOT_FOUND);
			}
			Question questionObj = questionRepo.findByQuestionContentAndIsDeleted(questionContent.trim(), false);

			if (Objects.nonNull(questionObj) && subject.get().getQuestions().contains(questionObj)
					&& questionObj.getQuestionId() != question.getQuestionId()) {
				throw new ResourceAlreadyExistException(AppConstants.QUESTION_ALREDY_EXISTS);
			}

		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		if (questionContent != null)
			question.setQuestionContent(questionContent.trim());
		if (option1 != null)
			question.setOption1(option1.trim());
		if (option2 != null)
			question.setOption2(option2.trim());
		if (option3 != null)
			question.setOption3(option3.trim());
		if (option4 != null)
			question.setOption4(option4.trim());
		if (correctOption != null)
			question.setCorrectOption(correctOption.trim());

		if (image != null && !image.isEmpty()) {
			if (image != null) {
				question.setQuestionImage(image.getOriginalFilename());
				String file = fileService.uploadFileInFolder(image, AppConstants.SUBJECT_AND_CHAPTER_IMAGES);
				question.setQuestionImage(file);
			}
		} else {
			question.setQuestionImage(question.getQuestionImage());
		}

		Question res = questionRepo.save(question);
		QuestionResponse q = new QuestionResponse();
		q.setCorrectOption(res.getCorrectOption());
		q.setOption1(res.getOption1());
		q.setOption2(res.getOption2());
		q.setOption3(res.getOption3());
		q.setOption4(res.getOption4());
		q.setQuestionContent(res.getQuestionContent());
		q.setQuestionId(res.getQuestionId());
		q.setQuestionImage(res.getQuestionImage());

		response.put(AppConstants.MESSAGE, AppConstants.UPDATE_SUCCESSFULLY);
		response.put("question", q);

		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@Override
	public List<Question> getAllQuestionByChapterId(Integer chapterId) {

		Map<String, Object> chapter = chapterServiceImpl.getChapterById(chapterId);
		Chapter chapter1 = (Chapter) chapter.get("chapter");
		return chapter1.getExam().getQuestions();
	}

	@Override
	public void deleteQuestion(Integer questionId) {
		Question question = questionRepo.findByQuestionIdAndIsDeleted(questionId, false)
				.orElseThrow(() -> new ResourceNotFoundException(AppConstants.QUESTION_NOT_FOUND));

		Exam exam = examRepo.findExamByQuestionId(questionId)
				.orElseThrow(() -> new ResourceNotFoundException(AppConstants.EXAM_NOT_FOUND));
		if (Boolean.TRUE.equals(exam.getIsStarted())) {
			throw new BadRequestException("Cannot update question status. Exam has already started.");
		}
		question.setIsDeleted(true);
		questionRepo.save(question);
	}

	@Override
	public void updateQuestionStatus(Integer questionId) {
		Question question = questionRepo.findByQuestionIdAndIsDeleted(questionId, false)
				.orElseThrow(() -> new ResourceNotFoundException(AppConstants.QUESTION_NOT_FOUND));

		Exam exam = examRepo.findExamByQuestionId(questionId)
				.orElseThrow(() -> new ResourceNotFoundException(AppConstants.EXAM_NOT_FOUND));
		if (Boolean.TRUE.equals(exam.getIsStarted())) {
			throw new BadRequestException("Cannot update question status. Exam has already started.");
		}

		question.setIsActive(!Boolean.TRUE.equals(question.getIsActive()));

		questionRepo.save(question);

	}

	@Override
	public Question getQuestionById(Integer questionId) {
		return this.questionRepo.findById(questionId)
				.orElseThrow(() -> new ResourceNotFoundException("Question not found with this id " + questionId));
	}

	int questionCount = 0;

	@Override
	public Map<String, Object> getAllSubjectQuestionBySubjectId(Integer subjectId) {
		Map<String, Object> response = new HashMap<>();
		Subject subject = subjectServiceImpl.checkSubjectIsPresent(subjectId);
		subject.getChapters().stream().forEach(obj -> {
			obj.getExam().getQuestions().stream().forEach(obj1 -> {
				if (obj1.getIsActive() && !obj1.getIsDeleted()) {
					questionCount += 1;
				}
			});
		});
		response.put(AppConstants.QUESTIONS, subject.getQuestions().stream()
				.filter(obj -> !obj.getIsDeleted() && obj.getIsActive()).collect(Collectors.toList()));
		response.put("questionCount", questionCount);
		questionCount = 0;
		return response;

	}

	@Override
	public ResponseEntity<?> getAllSubjectQuestionForTest(Integer examId, Integer studentId) {

		List<Question> allQuestions = new ArrayList<>();
		List<Question> randomQuestionList = new ArrayList<>();
		Map<String, Object> response = new HashMap<>();

		// check for test given or not
		Optional<SubjectExam> isExamTaken = subjectExamRepo.findByExamIdAndStudentId(examId, studentId);
		SubjectExam exam2 = examServiceImpl.checkSubjectExamIsPresent(examId);

		if (exam2.getScheduleTestDate() == LocalDate.now()
				&& exam2.getExamStartTime().getMinute() <= LocalDateTime.now().getMinute() + 15
				&& exam2.getExamStartTime().getHour() == LocalDateTime.now().getHour())

			// return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

			if (isExamTaken.isPresent()) {
				response.put(AppConstants.MESSAGE, AppConstants.EXAM_ALREADY_GIVEN);
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}

		SubjectExam exam = examServiceImpl.checkSubjectExamIsPresent(examId);

		Subject subject = subjectRepository.findByExamId(examId);

		allQuestions.addAll(subject.getQuestions().parallelStream()
				.filter(obj -> !obj.getIsDeleted() && obj.getIsActive()).collect(Collectors.toList()));

		List<Chapter> chapters = subject.getChapters().parallelStream().filter(obj -> !obj.getIsDeleted())
				.collect(Collectors.toList());
		List<List<Question>> collect = chapters.parallelStream().filter(o -> !o.getIsDeleted())
				.map(obj -> obj.getExam().getQuestions()).collect(Collectors.toList());

		for (List<Question> q : collect) {
			allQuestions.addAll(q);
		}

		// filter questions that are not given by the student
		List<Integer> allPriviousSubmittedQuestions = submittedExamHistoryRepo.findByStudentId(studentId);
		if (allPriviousSubmittedQuestions != null && !allPriviousSubmittedQuestions.isEmpty()) {
			allQuestions = allQuestions.parallelStream()
					.filter(obj -> !allPriviousSubmittedQuestions.contains(obj.getQuestionId()))
					.collect(Collectors.toList());
		}

		Random random = new Random();
		int size = Math.min(exam.getTotalQuestionForTest(), allQuestions.size());
		for (int i = 0; i < size; i++) {
			int randomIndex = random.nextInt(allQuestions.size());
			randomQuestionList.add(allQuestions.remove(randomIndex));
		}
		// If no questions are available ,refresh the question list
		if (randomQuestionList.isEmpty() && !allQuestions.isEmpty()) {
			for (int i = 0; i < size; i++) {
				int randomIndex = random.nextInt(allQuestions.size());
				randomQuestionList.add(allQuestions.remove(randomIndex));
			}
		}

		// If still no questions are available, return an error message
		if (randomQuestionList.isEmpty() || allQuestions.isEmpty()) {
			response.put(AppConstants.MESSAGE, AppConstants.NO_QUESTION_AVAILABLE);
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		questionRepo.setQuestionIsSelectdTrue(randomQuestionList);
		response.put(AppConstants.MESSAGE, AppConstants.SUCCESS);
		response.put(AppConstants.QUESTIONS,
				randomQuestionList.parallelStream().map(obj -> questionFilter(obj)).collect(Collectors.toList()));
		response.put(AppConstants.TIMER, exam.getExamTimer());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	public QuestionResponse questionFilter(Question question) {
		QuestionResponse q = new QuestionResponse();
		q.setOption1(question.getOption1());
		q.setOption2(question.getOption2());
		q.setOption3(question.getOption3());
		q.setOption4(question.getOption4());
		q.setQuestionContent(question.getQuestionContent());
		q.setQuestionImage(question.getQuestionImage());
		q.setQuestionId(question.getQuestionId());
		q.setIsSelected(question.getIsSelected());

		return q;
	}

//	private List<Question> parseCSV(MultipartFile file) throws IOException {
//		List<Question> questions = new ArrayList<>();
//		try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
//			String line;
//			boolean header = true;
//			while ((line = br.readLine()) != null) {
//				if (header) {
//					header = false;
//					continue; // skip header
//				}
//				String[] data = line.split(",", -1); // keep empty fields
//				if (data.length < 6) // changed from 11 to 6
//					continue; // skip invalid/incomplete rows
//
//				Question question = new Question();
//				question.setQuestionContent(data[0].trim());
//				question.setOption1(data[1].trim());
//				question.setOption2(data[2].trim());
//				question.setOption3(data[3].trim());
//				question.setOption4(data[4].trim());
//
//				String correctLetter = data[5].trim().toUpperCase();
//				String correctValue = switch (correctLetter) {
//				case "A" -> question.getOption1();
//				case "B" -> question.getOption2();
//				case "C" -> question.getOption3();
//				case "D" -> question.getOption4();
//				default -> correctLetter; // fallback in case value is already actual text
//				};
//				question.setCorrectOption(correctValue);
//
//				// Handle questionImage (column 6) if present, otherwise null
//				question.setQuestionImage(data.length > 6 && !data[6].isEmpty() ? data[6].trim() : null);
//
//				questions.add(question);
//			}
//		}
//		return questions;
//	}

	private List<Question> parseExcel(MultipartFile file) throws IOException {
		List<Question> questions = new ArrayList<>();

		try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {

			Sheet sheet = workbook.getSheetAt(0); // Get first sheet
			Iterator<Row> rowIterator = sheet.iterator();

			if (rowIterator.hasNext()) {
				rowIterator.next(); // Skip header row
			}

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				if (row.getPhysicalNumberOfCells() < 6) {
					continue; // Skip rows with less than 6 columns
				}

				Question question = new Question();

				// Question Content
				question.setQuestionContent(getCellValue(row.getCell(0)));

				// Options 1-4
				question.setOption1(getCellValue(row.getCell(1)));
				question.setOption2(getCellValue(row.getCell(2)));
				question.setOption3(getCellValue(row.getCell(3)));
				question.setOption4(getCellValue(row.getCell(4)));

				// Correct Option
				String correctLetter = getCellValue(row.getCell(5)).trim().toUpperCase();
				String correctValue = switch (correctLetter) {
				case "A" -> question.getOption1();
				case "B" -> question.getOption2();
				case "C" -> question.getOption3();
				case "D" -> question.getOption4();
				default -> correctLetter;
				};
				question.setCorrectOption(correctValue);

				// Question Image (optional)
				if (!getCellValue(row.getCell(6)).isEmpty() && getCellValue(row.getCell(6)) != null) {
					question.setQuestionImage(getCellValue(row.getCell(6)));
				}
				questions.add(question);
			}
		}
		return questions;
	}

	private String getCellValue(Cell cell) {
		if (cell == null) {
			return "";
		}
		switch (cell.getCellType()) {
		case STRING:
			return cell.getStringCellValue().trim();
		case NUMERIC:
			return String.valueOf((int) cell.getNumericCellValue());
		case BOOLEAN:
			return String.valueOf(cell.getBooleanCellValue());
		default:
			return "";
		}
	}

	@Override
	public ResponseEntity<?> uploadSubjectBulkQuestion(MultipartFile file, Integer subjectId) {
		Subject subject = subjectServiceImpl.checkSubjectIsPresent(subjectId);

		try {
			List<Question> questions = parseExcel(file);
			if (questions.isEmpty()) {
				return new ResponseEntity<>(Map.of("message", "No valid questions found in the file"),
						HttpStatus.BAD_REQUEST);
			}

			// Collect existing question contents from subject
			Set<String> existingContents = subject.getQuestions().stream().map(Question::getQuestionContent)
					.collect(Collectors.toSet());

			// Filter only new questions that don't already exist
			List<Question> newQuestions = questions.stream()
					.filter(q -> !existingContents.contains(q.getQuestionContent())).collect(Collectors.toList());

			if (newQuestions.isEmpty()) {
				return new ResponseEntity<>(Map.of("message", "All questions already exist in the subject"),
						HttpStatus.BAD_REQUEST);
			}

			// Save and link to subject
			List<Question> savedQuestions = questionRepo.saveAll(newQuestions);
			subject.getQuestions().addAll(savedQuestions);
			subjectRepository.save(subject);

			Map<String, Object> response = Map.of("message", "Questions uploaded successfully", "totalQuestions",
					questionRepo.count(), "uploadedQuestions",
					savedQuestions.stream().map(this::questionFilter).collect(Collectors.toList()));
			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (IOException e) {
			throw new RuntimeException("CSV upload failed: " + e.getMessage());
		}
	}

	@Override
	public ResponseEntity<?> uploaodChapterBulkQuestion(MultipartFile file, Integer chapterId) {
		try {
			List<Question> questions = parseExcel(file);
			if (questions.isEmpty()) {
				return new ResponseEntity<>(Map.of("message", "No valid questions found in the file"),
						HttpStatus.BAD_REQUEST);
			}

			// Get chapter and existing questions
			Chapter chapter = ichapterService.getChapterByid(chapterId);
			Set<String> existingContents = chapter.getExam().getQuestions().stream().map(Question::getQuestionContent)
					.collect(Collectors.toSet());

			// Filter new questions not already present
			List<Question> newQuestions = questions.stream()
					.filter(q -> !existingContents.contains(q.getQuestionContent())).collect(Collectors.toList());

			if (newQuestions.isEmpty()) {
				return new ResponseEntity<>(Map.of("message", "All questions already exist"), HttpStatus.BAD_REQUEST);
			}

			// Save new questions and associate with the exam
			List<Question> savedQuestions = questionRepo.saveAll(newQuestions);
			chapter.getExam().getQuestions().addAll(savedQuestions);
			chapterRepository.save(chapter);

			// Response
			Map<String, Object> response = Map.of("message", "Questions uploaded successfully", "totalQuestions",
					questionRepo.count(), "uploadedQuestions",
					savedQuestions.stream().map(this::questionFilter).collect(Collectors.toList()));
			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (IOException e) {
			throw new RuntimeException("CSV upload failed: " + e.getMessage());
		}
	}

	@Override
	public ResponseEntity<?> getAllSubjectQuestionBySubjectIdWithPagination(Integer subjectId, Integer pageSise,
			Integer pageNumber) {

		PageRequest of = PageRequest.of(pageNumber, pageSise);

		Page<Question> questions = questionRepo.findBySubjectIdAndIsDeleted(subjectId, false, of);

		return new ResponseEntity<>(questions, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<?> getAllCourseQuestionForTest(Integer examId, Integer studentId) {

		List<Question> allQuestions = new ArrayList<>();
		List<Question> randomQuestionList = new ArrayList<>();
		Map<String, Object> response = new HashMap<>();
		// check for test given or not
		Optional<CourseExam> isExamTaken = courseExamRepository.findByExamIdAndStudentId(examId);
		CourseExam exam2 = examServiceImpl.checkCourseExamIsPresent(examId);

		if (exam2.getScheduleTestDate() == LocalDate.now()
				&& exam2.getExamStartTime().getMinute() <= LocalDateTime.now().getMinute() + 15
				&& exam2.getExamStartTime().getHour() == LocalDateTime.now().getHour())

			// return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

			if (isExamTaken.isPresent()) {
				response.put(AppConstants.MESSAGE, AppConstants.EXAM_ALREADY_GIVEN);
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}

		CourseExam exam = examServiceImpl.checkCourseExamIsPresent(examId);

		// not optimisedd code
		List<Subject> subjects = exam.getCourse().getSubjects();
		for (Subject subject : subjects) {
			if (!subject.getIsDeleted()) {
				allQuestions.addAll(subject.getQuestions().parallelStream()
						.filter(obj -> !obj.getIsDeleted() && obj.getIsActive()).collect(Collectors.toList()));
				allQuestions.addAll(subject.getChapters().parallelStream().filter(obj -> !obj.getIsDeleted())
						.flatMap(chapter -> chapter.getExam().getQuestions().stream())
						.filter(obj -> !obj.getIsDeleted() && obj.getIsActive()).collect(Collectors.toList()));
			}
		}

		// fetch all chapter's question's

		// filter questions that are not given by the student
		List<Integer> allPriviousSubmittedQuestions = submittedExamHistoryRepo.findByStudentId(studentId);
		if (allPriviousSubmittedQuestions != null && !allPriviousSubmittedQuestions.isEmpty()) {
			allQuestions = allQuestions.parallelStream()
					.filter(obj -> !allPriviousSubmittedQuestions.contains(obj.getQuestionId()))
					.collect(Collectors.toList());
		}
		Random random = new Random();
		int size = Math.min(exam.getTotalQuestionForTest(), allQuestions.size());
		for (int i = 0; i < size; i++) {
			int randomIndex = random.nextInt(allQuestions.size());
			randomQuestionList.add(allQuestions.remove(randomIndex));
		}
		// If no questions are available ,refresh the question list
		if (randomQuestionList.isEmpty() && !allQuestions.isEmpty()) {
			for (int i = 0; i < size; i++) {
				int randomIndex = random.nextInt(allQuestions.size());
				randomQuestionList.add(allQuestions.remove(randomIndex));
			}
		}

		// If still no questions are available, return an error message
		if (randomQuestionList.isEmpty() || allQuestions.isEmpty()) {
			response.put(AppConstants.MESSAGE, AppConstants.NO_QUESTION_AVAILABLE);
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		questionRepo.setQuestionIsSelectdTrue(randomQuestionList);
		response.put(AppConstants.MESSAGE, AppConstants.SUCCESS);
		response.put(AppConstants.QUESTIONS,
				randomQuestionList.parallelStream().map(obj -> questionFilter(obj)).collect(Collectors.toList()));
		response.put(AppConstants.TIMER, exam.getExamTimer());
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	private void validateUniqueOptions(String option1, String option2, String option3, String option4) {
		// Check for duplicate options
		Set<String> optionSet = new HashSet<String>();
		optionSet.add(option1.trim());
		optionSet.add(option2.trim());
		optionSet.add(option3.trim());
		optionSet.add(option4.trim());

		if (optionSet.size() < 4) {
			throw new ResourceAlreadyExistException(AppConstants.OPTION_ALREDY_EXISTS);
		}
	}

}
