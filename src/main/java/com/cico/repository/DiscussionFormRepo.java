package com.cico.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cico.model.DiscusssionForm;

@Repository
public interface DiscussionFormRepo extends JpaRepository<DiscusssionForm, Integer> {

	@Query("SELECT d FROM DiscusssionForm d WHERE d.student.fullName LIKE %:name% ")
	List<DiscusssionForm> searching(@Param("name") String name);

	Page<DiscusssionForm> findByStudent_StudentId(Integer studentId, Pageable pageable);

	@Query("""
			    SELECT d
			    FROM DiscusssionForm d
			    WHERE d.isDeleted = false
			      AND (
			            LOWER(d.Content) LIKE LOWER(CONCAT('%', :keyword, '%'))
			         OR LOWER(d.audioFile) LIKE LOWER(CONCAT('%', :keyword, '%'))
			         OR LOWER(d.file) LIKE LOWER(CONCAT('%', :keyword, '%'))
			         OR LOWER(d.student.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
			      )
			""")
	List<DiscusssionForm> searchingByAllFields(@Param("keyword") String keyword);

	Boolean existsByIdAndStudent_StudentId(Integer id, Integer studentId);

	Optional<DiscusssionForm> findByIdAndStudent_StudentId(Integer id, Integer studentId);

	Page<DiscusssionForm> findByIsDeletedFalse(Pageable pageable);

	Optional<DiscusssionForm> findByIdAndIsDeletedFalse(Integer id);

}
