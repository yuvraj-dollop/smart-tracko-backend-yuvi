package com.cico.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.cico.model.Chapter;
import com.cico.model.ChapterContent;

@Repository
public interface ChapterContentRepository extends JpaRepository<ChapterContent, Integer> {

	@Query("SELECT c FROM ChapterContent c WHERE c.isDeleted = 0 AND c.id =:contentId")
	Optional<ChapterContent> findById(@Param("contentId") Integer contentId);

	@Transactional
	@Modifying
	@Query("UPDATE ChapterContent c SET c.isDeleted = 1 WHERE c.id = :contentId")
	void deleteChapterContent(@Param("contentId") Integer contentId);

	@Query("SELECT c FROM Chapter ch JOIN ch.chapterContent c WHERE c.isDeleted = 0 AND ch.chapterId = :chapterId")
	Page<ChapterContent> findAllByChapterId(Integer chapterId, PageRequest of);

	@Query("SELECT ch FROM Chapter ch JOIN ch.chapterContent cc WHERE cc.id = :contentId")
	Chapter findByContentId(@Param("contentId") Integer contentId);
}
