package com.cico.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cico.model.Subject;
import com.cico.model.Task;
import com.cico.payload.AssignmentAndTaskSubmission;
import com.cico.payload.AssignmentSubmissionResponse;
import com.cico.payload.TaskResponse;
import com.cico.payload.TaskStatusSummary;
import com.cico.util.SubmissionStatus;

public interface TaskRepo extends JpaRepository<Task, Long> {

	Task findByTaskNameAndIsDeleted(String taskName, boolean b);

	Object findByTaskName(String taskName);

	Optional<Task> findByTaskIdAndIsDeleted(Long taskId, boolean b);

	List<Task> findByIsDeletedFalse();

	List<Task> findBySubjectAndIsDeletedFalse(Subject subject);

	Optional<Task> findBySubjectAndIsDeletedFalse(Long taskId);

	Optional<Task> findByTaskIdAndIsDeletedFalse(Long taskId);

	// changed
	@Query("SELECT  NEW com.cico.payload.AssignmentAndTaskSubmission( t.id, " + "COUNT(DISTINCT ts), "
			+ "COUNT(DISTINCT CASE WHEN ts.status = 'Unreviewed' THEN ts END), "
			+ "COUNT(DISTINCT CASE WHEN ts.status IN ('Rejected' , 'Accepted' ,'Reviewing' ) THEN ts END), "
			+ "t.taskName ,t.isActive  ) " + "FROM Task t " + "LEFT JOIN  t.taskQuestion tq "
			+ "LEFT JOIN tq.taskSubmissions ts "
			+ "WHERE (t.course.courseId = :courseId OR :courseId = 0) AND (t.subject.subjectId = :subjectId OR :subjectId = 0) "
			+ "AND t.isDeleted = false " + "GROUP BY t.id ,ts.id ")
	Page<AssignmentAndTaskSubmission> findAllTaskStatusWithCourseIdAndSubjectId(@Param("courseId") Integer courseId,
			@Param("subjectId") Integer subjectId, PageRequest pageRequest);

	@Query("SELECT "
			+ "NEW com.cico.payload.TaskStatusSummary(  COUNT(ts) as totalCount, COUNT(CASE WHEN ts.status IN ('Rejected', 'Accepted', 'Reviewing') THEN ts END) as reviewedCount,COUNT(CASE WHEN ts.status = 'Unreviewed' THEN ts END) as unreviewedCount) "
			+ "FROM Task t " + " LEFT JOIN t.taskQuestion tq " + " LEFT JOIN tq.taskSubmissions  ts")
	TaskStatusSummary getOverAllTaskQuestionStatus();

	@Query("SELECT NEW com.cico.payload.AssignmentAndTaskSubmission(" + "t.taskName, " + "t.id, "
			+ "COUNT(DISTINCT ts), " + "COUNT(CASE WHEN ts.status = 'Unreviewed' THEN ts END) as unreviewedCount, "
			+ "COUNT(CASE WHEN ts.status IN ('Rejected', 'Accepted', 'Reviewing') THEN ts END) as reviewedCount) "
			+ "FROM Task t LEFT JOIN " + "t.taskQuestion tq LEFT JOIN "
			+ "tq.taskSubmissions ts GROUP BY  t.id ,tq.questionId")
	Page<AssignmentAndTaskSubmission> getAllSubmissionTaskStatus(PageRequest pageRequest);

	// changed
	@Query("SELECT "
			+ " NEW com.cico.payload.AssignmentSubmissionResponse(ts.student.applyForCourse, ts.student.fullName, ts.submissionDate, ts.status, ts.student.profilePic, t.taskName, ts.submittionFileName, ts.taskDescription, ts.id, ts.review ,t.taskId,tq.codeSubmisionStatus,ts.codeSubmission,tq.taskNumber) "
			+ " FROM Task t " + " LEFT JOIN t.taskQuestion tq " + "LEFT JOIN tq.taskSubmissions ts "
			+ " WHERE (:courseId  IS NULL OR t.course.courseId = :courseId ) "
			+ "AND (:subjectId IS NULL OR t.subject.subjectId = :subjectId) "
			+ "AND (:status IS NULL OR ts.status = :status ) " + " AND t.isDeleted = 0 "
			+ " GROUP BY ts.submissionDate, ts.id ,t.id "
			+ " ORDER BY CASE WHEN ts.status = 'Unreviewed' THEN 1  WHEN  ts.status = 'Reviewing' THEN 2 ELSE  3 END, "
			+ " CASE WHEN ts.status NOT IN ('Unreviewed', 'Reviewing') THEN ts.submissionDate END DESC, "
			+ "  ts.status, MAX(ts.submissionDate) DESC, ts.id, t.id ")
	Page<AssignmentSubmissionResponse> findAllSubmissionTaskWithCourseIdAndSubjectId(
			@Param("courseId") Integer courseId, @Param("subjectId") Integer subjectId,
			@Param("status") SubmissionStatus status, PageRequest pageRequest);

