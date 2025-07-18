package com.cico.service.impl;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cico.exception.OtpVerificationLimitExceededException;
import com.cico.exception.TooManyOtpRequestsException;
import com.cico.model.Otp;
import com.cico.repository.OtpRepository;
import com.cico.service.IOtpService;
import com.cico.util.OtpType;

@Service
public class OtpServiceImpl implements IOtpService {

    @Autowired
    private OtpRepository otpRepo;

    private final Random random = new Random();
    private static final Integer MAX_RESEND_ATTEMPTS = 3;
    private static final Integer MAX_VERIFICATION_ATTEMPTS = 3;

    @Override
    public String generateOtp(String email, OtpType otpType) {
        String otp = String.format("%06d", random.nextInt(1_000_000));
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryTime = now.plusMinutes(5);

        Otp otpEntry = otpRepo.findByEmailAndOtpType(email, otpType).orElse(null);

        if (otpEntry == null || otpEntry.getExpirationTime().isBefore(now)) {
            // Fresh OTP (or expired), reset everything
            otpEntry = Otp.builder()
                    .email(email)
                    .otp(otp)
                    .expirationTime(expiryTime)
                    .otpType(otpType)
                    .resendAttempts(0)
                    .verificationAttempts(0)
                    .lastModifiedTime(now)
                    .build();
        } else {
//        	if (otpEntry.getResendAttempts() >= MAX_RESEND_ATTEMPTS) {
//        	    throw new TooManyOtpRequestsException("Maximum OTP resend attempts reached. Please try again later.");
//        	}

            otpEntry.setOtp(otp);
            otpEntry.setExpirationTime(expiryTime);
            otpEntry.setResendAttempts(otpEntry.getResendAttempts() + 1);
            otpEntry.setLastModifiedTime(now);
        }

        otpRepo.save(otpEntry);
        return otp;
    }


    @Override
    public boolean validateOtp(String email, String otp, OtpType otpType) {
        Optional<Otp> optionalOtp = otpRepo.findByEmailAndOtpType(email, otpType);
        LocalDateTime now = LocalDateTime.now();

        if (optionalOtp.isEmpty()) return false;

        Otp otpEntry = optionalOtp.get();

        if (otpEntry.getExpirationTime().isBefore(now)) return false;

//        if (otpEntry.getVerificationAttempts() >= MAX_VERIFICATION_ATTEMPTS) {
//            throw new OtpVerificationLimitExceededException("Maximum OTP verification attempts reached. Please try again later.");
//        }
        otpEntry.setVerificationAttempts(otpEntry.getVerificationAttempts() + 1);
        otpEntry.setLastModifiedTime(now);
        otpRepo.save(otpEntry);

        return otpEntry.getOtp().equals(otp);
    }

}
