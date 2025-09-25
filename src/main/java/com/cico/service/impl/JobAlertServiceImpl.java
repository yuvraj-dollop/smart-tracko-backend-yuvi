package com.cico.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.cico.exception.ResourceNotFoundException;
import com.cico.kafkaServices.KafkaProducerService;
import com.cico.model.JobAlert;
import com.cico.model.TechnologyStack;
import com.cico.payload.CreateJobRequest;
import com.cico.payload.JobAlertResponse;
import com.cico.payload.NotificationInfo;
import com.cico.payload.PageResponse;
import com.cico.payload.UpdateJobRequest;
import com.cico.repository.JobAlertRepository;
import com.cico.repository.StudentRepository;
import com.cico.service.IJobAlertService;
import com.cico.service.ITechnologyStackService;
import com.cico.util.JobType;
import com.cico.util.NotificationConstant;

@Service
public class JobAlertServiceImpl implements IJobAlertService {

	@Autowired
	private JobAlertRepository repository;

	@Autowired
	private ITechnologyStackService technologyStackService;

	@Autowired
	private ModelMapper mapper;

	@Autowired
	private KafkaProducerService kafkaProducerService;

	@Autowired
	private StudentRepository studentRepository;

	@Override
	public JobAlert createJob(Integer technologyStackId, String jobTitle, String jobDescription, String companyName,
			String experienceRequired, String technicalSkills, JobType type, String jobPackage) {
		JobAlert alert = new JobAlert(jobTitle, jobDescription, companyName, experienceRequired, technicalSkills, true,
				type, jobPackage);
		alert.setIsDeleted(false);
		alert.setCreatedDate(LocalDateTime.now());
		alert.setUpdatedDate(LocalDateTime.now());
		alert.setTechnologyStack(technologyStackService.getTechnologyStack(technologyStackId));

		// .....firebase notification .....//

		List<NotificationInfo> fcmIds = studentRepository.fetchAllStudentIsCompletedFalse();

		String message = String.format(
				type.equals(JobType.JOB) ? "An exciting job opportunity has just been posted. Explore the details!."
						: "An exciting internship opportunity has just been posted. Explore the details!");

		List<NotificationInfo> newlist = fcmIds.stream().parallel().map(obj1 -> {
			obj1.setTitle(type.equals(JobType.JOB) ? "New Job Alert!" : " New Internship Alert!");
			obj1.setMessage(message);
			return obj1;
		}).toList();

		kafkaProducerService.sendNotification(NotificationConstant.ASSIGNMENT_TOPIC, newlist.toString());
		// .....firebase notification .....//

		return repository.save(alert);
	}

//	@Override
//	public JobAlert getJob(Integer jobId) {
//		// TODO Auto-generated method stub
//		Optional<JobAlert> findById = repository.findById(jobId);
//		if (!findById.isPresent()) {
//			throw new ResourceNotFoundException("Job is not found from given Id");
//		}
//
//		return findById.get();
//	}

//	@Override
//	public JobAlert update(Integer jobId, String jobTitle, String jobDescription, String companyName,
//			String experienceRequired, String technicalSkills, String jobPackage, String type,
//			Integer technologyStackId) {
//		JobAlert alert = new JobAlert();
//		Optional<JobAlert> findById = repository.findById(jobId);
//		alert = findById.get();
//
//		if (jobTitle != null)
//			alert.setJobTitle(jobTitle);
//
//		if (jobDescription != null)
//			alert.setJobDescription(jobDescription);
//
//		if (companyName != null)
//			alert.setCompanyName(companyName);
//
//		if (experienceRequired != null)
//			alert.setExperienceRequired(experienceRequired);
//
//		if (technicalSkills != null)
//			alert.setTechnicalSkills(technicalSkills);
//
//		if (technologyStackId != null) {
//			TechnologyStack technologyStack = technologyStackService.getTechnologyStack(technologyStackId);
//			alert.setTechnologyStack(technologyStack);
//		} else {
//			alert.setTechnologyStack(alert.getTechnologyStack());
//		}
//		alert.setUpdatedDate(LocalDateTime.now());
//		return repository.save(alert);
//	}

//	@Override
//	public void delete(Integer jobId) {
//
//		Optional<JobAlert> findById = repository.findById(jobId);
//		if (!findById.isPresent()) {
//			throw new ResourceNotFoundException("Job Not Found From Given Id");
//		}
//		findById.get().setIsDeleted(true);
//		repository.save(findById.get());
//
//	}

//	@Override
//	public List<JobAlert> searchJob(String field, String role) {
//		return role.equalsIgnoreCase("student") == true ? repository.jobSearchStudent(field)
//				: repository.jobSearchAdmin(field);
//	}

//	@Override
//	public JobAlert activeJob(Integer jobId) {
//		JobAlert jobAlert = repository.findById(jobId)
//				.orElseThrow(() -> new ResourceNotFoundException("Job not found !!"));
//
//		jobAlert.setIsActive(!jobAlert.getIsActive());
//
//		return repository.save(jobAlert);
//	}
//
//	@Override
//	public PageResponse<JobAlertResponse> getAllJobsAndIntership(Integer page, Integer size) {
//		Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "jobId");
//		Page<JobAlert> jobAlert = repository.findAllByIsDeleted(false, pageable);
//		if (jobAlert.getNumberOfElements() == 0) {
//			return new PageResponse<>(Collections.emptyList(), jobAlert.getNumber(), jobAlert.getSize(),
//					jobAlert.getTotalElements(), jobAlert.getTotalPages(), jobAlert.isLast());
//		}
//		List<JobAlertResponse> alertResponses = Arrays
//				.asList(mapper.map(jobAlert.getContent(), JobAlertResponse[].class));
//
//		return new PageResponse<>(alertResponses, jobAlert.getNumber(), jobAlert.getSize(), jobAlert.getTotalElements(),
//				jobAlert.getTotalPages(), jobAlert.isLast());
//	}

//	@Override
//	public PageResponse<JobAlertResponse> getAllJobAlert(int page, int size, JobType type) {
//
//		Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "type");
//		Page<JobAlert> jobAlert = repository.findAllByTypeAndIsDeletedAndIsActive(type, false, true, pageable);
//		if (jobAlert.getNumberOfElements() == 0) {
//			return new PageResponse<>(Collections.emptyList(), jobAlert.getNumber(), jobAlert.getSize(),
//					jobAlert.getTotalElements(), jobAlert.getTotalPages(), jobAlert.isLast());
//		}
//		List<JobAlertResponse> alertResponses = Arrays
//				.asList(mapper.map(jobAlert.getContent(), JobAlertResponse[].class));
//
//		return new PageResponse<>(alertResponses, jobAlert.getNumber(), jobAlert.getSize(), jobAlert.getTotalElements(),
//				jobAlert.getTotalPages(), jobAlert.isLast());
//	}

//	@Override
//	public ApiResponse update(JobAlert jobAlert) {
//	
//		JobAlert save = repository.save(jobAlert);
//		if (Objects.nonNull(save))
//			return new ApiResponse(Boolean.TRUE, AppConstants.CREATE_SUCCESS, HttpStatus.CREATED);
//		return new ApiResponse(Boolean.FALSE, AppConstants.FAILED, HttpStatus.OK);
//
//	}
//	

