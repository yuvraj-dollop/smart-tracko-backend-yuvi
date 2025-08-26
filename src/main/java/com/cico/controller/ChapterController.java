package com.cico.controller;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cico.model.Chapter;
import com.cico.model.ChapterContent;
import com.cico.payload.AddChapterContentRequest;
import com.cico.payload.AddChapterRequest;
import com.cico.payload.UpdateChapterRequest;
import com.cico.service.IChapterService;
import com.cico.util.AppConstants;

@RestController
@RequestMapping("/chapter")
@CrossOrigin("*")
public class ChapterController {

	@Autowired
	private IChapterService chapterService;

	// =============== CHAPTER MANAGEMENT ===============
	@PostMapping("/addChapter")
	public ResponseEntity<?> addChapter(@RequestParam("subjectId") Integer subjectId,
			@RequestParam("chapterName") String chapterName,
			@RequestParam(name = "image", required = false) MultipartFile image) {
		return chapterService.addChapter(subjectId, chapterName, image);
	}

	@PutMapping("/updateChapter")
	public ResponseEntity<?> updateChapter(@RequestParam("chapterId") Integer chapterId,
			@RequestParam("chapterName") String chapterName, @RequestParam("subjectId") Integer subjectId) {
		return chapterService.updateChapter(chapterId, chapterName, subjectId);
	}

	@PutMapping("/deleteChapter")
	public ResponseEntity<?> deleteChapter(@RequestParam(name = AppConstants.CHAPTER_ID) Integer chapterId) {
		return chapterService.deleteChapter(chapterId);
	}

	@PutMapping("/updateChapterStatus")
	public ResponseEntity<String> updateChapterStatus(@RequestParam(name = AppConstants.CHAPTER_ID) Integer chapterId) {
		chapterService.updateChapterStatus(chapterId);
		return ResponseEntity.ok("Chapter Updated");
	}

	// =============== CHAPTER CONTENT MANAGEMENT ===============
	@PostMapping("/addChapterContent")
	public ResponseEntity<?> addContentToChapter(@RequestParam("chapterId") Integer chapterId,
			@RequestParam("title") String title, @RequestParam("subTitle") String subTitle,
			@RequestParam("content") String content) {
		return chapterService.addContentToChapter(chapterId, title, subTitle, content);
	}

	@PutMapping("/updateChapterContent")
	public ResponseEntity<ChapterContent> updateChapterContent(@RequestParam("title") String title,
			@RequestParam("subTitle") String subTitle, @RequestParam("content") String content,
			@RequestParam("contentId") Integer contentId) {
		ChapterContent chapterContent = chapterService.updateChapterContent(title, subTitle, content, contentId);
		return new ResponseEntity<>(chapterContent, HttpStatus.OK);
	}

