package com.cico.service;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.cico.model.Chapter;
import com.cico.model.ChapterContent;
import com.cico.payload.AddChapterContentRequest;
import com.cico.payload.AddChapterRequest;
import com.cico.payload.ChapterContentResponse;
import com.cico.payload.UpdateChapterRequest;

public interface IChapterService {

	ResponseEntity<?> addChapter(Integer subjectId, String chapterName, MultipartFile image);

	ResponseEntity<?> updateChapter(Integer chapterId, String chapterName, Integer subjectId);

	Map<String, Object> getChapterById(Integer chapterId);

	ResponseEntity<?> deleteChapter(Integer chapterId);

	void updateChapterStatus(Integer chapterId);

	List<Chapter> getAllChapters(Integer subjectId);

	List<Chapter> getChaptersBySubject(Integer subjectId);

	ResponseEntity<?> addContentToChapter(Integer chapterId, String title, String subTitle, String content);

	ChapterContent updateChapterContent(String title, String subTitle, String content, Integer contentId);

	ChapterContent getChapterContent(Integer chapterContentId);

	void deleteChapterContent(Integer contentId);

	ResponseEntity<?> getChapterContentWithChapterId(Integer chapterId);

	ResponseEntity<?> getChaperExamQuestions(Integer chapterId);

	public Chapter getChapterByid(Integer chapterId);

	ResponseEntity<?> getChapterContentListByChapterId(Integer chapterId, Integer pageNumber, Integer pageSize);

	ResponseEntity<?> getChapterExamQuestionsWithPagination(Integer chapterId, Integer pageNumber, Integer pageSize);

	ResponseEntity<?> getChapterDetails(Integer chapterId);

	// .................. NEW METHOD'S .........................
	ResponseEntity<?> addChapter(AddChapterRequest chapterRequest);

	ResponseEntity<?> updateChapter(UpdateChapterRequest chapterRequest);

	ResponseEntity<?> addContentToChapter(@Valid AddChapterContentRequest chapterContentRequest);

	ResponseEntity<?> getChapterContentListByChapterId(Integer chapterId, Integer studentId, Integer pageNumber,
			Integer pageSize);

	ChapterContentResponse updateChapterContentNew(String title, String subTitle, String content, Integer contentId);

}
