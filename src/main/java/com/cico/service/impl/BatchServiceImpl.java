package com.cico.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.cico.exception.ResourceAlreadyExistException;
import com.cico.exception.ResourceNotFoundException;
import com.cico.model.Batch;
import com.cico.model.Course;
import com.cico.model.Student;
import com.cico.payload.ApiResponse;
import com.cico.payload.BatchRequest;
import com.cico.payload.BatchResponse;
import com.cico.payload.SubjectResponse;
import com.cico.payload.TechnologyStackResponse;
import com.cico.repository.BatchRepository;
import com.cico.repository.CourseRepository;
import com.cico.repository.StudentRepository;
import com.cico.repository.SubjectRepository;
import com.cico.service.IBatchService;
import com.cico.service.ISubjectService;
import com.cico.util.AppConstants;

@Service
public class BatchServiceImpl implements IBatchService {

	public static final String BATCH_NOT_FOUND = "BATCH NOT FOUND";
	public static final String BATCH_NOT_AVAILABLE = "No Upcoming Batch Available";
	public static final String BATCH_ADD_SUCCESS = "Batch Created Successfully";
	public static final String BATCH_UPDATE_SUCCESS = "Batch Update Successfully";
	public static final String COURSE_NOT_FOUND = "Course Not Found";
	@Autowired
	private BatchRepository batchRepository;
	@Autowired
	private ISubjectService subjectService;
	@Autowired
	private CourseRepository courseRepository;

	@Autowired
	private SubjectRepository subjectRepository;
	@Autowired
	private StudentRepository studentRepository;

	@Override
	public ApiResponse createBatch(BatchRequest request) {

		Batch isPresent = batchRepository.findByBatchNameAndIsDeletedFalse(request.getBatchName());
		if (isPresent != null) {
			throw new ResourceAlreadyExistException("Batch already exist");
		}

		Course course = courseRepository.findById(request.getCourseId())
				.orElseThrow(() -> new ResourceNotFoundException(AppConstants.NO_DATA_FOUND));
		Batch batch = new Batch(request.getBatchName(), request.getBatchStartDate(), request.getBatchTiming(),
				request.getBatchDetails());
		batch.setSubject(subjectRepository.findBySubjectIdAndIsDeleted(request.getSubjectId()).get());

		List<Batch> batches = course.getBatches();
		batches.add(batch);
		course.setBatches(batches);
		System.err.println(request);
		Course course2 = courseRepository.save(course);
		if (Objects.nonNull(course2))
			return new ApiResponse(Boolean.TRUE, BATCH_ADD_SUCCESS, HttpStatus.CREATED);
		return new ApiResponse(Boolean.FALSE, AppConstants.FAILED, HttpStatus.OK);

	}

	@Override
	public ApiResponse deleteBatch(Integer batchId) {
		Batch batch = batchRepository.findById(batchId)
				.orElseThrow(() -> new ResourceNotFoundException(BATCH_NOT_FOUND));
		batch.setDeleted(true);
		batchRepository.save(batch);
		return new ApiResponse(Boolean.TRUE, AppConstants.DELETE_SUCCESS, HttpStatus.OK);
	}

	@Override
	public BatchResponse getBatchById(Integer batchId) {
		Batch obj = batchRepository.findByBatchIdAndIsDeleted(batchId, false);
		System.out.println(obj.getSubject());

		if (Objects.isNull(obj)) {
			throw new ResourceNotFoundException(BATCH_NOT_FOUND);
		}
		BatchResponse batchResponse = new BatchResponse();

		batchResponse.setBatchId(obj.getBatchId());
		batchResponse.setBatchName(obj.getBatchName());
		batchResponse.setBatchStartDate(obj.getBatchStartDate());
		batchResponse.setBatchTiming(obj.getBatchTiming());
		batchResponse.setBatchDetails(obj.getBatchDetails());

		SubjectResponse response = new SubjectResponse();

		response.setSubjectId(obj.getSubject().getSubjectId());
		response.setSubjectName(obj.getSubject().getSubjectName());

		TechnologyStackResponse stackResponse = new TechnologyStackResponse();
		stackResponse.setId(obj.getSubject().getTechnologyStack().getId());
		stackResponse.setImageName(obj.getSubject().getTechnologyStack().getImageName());
		stackResponse.setTechnologyName(obj.getSubject().getTechnologyStack().getTechnologyName());

		response.setTechnologyStack(stackResponse);

		batchResponse.setSubject(response);

		return batchResponse;
	}