	// changed
	@Query("SELECT "
			+ " NEW com.cico.payload.AssignmentSubmissionResponse(ts.student.applyForCourse, ts.student.fullName, ts.submissionDate, ts.status, ts.student.profilePic, t.taskName, ts.submittionFileName, ts.taskDescription, ts.id, ts.review ,t.taskId,tq.codeSubmisionStatus,ts.codeSubmission,tq.taskNumber) "
			+ " FROM Task t " + " LEFT JOIN t.taskQuestion tq " + "LEFT JOIN tq.taskSubmissions ts " + " WHERE "
			+ " ts.status IN('Unreviewed','Reviewing','Rejected', 'Accepted')   AND t.taskId=:taskId "
			+ " AND t.isDeleted = 0 " + " GROUP BY ts.submissionDate, ts.id ,t.id ")
	List<AssignmentSubmissionResponse> getAllTaskSubmissionBYTaskId(Long taskId);

	@Query(value = "SELECT t.task_name FROM task t WHERE t.task_id = (SELECT ts.assignment_submissions_task_id FROM task_submission ts WHERE ts.id =:id) ", nativeQuery = true)
	Optional<String> fetchTaskNameByTaskSubmissionId(@Param("id") Long id);

//	@Query("""
//			    SELECT new com.cico.payload.TaskResponse(
//			        t.taskId,
//			        t.taskName,
//			        CASE
//			            WHEN COUNT(DISTINCT tq.questionId) = COUNT(DISTINCT ts.question.questionId)
//			            THEN true ELSE false
//			        END
//			    )
//			    FROM Student st
//			    JOIN st.course c
//			    JOIN c.subjects s
//			    JOIN Task t ON t.subject.subjectId = s.subjectId
//			    JOIN t.taskQuestion tq
//			    LEFT JOIN TaskSubmission ts ON ts.question.questionId = tq.questionId
//			        AND ts.student.studentId = :studentId
//			    WHERE st.studentId = :studentId
//			      AND t.isDeleted IN (true, false)
//			    GROUP BY t.taskId, t.taskName, t.isDeleted
//			    HAVING (
//			        (t.isDeleted = false) OR (t.isDeleted = true AND COUNT(ts) > 0)
//			    )
//			    AND (
//			        :status IS NULL OR
//			        (:status = 'Completed' AND COUNT(DISTINCT tq.questionId) = COUNT(DISTINCT ts.question.questionId)) OR
//			        (:status = 'Pending' AND COUNT(DISTINCT tq.questionId) > COUNT(DISTINCT ts.question.questionId))
//			    )
//			    ORDER BY t.createdDate DESC
//			""")
//	Page<TaskResponse> getAllTaskOfStudent(@Param("studentId") Integer studentId, @Param("status") String status,
//			Pageable pageRequest);

	@Query("""
			    SELECT new com.cico.payload.TaskResponse(
			        t.taskId,
			        t.taskName,

			        CASE
			            WHEN COUNT(DISTINCT tq.questionId) = COUNT(DISTINCT ts.question.questionId)
			            THEN true ELSE false
			        END
			        ,  t.createdDate
			    )
			    FROM Student st
			    JOIN st.course c
			    JOIN c.subjects s
			    JOIN Task t ON t.subject.subjectId = s.subjectId
			    JOIN t.taskQuestion tq
			    LEFT JOIN TaskSubmission ts ON ts.question.questionId = tq.questionId
			        AND ts.student.studentId = :studentId
			    WHERE st.studentId = :studentId
			      AND tq.isDeleted = false
			    GROUP BY t.taskId, t.taskName, t.isDeleted, t.isActive,t.createdDate
			    HAVING (
			        (t.isActive = true)
			        OR ((t.isDeleted = true OR t.isActive = false) AND COUNT(ts) > 0)
			    )
			    AND (
			        :status IS NULL OR
			        (:status = 'Completed' AND COUNT(DISTINCT tq.questionId) = COUNT(DISTINCT ts.question.questionId)) OR
			        (:status = 'Pending' AND COUNT(DISTINCT tq.questionId) > COUNT(DISTINCT ts.question.questionId))
			    )
			    ORDER BY t.createdDate DESC
			""")
//	@Query(value = """
//			    SELECT
//			        t.task_id AS taskId,
//			        t.task_name AS taskName,
//			        CASE
//			            WHEN COUNT(DISTINCT tq.question_id) = COUNT(DISTINCT ts.question_id)
//			            THEN true ELSE false
//			        END AS completed,
//			        t.created_date AS createdDate,
//			        JSON_ARRAYAGG(
//			            JSON_OBJECT(
//			                'questionId', tq.question_id,
//			                'questionText', tq.question
//			            )
//			        ) AS questions
//			    FROM students st
//			    JOIN courses c ON st.course_id = c.course_id
//			    JOIN subject s ON s.course_id = c.course_id
//			    JOIN task t ON t.subject_id = s.subject_id
//			    JOIN task_question tq ON tq.task_id = t.task_id
//			    LEFT JOIN task_submission ts ON ts.question_id = tq.question_id
//			        AND ts.student_student_id = :studentId
//			    WHERE st.student_id = :studentId
//			      AND tq.is_deleted = false
//			    GROUP BY t.task_id, t.task_name, t.is_deleted, t.is_active, t.created_date
//			    HAVING (
//			        (t.is_active = true)
//			        OR ((t.is_deleted = true OR t.is_active = false) AND COUNT(ts) > 0)
//			    )
//			    AND (
//			        :status IS NULL OR
//			        (:status = 'Completed' AND COUNT(DISTINCT tq.question_id) = COUNT(DISTINCT ts.question_id)) OR
//			        (:status = 'Pending' AND COUNT(DISTINCT tq.question_id) > COUNT(DISTINCT ts.question_id))
//			    )
//			    ORDER BY t.created_date DESC
//			""", countQuery = """
//			    SELECT COUNT(*)
//			    FROM students st
//			    JOIN courses c ON st.course_id = c.course_id
//			    JOIN subject s ON s.course_id = c.course_id
//			    JOIN task t ON t.subject_id = s.subject_id
//			    JOIN task_question tq ON tq.task_id = t.task_id
//			    WHERE st.student_id = :studentId AND tq.is_deleted = false
//			""", nativeQuery = true)
	Page<TaskResponse> getAllTaskOfStudent(@Param("studentId") Integer studentId, @Param("status") String status,
			Pageable pageRequest);