	// ===================================new updated
	// methods=====================================
	@Override
	public JobAlertResponse createJob(CreateJobRequest request) {
		Integer technologyStackId = request.getTechnologyStackId();
		TechnologyStack stack = technologyStackService.getTechnologyStack(technologyStackId);

		JobAlert job = JobAlert.builder().jobTitle(request.getJobTitle()).jobDescription(request.getJobDescription())
				.companyName(request.getCompanyName()).experienceRequired(request.getExperienceRequired())
				.technicalSkills(request.getTechnicalSkills()).jobPackage(request.getJobPackage())
				.type(request.getType()).isActive(true).isDeleted(false).createdDate(LocalDateTime.now())
				.updatedDate(LocalDateTime.now()).technologyStack(stack).build();

		JobAlert saved = repository.save(job);

		// Firebase notification logic
		List<NotificationInfo> fcmIds = studentRepository.fetchAllStudentIsCompletedFalse();
		String message = request.getType().equals(JobType.JOB)
				? "An exciting job opportunity has just been posted. Explore the details!."
				: "An exciting internship opportunity has just been posted. Explore the details!";

		List<NotificationInfo> payload = fcmIds.stream().parallel().map(info -> {
			info.setTitle(request.getType().equals(JobType.JOB) ? "New Job Alert!" : "New Internship Alert!");
			info.setMessage(message);
			return info;
		}).toList();

		kafkaProducerService.sendNotification(NotificationConstant.ASSIGNMENT_TOPIC, payload.toString());

		return mapToJobAlertResponse(saved);
	}

	@Override
	public JobAlertResponse getJob(Integer jobId) {
		JobAlert jobAlert = repository.findById(jobId)
				.orElseThrow(() -> new ResourceNotFoundException("Job is not found from given Id"));

		return mapToJobAlertResponse(jobAlert);
	}

