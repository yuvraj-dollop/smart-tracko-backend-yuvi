package com.cico.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
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

    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AssignmentSubmission> assignmentSubmissions = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    private Boolean isDeleted = Boolean.FALSE;
    private LocalDateTime createdDate;
    private long taskNumber;
    private Boolean isActive = Boolean.FALSE;
}
