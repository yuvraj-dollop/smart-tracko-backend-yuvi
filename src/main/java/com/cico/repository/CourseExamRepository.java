package com.cico.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cico.model.CourseExam;
import com.cico.payload.CourseExamResponse;
import com.cico.util.ExamType;

@Repository
public interface CourseExamRepository extends JpaRepository<CourseExam, Integer> {

//	CourseExam findLatestExamByCourseId(Integer courseId);

	@Query("SELECT ce FROM CourseExam ce WHERE ce.examName =:name AND ce.course.id =:courseId")
	Optional<CourseExam> findByExamNameAndCourseId(String name, Integer courseId);

	@Query("SELECT Count(ce) > 0 FROM CourseExam ce WHERE ce.examName =:examName AND ce.course.courseId =:courseId")
	boolean existsByExamNameAndCourseId(@Param("examName") String examName, @Param("courseId") Integer courseId);

	@Query(value = """
			    SELECT * FROM course_exam e
			    WHERE e.course_id = :courseId
			      AND e.schedule_test_date = :date
			      AND (
			           (e.exam_start_time <= :startTime
			             AND DATE_ADD(e.exam_start_time, INTERVAL e.exam_timer MINUTE) > :startTime)
			        OR
			           (e.exam_start_time < DATE_ADD(:startTime, INTERVAL :duration MINUTE)
			             AND DATE_ADD(e.exam_start_time, INTERVAL e.exam_timer MINUTE) >= DATE_ADD(:startTime, INTERVAL :duration MINUTE))
			      )
			""", nativeQuery = true)
	List<CourseExam> findScheduledExamsByCourse(@Param("courseId") Integer courseId, @Param("date") LocalDate date,
			@Param("startTime") LocalTime startTime, @Param("duration") Integer duration);

	@Query("SELECT ce FROM CourseExam ce WHERE ce.course.courseId=:couseId AND ce.isActive = true AND ce.isDeleted = false")
	List<CourseExam> findAllByCourse_CourseId(Integer couseId);

	@Query("SELECT NEW com.cico.payload.CourseExamResponse("
			+ "e.examName, e.examId, c.technologyStack.imageName, e.examTimer, "
			+ "e.passingMarks, sr.scoreGet, e.scheduleTestDate, e.totalQuestionForTest, "
			+ "e.examType, sr.id, c.courseId, e.examStartTime, e.isStart) " + "FROM Student s " + "JOIN s.course c "
			+ "JOIN CourseExam e ON e.course.courseId = c.courseId "
			+ "LEFT JOIN CourseExamResult as   sr ON sr.student.studentId = s.studentId AND sr.courseExam.examId = e.examId "
			+ "WHERE c.isDeleted = false AND s.studentId = :studentId")
	List<CourseExamResponse> findAllCourseExam(@Param("studentId") Integer studentId);

	@Query("SELECT ce FROM CourseExam ce WHERE ce.examName =:examName AND ce.course.courseId =:courseId")
	Optional<CourseExam> findByCourseIdAndCourseName(@Param("examName") String examName,
			@Param("courseId") Integer courseId);

//	@Query("SELECT NEW com.cico.payload.CourseExamResponse("
//			+ "e.examName, e.examId, c.technologyStack.imageName, e.examTimer, "
//			+ "e.passingMarks, sr.scoreGet, e.scheduleTestDate, e.totalQuestionForTest, "
//			+ "e.examType, sr.id, c.courseId, e.examStartTime, e.isStart) " + "FROM CourseExam e " + "JOIN e.course c "
//			+ "LEFT JOIN CourseExamResult sr ON sr.courseExam.examId = e.examId "
//			+ "WHERE c.courseId = :courseId AND e.isActive = true AND e.isDeleted = false AND e.examType = :examType")
//	Page<CourseExamResponse> findCourseExams(ExamType examType, Integer courseId, PageRequest pageRequest);

	
	@Query("SELECT NEW com.cico.payload.CourseExamResponse("
			+ "e.examName, e.examId, c.technologyStack.imageName, e.examTimer, "
			+ "e.passingMarks, sr.scoreGet, e.scheduleTestDate, e.totalQuestionForTest, "
			+ "e.examType, sr.id, c.courseId, e.examStartTime, e.isStart) " +
		       "FROM CourseExam e " +
		       "JOIN e.course c " +
		       "LEFT JOIN CourseExamResult sr ON (sr.courseExam.examId = e.examId AND sr.student.studentId = :studentId) " +
		       "WHERE c.courseId = :courseId " +
		       "AND e.isActive = true " +
		       "AND e.isDeleted = false " +
		       "AND e.examType = :examType " +
		       "AND (:status IS NULL OR " +
		       "    (:status = 'PENDING' AND sr.id IS NULL) OR " +
		       "    (:status = 'COMPLETED' AND sr.id IS NOT NULL))")
		Page<CourseExamResponse> findCourseExams(
		    @Param("examType") ExamType examType,
		    @Param("courseId") Integer courseId,
		    @Param("studentId") Integer studentId,
		    @Param("status") String status,
		    Pageable pageable);	
	

	
	// Method to find a CourseExam by examId and studentId
	
	@Query("SELECT ce FROM CourseExam ce JOIN ce.course c   WHERE ce.examId = :examId")
	Optional<CourseExam> findByExamIdAndStudentId(Integer examId);

}
