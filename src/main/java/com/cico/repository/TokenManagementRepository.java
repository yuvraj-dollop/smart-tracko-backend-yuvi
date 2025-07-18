package com.cico.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cico.model.TokenManagement;

@Repository
public interface TokenManagementRepository extends JpaRepository<TokenManagement, Integer> {
	
	Boolean existsByToken(String token);
	Optional<TokenManagement> findByToken(String token);
	void deleteByToken(String token);

}
