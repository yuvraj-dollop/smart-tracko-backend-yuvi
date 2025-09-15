package com.cico.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class SubjectExamResult {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	private Integer correcteQuestions;
	private Integer wrongQuestions;
	private Integer notSelectedQuestions;

	@OneToOne
	@JoinColumn
	private Chapter chapter;
	@OneToOne
	@JoinColumn
	private Student student;
	private Integer scoreGet;
	public Integer totalQuestion;

	@ElementCollection
	@CollectionTable
	private Map<Integer, String> review = new HashMap<>();
	private Integer subjectExamId;
	@JsonIgnore
	@OneToOne
	@JoinColumn
	private Subject subject;
	@ElementCollection
	@CollectionTable
	private List<Integer> randomQuestoinList = new ArrayList<>();
	private double latestPercentage;
}
