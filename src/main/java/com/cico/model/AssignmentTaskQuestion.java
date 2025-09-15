package com.cico.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class AssignmentTaskQuestion {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long questionId;

	private String title;

	@Column(columnDefinition = "longtext")
	private String question;

	@ElementCollection
	@CollectionTable(name = "assignment_question_images", joinColumns = @JoinColumn(name = "question_id"))
	@Column(name = "image_url")
	private List<String> questionImages = new ArrayList<>();

	@Column(columnDefinition = "longtext")
	private String videoUrl;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<AssignmentSubmission> assignmentSubmissions = new ArrayList<>();

	@ManyToOne
	@JoinColumn(name = "assignment_id", nullable = false)
	private Assignment assignment;

	private Boolean isDeleted = Boolean.FALSE;
	private LocalDateTime createdDate;
	private long taskNumber;
	private Boolean isActive = Boolean.FALSE;
}
