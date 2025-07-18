package com.cico.payload;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.springframework.web.multipart.MultipartFile;

import com.cico.model.Course;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateStudentRequest {

	private Integer studentId;
	private String userId;
	private String fullName;
	private String mobile;
	private LocalDate dob;
	private String email;
	@JsonIgnore
	private String password;
	private String college;
	private LocalDate joinDate;
	private MultipartFile profilePic;
	private LocalDateTime createdDate;
	private String applyForCourse;
	private String currentSem;
	private String fathersName;
	private String mothersName;
	private String fathersOccupation;
	private String contactFather;
	private String contactMother;
	private String localAddress;
	private String parmanentAddress;
	private String languageKnown;
	private String currentCourse;
	private String fcmId;
	private String deviceId;
	private String inUseDeviceId;
	private String isDeviceApproved;
	private String deviceType;
	private Boolean isFromEnquiry=false;
	private Boolean isConverted=false;
	private Boolean isCompleted=false;
	private Boolean isActive =true;
	private String role;
	
	private Course course;
	    // Getters and Setters
	    public MultipartFile getProfilePic() {
	        return profilePic;
	    }

	    public void setProfilePic(MultipartFile fileName) {
	        this.profilePic = fileName;
	    }

		public void setProfilePic(String fileName) {
			// TODO Auto-generated method stub
			
		}
	
}
