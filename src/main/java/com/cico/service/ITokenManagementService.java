package com.cico.service;

import com.cico.model.TokenManagement;

public interface ITokenManagementService {

	public Boolean existsByToken(String token);

	public void save(TokenManagement tokenManagement);

//	public TokenManagement getTokenById(String id);

	public void deleteToken(TokenManagement tokenManagement);

	TokenManagement getTokenByToken(String token);

}
