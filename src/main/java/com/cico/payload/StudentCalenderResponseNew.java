package com.cico.payload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentCalenderResponseNew {
	List<Map<String, Object>> calenderData = new ArrayList<>();
	Map<String, Object> attendanceStats = new HashMap<>();

	Integer sundayCount = 0;

}
