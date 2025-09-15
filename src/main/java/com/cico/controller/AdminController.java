package com.cico.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cico.payload.AdminRequest;
import com.cico.payload.AdminResponse;
import com.cico.payload.ApiResponse;
import com.cico.payload.JwtResponse;
import com.cico.payload.UpdateAdminRequest;
import com.cico.service.IAdminService;
import com.cico.util.AppConstants;

@RestController
@RequestMapping("/admin")
@CrossOrigin("*")
public class AdminController {

	@Autowired
	private IAdminService service;

	@PostMapping("/adminLoginApi")
	public ResponseEntity<JwtResponse> adminLogin(@RequestParam(name = AppConstants.ADMIN_ID) String adminId,
			@RequestParam("password") String password) {
		return ResponseEntity.ok(service.adminLogin(adminId, password));
	}

	@PostMapping("/createAdmin")
	public ResponseEntity<ApiResponse> createAdmin(@Valid @RequestBody AdminRequest adminRequest) {
		ApiResponse response = service.createAdmin(adminRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/getAdmin")
	public ResponseEntity<AdminResponse> getAdmin(@RequestParam(name = AppConstants.ADMIN_ID) String adminId) {
		AdminResponse admin = service.getAdmin(adminId);
		return ResponseEntity.ok(admin);
	}

	@PutMapping("/update")
	public ResponseEntity<AdminResponse> updateAdmin(@Valid @RequestBody UpdateAdminRequest adminRequest) {
		AdminResponse admin = service.updateAdmin(adminRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body(admin);
	}

	@DeleteMapping("/{adminId}")
	public ResponseEntity<ApiResponse> deleteAdmin(@RequestParam(name = AppConstants.ADMIN_ID) Integer adminId) {
		ApiResponse response = service.deleteAdmin(adminId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/getAll")
	public ResponseEntity<List<AdminResponse>> getAll() {
		List<AdminResponse> admins = service.getAll();
		return ResponseEntity.ok(admins);
	}

	@PostMapping("/uploadImage")
	public ResponseEntity<AdminResponse> uploadImage(@RequestParam(name = AppConstants.ADMIN_ID) Integer adminId,
			@RequestParam("file") MultipartFile file) {
		AdminResponse admin = service.profileUpload(file, adminId);
		return ResponseEntity.ok(admin);
	}

	@GetMapping("/v2/getAdmin")
	public ResponseEntity<AdminResponse> getAdminNew(@RequestParam(name = AppConstants.ADMIN_ID) String adminId) {
		AdminResponse admin = service.getAdmin(adminId);
		return ResponseEntity.ok(admin);
	}

}
