package com.cico.util;

import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.cico.exception.ResourceNotFoundException;
import com.cico.model.EmailTemplate;
import com.cico.repository.EmailTemplateRepository;

@Service
public class EmailService {

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private EmailTemplateRepository emailTemplateRepo;

	public void sendEmail(String toEmail, TemplateType templateType, Map<String, String> dynamicValues) {
		EmailTemplate template = emailTemplateRepo.findByTemplateType(templateType.name())
				.orElseThrow(() -> new ResourceNotFoundException("Email template not found for type: " + templateType));

		String body = replacePlaceholders(template.getBody(), dynamicValues);
		String subject = replacePlaceholders(template.getSubject(), dynamicValues);

		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);

			helper.setTo(toEmail);
			helper.setSubject(subject);
			helper.setText(body, true); 

			mailSender.send(message);
		} catch (MessagingException e) {
			System.err.println("Error sending email: " + e.getMessage());
			
		}
	}

	private String replacePlaceholders(String text, Map<String, String> values) {
		for (Map.Entry<String, String> entry : values.entrySet()) {
			text = text.replace("{{" + entry.getKey() + "}}", entry.getValue());
		}
		return text;
	}

	
}
