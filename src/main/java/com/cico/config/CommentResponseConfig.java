package com.cico.config;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseConfig {

	private Integer id;
	public LocalDateTime createdDate;
	private String content;
	private String studentName;
	private String studentProfilePic;
	private Integer studentId;
	private String file;
	private String type;
	private  Integer discussionFormId;

	 @Override
	    public String toString() {
	        return "{\"id\":" + id + ", "
	                + "\"type\":\"" + type + "\", "
	                + "\"createdDate\":\"" + createdDate + "\", "
	                + "\"content\":\"" + content + "\", "
	                + "\"studentId\":\"" + studentId + "\", "
	                + "\"studentName\":\"" + studentName + "\", "
	                + "\"studentProfilePic\":\"" + studentProfilePic + "\", "
	                + "\"file\":\"" + file + "\", "
	                + "\"discussionFormId\":" + discussionFormId + "}";
	    }
		
}
