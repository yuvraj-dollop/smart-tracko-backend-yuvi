package com.cico.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.cico.payload.AdminRequest;
import com.cico.payload.AdminResponse;
import com.cico.payload.ApiResponse;
import com.cico.payload.ChangeStudentPasswordRequest;
import com.cico.payload.JwtResponse;
import com.cico.payload.UpdateAdminRequest;

public interface IAdminService {

	public JwtResponse adminLogin(String adminId, String password);

	public ApiResponse createAdmin(AdminRequest adminRequest);

	public AdminResponse getAdmin(String adminId);

	public AdminResponse updateAdmin(UpdateAdminRequest adminRequest);

	ApiResponse deleteAdmin(Integer adminId);

	List<AdminResponse> getAll();

	AdminResponse profileUpload(MultipartFile profileImage, Integer adminId);

	public ResponseEntity<?> changeStudentPasswword(ChangeStudentPasswordRequest request);
}
