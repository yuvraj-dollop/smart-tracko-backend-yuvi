package com.cico.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cico.model.Subject;
import com.cico.payload.SubjectExamResponse;
import com.cico.payload.SubjectResponse;
import com.cico.util.ExamType;

public interface SubjectRepository extends JpaRepository<Subject, Integer> {

	@Query("SELECT s FROM Subject s  WHERE s.subjectName = :subjectName AND s.isDeleted = 0 ")
	Subject findBySubjectNameAndIsDeleted(@Param("subjectName") String subjectName);

	@Query("SELECT s FROM Subject s  WHERE s.subjectId = :subjectId AND s.isDeleted=0  ")
	Optional<Subject> findBySubjectIdAndIsDeleted(@Param("subjectId") Integer subjectId);

	@Query("SELECT s.subjectId,s.subjectName, s.technologyStack.technologyName, s.technologyStack.imageName, COUNT( DISTINCT c) ,s.technologyStack.id "
			+ "FROM Subject s " + "LEFT JOIN s.chapters c ON c.isDeleted = 0 " + "WHERE s.isDeleted = 0 "
			+ "GROUP BY s.subjectId")
	List<Object[]> findByIsDeletedFalse();

	@Query("SELECT  s.subjectId ,s.technologyStack.imageName ,s.subjectName,c.chapterId  , c.chapterName FROM Subject s   LEFT JOIN s.chapters  c ON c.isDeleted = 0      WHERE  s.isDeleted =0  AND  s.subjectId =:subjectId ")
	List<Object[]> getAllChapterWithSubjectId(Integer subjectId);

	@Query("SELECT NEW com.cico.payload.SubjectResponse(s.subjectId, s.subjectName, s.technologyStack.id, s.technologyStack.technologyName, s.technologyStack.imageName) "
			+ "FROM Course c " + "JOIN c.subjects s ON s.isDeleted = 0 AND s.isActive = 1"
			+ "WHERE c.courseId = :courseId AND c.isDeleted =0 AND s.isActive = 1")
	List<SubjectResponse> getAllSubjectByCourseId(@Param("courseId") Integer courseId);

	@Query("SELECT s.subjectId, s.technologyStack.imageName, s.subjectName, c.chapterId, c.chapterName, er.scoreGet "
			+ "FROM Subject s " + "LEFT JOIN s.chapters c ON c.isDeleted = 0 "
			+ "LEFT JOIN ChapterExamResult er ON (er.chapter.chapterId = c.chapterId AND er.student.studentId = :studentId) "
			+ "WHERE s.isDeleted = 0 AND s.subjectId = :subjectId ")
	List<Object[]> getAllChapterWithSubjectIdAndStudentId(@Param("subjectId") Integer subjectId,
			@Param("studentId") Integer studentId);

	// for student test
//	@Query("SELECT NEW com.cico.payload.SubjectExamResponse(e.examName,e.examId,s.technologyStack.imageName,e.examTimer,e.passingMarks,sr.scoreGet ,e.scheduleTestDate,e.totalQuestionForTest ,e.examType ,sr.id,s.subjectId,e.examStartTime ,e.isStart) "
//			+ "FROM Course c " + "LEFT JOIN c.subjects s  ON s.isDeleted = 0 "
//			+ "JOIN s.exams e ON e.isActive = 1 AND e.isDeleted =0 AND e.examType = :examType "
//			+ "LEFT JOIN e.results sr ON sr.student.studentId =:studentId "
//			+ "JOIN Student student ON student.studentId = :studentId "
//			+ "WHERE student.course = c AND c.isDeleted = 0  "
//			+ "GROUP BY s.subjectId, e.examId ,sr.id ORDER BY e.createdDate DESC")
//	Page<SubjectExamResponse> getAllSubjectExam( ExamType examType,@Param("studentId") Integer studentId, PageRequest pageable);

