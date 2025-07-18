package com.cico.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cico.payload.AddExamRequest;
import com.cico.payload.ExamRequest;
import com.cico.service.IExamService;

@RestController
@RequestMapping("/courseExam")
@CrossOrigin("*")
public class CourseExamController {

    @Autowired
    private IExamService examService;

    // =============== EXAM RESULT OPERATIONS ===============
    @PostMapping("/addCourseExamResult")
    public ResponseEntity<?> addCourseExamResult(@RequestBody ExamRequest request) {
        return examService.addCourseExamResult(request);
    }

    @GetMapping("/getCourseExamResult")
    public ResponseEntity<?> getCourseExamResult(@RequestParam("resultId") Integer resultId) {
        return examService.getCourseExamResult(resultId);
    }

    @GetMapping("/getCourseExamResultsBExamId")
    public ResponseEntity<?> getCourseExamResultsByExamId(@RequestParam("examId") Integer examId) {
        return examService.getCourseExamResultsByExamId(examId);
    }

    // =============== EXAM MANAGEMENT ===============
    @PostMapping("/addCourseExam")
    public ResponseEntity<?> addCourseExam(@RequestBody AddExamRequest request) {
        return examService.addCourseExam(request);
    }

    @PutMapping("/updateCourseExam")
    public ResponseEntity<?> updateCourseExam(@RequestBody AddExamRequest request) {
        return examService.updateCourseExam(request);
    }

    @PutMapping("/deleteExamById")
    public ResponseEntity<?> deleteExamById(@RequestParam("examId") Integer examId) {
        return examService.deleteExamById(examId);
    }

    // =============== EXAM STATUS OPERATIONS ===============
    @PutMapping("/setCourseExamStartStatus")
    public ResponseEntity<?> setCourseExamStartStatus(@RequestParam("examId") Integer examId) {
        return examService.setCourseExamStartStatus(examId);
    }

    @PutMapping("/changeCourseExamStatus")
    public ResponseEntity<?> changeCourseExamStatus(@RequestParam("examId") Integer examId) {
        return examService.changeCourseExamStatus(examId);
    }

    // =============== EXAM RETRIEVAL ===============
    @GetMapping("/getAllCourseNormalAndScheduleExamForStudent")
    public ResponseEntity<?> getAllCourseNormalAndScheduleExamForStudent(
            @RequestParam("studentId") Integer studentId) {
        return examService.getAllCourseNormalAndScheduleExamForStudent(studentId);
    }

    @GetMapping("/getAllCourseNormalAndScheduleExam")
    public ResponseEntity<?> getAllCourseNormalAndScheduleExam(
            @RequestParam("courseId") Integer courseId) {
        return examService.getAllCourseNormalAndScheduleExam(courseId);
    }
}