	@Query("  SELECT new com.cico.payload.TaskResponse(t.taskId,t.taskName,COUNT(tq),t.isActive)  FROM Task t"
			+ " LEFT JOIN t.taskQuestion tq " + "" + " WHERE"
			+ "( :courseId IS NULL OR t.course.courseId = :courseId ) "
			+ " AND ( :subjectId IS NULL OR t.subject.subjectId = :subjectId ) AND " + " t.isDeleted = false" + ""
			+ " GROUP BY t.taskId")
	Page<TaskResponse> findAllByIsDeletedFalse(PageRequest of, Integer courseId, Integer subjectId);

	@Query("""
			SELECT NEW com.cico.payload.AssignmentAndTaskSubmission(
			    tq.title,
			    tq.questionId,
			    COUNT(DISTINCT ts),
			    COUNT(DISTINCT CASE WHEN ts.status = 'Unreviewed' THEN ts END),
			    COUNT(DISTINCT CASE WHEN ts.status IN ('Rejected', 'Accepted', 'Reviewing') THEN ts END),
			    tq.isActive
			)
			FROM Task t
			LEFT JOIN t.taskQuestion tq
			LEFT JOIN tq.taskSubmissions ts
			WHERE t.taskId = :taskId
			AND t.isDeleted = false
			GROUP BY tq.title, tq.questionId
			""")
	List<AssignmentAndTaskSubmission> getAllTaskQuestionWithSubmissionCountByTaskId(Long taskId);

	// ................. NEW QUERIES ..........................

//	@Query(value = """
//			SELECT COUNT(*) FROM (
//			    SELECT t.task_id
//			    FROM students st
//			    JOIN courses c ON c.course_id = st.course_course_id
//			    JOIN subject s ON s.technology_stack_id = c.course_id
//			    JOIN task t ON t.subject_id = s.subject_id
//			    JOIN task_question tq ON tq.task_id = t.task_id
//			    LEFT JOIN task_submission ts ON ts.question_id = tq.question_id
//			        AND ts.student_student_id = :studentId
//			    WHERE st.student_id = :studentId
//			      AND tq.is_deleted = false
//			    GROUP BY t.task_id, t.task_name, t.is_deleted, t.is_active
//			    HAVING (
//			        t.is_active = true
//			        OR ((t.is_deleted = true OR t.is_active = false) AND COUNT(ts.question_id) > 0)
//			    )
//			) AS result
//			""", nativeQuery = true)
//	Long countAllTaskOfStudent(@Param("studentId") Integer studentId);

	@Query("""
			    SELECT COUNT(t)
			    FROM Student st
			    JOIN st.course c
			    JOIN c.subjects s
			    JOIN Task t ON t.subject.subjectId = s.subjectId
			    JOIN t.taskQuestion tq
			    LEFT JOIN TaskSubmission ts
			        ON ts.question.questionId = tq.questionId
			       AND ts.student.studentId = :studentId
			    WHERE st.studentId = :studentId
			      AND tq.isDeleted = false
			    GROUP BY t.taskId, t.taskName, t.isDeleted, t.isActive
			    HAVING (
			        t.isActive = true
			        OR ((t.isDeleted = true OR t.isActive = false) AND COUNT(ts) > 0)
			    )
			""")
	Long countAllTaskOfStudent(@Param("studentId") Integer studentId);

}
