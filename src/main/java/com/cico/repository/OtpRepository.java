package com.cico.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cico.model.Otp;
import com.cico.util.OtpType;

public interface OtpRepository extends JpaRepository<Otp, String> {

	Optional<Otp> findByEmailAndOtpType(String email, OtpType otpType);

}
