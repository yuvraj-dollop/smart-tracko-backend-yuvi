package com.cico.config;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

public class AppUtils {
  
	public static Cloudinary cloudinary;
	
	public static Page<?> convertListToPage(List<?> dataList, Pageable pageable) {
		int pageSize = pageable.getPageSize(); 
		int currentPage = pageable.getPageNumber();
		int startItem = currentPage * pageSize;

		List<?> pageList;

		if (dataList.size() < startItem) {
			pageList = Collections.emptyList();
		} else {
			int toIndex = Math.min(startItem + pageSize, dataList.size());
			pageList = dataList.subList(startItem, toIndex);
		}

		return new PageImpl<>(pageList, pageable, dataList.size());
	}
	
	  // Upload photo on cloudinary server
	 	public static String uploadPhoto(MultipartFile myFile, String destinationPath) {
	 		String uuid = UUID.randomUUID().toString();
	 		String randomName = uuid.concat(myFile.getOriginalFilename());
	 		String fileName = StringUtils.cleanPath(randomName);
	 		Map uploadResponse;
	 		try {
	 			uploadResponse = cloudinary.uploader().upload(myFile.getBytes(),
	 					ObjectUtils.asMap("public_id", destinationPath + "/" + fileName));
	 			return (String) uploadResponse.get("secure_url");
	 		} catch (IOException e) {
	 			e.printStackTrace();
	 		}
	 		return null;
	 	}

}