	@Query("SELECT NEW com.cico.payload.SubjectExamResponse("
			+ "e.examName,  s.technologyStack.imageName,e.examId, s.technologyStack.imageName, "
			+ "e.examTimer, e.passingMarks, sr.scoreGet, " + "e.scheduleTestDate, e.totalQuestionForTest, "
			+ "e.examType, sr.id, s.subjectId, " + "e.examStartTime, e.isStart, "
			+ "CASE WHEN sr.id IS NOT NULL THEN 'COMPLETED' ELSE 'PENDING' END) " + "FROM Course c "
			+ "LEFT JOIN c.subjects s ON s.isDeleted = 0 "
			+ "JOIN s.exams e ON e.isActive = 1 AND e.isDeleted = 0 AND e.examType = :examType "
			+ "LEFT JOIN e.results sr ON sr.student.studentId = :studentId "
			+ "JOIN Student student ON student.studentId = :studentId "
			+ "WHERE student.course = c AND c.isDeleted = 0 " + "AND (:status IS NULL OR "
			+ "    (:status = 'PENDING' AND sr.id IS NULL) OR " + "    (:status = 'COMPLETED' AND sr.id IS NOT NULL)) "
			+ "GROUP BY s.subjectId, e.examId, sr.id " + "ORDER BY e.createdDate DESC")
	Page<SubjectExamResponse> getAllSubjectExam(@Param("examType") ExamType examType,
			@Param("studentId") Integer studentId, @Param("status") String status, Pageable pageable);

	@Query("SELECT  s FROM Subject s RIGHT JOIN  s.exams e ON e.isDeleted = 0 AND s.isActive = 1 WHERE e.examId =:examId ")
	Subject findByExamId(Integer examId);

	// ............... NEW QUERY ...................
//
//	@Query("SELECT NEW com.cico.payload.SubjectExamResponse("
//			+ "e.examName, s.technologyStack.imageName, e.examId, s.technologyStack.imageName, "
//			+ "e.examTimer, e.passingMarks, sr.scoreGet, " + "e.scheduleTestDate, e.totalQuestionForTest, "
//			+ "e.examType, sr.id, s.subjectId, " + "e.examStartTime, e.isStart, "
//			+ "CASE WHEN sr.id IS NOT NULL THEN 'COMPLETED' ELSE 'PENDING' END) " + "FROM Course c "
//			+ "LEFT JOIN c.subjects s ON s.isDeleted = 0 "
//			+ "JOIN s.exams e ON e.isActive = true AND e.isDeleted = false  AND e.isStart = false AND e.examType = :examType "
//			+ "LEFT JOIN e.results sr ON sr.student.studentId = :studentId "
//			+ "JOIN Student student ON student.studentId = :studentId "
//			+ "WHERE student.course = c AND c.isDeleted = false"
//			+ " AND ((e.scheduleTestDate > :startDate) OR (e.scheduleTestDate = :startDate AND e.examStartTime >= CURRENT_TIME))"
//			+ " AND (:endDate IS NULL OR e.scheduleTestDate <= :endDate)" + "GROUP BY s.subjectId, e.examId, sr.id"
//			+ "ORDER BY e.scheduleTestDate ASC, e.examStartTime ASC")
//	List<SubjectExamResponse> findUpcomingSubjectExams(@Param("examType") ExamType examType,
//			@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,
//			@Param("studentId") Integer studentId);

	@Query("""
			    SELECT NEW com.cico.payload.SubjectExamResponse(
			        e.examName,
			        s.technologyStack.imageName,
			        e.examId,
			        s.technologyStack.imageName,
			        e.examTimer,
			        e.passingMarks,
			        sr.scoreGet,
			        e.scheduleTestDate,
			        e.totalQuestionForTest,
			        e.examType,
			        sr.id,
			        s.subjectId,
			        e.examStartTime,
			        e.isStart,
			        CASE WHEN sr.id IS NOT NULL THEN 'COMPLETED' ELSE 'PENDING' END
			    )
			    FROM Course c
			    LEFT JOIN c.subjects s ON s.isDeleted = false
			    JOIN s.exams e ON e.isActive = true AND e.isDeleted = false AND e.isStart = false AND e.examType = :examType
			    LEFT JOIN e.results sr ON sr.student.studentId = :studentId
			    JOIN Student student ON student.studentId = :studentId
			    WHERE student.course = c AND c.isDeleted = false
			      AND (
			            (e.scheduleTestDate > :startDate)
			         OR (e.scheduleTestDate = :startDate AND e.examStartTime >= CURRENT_TIME)
			      )
			      AND (
			           :endDate IS NULL OR e.scheduleTestDate <= :endDate
			      )
			    GROUP BY s.subjectId, e.examId, sr.id
			    ORDER BY e.scheduleTestDate ASC, e.examStartTime ASC
			""")
	List<SubjectExamResponse> findUpcomingSubjectExams(@Param("examType") ExamType examType,
			@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,
			@Param("studentId") Integer studentId);

}
