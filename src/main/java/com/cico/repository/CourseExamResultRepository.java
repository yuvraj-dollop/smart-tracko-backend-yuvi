package com.cico.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cico.model.CourseExam;
import com.cico.model.CourseExamResult;
import com.cico.model.Student;
import com.cico.payload.ExamResultResponse;

public interface CourseExamResultRepository extends JpaRepository<CourseExamResult, Integer> {

	@Query("SELECT cer FROM CourseExamResult cer WHERE cer.courseExam = :exam AND cer.student = :student")
	Optional<CourseExamResult> findByCourseExamAndStudent(CourseExam exam, Student student);

	@Query("SELECT  NEW com.cico.payload.ExamResultResponse( r.id, r.correcteQuestions , " + "r.wrongQuestions ,  "
			+ "r.notSelectedQuestions ," + " r.student.profilePic," + "r.student.studentId ," + "r.student.fullName,"
			+ "r.scoreGet , " + "r.totalQuestion " + " )FROM  CourseExamResult r   WHERE r.courseExam.examId =:examId ")
	List<ExamResultResponse> findAllResutls(Integer examId);

	@Query("SELECT COUNT(cer) FROM CourseExamResult cer WHERE cer.courseExam = :exam ")
	Long findByCourseExam(CourseExam exam);

//............................................... New ......................................
	@Query("""
			    SELECT r
			    FROM CourseExamResult r
			    JOIN r.courseExam ce
			    JOIN ce.course c
			    WHERE r.student.studentId = :studentId
			    AND c.courseId = :courseId
			""")
	List<CourseExamResult> findByStudentAndCourse(@Param("studentId") Integer studentId,
			@Param("courseId") Integer courseId);

	List<CourseExamResult> findByStudent_StudentId(Integer studentId);

}