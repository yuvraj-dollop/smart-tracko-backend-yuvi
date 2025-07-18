package com.cico.service;

import com.cico.model.TokenManagement;

public interface ITokenManagementService {
	
	public Boolean existsByToken(String token);
	public void save(TokenManagement tokenManagement);
	public TokenManagement getTokenByToken(String token);
	public void deleteToken(String token);

}
