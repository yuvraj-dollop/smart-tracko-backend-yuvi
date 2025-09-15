package com.cico.model;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
@NoArgsConstructor
@Entity
public class Task {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long taskId;

	private String taskName;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "task")
	@JsonManagedReference
	private List<TaskQuestion> taskQuestion;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id")
	private Course course;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subject_id")
	private Subject subject;

	private Boolean isDeleted = Boolean.FALSE;
	private Boolean isActive = Boolean.FALSE;
	private LocalDateTime createdDate = LocalDateTime.now();
	private LocalDateTime updatedDate = LocalDateTime.now();
	private Boolean isLatest = Boolean.TRUE;

}
