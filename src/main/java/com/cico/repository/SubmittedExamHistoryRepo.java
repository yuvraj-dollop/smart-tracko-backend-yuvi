package com.cico.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cico.model.SubmittedExamQuestionHistory;

@Repository
public interface SubmittedExamHistoryRepo extends JpaRepository<SubmittedExamQuestionHistory, Integer> {

	@Query("SELECT DISTINCT q.questionId FROM SubmittedExamQuestionHistory q WHERE q.student.studentId =:studentId")
	List<Integer> findByStudentId(Integer studentId);

	@Modifying
	@Transactional
	@Query("DELETE FROM SubmittedExamQuestionHistory seqh WHERE seqh.student.studentId = :studentId")
	void deleteAllByStudentId(@Param("studentId") Integer studentId);
}
