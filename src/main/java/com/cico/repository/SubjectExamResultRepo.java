package com.cico.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cico.model.Student;
import com.cico.model.SubjectExamResult;

@Repository
public interface SubjectExamResultRepo extends JpaRepository<SubjectExamResult, Integer> {

	@Query("SELECT s FROM  SubjectExamResult s WHERE s.student =:student  AND  s.subjectExamId =:examId ")
	Optional<SubjectExamResult> findByExamIdAndStudentId(Integer examId, Student student);

//..................................... New ......................................................................
	@Query(value = """
			    SELECT r.*
			    FROM subject_exam_result r
			    JOIN subject s ON r.subject_subject_id = s.subject_id
			    JOIN courses_subjects cs ON cs.subjects_subject_id = s.subject_id
			    JOIN courses c ON cs.course_course_id = c.course_id
			    WHERE r.student_student_id = :studentId
			      AND c.course_id = :courseId
			""", nativeQuery = true)
	List<SubjectExamResult> findByStudentAndCourse(@Param("studentId") Integer studentId,
			@Param("courseId") Integer courseId);

	List<SubjectExamResult> findByStudent_StudentIdAndSubject_SubjectId(Integer studentId, Integer subjectId);

	List<SubjectExamResult> findByStudent_StudentId(Integer studentId);

}
