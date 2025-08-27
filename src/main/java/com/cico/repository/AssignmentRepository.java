package com.cico.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cico.model.Assignment;
import com.cico.model.Course;
import com.cico.payload.AssignmentAndTaskSubmission;
import com.cico.payload.AssignmentSubmissionResponse;
import com.cico.payload.TaskStatusSummary;
import com.cico.util.SubmissionStatus;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

	Optional<Assignment> findByIdAndIsDeleted(Long id, boolean b);

	List<Assignment> findByIsDeletedFalse();

	Course findByCourse(Optional<Course> findByCourseId);

	@Query("SELECT a FROM Assignment a WHERE a.course.courseId = :courseId AND a.isActive = 1 AND a.isDeleted =0 ORDER BY a.createdDate ASC")
	List<Assignment> findAllByCourseIdAndIsDeletedFalse(@Param("courseId") Integer courseId);

	@Query("SELECT a FROM Assignment a WHERE a.course.courseId = :courseId  ")
	List<Assignment> findAll(@Param("courseId") Integer courseId);

	@Query("SELECT a FROM Assignment a WHERE a.course.courseId = :courseId  AND a.subject.subjectId =:subjectId AND a.isDeleted =0 ")
	List<Assignment> findAllByCourseIdAndSubjectIdAndIsDeletedFalse(@Param("courseId") Integer courseId,
			@Param("subjectId") Integer subjectId);

	@Query("SELECT a FROM Assignment a WHERE a.title =:title AND a.isDeleted =0 ")
	Optional<Assignment> findByName(@Param("title") String title);

	/////////////////////////////////////// start

	@Query("SELECT NEW com.cico.payload.AssignmentAndTaskSubmission(t.id, " + "COUNT(DISTINCT ts), "
			+ "COUNT(DISTINCT CASE WHEN ts.status = 'Unreviewed' THEN ts END), "
			+ "COUNT(DISTINCT CASE WHEN ts.status IN ('Rejected' , 'Accepted' ,'Reviewing' ) THEN ts END), "
			+ "COUNT(t), SUBSTRING(t.question ,1,15) ,a.id , a.title ,a.isActive ,t.taskNumber)  "
			+ "FROM Assignment a " + "LEFT JOIN a.AssignmentQuestion t ON  t.isDeleted = 0 "
			+ "LEFT JOIN t.assignmentSubmissions ts "
			+ "WHERE ( a.course.courseId =:courseId OR :courseId = 0 )AND ( a.subject.subjectId =:subjectId  OR :subjectId = 0) "
			+ "AND a.isDeleted = 0 "
			+ " AND a.isDeleted =0 GROUP BY a.id,a.title, t.id  ORDER BY a.createdDate ,t.createdDate ")
	Page<AssignmentAndTaskSubmission> findAllAssignmentStatusWithCourseIdAndSubjectId(
			@Param("courseId") Integer courseId, @Param("subjectId") Integer subjectId, PageRequest reuqest);

//	@Query("SELECT  COUNT(DISTINCT ts), "
//			+ "COUNT(DISTINCT CASE WHEN ts.status = 'Unreviewed' THEN ts END), "
//			+ "COUNT(DISTINCT CASE WHEN ts.status IN ('Rejected' , 'Accepted' ,'Reviewing' ) THEN ts END), "
//			+ "COUNT(t), SUBSTRING(t.question ,1,15) ,a.id , a.title ,a.isActive ,t.taskNumber,t.isActive , t.id " + "FROM Assignment a " + "LEFT JOIN a.AssignmentQuestion t ON  t.isDeleted = 0 "
//			+ "LEFT JOIN t.assignmentSubmissions ts "
//			+ "WHERE ( a.course.courseId =:courseId OR :courseId = 0 )AND ( a.subject.subjectId =:subjectId  OR :subjectId = 0) "
//			+ "AND a.isDeleted = 0 " + " AND a.isDeleted =0 GROUP BY a.id,a.title, t.id  ORDER BY a.createdDate ,t.createdDate ")
//	Page<Object[]> findAllAssignmentStatusWithCourseIdAndSubjectId(
//			@Param("courseId") Integer courseId, @Param("subjectId") Integer subjectId, PageRequest reuqest);

	//////////////////////////////////////// end

	//// start

