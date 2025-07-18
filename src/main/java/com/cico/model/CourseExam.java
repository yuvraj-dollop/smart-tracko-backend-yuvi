package com.cico.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.persistence.*;

import com.cico.util.ExamType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "course_exam")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CourseExam {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "exam_id")
	private Integer examId;

	@Column(name = "exam_name", nullable = false)
	private String examName;

	@Column(name = "score")
	private Integer score;

	@Column(name = "exam_image")
	private String examImage;

	@Enumerated(EnumType.STRING)
	@Column(name = "exam_type")
	private ExamType examType;

	@Column(name = "is_deleted")
	private Boolean isDeleted = Boolean.FALSE;

	@Column(name = "is_active")
	private Boolean isActive = Boolean.FALSE;

	@Column(name = "exam_timer")
	private Integer examTimer;

	@Column(name = "passing_marks")
	private Integer passingMarks;

	@Column(name = "total_questions_for_test")
	private Integer totalQuestionForTest;

	@Column(name = "schedule_test_date")
	private LocalDate scheduleTestDate;

	@Column(name = "exam_start_time")
	private LocalTime examStartTime;

	@Column(name = "created_date")
	private LocalDateTime createdDate;

	@Column(name = "updated_date")
	private LocalDateTime updatedDate;

	@Column(name = "extra_time")
	private LocalTime extraTime;

	@Column(name = "is_start")
	private Boolean isStart = Boolean.FALSE;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id")
	private Course course;
}
