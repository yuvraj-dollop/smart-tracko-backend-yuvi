package com.cico.kafkaServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.cico.payload.EmailRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class KafkaProducerService {

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	
	@Autowired
	private ObjectMapper objectMapper;

	public void sendNotification(String topic, String message) {
//		kafkaTemplate.send(topic, message);
		// kafkaTemplate.send(topic, message);
	}
	
	
	public void sendEmailEvent(EmailRequest emailPayload) throws JsonProcessingException {
	
			String json = objectMapper.writeValueAsString(emailPayload);
			kafkaTemplate.send("email-topic", json);
	}
}
