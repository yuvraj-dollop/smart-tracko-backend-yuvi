package com.cico.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cico.model.Course;
import com.cico.model.Student;
import com.cico.payload.NotificationInfo;
import com.cico.payload.OnLeavesResponse;
import com.cico.payload.StudentReponseForWeb;
import com.cico.payload.TodayLeavesRequestResponse;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {

	Optional<Student> findByUserIdOrEmailAndAndIsActive(String username, String email, boolean b);

	Optional<Student> findByUserIdAndIsActive(String username, boolean b);

	Student findByInUseDeviceId(String deviceId);

	Student findByUserId(String userId);

	Student findByStudentId(Integer studentId);

	Optional<Student> findByEmail(String email);

	Student findByinUseDeviceId(String deviceId);

	@Query("SELECT NEW com.cico.payload.StudentReponseForWeb (s.fullName, s.mobile ,s.profilePic ,s.applyForCourse , s.studentId ) FROM Student s WHERE  s.isCompleted = 0 AND  s.studentId  NOT IN ("
			+ "SELECT a.studentId FROM Attendance a WHERE DATE(a.checkInDate) = DATE(:todaysdate))  ")
	Page<StudentReponseForWeb> getTotalTodayAbsentStudent(@Param("todaysdate") LocalDate todaysdate,
			PageRequest pageRequest);

	@Query("SELECT NEW com.cico.payload.OnLeavesResponse(l.leaveDate, l.leaveEndDate, s.profilePic, s.applyForCourse, s.studentId, s.fullName) FROM Leaves l JOIN Student s ON s.studentId = l.studentId WHERE l.leaveStatus = 1 AND CURRENT_DATE() BETWEEN DATE(l.leaveDate) AND DATE(l.leaveEndDate)")
	Page<OnLeavesResponse> getTotalStudentInLeaves(PageRequest pageRequest);

	@Query("SELECT NEW com.cico.payload.TodayLeavesRequestResponse("
			+ "l.leaveDate, l.leaveEndDate, s.studentId, s.fullName, s.profilePic, s.applyForCourse, "
			+ "l.leaveTypeId, l.leaveDuration, l.leaveReason, l.leaveId, lt.leaveTypeName ) " + "FROM Leaves l "
			+ "INNER JOIN Student s ON l.studentId = s.studentId "
			+ "INNER JOIN LeaveType lt ON l.leaveTypeId = lt.leaveTypeId "
			+ "WHERE l.leaveStatus = 0 AND CURRENT_DATE() < l.leaveDate")
	Page<TodayLeavesRequestResponse> getTotalTodaysLeavesRequest(PageRequest pageRequest);

	@Query("SELECT  s.studentId,s.fullName,s.mobile,s.dob,s.profilePic,c.courseName ,c.courseId ,s.email, s.joinDate , s.applyForCourse ,f.finalFees "
			+ "FROM Student s " + "LEFT JOIN Fees f ON f.student.studentId = s.studentId " + "LEFT JOIN s.course  c  "
			+ " WHERE s.isCompleted=:isCompleted AND s.isActive=:isActive GROUP BY s.studentId ,f.feesId ,c.courseId ")
	Page<Object[]> findAllByIsCompletedAndIsActive(Boolean isCompleted, Boolean isActive, Pageable pageable);

	@Query("SELECT NEW com.cico.payload.StudentReponseForWeb(s.studentId, s.userId, s.fullName, s.mobile, s.email, s.dob, s.profilePic, s.currentCourse, s.applyForCourse, c.courseId, c.courseName ,s.joinDate) FROM Student s LEFT JOIN s.course c WHERE (s.fullName LIKE %:fullName% OR :fullName = '' ) GROUP BY s.studentId, c.courseId")
	Page<StudentReponseForWeb> findAllByFullNameContaining(@Param("fullName") String fullName, PageRequest pageRequest);

	@Query("select count(s) from Student s where s.isCompleted=0")
	Long countTotalStudents();

	@Query("SELECT s.userId AS userId, s.fullName AS fullName, s.profilePic AS profilePic, "
			+ "a.checkInDate AS checkInDate, a.checkOutDate AS checkOutDate, "
			+ "a.checkInTime AS checkInTime, a.checkOutTime AS checkOutTime, "
			+ "a.checkInImage AS checkInImage, a.checkOutImage AS checkOutImage, " + "seat.seatNumber AS seatNumber "
			+ "FROM Student s " + "INNER JOIN Attendance a ON a.studentId = s.studentId "
			+ "INNER JOIN StudentSeatingAlloatment seat ON seat.student.studentId = s.studentId "
			+ "WHERE s.isCompleted = 0 AND a.checkInDate = :date " + "ORDER BY a.workingHour DESC , a.checkInTime")
	List<Object[]> getStudentAttendanceDataForTv(@Param("date") LocalDate date);

	@Query("SELECT MONTH(s.joinDate) AS month, COUNT(s.studentId) AS count FROM Student s "
			+ "WHERE YEAR(s.joinDate) = :year GROUP BY MONTH(s.joinDate)")
	List<Object[]> getMonthwiseAdmissionCountForYear(Integer year);

	@Query("SELECT MONTH(s.joinDate) AS month, COUNT(s.studentId) AS count FROM Student s "
			+ "WHERE YEAR(s.joinDate) = :year   GROUP BY MONTH(s.joinDate)")
	List<Object[]> getAbsent(Integer year);

	Optional<Student> findByEmailAndMobile(String email, String mobile);

	@Query("SELECT s FROM Student s Where s.isCompleted=0")
	List<Student> getIsCompleted();

	@Query("SELECT COUNT(s) FROM Student s WHERE s.course.courseId IN :courseIds AND s.isActive = 1 AND s.isCompleted = 0")
	long findBycourseIdInAndIsActiveTrueAndIsCompletedFalse(@Param("courseIds") List<Integer> courseIds);

	@Query("SELECT COUNT(l) FROM Leaves l Where l.leaveStatus=1 AND CURRENT_DATE() BETWEEN DATE(l.leaveDate) AND DATE(l.leaveEndDate)  ")
	public Long getTotalOnLeavesCount();

	@Query("SELECT NEW com.cico.payload.StudentReponseForWeb(s.fullName,s.studentId) FROM Student s WHERE s NOT IN (SELECT f.student FROM Fees f )")
	List<StudentReponseForWeb> allFeesRemainingStudent();

	// FINDING FCM ID'S FOR FIREBASE NOTIFICATION

	@Query("SELECT NEW com.cico.payload.NotificationInfo(s.studentId ,s.fcmId,s.fullName) FROM Student s  WHERE s.course IN :course")
	List<NotificationInfo> findAllFcmIdByCourseIds(@Param("course") List<Course> course);

	@Query("SELECT NEW com.cico.payload.NotificationInfo(s.studentId ,s.fcmId,s.fullName) FROM Student s  WHERE s.course.courseId =:courseId")
	List<NotificationInfo> findAllFcmIdByCourseId(Integer courseId);

	@Query("SELECT NEW com.cico.payload.NotificationInfo(s.studentId ,s.fcmId,s.fullName) FROM Student s  WHERE s.studentId =:studentId")
	NotificationInfo findFcmIdByStudentId(@Param("studentId") Integer studentId);

	@Query("SELECT NEW com.cico.payload.NotificationInfo(s.studentId ,s.fcmId,s.fullName) FROM Student s "
			+ "JOIN s.course  as  c "
			+ " WHERE  c IN (SELECT course  FROM Course  as course JOIN course.subjects as subject WHERE subject.subjectId =: subjectId) ")
	List<NotificationInfo> findAllFcmIdBySubjectId(@Param("subjectId") Integer subjectId);

	@Query("SELECT NEW com.cico.payload.NotificationInfo(s.studentId ,s.fcmId,s.fullName) FROM Student s "
			+ "JOIN s.course  as  c "
			+ " WHERE  c IN (SELECT course  FROM Course  as course JOIN course.subjects as subject "
			+ " LEFT JOIN subject.exams as e " + "WHERE e.examId =:examId ) ")
	List<NotificationInfo> findAllFcmIdByExamId(Integer examId);

	@Query("SELECT NEW com.cico.payload.NotificationInfo(s.studentId ,s.fcmId,s.fullName) FROM Student s  WHERE s.isCompleted =FALSE ")
	List<NotificationInfo> fetchAllStudentIsCompletedFalse();

	@Transactional
	@Modifying
	@Query("UPDATE  Student  s SET s.fcmId =:fcmId   WHERE s.studentId =:studentId ")
	int updateFcmId(@Param("fcmId") String fcmId, @Param("studentId") Integer studentId);

//	@Query("SELECT  DAY(ts.submissionDate) as day "
//			+ ",COUNT(ts) as totalSubmission "
//			+ ",COUNT(CASE WHEN ts.status ='Accepted' THEN 1 ELSE NULL END )as totalAccepted"
//			+ " ,COUNT(CASE WHEN ts.status ='Rejected' THEN 1 ELSE NULL END) as totalRejected "
//			+ "  TaskSubmission  as ts "
//			+ " WHERE ts.student.studentId =:studentId AND "
//			+ " MONTH(ts.submissionDate) =:month "
//			+ " GROUP BY  DAY(ts.submissionDate ) ")
//	 List<Object[]> getTaskStatics(@Param("studentId") Integer studentId,@Param("month") Integer month);

	Optional<Student> findByEmailAndIsActive(String username, boolean b);

	@Query("SELECT COUNT(s) > 0 FROM Student s WHERE s.course.courseId = :courseId AND s.studentId = :studentId AND s.isActive = true AND s.isCompleted = false ")
	boolean validateStudentCourse(Integer courseId, Integer studentId);

	// END FCM ID'S //
	Long countByCourse_CourseId(Integer courseId);

	List<Student> findByCourse_CourseId(Integer id);

	@Query("SELECT COUNT(A.studentId) FROM Attendance A WHERE DATE(A.checkInDate) = DATE(:todaysdate)")
	Long getTotalPresentToday(@Param("todaysdate") LocalDate todaysdate);

	@Query("""
			    SELECT COUNT(l)
			    FROM Leaves l
			    WHERE l.leaveStatus = 1
			      AND FUNCTION('DATE', l.leaveDate) <= DATE(:endDate)
			      AND FUNCTION('DATE', l.leaveEndDate) >= DATE(:startDate)
			""")
	Long getTotalOnLeavesCountNew(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	@Query("""
			    SELECT COUNT(A.studentId)
			    FROM Attendance A
			    WHERE DATE(A.checkInDate) BETWEEN DATE(:startDate) AND DATE(:endDate)
			""")
	Long getTotalPresentTodayNew(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
