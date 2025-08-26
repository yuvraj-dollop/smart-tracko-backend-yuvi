package com.cico.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cico.exception.ResourceAlreadyExistException;
import com.cico.exception.ResourceNotFoundException;
import com.cico.model.Chapter;
import com.cico.model.ChapterContent;
import com.cico.model.ChapterExamResult;
import com.cico.model.Exam;
import com.cico.model.Question;
import com.cico.model.Student;
import com.cico.model.Subject;
import com.cico.payload.AddChapterContentRequest;
import com.cico.payload.AddChapterRequest;
import com.cico.payload.ChapterContentResponse;
import com.cico.payload.ChapterResponse;
import com.cico.payload.QuestionResponse;
import com.cico.payload.UpdateChapterRequest;
import com.cico.repository.ChapterContentRepository;
import com.cico.repository.ChapterExamResultRepo;
import com.cico.repository.ChapterRepository;
import com.cico.repository.ExamRepo;
import com.cico.repository.QuestionRepo;
import com.cico.repository.StudentRepository;
import com.cico.repository.SubjectRepository;
import com.cico.service.IChapterService;
import com.cico.util.AppConstants;

@Service
public class ChapterServiceImpl implements IChapterService {

	@Autowired
	ChapterRepository chapterRepo;

	@Autowired
	ExamRepo examRepo;

	@Autowired
	SubjectRepository subjectRepo;
	@Autowired
	ChapterContentRepository chapterContentRepository;
	@Autowired
	private ChapterExamResultRepo chapterExamResultRepo;

	@Autowired
	private StudentRepository studentRepository;

	@Autowired
	FileServiceImpl fileServiceImpl;

	@Autowired
	private QuestionRepo questionRepo;

	@Override
	public ResponseEntity<?> addChapter(Integer subjectId, String chapterName, MultipartFile image) {
		Chapter ch = chapterRepo.findByChapterNameAndSubjectIdAndIsDeleted(chapterName, subjectId, false);
		if (ch != null)
			throw new ResourceAlreadyExistException("Chapter already present with name..");
		Map<String, Object> response = new HashMap<>();

		Subject subject = subjectRepo.findById(subjectId)
				.orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

		Chapter chapter = new Chapter();
		chapter.setChapterName(chapterName.trim());
		chapter.setIsCompleted(false);

		Exam exam = new Exam();
		exam.setIsDeleted(false);
		Exam exam1 = examRepo.save(exam);
		chapter.setExam(exam1);
		Chapter obj1 = chapterRepo.save(chapter);
		subject.getChapters().add(obj1);
		subjectRepo.save(subject);

		ChapterResponse chapterResponse = new ChapterResponse();
		chapterResponse.setChapterId(obj1.getChapterId());
		chapterResponse.setChapterImage(subject.getTechnologyStack().getImageName());
		chapterResponse.setSubjectName(subject.getSubjectName());
		chapterResponse.setChapterName(obj1.getChapterName());
		response.put(AppConstants.MESSAGE, AppConstants.SUCCESS);
		response.put("chapter", chapterResponse);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> updateChapter(Integer chapterId, String chapterName, Integer subjectId) {
		Map<String, Object> response = new HashMap<>();
		Chapter chapter = chapterRepo.findByChapterIdAndIsDeleted(chapterId, false)
				.orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));

		Chapter ch = chapterRepo.findByChapterNameAndSubjectIdAndIsDeleted(chapterName.trim(), subjectId, false);
		if (ch != null && ch.getChapterId() != chapterId)
			throw new ResourceAlreadyExistException("Chapter already present with name..");

		chapter.setChapterName(chapterName.trim());
		chapterRepo.save(chapter);

