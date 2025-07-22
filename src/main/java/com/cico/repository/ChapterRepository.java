package com.cico.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cico.model.Chapter;

public interface ChapterRepository extends JpaRepository<Chapter, Integer> {

	@Query("SELECT c FROM Chapter c  WHERE c.chapterId = :chapterId AND c.isDeleted = :isDeleted ")
	Optional<Chapter> findByChapterIdAndIsDeleted(@Param("chapterId") Integer chapterId,
			@Param("isDeleted") Boolean isDeleted);

	@Query("SELECT c FROM Chapter  c WHERE c.chapterName = :chapterName AND c.isDeleted = :isDeleted  AND c.isActive =1 ")
	Chapter findByChapterNameAndIsDeleted(@Param("chapterName") String chapterName,
			@Param("isDeleted") boolean isDeleted);

	@Query("SELECT DISTINCT c.chapterName , cc.id,cc.title,cc.subTitle,cc.content FROM  Chapter c LEFT JOIN  c.chapterContent cc ON cc.isDeleted = 0   WHERE  c.chapterId =:chapterId")
	List<Object[]> getChapterContentWithChapterId(Integer chapterId);

	Chapter findByChapterNameAndIsDeletedAndIsActiveTrue(String chapterName, boolean b);

	@Query(nativeQuery = true, value = "SELECT * FROM chapter as c WHERE c.chapter_name =:chapterName AND c.is_deleted =:b AND c.subject_id = :subjectId")
	Chapter findByChapterNameAndSubjectIdAndIsDeleted(String chapterName, Integer subjectId, boolean b);

	// ............. NEW QUERIES ...................

	@Query("SELECT s.subjectId FROM Subject s JOIN s.chapters c WHERE c.chapterId = :chapterId AND c.isDeleted = false")
	Integer findSubjectIdByChapterId(@Param("chapterId") Integer chapterId);

}