	@Override
	public List<BatchResponse> getAllBatches(Integer studentId) {

		Student student = studentRepository.findByStudentId(studentId);
		Course currentCourse = student.getCourse();

		List<Batch> batches = currentCourse.getBatches().stream().filter(batch -> !batch.isDeleted())
				.collect(Collectors.toList());

		if (batches.isEmpty()) {
			throw new ResourceNotFoundException(BATCH_NOT_FOUND);
		}

		List<BatchResponse> batchResponses = new ArrayList<>();

		batches.forEach(obj -> {

			if (!obj.isDeleted()) {
				BatchResponse batchResponse = new BatchResponse();

				batchResponse.setBatchId(obj.getBatchId());
				batchResponse.setBatchName(obj.getBatchName());
				batchResponse.setBatchStartDate(obj.getBatchStartDate());
				batchResponse.setBatchTiming(obj.getBatchTiming());
				batchResponse.setBatchDetails(obj.getBatchDetails());

				SubjectResponse response = new SubjectResponse();

				response.setSubjectId(obj.getSubject().getSubjectId());
				response.setSubjectName(obj.getSubject().getSubjectName());

				TechnologyStackResponse stackResponse = new TechnologyStackResponse();
				stackResponse.setId(obj.getSubject().getTechnologyStack().getId());
				stackResponse.setImageName(obj.getSubject().getTechnologyStack().getImageName());
				stackResponse.setTechnologyName(obj.getSubject().getTechnologyStack().getTechnologyName());

				response.setTechnologyStack(stackResponse);
				batchResponse.setSubject(response);

				batchResponses.add(batchResponse);
			}

		});

		return batchResponses;
	}

	@Override
	public List<Batch> getUpcomingBatches() {
		List<Batch> batches = batchRepository.findAllByBatchStartDate(LocalDate.now());
		return batches;
	}

	@Override
	public ApiResponse updateBatchStatus(Integer batchId) {

		Batch batch = batchRepository.findByBatchIdAndIsDeleted(batchId, false);
		if (Objects.isNull(batch)) {
			throw new ResourceNotFoundException(BATCH_NOT_FOUND);
		}
		if (batch.isActive() == true)
			batch.setActive(false);

		else
			batch.setActive(true);

		batchRepository.save(batch);
		return new ApiResponse(Boolean.TRUE, AppConstants.SUCCESS, HttpStatus.OK);

	}

	@Override
	public ApiResponse updateBatch(Batch batch) {

		Optional<Batch> batches = batchRepository.findById(batch.getBatchId());
		if (!batches.isPresent()) {
			throw new ResourceNotFoundException("Batch not found.");
		}

		Batch isPresent = batchRepository.findByBatchNameAndIsDeletedFalse(batch.getBatchName());

		if (isPresent != null && isPresent.getBatchId() != batches.get().getBatchId()) {
			throw new ResourceAlreadyExistException("Batch already exist");
		}

		Batch save = batchRepository.save(batch);

		if (Objects.nonNull(save))
			return new ApiResponse(Boolean.TRUE, BATCH_UPDATE_SUCCESS, HttpStatus.CREATED);

		return new ApiResponse(Boolean.FALSE, AppConstants.FAILED, HttpStatus.OK);
	}

	@Override
	public Batch getFirstUpcomingBatchOfCurrentCourse(String courseName) {

		Course course = courseRepository.findByCourseNameAndIsDeletedFalse(courseName.trim());
		if (course == null)
			throw new ResourceNotFoundException("Course not found");

		return batchRepository.findByCourseId(course.getCourseId()).orElse(null);
	}
	 // Convert List<Batch> → List<BatchResponse>
	@Override
    public List<BatchResponse> batchToBatchResponse(List<Batch> batches) {
        return batches.stream()
                .map(this::batchToBatchResponse) // reuse single converter
                .collect(Collectors.toList());
    }

    // Convert Batch → BatchResponse
    @Override
    public BatchResponse batchToBatchResponse(Batch batch) {
        if (batch == null) {
            return null;
        }

        return BatchResponse.builder()
                .batchId(batch.getBatchId())
                .batchName(batch.getBatchName())
                .batchStartDate(batch.getBatchStartDate())
                .batchTiming(batch.getBatchTiming())
                .batchDetails(batch.getBatchDetails())
                .isDeleted(batch.isDeleted())
                .isActive(batch.isActive())
                .subject(batch.getSubject() != null ? subjectService.toResponse(batch.getSubject()):null)
                .build();
    }

	// =====================================New Methods
	// ===================================
	@Override
	public List<BatchResponse> getUpcomingBatchesNew() {
		List<Batch> batches = batchRepository.findAllByBatchStartDate(LocalDate.now());
		
		
		return batchToBatchResponse(batches);
	}

}
