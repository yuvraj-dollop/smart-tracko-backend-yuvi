package com.cico.controller;

import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cico.payload.ApiResponse;
import com.cico.payload.CourseRequest;
import com.cico.payload.CourseResponse;
import com.cico.service.ICourseService;
import com.cico.util.AppConstants;

@RestController
@RequestMapping("/course")
@CrossOrigin("*")
public class CourseController {

	@Autowired
	private ICourseService courseService;

	// =============== COURSE CRUD OPERATIONS ===============
	@PostMapping("/addCourseApi")
	public ResponseEntity<?> createCourse(@RequestBody @Valid CourseRequest courseRequest) {
		return courseService.createCourse(courseRequest);
	}

	@GetMapping("/findCourseByIdApi")
	public ResponseEntity<CourseResponse> getCourseById(@RequestParam(name = AppConstants.COURSE_ID) Integer courseId) {
		CourseResponse courseResponse = courseService.findCourseById(courseId);
		return ResponseEntity.ok(courseResponse);
	}

	@PutMapping("/updateCourseApi")
	public ResponseEntity<?> updateCourse(@Valid @RequestBody CourseRequest course) {
		ApiResponse updateCourse = courseService.updateCourse(course);
		return ResponseEntity.status(HttpStatus.CREATED).body(updateCourse);
	}

	@PutMapping("/deleteCourseByIdApi")
	public ResponseEntity<ApiResponse> deleteCourseById(@RequestParam(name = AppConstants.COURSE_ID) Integer courseId) {
		Boolean deleteCourseById = courseService.deleteCourseById(courseId);
		if (deleteCourseById != null) {
			return ResponseEntity.ok(new ApiResponse(Boolean.TRUE, AppConstants.DELETE_SUCCESS, HttpStatus.OK));
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
				new ApiResponse(Boolean.FALSE, AppConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR));
	}

	// =============== COURSE LISTS ===============
	@GetMapping("/findAllCourseApi")
	public ResponseEntity<?> getAllCourses(
			@RequestParam(name = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer page,
			@RequestParam(name = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer size) {
		return courseService.getAllCourses(page, size);
	}

	@GetMapping("/getAllNonStarterCourses")
	public ResponseEntity<?> getAllNonStarterCourses() {
		return courseService.getAllNonStarterCourses();
	}

	@GetMapping("/getAllCourseApi")
	public ResponseEntity<?> getAllStarterCourses() {
		return courseService.getAllStarterCourses();
	}

	// =============== STUDENT-SPECIFIC OPERATIONS ===============
	@PutMapping("/studentUpgradeCourse")
	public ResponseEntity<?> studentUpgradeCourse(@RequestParam(name = AppConstants.STUDENT_ID) Integer studnetId,
			@RequestParam(name = AppConstants.COURSE_ID) Integer courseId) {
		Map<String, Object> response = courseService.studentUpgradeCourse(studnetId, courseId);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/getCourseProgress")
	public ResponseEntity<?> getCourseProgress(@RequestParam(name = AppConstants.STUDENT_ID) Integer studentId) {
		return courseService.getCourseProgress(studentId);
	}

	@GetMapping("/getAllCourseForStudent")
	public ResponseEntity<?> getAllCourseForStudent(@RequestParam(name = AppConstants.STUDENT_ID) Integer studentId) {
		return courseService.getAllCourseForStudent(studentId);
	}

	// =============== COURSE DETAILS ===============
	@GetMapping("/getCoureWithBatchesAndSubjects")
	public ResponseEntity<?> getCoureWithBatchesAndSubjects(
			@RequestParam(name = AppConstants.COURSE_ID) Integer courseId) {
		return courseService.getCoureWithBatchesAndSubjects(courseId);
	}
	// =================================================== New Apis
	// =============================================================

	@GetMapping("/v2/getAllCourseForStudent")
	public ResponseEntity<?> getAllCourseForStudentNew(
			@RequestParam(name = AppConstants.STUDENT_ID) Integer studentId) {
		return courseService.getAllCourseForStudentNew(studentId);
	}

	@GetMapping("/v2/getCourseProgress")
	public ResponseEntity<?> getCourseProgressNew(@RequestParam(name = AppConstants.STUDENT_ID) Integer studentId) {
		return courseService.getCourseProgressNew(studentId);
	}

	@GetMapping("/v2/findAllCourseApi")
	public ResponseEntity<?> getAllCoursesNew(
			@RequestParam(name = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer page,
			@RequestParam(name = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer size) {
		return courseService.getAllCourses(page, size);
	}

	@PostMapping("/v2/addCourseApi")
	public ResponseEntity<?> createCourseNew(@RequestBody @Valid CourseRequest courseRequest) {
		return courseService.createCourseNew(courseRequest);
	}

	@GetMapping("/v2/findCourseByIdApi")
	public ResponseEntity<CourseResponse> getCourseByIdNew(
			@RequestParam(name = AppConstants.COURSE_ID) Integer courseId) {
		CourseResponse courseResponse = courseService.findCourseById(courseId);
		return ResponseEntity.ok(courseResponse);
	}
}