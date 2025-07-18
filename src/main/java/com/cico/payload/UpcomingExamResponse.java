package com.cico.payload;

import java.time.LocalDate;
import java.time.LocalTime;

import com.cico.util.ExamType;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpcomingExamResponse {

    private Integer examId;
    private String examName;
    private String examImage;

    private Integer examTimer;
    private Integer totalQuestionForTest;
    private Integer passingMarks;
    private Integer scoreGet;

    private LocalDate scheduleTestDate;
    private LocalTime examStartTime;

    private Boolean isStart;
    private Boolean isExamEnd;
    private Integer extraTime;

    private ExamType examType;
    private Integer resultId;
    private String status;

    // Differentiator fields
    private String examFrom; // "COURSE" or "SUBJECT"

    // Optional fields
    private Integer courseId;
    private String courseName;

    private Integer subjectId;
    private String subjectName;
}