//	@Query("SELECT  "
//			+ " NEW com.cico.payload.AssignmentSubmissionResponse(ts.student.applyForCourse ,ts.student.fullName ,ts.submissionDate ,ts.status,ts.student.profilePic,a.title,ts.submitFile,ts.description,ts.submissionId,ts.review , a.id ,t.taskNumber) "
//			+ "FROM Assignment a " + "JOIN a.AssignmentQuestion t " + "  JOIN t.assignmentSubmissions ts "
//			+ "WHERE ( a.course.courseId =:courseId OR :courseId =0 )AND ( a.subject.subjectId =:subjectId  OR :subjectId =0) AND (ts.status =:status OR :status ='NOT_CHECKED_WITH_IT' ) "
//			+ "AND a.isDeleted = 0 " + "AND t.isDeleted = 0  AND a.isDeleted =0 " + " GROUP BY  ts.id ,t.id "
//			  + "ORDER BY CASE WHEN ts.status = 'Unreviewed' THEN 1 "
//		        + "WHEN ts.status = 'Reviewing' THEN 2 ELSE 3 END, ts.status, MAX(ts.submissionDate) DESC, ts.id, t.id")
//	Page<AssignmentSubmissionResponse> findAllAssignmentSubmissionWithCourseIdAndSubjectId(
//			@Param("courseId") Integer courseId, @Param("subjectId") Integer subjectId, SubmissionStatus status,
//			PageRequest pageRequest);

	@Query("SELECT NEW com.cico.payload.AssignmentSubmissionResponse(ts.student.applyForCourse, ts.student.fullName, ts.submissionDate, ts.status, ts.student.profilePic, a.title, ts.submitFile, ts.description, ts.submissionId, ts.review, a.id, t.taskNumber) "
			+ "FROM Assignment a " + "JOIN a.AssignmentQuestion t " + "JOIN t.assignmentSubmissions ts "
			+ "WHERE (:courseId IS NULL  OR a.course.courseId = :courseId ) "
			+ "AND ( :subjectId IS NULL OR a.subject.subjectId = :subjectId ) "
			+ "AND (:status IS NULL OR ts.status = :status ) " + "AND a.isDeleted = 0 " + "AND t.isDeleted = 0 "
			+ "AND a.isDeleted = 0 " + "GROUP BY ts.id, t.id " + "ORDER BY "
			+ "CASE WHEN ts.status = 'Unreviewed' THEN 1 " + // Unreviewed submissions first
			"WHEN ts.status = 'Reviewing' THEN 2 " + // Then reviewing submissions
			"ELSE 3 END, " + // Else for other statuses
			"CASE WHEN ts.status NOT IN ('Unreviewed', 'Reviewing') THEN ts.submissionDate END DESC, " + // Sort by date
																											// desc for
																											// other
																											// statuses
			"ts.status, " + // Then sort by status
			"MAX(ts.submissionDate) DESC, " + // Latest submission date
			"ts.id, t.id") // Additional sorting criteria if needed
	Page<AssignmentSubmissionResponse> findAllAssignmentSubmissionWithCourseIdAndSubjectId(
			@Param("courseId") Integer courseId, @Param("subjectId") Integer subjectId,
			@Param("status") SubmissionStatus status, PageRequest pageRequest);

	///// end
	@Query("SELECT "
			+ "NEW com.cico.payload.TaskStatusSummary(  COUNT(ts) as totalCount, COUNT(CASE WHEN ts.status IN ('Rejected', 'Accepted', 'Reviewing') THEN ts END) as reviewedCount,COUNT(CASE WHEN ts.status = 'Unreviewed' THEN ts END) as unreviewedCount)"
			+ "FROM Assignment a " + " JOIN a.AssignmentQuestion t ON t.isDeleted = 0 "
			+ " JOIN t.assignmentSubmissions ts WHERE a.isDeleted =0 ")
	TaskStatusSummary getOverAllAssignmentTaskStatus();

	@Query("SELECT  a.id , a.title ," + "COUNT(CASE WHEN ts.status = 'Unreviewed' THEN  ts  END), "
			+ "COUNT(CASE WHEN ts.status IN ('Rejected', 'Accepted', 'Reviewing') THEN ts END), " + "COUNT(ts), "
			+ "COUNT (t)," + " t.questionId " + "FROM Assignment a "
			+ "LEFT JOIN a.AssignmentQuestion t ON t.isDeleted = 0 " + "LEFT JOIN t.assignmentSubmissions ts  "
			+ " WHERE a.isDeleted = 0 " + "GROUP BY  a.id ,t ")
	List<Object[]> getAllSubmissionAssignmentTaskStatus();

	@Query("SELECT  "
			+ " NEW com.cico.payload.AssignmentSubmissionResponse(ts.student.fullName ,ts.submissionDate ,ts.status,ts.student.profilePic,ts.submitFile,ts.description,ts.submissionId,ts.review ,t.question) "
			+ "FROM Assignment a " + "JOIN a.AssignmentQuestion t " + "  JOIN t.assignmentSubmissions ts "
			+ " WHERE ts.status IN ('Unreviewed' ,'Reviewing' ,'Rejected', 'Accepted' )"
			+ "AND t.isDeleted = 0  AND a.isDeleted =0 AND a.id=:assignmentId " + " GROUP BY  ts.id ,t.id ")
	List<AssignmentSubmissionResponse> getAllSubmittedAssignmentTask(Long assignmentId);

	@Query("SELECT a.title ,t.taskNumber FROM Assignment  a  "
			+ "JOIN  a.AssignmentQuestion  as t ON t.isDeleted = FALSE  "
			+ " LEFT JOIN t.assignmentSubmissions  as  submission "
			+ "WHERE submission.submissionId =:submissionId AND a.isDeleted = FALSE")
	Object[] fetchAssignmentNameAndTaskNumberByAssignmentSubmissionId(Long submissionId);

	@Query("SELECT a FROM Assignment a WHERE (:courseId IS NULL OR a.course.courseId = :courseId ) AND (:subjectId IS NULL OR a.subject.subjectId =:subjectId) AND a.isDeleted =0 ")
	Page<Assignment> findAllByCourseIdAndSubjectIdAndIsDeletedFalse(Integer courseId, Integer subjectId,
			PageRequest of);

	@Query("""
			    SELECT NEW com.cico.payload.AssignmentAndTaskSubmission(
			        t.questionId,
			        t.title,
			        COUNT(DISTINCT ts),
			        COUNT(DISTINCT CASE WHEN ts.status = 'Unreviewed' THEN ts END),
			        COUNT(DISTINCT CASE WHEN ts.status IN ('Rejected', 'Accepted', 'Reviewing') THEN ts END)
			        , t.isActive
			    )
			    FROM Assignment a
			    JOIN AssignmentTaskQuestion t ON t.assignment.id = a.id AND t.isDeleted = false
			    LEFT JOIN t.assignmentSubmissions ts
			    WHERE a.id = :assignmentId AND a.isDeleted = false
			    GROUP BY t.questionId, t.question
			    ORDER BY t.createdDate ASC
			""")
	Page<AssignmentAndTaskSubmission> findAllAssignmentStatusWithAssignmentId(@Param("assignmentId") Long assignmentId,
			PageRequest request);

	@Query("""
			    SELECT COUNT(a)
			    FROM Assignment a
			    WHERE a.course.courseId = :courseId AND a.isDeleted = false AND a.isActive = true
			""")
	Long countByCourseIdAndIsDeletedFalse(@Param("courseId") Integer courseId);

//	.....................................New ....................
	// 🔹 Count total assignments for a student's course in a given month range
	@Query("""
			    SELECT COUNT(a)
			    FROM Assignment a
			    WHERE a.course.courseId = :courseId
			    AND a.createdDate BETWEEN :startDate AND :endDate
			    AND a.isDeleted = false
			""")
	int countAssignmentsByCourseAndMonth(@Param("courseId") Integer courseId,
			@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

	// 🔹 Count assignments by status for a student's course in a given month range
	@Query("""
			    SELECT COUNT(DISTINCT a.id)
			    FROM Assignment a
			    JOIN a.AssignmentQuestion t
			    JOIN t.assignmentSubmissions ts
			    WHERE a.course.courseId = :courseId
			    AND ts.student.studentId = :studentId
			    AND ts.status = :status
			    AND a.createdDate BETWEEN :startDate AND :endDate
			    AND a.isDeleted = false
			""")
	int countCompletedAssignmentsByStudentAndMonth(@Param("courseId") Integer courseId,
			@Param("studentId") Integer studentId, @Param("status") SubmissionStatus status,
			@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

}
