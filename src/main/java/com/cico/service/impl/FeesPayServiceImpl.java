package com.cico.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cico.exception.ResourceNotFoundException;
import com.cico.model.Fees;
import com.cico.model.FeesPay;
import com.cico.payload.FeesPayRequest;
import com.cico.payload.FeesPayResponse;
import com.cico.payload.FeesResponse;
import com.cico.payload.PageResponse;
import com.cico.payload.UpdateFeesPayRequest;
import com.cico.repository.FeesPayRepository;
import com.cico.repository.FeesRepository;
import com.cico.service.IFeesPayService;
import com.cico.util.AppConstants;

@Service
public class FeesPayServiceImpl implements IFeesPayService {

	@Autowired
	private FeesPayRepository feesPayRepository;
	@Autowired
	private FeesRepository feesRepository;

	private final static DateTimeFormatter FORMATER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Override
	public FeesPay feesPayService(Integer feesId, Double feesPayAmount, String payDate, String recieptNo,
			String description) {

		FeesPay feesPay = new FeesPay(feesPayAmount, LocalDate.parse(payDate), recieptNo, description);
		Fees findByFeesId = feesRepository.findByFeesId(feesId);

		if (findByFeesId.getRemainingFees() != 0) {
			feesPay.setFees(feesRepository.findById(feesId).get());
			feesPay.setFeesPayAmount(feesPayAmount);
			Fees fees = feesPay.getFees();
			if (feesPay.getFeesPayAmount() <= findByFeesId.getRemainingFees()) {
				fees.setRemainingFees(fees.getRemainingFees() - feesPay.getFeesPayAmount());
				fees.setFeesPaid(fees.getFinalFees() - fees.getRemainingFees());

				if (findByFeesId.getRemainingFees() == 0) {
					feesRepository.updateIsCompleted(feesId);
				}
				feesRepository.save(fees);

				return feesPayRepository.save(feesPay);
			} else
				throw new ResourceNotFoundException(AppConstants.FEES_AMOUNT_EXCEEDS_REMAINING);
		}
		throw new ResourceNotFoundException(AppConstants.FEES_NOT_FOUND);

	}

	@Override
	public PageResponse<FeesResponse> feesPendingList(Integer page, Integer size) {
		// TODO Auto-generated method stub
		Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "feesId");
		Page<Fees> fees = feesRepository.findByRemainingFees(pageable);

		if (fees.getNumberOfElements() == 0) {
			return new PageResponse<>(Collections.emptyList(), fees.getNumber(), fees.getSize(),
					fees.getTotalElements(), fees.getTotalPages(), fees.isLast());
		}
		// List<FeesResponse> asList = Arrays.asList(mapper.map(fees.getContent(),
		// FeesResponse[].class));

