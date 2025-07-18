package com.cico.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import lombok.*;

@Data
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "assignments")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String title;
    private String taskAttachment;

    @OneToMany( mappedBy = "assignment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AssignmentTaskQuestion> AssignmentQuestion = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "course_id") // Explicit join column name
    private Course course;

    @OneToOne
    @JoinColumn(name = "subject_id") // Explicit join column name
    private Subject subject;

    private Boolean isDeleted = Boolean.FALSE;
    private Boolean isActive = Boolean.TRUE;

    private LocalDateTime createdDate;
    private Boolean isNotificatioSend;
}
