package com.cico.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class SubjectExamResult extends ChapterExamResult {

	private Integer subjectExamId;
	@JsonIgnore
	@OneToOne
	@JoinColumn
	private Subject subject;
	@ElementCollection
	@CollectionTable
	private List<Integer> randomQuestoinList = new ArrayList<>();
}
