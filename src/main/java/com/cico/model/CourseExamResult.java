package com.cico.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "course_exam_result") // Added table name
public class CourseExamResult {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;

	@Column(name = "correct_questions")
	private Integer correcteQuestions;

	@Column(name = "wrong_questions")
	private Integer wrongQuestions;

	@Column(name = "not_selected_questions")
	private Integer notSelectedQuestions;

	@ManyToOne
	@JoinColumn(name = "student_id")
	private Student student;

	@Column(name = "score_obtained")
	private Integer scoreGet;

	@Column(name = "total_questions")
	private Integer totalQuestion;

	@ElementCollection
	@CollectionTable(name = "course_exam_review", joinColumns = @JoinColumn(name = "course_exam_result_id"))
	@MapKeyColumn(name = "question_id")
	@Column(name = "selected_option")
	private Map<Integer, String> review = new HashMap<>();

	@ManyToOne
	@JoinColumn(name = "course_exam_id")
	private CourseExam courseExam;

	@ElementCollection
	@CollectionTable(name = "course_exam_random_questions", joinColumns = @JoinColumn(name = "course_exam_result_id"))
	@Column(name = "question_id")
	private List<Integer> randomQuestoinList = new ArrayList<>();
}
