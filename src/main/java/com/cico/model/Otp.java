package com.cico.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import com.cico.util.OtpType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "otp_tab")
@Builder
public class Otp {

    @Id
    private String email;

    private String otp;

    private LocalDateTime expirationTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "otp_type")
    private OtpType otpType;

    @Column(name = "resend_attempts")
    private Integer resendAttempts = 0;

    @Column(name = "verification_attempts")
    private Integer verificationAttempts = 0;

    @Column(name = "last_modified_time")
    private LocalDateTime lastModifiedTime = LocalDateTime.now();
}
