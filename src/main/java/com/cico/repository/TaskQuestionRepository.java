package com.cico.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.cico.model.TaskQuestion;
import com.cico.payload.TaskQuestionResponse;

public interface TaskQuestionRepository extends JpaRepository<TaskQuestion, Long> {

	Optional<TaskQuestion> findByQuestionId(Long questionId);

	@Modifying
	@Transactional
	@Query("UPDATE TaskQuestion t set t.isDeleted =1  WHERE   t.questionId =:questionId ")
	void deleteTaskQuestion(@Param("questionId") Long questionId);

	@Query("SELECT COUNT(tq) > 0 FROM TaskQuestion tq WHERE tq.questionId = :questionId AND tq.task.taskId = :taskId and tq.isDeleted = false")
	boolean existByQuestionIdAndTaskId(Long questionId, Long taskId);

	@Query("SELECT  tq FROM TaskQuestion  tq WHERE tq.questionId = :questionId AND tq.isDeleted = false")
	Optional<TaskQuestion> findById(Integer questionId);

//	@Query("""
//			    SELECT new com.cico.payload.TaskQuestionResponse(
//			        tq.questionId,
//			       SUBSTRING(tq.question, 0, 50) || '...',
//			        CASE
//			            WHEN ts IS NOT NULL AND ts.status IN ('Accepted', 'Rejected', 'Reviewing', 'Completed','Unreviewed')
//			            THEN true
//			            ELSE false
//			        END,
//			        ts.submissionDate
//			    )
//
//			    FROM TaskQuestion tq
//			    LEFT JOIN tq.taskSubmissions ts ON ts.student.studentId = :studentId
//			    WHERE tq.task.taskId = :taskId
//			      AND (
//			        (tq.isDeleted = true AND ts IS NOT NULL) OR tq.isDeleted = false
//			      )
//
//			""")
//	List<TaskQuestionResponse> getAllTaskQuestionOfStudent(@Param("studentId") Integer studentId, Long taskId);

	@Query("""
			    SELECT new com.cico.payload.TaskQuestionResponse(
			        tq.questionId,
			        CONCAT(SUBSTRING(tq.question, 1, 50), '...'),
			        CASE
			            WHEN ts IS NOT NULL AND ts.status IN ('Accepted', 'Rejected', 'Reviewing', 'Completed', 'Unreviewed')
			            THEN true ELSE false
			        END,
			        ts.submissionDate
			    )
			    FROM TaskQuestion tq
			    LEFT JOIN tq.taskSubmissions ts ON ts.student.studentId = :studentId
			    WHERE tq.task.taskId = :taskId
			      AND (
			        (tq.isActive = true AND tq.isDeleted = false)
			        OR (
			            (tq.isActive = false OR tq.isDeleted = true)
			            AND ts IS NOT NULL
			        )
			      )
			""")
	List<TaskQuestionResponse> getAllTaskQuestionOfStudent(@Param("studentId") Integer studentId,
			@Param("taskId") Long taskId);

	@Query("SELECT tq FROM TaskQuestion tq WHERE tq.task.taskId = :taskId AND tq.isDeleted = false")
	List<TaskQuestion> findByTaskIdAndIsDeletedFalse(Long taskId);

	@Query("SELECT tq FROM TaskQuestion tq WHERE tq.questionId = :questionId AND tq.task.taskId = :taskId ")
	Optional<TaskQuestion> findByQuestionIdAndTaskId(Long questionId, Long taskId);

}
