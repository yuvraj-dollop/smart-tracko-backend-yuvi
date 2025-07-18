package com.cico.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import lombok.AllArgsConstructor;
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
public class TaskQuestion {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long questionId;

	@Column(nullable = false, columnDefinition = "varchar(255)")
	private String title;

	@Column(columnDefinition = "longtext")
	private String question;

	@ElementCollection
	@CollectionTable
	private List<String> questionImages = new ArrayList<>();

	private String videoUrl;

	@ManyToOne
	@JoinColumn(name = "task_id")
	private Task task;

	private Boolean isDeleted = Boolean.FALSE;
	private long taskNumber;
	private Boolean isActive = Boolean.TRUE;

	private String taskAttachment;
	@Enumerated(EnumType.STRING)
	private AttachmentStatus attachmentStatus;

	@OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
	private List<TaskSubmission> taskSubmissions;

	private Boolean codeSubmisionStatus = Boolean.FALSE;
}