	@Override
	public JobAlertResponse activeJob(Integer jobId) {
		JobAlert jobAlert = repository.findById(jobId)
				.orElseThrow(() -> new ResourceNotFoundException("Job not found !!"));

		jobAlert.setIsActive(!jobAlert.getIsActive());

		JobAlert updated = repository.save(jobAlert);

		return mapToJobAlertResponse(updated); // Convert to DTO
	}

	@Override
	public JobAlertResponse update(UpdateJobRequest request) {
		JobAlert alert = repository.findById(request.getJobId())
				.orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + request.getJobId()));

		alert.setJobTitle(request.getJobTitle() != null ? request.getJobTitle() : alert.getJobTitle());

		alert.setJobDescription(
				request.getJobDescription() != null ? request.getJobDescription() : alert.getJobDescription());

		alert.setCompanyName(request.getCompanyName() != null ? request.getCompanyName() : alert.getCompanyName());

		alert.setExperienceRequired(request.getExperienceRequired() != null ? request.getExperienceRequired()
				: alert.getExperienceRequired());

		alert.setTechnicalSkills(
				request.getTechnicalSkills() != null ? request.getTechnicalSkills() : alert.getTechnicalSkills());

		alert.setJobPackage(request.getJobPackage() != null ? request.getJobPackage() : alert.getJobPackage());

		alert.setType(request.getType() != null ? request.getType() : alert.getType());

		alert.setTechnologyStack(request.getTechnologyStackId() != null
				? technologyStackService.getTechnologyStack(request.getTechnologyStackId())
				: alert.getTechnologyStack());

		alert.setUpdatedDate(LocalDateTime.now());

		JobAlert updated = repository.save(alert);

		return mapToJobAlertResponse(updated);
	}

	@Override
	public PageResponse<JobAlertResponse> getAllJobAlert(int page, int size, JobType type) {
		Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "type");

		Page<JobAlert> jobAlertPage = repository.findAllByTypeAndIsDeletedAndIsActive(type, false, true, pageable);

		List<JobAlertResponse> jobAlertResponses = jobAlertPage.getContent().stream().map(this::mapToJobAlertResponse)
				.toList();

		return new PageResponse<>(jobAlertResponses, jobAlertPage.getNumber(), jobAlertPage.getSize(),
				jobAlertPage.getTotalElements(), jobAlertPage.getTotalPages(), jobAlertPage.isLast());
	}

	@Override
	public PageResponse<JobAlertResponse> getAllJobsAndIntership(Integer page, Integer size) {
		Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "jobId");
		Page<JobAlert> jobAlertPage = repository.findAllByIsDeleted(false, pageable);

		List<JobAlertResponse> responseList = jobAlertPage.getContent().stream().map(this::mapToJobAlertResponse)
				.toList();

		return new PageResponse<>(responseList, jobAlertPage.getNumber(), jobAlertPage.getSize(),
				jobAlertPage.getTotalElements(), jobAlertPage.getTotalPages(), jobAlertPage.isLast());
	}

	@Override
	public void delete(Integer jobId) {
		JobAlert job = repository.findById(jobId)
				.orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

		job.setIsDeleted(true);
		job.setUpdatedDate(LocalDateTime.now()); // Optional: update timestamp for audit
		repository.save(job);
	}

	@Override
	public List<JobAlertResponse> searchJob(String field, String role) {
		List<JobAlert> jobs = role.equalsIgnoreCase("student") ? repository.jobSearchStudent(field)
				: repository.jobSearchAdmin(field);

		return jobs.stream().map(this::mapToJobAlertResponse).toList();
	}

	// ===================================utility
	// method=====================================//

	private JobAlertResponse mapToJobAlertResponse(JobAlert jobAlert) {
		return JobAlertResponse.builder().jobId(jobAlert.getJobId()).jobTitle(jobAlert.getJobTitle())
				.technologyStackId(jobAlert.getTechnologyStack().getId()).jobDescription(jobAlert.getJobDescription())
				.companyName(jobAlert.getCompanyName()).experienceRequired(jobAlert.getExperienceRequired())
				.technicalSkills(jobAlert.getTechnicalSkills()).jobPackage(jobAlert.getJobPackage())
				.isActive(jobAlert.getIsActive())
				.technologyStackName(
						jobAlert.getTechnologyStack() != null ? jobAlert.getTechnologyStack().getTechnologyName()
								: null)
				.type(jobAlert.getType()).createdDate(jobAlert.getCreatedDate()).updatedDate(jobAlert.getUpdatedDate())
				.build();
	}
}