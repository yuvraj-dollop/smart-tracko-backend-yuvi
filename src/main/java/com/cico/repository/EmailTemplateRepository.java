package com.cico.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cico.model.EmailTemplate;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Integer> {
	Optional<EmailTemplate> findByTemplateType(String templateType);
}
