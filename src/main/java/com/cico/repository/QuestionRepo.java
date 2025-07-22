package com.cico.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.cico.model.Exam;
import com.cico.model.Question;

public interface QuestionRepo extends JpaRepository<Question, Integer> {

	@Query(value = "SELECT * FROM question as q WHERE q.is_deleted =:b AND q.question_content =:question LIMIT 1", nativeQuery = true)
	Question findByQuestionContentAndIsDeleted(@Param("question") String question, Boolean b);

	Optional<Question> findByQuestionIdAndIsDeleted(Integer questionId, Boolean b);

	List<Question> findByIsDeleted(boolean b);

	List<Question> findByIsDeleted(Boolean b);

	@Transactional
	@Modifying
	@Query("UPDATE Question q set q.isSelected = true WHERE q IN :randomQuestionList ")
	void setQuestionIsSelectdTrue(@Param("randomQuestionList") List<Question> randomQuestionList);

	@Query("SELECT q FROM Subject s JOIN s.questions q WHERE q.isDeleted = false AND q.isActive = true AND s.isDeleted =:b AND s.subjectId =:subjectId")
	Page<Question> findBySubjectIdAndIsDeleted(@Param("subjectId") Integer subjectId, boolean b, PageRequest of);

	@Query("SELECT q FROM Chapter c JOIN c.exam e JOIN e.questions q "
			+ "WHERE c.chapterId = :chapterId AND q.isDeleted = false AND q.isActive = true")
	Page<Question> findAllByChapterId(Integer chapterId, PageRequest of);

	@Query("SELECT q FROM Question q WHERE q.isDeleted = false AND q.isActive = true AND q.questionId IN :randomQuestoinList")
	Optional<Question> findAllByIdAndIsDeletedFalse(List<Integer> randomQuestoinList);

	
	
	// ........... NEW QUESRIES ...................

	@Query("SELECT q FROM Chapter c JOIN c.exam e JOIN e.questions q WHERE q.questionContent = :content AND q.isDeleted = false AND e.examId = :examId")
	Optional<Question> findByContentAndExam(@Param("content") String content, @Param("examId") Integer examId);
}