		return new PageResponse<>(fees.stream().map(obj -> setFeesResponse(obj)).collect(Collectors.toList()),
				fees.getNumber(), fees.getSize(), fees.getTotalElements(), fees.getTotalPages(), fees.isLast());
	}

	@Override
	public ResponseEntity<?> getAllTransectionByStudentId(Integer studentId) {

		Fees fees = feesRepository.findFeesByStudentId(studentId);
		List<FeesPay> findByFees = feesPayRepository.findByFees(fees);

		return new ResponseEntity<>(
				findByFees.stream().map(obj -> setFeesPayResponse(obj)).collect(Collectors.toList()), HttpStatus.OK);
	}

	@Override
	public PageResponse<FeesPayResponse> feesPayList(Integer page, Integer size) {
		Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "payId");
		Page<FeesPay> fees = feesPayRepository.findByFeesPayAmount(pageable);

		if (fees.getNumberOfElements() == 0) {
			return new PageResponse<>(Collections.emptyList(), fees.getNumber(), fees.getSize(),
					fees.getTotalElements(), fees.getTotalPages(), fees.isLast());
		}

		return new PageResponse<>(fees.stream().map(obj -> setFeesPayResponse(obj)).collect(Collectors.toList()),
				fees.getNumber(), fees.getSize(), fees.getTotalElements(), fees.getTotalPages(), fees.isLast());
	}

	@Override
	public FeesPayResponse findByPayId(Integer payId) {
		FeesPay feesPay = feesPayRepository.findById(payId)
				.orElseThrow(() -> new ResourceNotFoundException(AppConstants.PAY_FEES_NOT_FOUND));
		return setFeesPayResponse(feesPay);
	}

	@Override
	public FeesPay updateFeesPay(FeesPay feesPay) {
		FeesPay feesPayData = feesPayRepository.findByPayId(feesPay.getPayId());
		System.out.println(feesPayData);
		if (Objects.nonNull(feesPayData)) {

			if (feesPay.getPayDate() != null) {
				feesPayData.setPayDate(feesPay.getPayDate());
			} else {
				feesPay.setPayDate(feesPay.getPayDate());
			}
			Fees fees = feesPayData.getFees();
			fees.setFeesPaid(fees.getFeesPaid() - feesPayData.getFeesPayAmount());
			fees.setFeesPaid(fees.getFeesPaid() + feesPay.getFeesPayAmount());
			feesPayData.setFeesPayAmount(feesPay.getFeesPayAmount());
			fees.setRemainingFees(fees.getFinalFees() - fees.getFeesPaid());
			if (fees.getFeesPaid() <= fees.getFinalFees()) {
				if (fees.getRemainingFees() == 0) {
					feesRepository.updateIsCompleted(fees.getFeesId());
				} else {
					feesRepository.updateNotIsCompleted(fees.getFeesId());
				}

				feesPayData.setFees(fees);
				feesRepository.save(fees);
				return feesPayRepository.save(feesPayData);
			}
		} else {
			throw new ResourceNotFoundException(AppConstants.FEES_AMOUNT_EXCEEDS_REMAINING);
		}
		throw new ResourceNotFoundException(AppConstants.PAY_FEES_NOT_FOUND);
	}

	@Override
	public List<FeesPayResponse> searchByNameInFeesPayList(String fullName) {
		List<FeesPay> findByFullName = feesPayRepository.findByFullName(fullName);
		return findByFullName.stream().map(this::setFeesPayResponse).collect(Collectors.toList());
	}

	@Override
	public List<FeesPayResponse> searchByMonthInFeesPayList(String startDate, String endDate) {
		List<FeesPay> findByMonth = feesPayRepository.findByMonth(LocalDate.parse(startDate), LocalDate.parse(endDate));
		return findByMonth.stream().map(this::setFeesPayResponse).collect(Collectors.toList());
	}

	public FeesResponse setFeesResponse(Fees fees) {
		return FeesResponse.builder().college(fees.getStudent().getCollege())
				.courseFees(fees.getCourse().getCourseFees()).courseId(fees.getCourse().getCourseId())
				.courseName(fees.getCourse().getCourseName()).createdDate(fees.getCreatedDate())
				.currentCourse(fees.getStudent().getCurrentCourse()).date(fees.getDate())
				.dob(fees.getStudent().getDob()).email(fees.getStudent().getEmail()).feesId(fees.getFeesId())
				.feesPaid(fees.getFeesPaid()).finalFees(fees.getFinalFees()).isCompleted(fees.getIsCompleted())
				.mobile(fees.getStudent().getMobile()).profilePic(fees.getStudent().getProfilePic())
				.remainingFees(fees.getRemainingFees()).studentId(fees.getStudent().getStudentId())
				.updatedDate(fees.getUpdatedDate()).fullName(fees.getStudent().getFullName()).build();
	}

	public FeesPayResponse setFeesPayResponse(FeesPay feesPay) {
		return FeesPayResponse.builder().payId(feesPay.getPayId()).createDate(feesPay.getCreateDate())
				.description(feesPay.getDescription()).feesPayAmount(feesPay.getFeesPayAmount())
				.payDate(feesPay.getPayDate()).recieptNo(feesPay.getRecieptNo()).updatedDate(feesPay.getUpdatedDate())
				.feesPay(setFeesResponse(feesPay.getFees())).build();
	}

	// ..................... NEW MWTHOD's ........................

	@Override
	public FeesPayResponse feesPayService(FeesPayRequest payRequest) {

		FeesPay feesPay = new FeesPay(payRequest.getFeesPayAmount(), LocalDate.parse(payRequest.getPayDate()),
				payRequest.getRecieptNo(), payRequest.getDescription());
		Fees findByFeesId = feesRepository.findByFeesId(payRequest.getFeesId());

		if (findByFeesId.getRemainingFees() != 0) {
			feesPay.setFees(feesRepository.findById(payRequest.getFeesId()).get());
			feesPay.setFeesPayAmount(payRequest.getFeesPayAmount());
			Fees fees = feesPay.getFees();
			if (feesPay.getFeesPayAmount() <= findByFeesId.getRemainingFees()) {
				fees.setRemainingFees(fees.getRemainingFees() - feesPay.getFeesPayAmount());
				fees.setFeesPaid(fees.getFinalFees() - fees.getRemainingFees());

				if (findByFeesId.getRemainingFees() == 0) {
					feesRepository.updateIsCompleted(payRequest.getFeesId());
				}
				feesRepository.save(fees);
				FeesPay payFees = feesPayRepository.save(feesPay);
				return setFeesPayResponse(payFees);
			} else
				throw new ResourceNotFoundException(AppConstants.FEES_AMOUNT_EXCEEDS_REMAINING);
		}
		throw new ResourceNotFoundException(AppConstants.FEES_NOT_FOUND);

	}

	@Override
	public FeesPayResponse updateFeesPay(UpdateFeesPayRequest request) {
		FeesPay feesPayData = feesPayRepository.findByPayId(request.getPayId());

		if (feesPayData == null) {
			throw new ResourceNotFoundException(AppConstants.PAY_FEES_NOT_FOUND);
		}

		Double oldAmount = feesPayData.getFeesPayAmount();
		Double newAmount = request.getFeesPayAmount();

		if (request.getPayDate() != null) {
			LocalDate parsedDate = LocalDate.parse(request.getPayDate(), FORMATER);
			feesPayData.setPayDate(parsedDate);
		}

		// Recalculate fees
		Fees fees = feesPayData.getFees();
		fees.setFeesPaid(fees.getFeesPaid() - oldAmount + newAmount);
		fees.setRemainingFees(fees.getFinalFees() - fees.getFeesPaid());

		if (fees.getFeesPaid() > fees.getFinalFees()) {
			throw new ResourceNotFoundException(AppConstants.FEES_AMOUNT_EXCEEDS_REMAINING);
		}

		// Update completion status
		if (fees.getRemainingFees() == 0) {
			feesRepository.updateIsCompleted(fees.getFeesId());
		} else {
			feesRepository.updateNotIsCompleted(fees.getFeesId());
		}

		// Save updates
		feesRepository.save(fees);
		feesPayData.setFeesPayAmount(newAmount);
		return setFeesPayResponse(feesPayRepository.save(feesPayData));
	}

	@Override
	public ResponseEntity<?> getAllTransectionByStudentIdNew(Integer studentId) {

		Fees fees = feesRepository.findFeesByStudentId(studentId);
		List<FeesPay> findByFees = feesPayRepository.findByFees(fees);

		return new ResponseEntity<>(findByFees.stream().map(this::setFeesPayResponse).toList(), HttpStatus.OK);
	}

}
