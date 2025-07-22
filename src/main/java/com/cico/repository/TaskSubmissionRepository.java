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

import com.cico.model.TaskSubmission;
import com.cico.payload.TaskQuestionSubmissionResponse;
import com.cico.util.SubmissionStatus;

@Repository
public interface TaskSubmissionRepository extends JpaRepository<TaskSubmission, Long> {

	
	
	//changed
	@Query("""
		    SELECT DISTINCT NEW com.cico.payload.TaskQuestionSubmissionResponse(
		        CAST(t.taskId AS java.lang.Long),
		        CAST(tq.questionId AS java.lang.Long),
		        ts.taskDescription,
		        ts.submittionFileName,
		        ts.submissionDate,
		        ts.status,
		        ts.review,
		        t.taskName,
		        CAST(tq.taskNumber AS java.lang.Integer)
		    )
		    FROM Task t
		    LEFT JOIN t.taskQuestion tq
		    LEFT JOIN tq.taskSubmissions ts
		    WHERE ts.student.studentId = :studentId
		      AND (:status IS NULL OR ts.status = :status)
		    ORDER BY ts.submissionDate DESC
		""")
		Page<TaskQuestionSubmissionResponse> getSubmitedTaskForStudent(
		    @Param("studentId") Integer studentId,
		    @Param("status") SubmissionStatus status,
		    PageRequest pageable  // ✅ FIXED
		);



	@Transactional
	@Modifying
	@Query("UPDATE TaskSubmission a SET a.status=:status , a.review=:review WHERE a.id=:id")
	int updateSubmitTaskStatus(@Param("id") Long submissionId, @Param("status") SubmissionStatus status,
			@Param("review") String review);

	@Query("SELECT s FROM TaskSubmission s WHERE s.id=:id")
	Optional<TaskSubmission> findBySubmissionId(@Param("id") Long submissionId);

	
	
	//changed
	@Query("SELECT ts FROM  TaskSubmission ts  WHERE ts.student.studentId =:studentId AND  ts.question.questionId=:questionId")
	TaskSubmission findByTaskIdAndStudentId(@Param("questionId") Long questionId, @Param("studentId") Integer studentId);

	
	//changed
	@Query("SELECT COUNT(ts) > 0 FROM TaskSubmission ts WHERE  ts.question.questionId= :questionId")
	Boolean submissionExistsByQuestionId(@Param("questionId") Long questionId);

	@Query("SELECT ts FROM TaskSubmission ts WHERE ts.question.questionId = :questionId AND ts.student.studentId = :studentId")
	Optional<TaskSubmission> findByQuestionIdAndStudentId(Long questionId, Integer studentId);

	
	//changed
	@Query("SELECT COUNT(ts) > 0 FROM TaskSubmission ts WHERE ts.question.questionId = :questionId AND ts.student.studentId = :studentId")
	Boolean submissionExistByQuestionIdAndStudentId(Long questionId, Integer studentId);


 	@Query("SELECT COUNT(ts) > 0 FROM TaskSubmission ts WHERE ts.question.questionId = :questionId AND ts.task.taskId = :taskId")
	Boolean submissionExistByQuestionIdAndTaskId(Long questionId, Long taskId);


 
 	@Query("SELECT ts.task.taskName FROM TaskSubmission ts WHERE ts.id = :submissionId")
	Optional<String> fetchTaskNameByTaskSubmissionId(Long submissionId);
 	
 	
 	
 	// ............... NEW QUERIES ...................
 	@Query("""
 	    SELECT COUNT(DISTINCT ts.task.taskId)
 	    FROM TaskSubmission ts
 	    WHERE ts.student.studentId = :studentId
 	""")
	Long countSubmittedTasksByStudentId(@Param("studentId") Integer studentId);


}
