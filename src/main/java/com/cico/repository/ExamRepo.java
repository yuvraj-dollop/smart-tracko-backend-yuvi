package com.cico.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cico.model.Exam;

public interface ExamRepo extends JpaRepository<Exam, Integer> {

	Optional<Exam> findByExamIdAndIsDeleted(Integer examId, boolean b);

	Exam findByExamNameAndIsDeleted(String examName, boolean b);

	List<Exam> findByIsDeleted(boolean b);
	
	@Query("SELECT e FROM Exam e JOIN e.questions q WHERE q.questionId = :questionId AND e.isDeleted = false")
	Optional<Exam> findExamByQuestionId(@Param("questionId") Integer questionId);


}