		response.put(AppConstants.MESSAGE, AppConstants.UPDATE_SUCCESSFULLY);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Override
	public Map<String, Object> getChapterById(Integer chapterId) {
		Map<String, Object> response = new HashMap<>();
		Chapter chapter = chapterRepo.findByChapterIdAndIsDeleted(chapterId, false)
				.orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));
		response.put("chapter", chapterFilter(chapter));
		response.put("questionLength", chapter.getExam().getQuestions().size());
		return response;
	}

	@Override
	public ResponseEntity<?> deleteChapter(Integer chapterId) {
		Chapter chapter = chapterRepo.findByChapterIdAndIsDeleted(chapterId, false)
				.orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));
		chapter.setIsDeleted(true);
		chapterRepo.save(chapter);
		Map<String, Object> response = new HashMap<>();
		response.put(AppConstants.MESSAGE, AppConstants.DELETE_SUCCESS);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Override
	public void updateChapterStatus(Integer chapterId) {
		Chapter chapter = chapterRepo.findByChapterIdAndIsDeleted(chapterId, false)
				.orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));

		if (chapter.getIsActive().equals(true))
			chapter.setIsActive(false);
		else
			chapter.setIsActive(true);

		chapterRepo.save(chapter);
	}

	@Override
	public List<Chapter> getAllChapters(Integer subjectId) {
//		chapterRepo.getAllChapter(subjectId);
//		Subject subject = subjectRepo.findById(subjectId).get();
//		List<Chapter> chapters = subject.getChapters();
//		if (chapters.isEmpty())
//			new ResourceNotFoundException("No chapter available");
//		return ChapterFilter(chapters);
		return null;
	}

	@Override
	public List<Chapter> getChaptersBySubject(Integer subjectId) {
		Subject subject = subjectRepo.findBySubjectIdAndIsDeleted(subjectId)
				.orElseThrow(() -> new ResourceNotFoundException("Subject Not Found"));
		List<Chapter> chapters = subject.getChapters();
		if (chapters.isEmpty())
			throw new ResourceNotFoundException("No Chapter available for the Subject: " + subject.getSubjectName());
		return ChapterFilter(subject.getChapters());
	}

	@Override
	public ResponseEntity<?> addContentToChapter(Integer chapterId, String title, String subTitle, String content) {
		Chapter chapter = chapterRepo.findById(chapterId)
				.orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));
		List<ChapterContent> chapters = chapter.getChapterContent();
		ChapterContent chapterContent = new ChapterContent();
		chapterContent.setContent(content);
		chapterContent.setSubTitle(subTitle);
		chapterContent.setTitle(title);
		chapterContent.setIsDeleted(false);
		ChapterContent save = chapterContentRepository.save(chapterContent);
		chapters.add(save);
		chapter.setChapterContent(chapters);
		chapterRepo.save(chapter);
		ChapterContentResponse contentResponse = new ChapterContentResponse();
		contentResponse.setChapterName(chapter.getChapterName());

		contentResponse.setContent(save.getContent());
		contentResponse.setId(save.getId());
		contentResponse.setTitle(save.getTitle());
		contentResponse.setSubTitle(save.getSubTitle());

		Map<String, Object> response = new HashMap<>();
		response.put(AppConstants.MESSAGE, AppConstants.CREATE_SUCCESS);
		response.put("chapterContent", contentResponse);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Override
	public ChapterContent updateChapterContent(String title, String subTitle, String content, Integer contentId) {
		Optional<ChapterContent> obj = this.chapterContentRepository.findById(contentId);
		ChapterContent chapter = obj.get();
		if (title != null)
			chapter.setTitle(title);
		else
			chapter.setTitle(chapter.getTitle());
		if (subTitle != null)
			chapter.setSubTitle(subTitle);
		else
			chapter.setSubTitle(chapter.getSubTitle());
		if (content != null)
			chapter.setContent(content);
		else
			chapter.setContent(chapter.getContent());

		return this.chapterContentRepository.save(chapter);
	}

	@Override
	public ChapterContent getChapterContent(Integer chapterContentId) {
		Optional<ChapterContent> obj = this.chapterContentRepository.findById(chapterContentId);
		if (obj.isEmpty())
			throw new ResourceNotFoundException("Chapter content not found");
		return obj.get();
	}

	@Override
	public void deleteChapterContent(Integer contentId) {
		chapterContentRepository.deleteChapterContent(contentId);
	}

	public List<Chapter> ChapterFilter(List<Chapter> list) {

		return list.parallelStream().filter(obj -> {
			obj.setChapterContent(obj.getChapterContent().parallelStream().filter(obj1 -> !obj1.getIsDeleted())
					.collect(Collectors.toList()));
			if (Objects.isNull(obj.getExam()) && !obj.getExam().getIsDeleted()) {
				obj.setExam(new Exam());
			} else {
				Exam exam = obj.getExam();
				List<Question> collect = obj.getExam().getQuestions().parallelStream()
						.filter(obj1 -> !obj1.getIsDeleted()).collect(Collectors.toList());
				exam.setQuestions(collect);
				obj.setExam(exam);
			}
			return obj != null;
		}).collect(Collectors.toList());
	}

	public Chapter chapterFilter(Chapter chapter) {

		chapter.setChapterContent(chapter.getChapterContent().parallelStream().filter(obj -> !obj.getIsDeleted())
				.collect(Collectors.toList()));
		Exam exam = chapter.getExam();
		List<Question> collect = chapter.getExam().getQuestions().parallelStream().filter(obj -> !obj.getIsDeleted())
				.collect(Collectors.toList());
		exam.setQuestions(collect);
		chapter.setExam(exam);
		return chapter;
	}

	@Override
	public ResponseEntity<?> getChapterContentWithChapterId(Integer chapterId) {

		Map<String, Object> response = new HashMap<>();

		Chapter chapter = chapterRepo.findById(chapterId)
				.orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));

		response.put("chapterName", chapter.getChapterName());
		response.put(AppConstants.MESSAGE, AppConstants.SUCCESS);
		response.put("examId", chapter.getExam().getExamId());

		response.put("isExamActivate", chapter.getExam().getIsActive());

		response.put("chapterContent", chapter.getChapterContent().parallelStream().filter(obj -> !obj.getIsDeleted())
				.map(this::chapterContentResponse).toList());
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	public ChapterContentResponse chapterContentResponse(ChapterContent chapterContent) {
		ChapterContentResponse chapterContentResponse = new ChapterContentResponse();
		chapterContentResponse.setContent(chapterContent.getContent());
		chapterContentResponse.setId(chapterContent.getId());
		chapterContentResponse.setTitle(chapterContent.getTitle());
		chapterContentResponse.setSubTitle(chapterContent.getSubTitle());
		return chapterContentResponse;
	}

	@Override
	public ResponseEntity<?> getChaperExamQuestions(Integer chapterId) {

		Optional<Chapter> chapter = chapterRepo.findById(chapterId);
		Map<String, Object> response = new HashMap<>();

		List<Question> questions = chapter.get().getExam().getQuestions().parallelStream()
				.filter(obj -> !obj.getIsDeleted()).collect(Collectors.toList());

		if (questions.isEmpty()) {
			response.put(AppConstants.MESSAGE, AppConstants.NO_DATA_FOUND);
			return new ResponseEntity<>(response, HttpStatus.OK);
		}

		List<QuestionResponse> list = questions.parallelStream().map(obj -> questionFilter(obj))
				.collect(Collectors.toList());

		response.put(AppConstants.MESSAGE, AppConstants.DATA_FOUND);
		response.put("questions", list);
		response.put("isActive", chapter.get().getExam().getIsActive());
		return new ResponseEntity<>(response, HttpStatus.OK);

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
		questionResponse.setIsSelected(question.getIsSelected());
		return questionResponse;
	}

	@Override
	public Chapter getChapterByid(Integer chapterId) {
		return chapterRepo.findById(chapterId).orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));
	}

	@Override
	public ResponseEntity<?> getChapterExamQuestionsWithPagination(Integer chapterId, Integer pageNumber,
			Integer pageSize) {
		Page<Question> qestionsPage = questionRepo.findAllByChapterId(chapterId, PageRequest.of(pageNumber, pageSize));
//		System.err.println("*************** getChapterExamQuestionsWithPagination => " + qestionsPage.getContent());
		return new ResponseEntity<>(qestionsPage, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getChapterContentListByChapterId(Integer chapterId, Integer pageNumber, Integer pageSize) {
		Map<String, Object> response = new HashMap<>();
		Page<ChapterContent> chapterContentPage = chapterContentRepository.findAllByChapterId(chapterId,
				PageRequest.of(pageNumber, pageSize));
		if (chapterContentPage.isEmpty()) {
			response.put(AppConstants.MESSAGE, AppConstants.NO_DATA_FOUND);
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		List<ChapterContentResponse> contentList = chapterContentPage.getContent().stream()
				.map(this::chapterContentResponse).collect(Collectors.toList());
		response.put("chapterContents", contentList);
		response.put("totalPages", chapterContentPage.getTotalPages());
		response.put("currentPage", chapterContentPage.getNumber());
		response.put("last", chapterContentPage.isLast());
		response.put("subjectId", chapterRepo.findSubjectIdByChapterId(chapterId));
		response.put(AppConstants.MESSAGE, AppConstants.DATA_FOUND);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getChapterDetails(Integer chapterId) {
		Map<String, Object> response = new HashMap<>();
		Chapter chapter = chapterRepo.findByChapterIdAndIsDeleted(chapterId, false)
				.orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));
		response.put("chapter", chapterResponse(chapter));
		response.put("examId", chapter.getExam().getExamId());
		response.put("isExamActivate", chapter.getExam().getIsActive());

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	public ChapterResponse chapterResponse(Chapter chapter) {
		ChapterResponse chapterResponse = new ChapterResponse();
		chapterResponse.setChapterId(chapter.getChapterId());
		chapterResponse.setChapterName(chapter.getChapterName());
		return chapterResponse;
	}

	// .................... NEW METHOD'S ..................................

	@Override
	public ResponseEntity<?> addChapter(AddChapterRequest chapterRequest) {
		String chapterName = chapterRequest.getChapterName().trim();
		Chapter ch = chapterRepo.findByChapterNameAndSubjectIdAndIsDeleted(chapterName, chapterRequest.getSubjectId(),
				false);
		if (ch != null)
			throw new ResourceAlreadyExistException(AppConstants.CHAPTER_ALREDY_EXISTS);
		Map<String, Object> response = new HashMap<>();

		Subject subject = subjectRepo.findById(chapterRequest.getSubjectId())
				.orElseThrow(() -> new ResourceNotFoundException(AppConstants.SUBJECT_NOT_FOUND));

		Chapter chapter = new Chapter();
		chapter.setChapterName(chapterName);
		chapter.setIsCompleted(false);

		Exam exam = new Exam();
		exam.setIsDeleted(false);
		Exam exam1 = examRepo.save(exam);
		chapter.setExam(exam1);
		Chapter obj1 = chapterRepo.save(chapter);
		subject.getChapters().add(obj1);
		subjectRepo.save(subject);

		response.put(AppConstants.MESSAGE, AppConstants.SUCCESS);
		response.put("chapter", mapToChapterResponse(chapter, subject));
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	private ChapterResponse mapToChapterResponse(Chapter chapter, Subject subject) {
		return ChapterResponse.builder().chapterId(chapter.getChapterId()).chapterName(chapter.getChapterName())
				.subjectName(subject.getSubjectName()).chapterImage(subject.getTechnologyStack().getImageName())
				.build();
	}

	@Override
	public ResponseEntity<?> updateChapter(UpdateChapterRequest chapterRequest) {
		Map<String, Object> response = new HashMap<>();
		Integer chapterId = chapterRequest.getChapterId();
		String chapterName = chapterRequest.getChapterName().trim();
		Chapter chapter = chapterRepo.findByChapterIdAndIsDeleted(chapterId, false)
				.orElseThrow(() -> new ResourceNotFoundException(AppConstants.CHAPTER_NOT_FOUND));

		Chapter ch = chapterRepo.findByChapterNameAndSubjectIdAndIsDeleted(chapterName, chapterRequest.getSubjectId(),
				false);
		if (ch != null && ch.getChapterId() != chapterId)
			throw new ResourceAlreadyExistException(AppConstants.CHAPTER_ALREDY_EXISTS);

		chapter.setChapterName(chapterName);
		chapterRepo.save(chapter);

		response.put(AppConstants.MESSAGE, AppConstants.UPDATE_SUCCESSFULLY);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> addContentToChapter(AddChapterContentRequest chapterContentRequest) {
		Chapter chapter = chapterRepo.findById(chapterContentRequest.getChapterId())
				.orElseThrow(() -> new ResourceNotFoundException(AppConstants.CHAPTER_NOT_FOUND));
		List<ChapterContent> chapters = chapter.getChapterContent();
		ChapterContent chapterContent = new ChapterContent();
		chapterContent.setContent(chapterContentRequest.getContent());
		chapterContent.setSubTitle(chapterContentRequest.getSubTitle());
		chapterContent.setTitle(chapterContentRequest.getTitle());
		chapterContent.setIsDeleted(false);
		ChapterContent savedContent = chapterContentRepository.save(chapterContent);
		chapters.add(savedContent);
		chapter.setChapterContent(chapters);
		chapterRepo.save(chapter);

		Map<String, Object> response = new HashMap<>();
		response.put(AppConstants.MESSAGE, AppConstants.CREATE_SUCCESS);
		response.put("chapterContent", mapToChapterContentResponse(savedContent, chapter.getChapterName()));
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	private ChapterContentResponse mapToChapterContentResponse(ChapterContent chapterContent, String chapterName) {
		return ChapterContentResponse.builder().id(chapterContent.getId()).title(chapterContent.getTitle())
				.subTitle(chapterContent.getSubTitle()).content(chapterContent.getContent())
				.isDeleted(chapterContent.getIsDeleted()).chapterName(chapterName).build();
	}

	@Override
	public ResponseEntity<?> getChapterContentListByChapterId(Integer chapterId, Integer studentId, Integer pageNumber,
			Integer pageSize) {
		Map<String, Object> response = new HashMap<>();
		Page<ChapterContent> chapterContentPage = chapterContentRepository.findAllByChapterId(chapterId,
				PageRequest.of(pageNumber, pageSize));
		if (chapterContentPage.isEmpty()) {
			response.put(AppConstants.MESSAGE, AppConstants.NO_DATA_FOUND);
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		List<ChapterContentResponse> contentList = chapterContentPage.getContent().stream()
				.map(this::chapterContentResponse).collect(Collectors.toList());

		// Fetch Chapter and Exam
		Optional<Chapter> chapterOpt = chapterRepo.findById(chapterId);
		boolean isExamAvailable = false;

		if (chapterOpt.isPresent()) {
			Exam exam = chapterOpt.get().getExam();
			if (exam != null && Boolean.TRUE.equals(exam.getIsActive()) && exam.getQuestions() != null
					&& exam.getQuestions().stream().anyMatch(q -> !Boolean.TRUE.equals(q.getIsDeleted()))) {
				isExamAvailable = true;
			}
		}

		response.put("chapterContents", contentList);
		response.put("totalPages", chapterContentPage.getTotalPages());
		response.put("currentPage", chapterContentPage.getNumber());
		response.put("last", chapterContentPage.isLast());
		response.put("subjectId", chapterRepo.findSubjectIdByChapterId(chapterId));
		response.put("resultId", getChapterExamResultId(chapterId, studentId));
		response.put("isExamAvailable", isExamAvailable);
		response.put(AppConstants.MESSAGE, AppConstants.DATA_FOUND);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	private Integer getChapterExamResultId(Integer chapterId, Integer studentId) {
		Optional<Chapter> chapterOpt = chapterRepo.findByChapterIdAndIsDeleted(chapterId, false);
		if (chapterOpt.isEmpty())
			return null;

		Chapter chapter = chapterOpt.get();
		if (chapter.getExam() == null)
			return null;

		Student student = studentRepository.findByStudentId(studentId);
		if (student == null)
			return null;

		return chapterExamResultRepo.findByChapterAndStudent(chapter, student).map(ChapterExamResult::getId)
				.orElse(null);
	}

}
