package com.cico.payload;

import java.util.Map;

import com.cico.util.TemplateType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequest {
	private String toEmail;
	private TemplateType templateType;
	private Map<String, String> placeholders;
}