	@PutMapping("/deleteChapterContent")
	public ResponseEntity<?> deleteChapterContent(@RequestParam(name = AppConstants.CONTENT_ID) Integer contentId) {
		chapterService.deleteChapterContent(contentId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	// =============== CHAPTER DATA RETRIEVAL ===============
	@GetMapping("/getChapterById")
	public ResponseEntity<?> getChapterById(@RequestParam(name = AppConstants.CHAPTER_ID) Integer chapterId) {
		Map<String, Object> chapter = chapterService.getChapterById(chapterId);
		return ResponseEntity.ok(chapter);
	}

	@GetMapping("/getAllChapters")
	public ResponseEntity<List<Chapter>> getAllChapters(
			@RequestParam(name = AppConstants.SUBJECT_ID) Integer subjectId) {
		List<Chapter> chapters = chapterService.getAllChapters(subjectId);
		return ResponseEntity.ok(chapters);
	}

	@GetMapping("/getChaptersBySubject")
	public ResponseEntity<List<Chapter>> getChaptersBySubject(
			@RequestParam(name = AppConstants.SUBJECT_ID) Integer subjectId) {
		List<Chapter> chapters = chapterService.getChaptersBySubject(subjectId);
		return ResponseEntity.ok(chapters);
	}

	// =============== CHAPTER CONTENT RETRIEVAL ===============
	@GetMapping("/getChapterContent")
	public ResponseEntity<ChapterContent> getChapterContent(
			@RequestParam(name = AppConstants.CHAPTER_CONTENT_ID) Integer chapterContentId) throws Exception {
		ChapterContent chapterContent = chapterService.getChapterContent(chapterContentId);
		return new ResponseEntity<>(chapterContent, HttpStatus.OK);
	}

	@GetMapping("/getChapterContentWithChapterIdForAdmin")
	public ResponseEntity<?> getChapterContentWithChapterId(
			@RequestParam(name = AppConstants.CHAPTER_ID) Integer chapterId) {
		return chapterService.getChapterContentWithChapterId(chapterId);
	}

	@GetMapping("/getChapterContentListByChapterId")
	public ResponseEntity<?> getChapterContentListByChapterId(
			@RequestParam(name = AppConstants.CHAPTER_ID) Integer chapterId,
			@RequestParam("pageNumber") Integer pageNumber, @RequestParam("pageSize") Integer pageSize) {
		return chapterService.getChapterContentListByChapterId(chapterId, pageNumber, pageSize);
	}

	// =============== CHAPTER EXAM RELATED ===============
	@GetMapping("/getChapterExamQuestions")
	public ResponseEntity<?> getChaperExamQuestions(@RequestParam(name = AppConstants.CHAPTER_ID) Integer chapterId) {
		return chapterService.getChaperExamQuestions(chapterId);
	}

	@GetMapping("/getChapterExamQuestionsWithPagination")
	public ResponseEntity<?> getChapterExamQuestionsWithPagination(
			@RequestParam(name = AppConstants.CHAPTER_ID) Integer chapterId,
			@RequestParam("pageNumber") Integer pageNumber, @RequestParam("pageSize") Integer pageSize) {
		return chapterService.getChapterExamQuestionsWithPagination(chapterId, pageNumber, pageSize);
	}

	// =============== CHAPTER DETAILS ===============
	@GetMapping("/getChapterDetails")
	public ResponseEntity<?> getChapterDetails(@RequestParam(name = AppConstants.CHAPTER_ID) Integer chapterId) {
		return chapterService.getChapterDetails(chapterId);
	}

	// ............... NEW API'S .................................

	@PostMapping("/v2/addChapter")
	public ResponseEntity<?> addChapter(@Valid @RequestBody AddChapterRequest chapterRequest) {
		return chapterService.addChapter(chapterRequest);
	}

	@PutMapping("/v2/updateChapter")
	public ResponseEntity<?> updateChapter(@Valid @RequestBody UpdateChapterRequest chapterRequest) {
		return chapterService.updateChapter(chapterRequest);
	}

	@PostMapping("/v2/addChapterContent")
	public ResponseEntity<?> addContentToChapter(@Valid @RequestBody AddChapterContentRequest chapterContentRequest) {
		return chapterService.addContentToChapter(chapterContentRequest);
	}

	@GetMapping("/v2/getChapterDetails/{chapterId}")
	public ResponseEntity<?> getChapterDetailsNew(@PathVariable(name = AppConstants.CHAPTER_ID) Integer chapterId) {
		return chapterService.getChapterDetails(chapterId);
	}

	@GetMapping("/v2/getChapterContentListByChapterId/{chapterId}")
	public ResponseEntity<?> getChapterContentListByChapterIdNew(
			@PathVariable(name = AppConstants.CHAPTER_ID) Integer chapterId,
			@RequestParam(name = AppConstants.STUDENT_ID) Integer studentId,
			@RequestParam(name = AppConstants.PAGE_NUMBER) Integer pageNumber,
			@RequestParam(name = AppConstants.PAGE_SIZE) Integer pageSize) {
		return chapterService.getChapterContentListByChapterId(chapterId, studentId, pageNumber, pageSize);
	}

}