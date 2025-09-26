package com.cico.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cico.exception.ResourceAlreadyExistException;
import com.cico.exception.ResourceNotFoundException;
import com.cico.kafkaServices.KafkaProducerService;
import com.cico.model.Announcement;
import com.cico.model.ClearedAnnouncement;
import com.cico.model.Course;
import com.cico.model.MessageSeenBy;
import com.cico.model.Student;
import com.cico.payload.AnnouncementActionRequest;
import com.cico.payload.AnnouncementRequest;
import com.cico.payload.AnnouncementResponseForAdmin;
import com.cico.payload.AnnouncementStudentResponse;
import com.cico.payload.NotificationInfo;
import com.cico.payload.PageResponse;
import com.cico.repository.AnnouncementRepository;
import com.cico.repository.ClearedAnnouncementRepository;
import com.cico.repository.CourseRepository;
import com.cico.repository.StudentRepository;
import com.cico.service.IAnnouncementService;
import com.cico.util.AppConstants;
import com.cico.util.NotificationConstant;

@Service
public class AnnouncementServiceImpl implements IAnnouncementService {

	@Autowired
	private AnnouncementRepository announcementRepository;

	@Autowired
	private CourseRepository courseRepository;

	@Autowired
	private StudentRepository studentRepository;

	@Autowired
	private KafkaProducerService kafkaProducerService;
	@Autowired
	private ClearedAnnouncementRepository clearedAnnouncementRepository;

