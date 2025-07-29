package com.cico.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cico.model.Announcement;
import com.cico.model.ClearedAnnouncement;
import com.cico.model.Student;

public interface ClearedAnnouncementRepository extends JpaRepository<ClearedAnnouncement, Long> {
	
	boolean existsByStudentAndAnnouncement(Student student, Announcement announcement);


}
