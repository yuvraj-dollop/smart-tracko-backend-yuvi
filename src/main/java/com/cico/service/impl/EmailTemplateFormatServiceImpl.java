package com.cico.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cico.model.EmailTemplate;
import com.cico.repository.EmailTemplateRepository;
@Service
public class EmailTemplateFormatServiceImpl {

    @Autowired
    private EmailTemplateRepository repo;

    public void preloadDefaultTemplates() {
        if (repo.count() > 0) return;

        save("OTP_LOGIN", "Your OTP for Login",
            """
            <div style='font-family: Arial, sans-serif; padding: 16px; color: #333;'>
                <h2 style='color: #1976D2;'>Login OTP Verification</h2>
                <p>Hello <strong>{{userName}}</strong>,</p>
                <p>Your OTP for login is: 
                    <span style='font-size: 18px; color: #d32f2f; font-weight: bold;'>{{otp}}</span>
                </p>
                <p>This OTP is valid for 5 minutes.</p>
            </div>
            """
        );

        save("FORGOT_PASSWORD", "Reset Your Password",
            """
            <div style='font-family: Arial, sans-serif; padding: 16px; color: #333;'>
                <h2 style='color: #1976D2;'>Password Reset Request</h2>
                <p>Hi <strong>{{userName}}</strong>,</p>
                <p>Your OTP to reset your password is: 
                    <span style='font-size: 18px; color: #d32f2f; font-weight: bold;'>{{otp}}</span>
                </p>
                <p>Please use this to reset your password. It will expire in 5 minutes.</p>
            </div>
            """
        );

        save("LEAVE_APPROVAL", "Leave Request {{status}}",
            """
            <div style='font-family: Arial, sans-serif; padding: 16px; color: #333;'>
                <h2 style='color: #388E3C;'>Leave Status Notification</h2>
                <p>Dear <strong>{{userName}}</strong>,</p>
                <p>Your leave request has been 
                    <span style='color: #d32f2f; font-weight: bold;'>{{status}}</span>.
                </p>
                <p>Check your dashboard for more details.</p>
            </div>
            """
        );

        save("TASK_ASSIGN", "New Task Assigned",
            """
            <div style='font-family: Arial, sans-serif; padding: 16px; color: #333;'>
                <h2 style='color: #F9A825;'>New Task Assignment</h2>
                <p>Hello <strong>{{userName}}</strong>,</p>
                <p>You have been assigned a new task titled: 
                    <strong>{{taskTitle}}</strong>.
                </p>
                <p>Please review the task and start accordingly.</p>
            </div>
            """
        );

        save("TEST_SUBMISSION", "Test Submitted",
            """
            <div style='font-family: Arial, sans-serif; padding: 16px; color: #333;'>
                <h2 style='color: #0288D1;'>Test Submission Confirmation</h2>
                <p>Hi <strong>{{userName}}</strong>,</p>
                <p>Your test titled 
                    <strong>{{testTitle}}</strong> has been successfully submitted.
                </p>
                <p>Thank you for completing it on time.</p>
            </div>
            """
        );
    }

    private void save(String type, String subject, String body) {
        EmailTemplate template = new EmailTemplate();
        template.setTemplateType(type);
        template.setSubject(subject);
        template.setBody(body);
        repo.save(template);
    }
}
