package com.cico.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cico.payload.AnnouncementActionRequest;
import com.cico.payload.AnnouncementRequest;
import com.cico.service.IAnnouncementService;
import com.cico.util.AppConstants;

@CrossOrigin("*")
@RestController
@RequestMapping("/announcement")
public class AnnouncementController {

	@Autowired
	private IAnnouncementService announcementService;

	@PostMapping("/publishAnnouncement")
	public ResponseEntity<?> publishAnnouncement(@RequestBody @Valid AnnouncementRequest announcementRequest) {
		return announcementService.publishAnnouncement(announcementRequest);
	}

	@GetMapping("/getAllAnnouncement")
	public ResponseEntity<?> getAllPublishedAnnouncement(@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "size", required = false) Integer size) {
		return announcementService.getAllPublishedAnnouncement(page, size);
	}

	@PostMapping("/seenAnnouncement")
	public ResponseEntity<?> seenAnnouncement(@RequestParam(name = AppConstants.ANNOUNCEMENT_ID) Long announcementId,
			@RequestParam(name = AppConstants.STUDENT_ID) Integer studentId) {
		return announcementService.seenAnnouncement(announcementId, studentId);
	}

	@GetMapping("/getAnnouncementForStudent")
	public ResponseEntity<?> getAnnouncementForStudent(
			@RequestParam(name = AppConstants.STUDENT_ID) Integer studentId) {
		return announcementService.getAnnouncementForStudent(studentId);
	}

	@GetMapping("/getNotificationCountForStudent")
	public ResponseEntity<?> countUnseenNotificationForStudent(
			@RequestParam(name = AppConstants.STUDENT_ID) Integer studentId) {
		return announcementService.countUnseenNotificationForStudent(studentId);
	}

	// ---------------- NEW API's --------------------------

	@GetMapping("/v2/getAnnouncementForStudent")
	public ResponseEntity<?> getAnnouncementForStudentNew(
			@RequestParam(name = AppConstants.STUDENT_ID) Integer studentId) {
		return announcementService.getAnnouncementForStudentNew(studentId);
	}

//	@GetMapping("/v2/getNotificationCountForStudent")
//	public ResponseEntity<?> countUnseenNotificationForStudentNew(
//			@RequestParam(name = AppConstants.STUDENT_ID) Integer studentId) {
//		return announcementService.countUnseenNotificationForStudent(studentId);
//	}

	@PostMapping("/v2/seenAnnouncement")
	public ResponseEntity<?> markAnnouncementsAsSeen(@Valid @RequestBody AnnouncementActionRequest request) {
		return announcementService.markAnnouncementsAsSeen(request);
	}

	@PostMapping("/v2/clearNotificationForStudent")
	public ResponseEntity<?> clearNotificationForStudent(@Valid @RequestBody AnnouncementActionRequest request) {
		return announcementService.clearNotificationForStudent(request);
	}

}
