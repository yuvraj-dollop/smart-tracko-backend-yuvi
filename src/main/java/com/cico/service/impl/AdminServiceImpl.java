package com.cico.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cico.exception.InvalidCredentialsException;
import com.cico.exception.ResourceAlreadyExistException;
import com.cico.exception.ResourceNotFoundException;
import com.cico.model.Admin;
import com.cico.model.Student;
import com.cico.payload.AdminRequest;
import com.cico.payload.AdminResponse;
import com.cico.payload.ApiResponse;
import com.cico.payload.ChangeStudentPasswordRequest;
import com.cico.payload.JwtResponse;
import com.cico.payload.UpdateAdminRequest;
import com.cico.repository.AdminRepository;
import com.cico.repository.StudentRepository;
import com.cico.security.JwtUtil;
import com.cico.service.IAdminService;
import com.cico.util.AppConstants;

@Service
public class AdminServiceImpl implements IAdminService {

	@Autowired
	private AdminRepository repo;

	@Autowired
	private BCryptPasswordEncoder encoder;

	@Autowired
	private FileServiceImpl fileService;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private StudentRepository studentRepository;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Override
	public JwtResponse adminLogin(String adminId, String password) {
		Optional<Admin> adminEmail = repo.findByAdminEmail(adminId);
		if (adminEmail.isPresent()) {
			if (encoder.matches(password, adminEmail.get().getPassword())) {
				String token = jwtUtil.generateTokenForAdmin(adminId);
				return new JwtResponse(token);
			} else {
				throw new InvalidCredentialsException(AppConstants.INVALID_CREDENTIALS);
			}
		} else {
			throw new InvalidCredentialsException(AppConstants.INVALID_CREDENTIALS);
		}
	}

	@Override
	public ApiResponse createAdmin(AdminRequest adminRequest) {
		Admin admin = new Admin(adminRequest.getAdminName(), adminRequest.getAdminEmail(),
				encoder.encode(adminRequest.getPassword()));

		admin.setUuid(UUID.randomUUID().toString());
		Optional<Admin> findByAdminEmail = repo.findByAdminEmail(admin.getAdminEmail());
		if (findByAdminEmail.isPresent()) {
			throw new ResourceAlreadyExistException(AppConstants.DATA_ALREADY_EXIST);
		} else {
			admin.setProfilePic(AppConstants.DEFAULT_ADMIN_IMAGE);
			admin = repo.save(admin);
		}
		return new ApiResponse(Boolean.TRUE, AppConstants.CREATE_SUCCESS, HttpStatus.CREATED);
	}

	@Override
	public AdminResponse getAdmin(String adminId) {
		Optional<Admin> findById = repo.findByAdminEmail(adminId);
		if (!findById.isPresent()) {
			throw new ResourceNotFoundException(AppConstants.NO_DATA_FOUND);
		}
		Admin admin = findById.get();
		AdminResponse adminResponse = modelMapper.map(admin, AdminResponse.class);
		return adminResponse;
	}

	@Override
	public AdminResponse updateAdmin(UpdateAdminRequest adminRequest) {
		Admin admin = new Admin();

		Optional<Admin> findById = repo.findById(adminRequest.getAdminId());
		admin = findById.get();
		if (!findById.isPresent()) {
			throw new ResourceNotFoundException(AppConstants.DATA_ALREADY_EXIST);
		}
		if (adminRequest.getAdminName() != null)
			admin.setAdminName(adminRequest.getAdminName());

		if (adminRequest.getAdminEmail() != null)
			admin.setAdminEmail(adminRequest.getAdminEmail());

		// Update profile picture if new file is present
		if (adminRequest.getFile() != null && !adminRequest.getFile().isEmpty()) {
			// delete old profile picture if needed
			if (admin.getProfilePic() != null && !admin.getProfilePic().isEmpty()) {
//	            fileService.deleteImagesInFolder(List.of(admin.getProfilePic()), AppConstants.PROFILE_PIC);
			}

			String image = fileService.uploadFileInFolder(adminRequest.getFile(), AppConstants.PROFILE_PIC);
			admin.setProfilePic(image);
		}

		repo.save(admin);
		return modelMapper.map(admin, AdminResponse.class);
	}

	@Override
	public ApiResponse deleteAdmin(Integer adminId) {
		Optional<Admin> admin = repo.findById(adminId);
		if (!admin.isPresent()) {
			throw new ResourceNotFoundException(AppConstants.NO_DATA_FOUND);
		}
		repo.deleteById(adminId);
		return new ApiResponse(Boolean.TRUE, AppConstants.DELETE_SUCCESS, HttpStatus.OK);
	}

	@Override
	public List<AdminResponse> getAll() {
		List<Admin> admins = repo.findAll();
		List<AdminResponse> adminsResponse = new ArrayList<>();
		for (Admin admin : admins) {
			adminsResponse.add(modelMapper.map(admin, AdminResponse.class));
		}

		return adminsResponse;
	}

	@Override
	public AdminResponse profileUpload(MultipartFile profileImage, Integer adminId) {
		Admin admin = repo.findById(adminId).get();

		if (profileImage != null && !profileImage.isEmpty()) {
			String image = fileService.uploadFileInFolder(profileImage, AppConstants.PROFILE_PIC);
			admin.setProfilePic(image);
			admin = repo.save(admin);
		}

		AdminResponse map = modelMapper.map(admin, AdminResponse.class);
		return map;
	}

	@Override
	public ResponseEntity<?> changeStudentPasswword(ChangeStudentPasswordRequest request) {
		Student student = studentRepository.findById(request.getStudentId())
				.orElseThrow(() -> new ResourceNotFoundException(AppConstants.STUDENT_NOT_FOUND));
		student.setPassword(passwordEncoder.encode(request.getNewPassword()));
		studentRepository.save(student);
		return ResponseEntity.ok(Map.of("message", AppConstants.PASSWORD_CHANGED_SUCCESSFULLY));
	}

}