	@Override
	public ResponseEntity<?> publishAnnouncement(AnnouncementRequest announcementRequest) {
		List<Course> courses = courseRepository.findBycourseIdInAndIsDeletedFalse(announcementRequest.getCourseId());
		long totalStudents = studentRepository
				.findBycourseIdInAndIsActiveTrueAndIsCompletedFalse(announcementRequest.getCourseId());
		Announcement announcement = new Announcement();
		announcement.setCourse(courses);
		announcement.setDate(LocalDateTime.now());
		announcement.setMessage(announcementRequest.getMessage());
		announcement.setTitle(announcementRequest.getTitle());

		MessageSeenBy seenBy = new MessageSeenBy();
		seenBy.setSeenBy(0L);
		seenBy.setTotalStudents(totalStudents);
		announcement.setSeenBy(seenBy);
		Announcement save = announcementRepository.save(announcement);

		// fetching all the fcmId
		// sending message via kafka to firebase
		List<NotificationInfo> fcmIds = studentRepository.findAllFcmIdByCourseIds(courses);
		kafkaProducerService.sendNotification(NotificationConstant.ANNOUNCEMENT_TOPIC, fcmIds.toString());
		return new ResponseEntity<>(announcementFilter(save), HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<?> getAllPublishedAnnouncement(Integer page, Integer size) {

		if (page != -1) {
			Page<Announcement> announcements = announcementRepository
					.findAll(PageRequest.of(page, size, Sort.by(Direction.DESC, "date")));
			PageResponse<Announcement> pageResponse = new PageResponse<>(announcements.getContent(),
					announcements.getNumber(), announcements.getSize(), announcements.getNumberOfElements(),
					announcements.getTotalPages(), announcements.isLast());

			List<AnnouncementResponseForAdmin> collect = pageResponse.getResponse().stream()
					.map(this::announcementFilterForAdmin).toList();

			return new ResponseEntity<>(collect, HttpStatus.OK);
		} else {
			List<AnnouncementResponseForAdmin> collect = announcementRepository.findAll().stream()
					.map(this::announcementFilterForAdmin).sorted((o1, o2) -> o1.getDate().compareTo(o2.getDate()))
					.toList();
			return new ResponseEntity<>(collect, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<?> seenAnnouncement(Long announcementId, Integer studentId) {
		Announcement announcement = announcementRepository.findById(announcementId).get();
		Student student = studentRepository.findByStudentId(studentId);
		if (!announcement.getStudents().contains(student)) {
			announcement.getStudents().add(student);
			announcement.getSeenBy().setSeenBy(announcement.getSeenBy().getSeenBy() + 1);
			return new ResponseEntity<>(announcementFilter(announcementRepository.save(announcement)),
					HttpStatus.CREATED);
		} else {
			throw new ResourceAlreadyExistException("Already Seen");
		}
	}

	@Override
	public ResponseEntity<?> getAnnouncementForStudent(Integer studentId) {

		Student student = studentRepository.findById(studentId).get();

		List<AnnouncementStudentResponse> collect = announcementRepository
				.getAnnouncementForStudentByCourse(student.getCourse(), student).stream().map(this::announcementFilter)
				.toList();

		return new ResponseEntity<>(collect, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> countUnseenNotificationForStudent(Integer studentId) {
		Student student = studentRepository.findById(studentId).get();
		Long announcements = announcementRepository.countUnseenNotificationForStudent(student.getCourse(), student);
		return new ResponseEntity<>(Map.of("announcementCount", announcements), HttpStatus.OK);
	}

	public AnnouncementStudentResponse announcementFilter(Announcement response) {
		AnnouncementStudentResponse res = new AnnouncementStudentResponse();
		res.setAnnouncementId(response.getAnnouncementId());
		res.setDate(response.getDate());
		res.setMessage(response.getMessage());
		res.setTitle(response.getTitle());
		return res;
	}

	public AnnouncementResponseForAdmin announcementFilterForAdmin(Announcement response) {
		AnnouncementResponseForAdmin res = new AnnouncementResponseForAdmin();
		res.setAnnouncementId(response.getAnnouncementId());
		res.setDate(response.getDate());
		res.setMessage(response.getMessage());
		res.setTitle(response.getTitle());
		res.setSeenBy(response.getSeenBy());
		res.setCourseName(response.getCourse().stream().map(obj -> obj.getCourseName()).collect(Collectors.toList()));
		return res;
	}

	// ...................... NEW METHODS ..................

	@Override
	public ResponseEntity<?> getAnnouncementForStudentNew(Integer studentId) {

		Student student = studentRepository.findById(studentId)
				.orElseThrow(() -> new RuntimeException(AppConstants.STUDENT_NOT_FOUND));

		List<AnnouncementStudentResponse> collect = announcementRepository
				.getAnnouncementForStudentByCourseNew(student.getCourse(), student).stream()
				.map(announcement -> announcementFilter(announcement, student)).toList();

		return new ResponseEntity<>(collect, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> markAnnouncementsAsSeen(AnnouncementActionRequest request) {
		Student student = studentRepository.findByStudentId(request.getStudentId());
		if (student == null)
			throw new ResourceNotFoundException(AppConstants.STUDENT_NOT_FOUND);

		List<Announcement> announcements = announcementRepository.findAllById(request.getAnnouncementIds());
		List<AnnouncementStudentResponse> newlySeenAnnouncements = new ArrayList<>();

		for (Announcement announcement : announcements) {
			if (!announcement.getStudents().contains(student)) {
				announcement.getStudents().add(student);

				MessageSeenBy seenBy = announcement.getSeenBy();
				if (seenBy == null) {
					seenBy = new MessageSeenBy();
					seenBy.setSeenBy(1L);
					announcement.setSeenBy(seenBy);
				} else {
					seenBy.setSeenBy(seenBy.getSeenBy() == null ? 1L : seenBy.getSeenBy() + 1);
				}

				announcementRepository.save(announcement);
				newlySeenAnnouncements.add(announcementFilter(announcement, student)); // only unseen ones processed
			}
		}

		if (newlySeenAnnouncements.isEmpty()) {
			throw new ResourceAlreadyExistException(AppConstants.ALL_ANNOUNCEMENTS_ALREADY_SEEN);
		}

		return new ResponseEntity<>(newlySeenAnnouncements, HttpStatus.CREATED);
	}

	public AnnouncementStudentResponse announcementFilter(Announcement response, Student student) {
		AnnouncementStudentResponse res = new AnnouncementStudentResponse();
		res.setAnnouncementId(response.getAnnouncementId());
		res.setDate(response.getDate());
		res.setMessage(response.getMessage());
		res.setTitle(response.getTitle());
		res.setIsSeen(response.getStudents().contains(student));
		return res;
	}

	@Override
	public ResponseEntity<?> clearNotificationForStudent(AnnouncementActionRequest clearAnnouncementRequest) {
		Student student = studentRepository.findByStudentId(clearAnnouncementRequest.getStudentId());
		if (student == null)
			throw new ResourceNotFoundException(AppConstants.STUDENT_NOT_FOUND);

		List<Announcement> announcements = announcementRepository
				.findAllById(clearAnnouncementRequest.getAnnouncementIds());

		List<ClearedAnnouncement> toBeCleared = new ArrayList<>();

		for (Announcement announcement : announcements) {

			// Mark as seen if not already
			if (!announcement.getStudents().contains(student)) {
				announcement.getStudents().add(student);
			}

			// Check if already cleared
			boolean alreadyCleared = clearedAnnouncementRepository.existsByStudentAndAnnouncement(student,
					announcement);
			if (!alreadyCleared) {
				ClearedAnnouncement cleared = new ClearedAnnouncement();
				cleared.setStudent(student);
				cleared.setAnnouncement(announcement);
				toBeCleared.add(cleared);
			}
		}

		// Save updated announcement (seen status)
		announcementRepository.saveAll(announcements);

		// If all were already cleared
		if (toBeCleared.isEmpty()) {
			return new ResponseEntity<>(AppConstants.ALL_ANNOUNCEMENTS_ALREADY_CLEARED, HttpStatus.OK);
		}

		// Save cleared announcements
		clearedAnnouncementRepository.saveAll(toBeCleared);

		return new ResponseEntity<>(AppConstants.ANNOUNCEMENTS_CLEARED_SUCCESSFULLY, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> deleteAnnouncement(Long announcementId) {
		Announcement announcement = announcementRepository.findById(announcementId)
				.orElseThrow(() -> new ResourceNotFoundException(AppConstants.ANNOUNCEMENT_NOT_FOUND));
		announcement.setIsDeleted(true);

		announcementRepository.save(announcement);

		return ResponseEntity.ok("Announcement deleted successfully.");
	}

}
