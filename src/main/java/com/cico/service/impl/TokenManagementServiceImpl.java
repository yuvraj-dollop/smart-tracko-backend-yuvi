package com.cico.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cico.exception.ResourceNotFoundException;
import com.cico.model.TokenManagement;
import com.cico.repository.TokenManagementRepository;
import com.cico.service.ITokenManagementService;

@Service
public class TokenManagementServiceImpl implements ITokenManagementService {

	@Autowired
	private TokenManagementRepository tokenManagementRepository;

	@Override
	public Boolean existsByToken(String token) {
		return tokenManagementRepository.existsByToken(token);
	}

	@Override
	public void save(TokenManagement tokenManagement) {
		tokenManagementRepository.save(tokenManagement);
	}

	@Override
	public TokenManagement getTokenByToken(String token){
		return tokenManagementRepository.findByToken(token)
				.orElseThrow(() -> new ResourceNotFoundException("Token Not Found"));
	}

	@Override
	public void deleteToken(String token) {
		tokenManagementRepository.deleteByToken(token);;
	}

}
