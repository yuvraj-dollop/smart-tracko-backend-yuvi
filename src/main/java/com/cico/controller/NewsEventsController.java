package com.cico.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cico.model.NewsEvents;
import com.cico.payload.ApiResponse;
import com.cico.payload.NewsEventsResponse;
import com.cico.payload.PageResponse;
import com.cico.service.INewsEventsService;
import com.cico.util.AppConstants;

@RestController
@RequestMapping("/newsEvents")
@CrossOrigin("*")
public class NewsEventsController {

	@Autowired
	private INewsEventsService newsEventsService;

	// create
	@PostMapping("/createNewsEvents")
	public ResponseEntity<ApiResponse> createNewsEvents(@RequestParam("shortDescriptoin") String shortDescription,
			@RequestParam("briefDescription") String briefDescription, @RequestParam("title") String title,
			@RequestParam(value = "fileName", required = false) MultipartFile file) {

		newsEventsService.createNewsEvents(shortDescription, briefDescription, title, file);

		return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, AppConstants.CREATE_SUCCESS, HttpStatus.CREATED),
				HttpStatus.CREATED);
	}

	// update
	@PutMapping("/updateNewsEvents")
	public ResponseEntity<NewsEvents> updateNewsEvent(@RequestParam("id") Integer id,
			@RequestParam(name = "shortDescriptoin", required = false) String shortDescription,
			@RequestParam("briefDescription") String briefDescription, @RequestParam("title") String title,
			@RequestParam(name = "fileName", required = false) MultipartFile file) {

		NewsEvents newsEvents = newsEventsService.updateNewsEvents(id, shortDescription, briefDescription, title, file);
		return new ResponseEntity<>(newsEvents, HttpStatus.CREATED);
	}

	// delete
	@DeleteMapping("/deleteNewsEvents")
	public ResponseEntity<ApiResponse> deleteNewsEvent(@RequestParam("id") Integer id) {
		newsEventsService.deleteNewsEvents(id);
		return ResponseEntity.ok(new ApiResponse(Boolean.TRUE, AppConstants.DELETE_SUCCESS, HttpStatus.OK));
	}

	// get
	@GetMapping("/getNewsEvents")
	public ResponseEntity<NewsEvents> getNewsEvent(@RequestParam("id") Integer id) {
		NewsEvents newsEvents = newsEventsService.getNewsEvents(id);
		return new ResponseEntity<NewsEvents>(newsEvents, HttpStatus.OK);
	}

	// get all
	@GetMapping("/getAllNewsEvents")
	public ResponseEntity<PageResponse<NewsEvents>> getAllNewsEvent(
			@RequestParam(name = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer page,
			@RequestParam(name = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer size) {
		PageResponse<NewsEvents> newsEvents = newsEventsService.getAllNewsEvents(page, size);
		return new ResponseEntity<>(newsEvents, HttpStatus.OK);

	}

	@GetMapping("/getAllNewsEventsIsActive")
	public ResponseEntity<PageResponse<NewsEvents>> getAllNewsEventsIsActive(
			@RequestParam(name = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer page,
			@RequestParam(name = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer size) {
		PageResponse<NewsEvents> newsEvents = newsEventsService.getAllNewsEventsIsActive(page, size);
		return new ResponseEntity<>(newsEvents, HttpStatus.OK);
	}

	@PutMapping("/activeAndInActiveNewsAndEvent")
	public ResponseEntity<ApiResponse> activeAndInActiveNewsAndEvent(@RequestParam("id") Integer id) {
		Boolean newsAndEvent = newsEventsService.activeAndInActiveNewsAndEvent(id);
		if (newsAndEvent)
			return ResponseEntity.ok(new ApiResponse(Boolean.TRUE, "Deleted Successfully", HttpStatus.OK));
		return ResponseEntity.ok(new ApiResponse(Boolean.FALSE, "Deleted Successfully", HttpStatus.OK));
	}

	@GetMapping("/searchNewsAndEvents")
	public ResponseEntity<PageResponse<NewsEvents>> searchNewsAndEvents(
			@RequestParam(name = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer page,
			@RequestParam(name = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer size,
			@RequestParam("search") String search, @RequestParam("role") String role) {
		PageResponse<NewsEvents> searchNewsAndEvents = newsEventsService.searchNewsAndEvents(search, role, page, size);
		return ResponseEntity.status(HttpStatus.OK).body(searchNewsAndEvents);
	}

	// ============================ NEW API's =============================

	@GetMapping("v2/getAllNewsEventsIsActive")
	public ResponseEntity<PageResponse<NewsEventsResponse>> getAllNewsEventsIsActiveNew(
			@RequestParam(name = AppConstants.PAGE_NUMBER, defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer page,
			@RequestParam(name = AppConstants.PAGE_SIZE, defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer size) {
		PageResponse<NewsEventsResponse> newsEvents = newsEventsService.getAllNewsEventsIsActiveNew(page, size);
		return new ResponseEntity<>(newsEvents, HttpStatus.OK);
	}

	// get
	@GetMapping("v2/getNewsEvents")
	public ResponseEntity<NewsEventsResponse> getNewsEventNew(@RequestParam("id") Integer id) {
		NewsEventsResponse newsEventsResponse = newsEventsService.getNewsEventsNew(id);
		return new ResponseEntity<NewsEventsResponse>(newsEventsResponse, HttpStatus.OK);
	}

	// get all
	@GetMapping("/v2/getAllNewsEvents")
	public ResponseEntity<PageResponse<NewsEventsResponse>> getAllNewsEventNew(
			@RequestParam(name = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer page,
			@RequestParam(name = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer size) {
		PageResponse<NewsEventsResponse> newsEvents = newsEventsService.getAllNewsEventsNew(page, size);
		return new ResponseEntity<>(newsEvents, HttpStatus.OK);

	}

	// create
	@PostMapping(value = "/v2/createNewsEvents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse> createNewsEventsNew(@RequestParam("shortDescriptoin") String shortDescription,
			@RequestParam("briefDescription") String briefDescription, @RequestParam("title") String title,
			@RequestParam(value = "fileName", required = false) MultipartFile file) {
		System.err.println("=========================== file " + file);
		newsEventsService.createNewsEvents(shortDescription, briefDescription, title, file);

		return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, AppConstants.CREATE_SUCCESS, HttpStatus.CREATED),
				HttpStatus.CREATED);
	}

	// update
	@PutMapping("/v2/updateNewsEvents")
	public ResponseEntity<NewsEventsResponse> updateNewsEventNew(@RequestParam("id") Integer id,
			@RequestParam(name = "shortDescriptoin", required = false) String shortDescription,
			@RequestParam("briefDescription") String briefDescription, @RequestParam("title") String title,
			@RequestParam(name = "fileName", required = false) MultipartFile file) {

		NewsEventsResponse newsEvents = newsEventsService.updateNewsEventsNew(id, shortDescription, briefDescription,
				title, file);
		return new ResponseEntity<>(newsEvents, HttpStatus.OK);
	}

	@DeleteMapping("/v2/deleteNewsEvents")
	public ResponseEntity<ApiResponse> deleteNewsEventNew(@RequestParam("id") Integer id) {
		newsEventsService.deleteNewsEvents(id);
		return ResponseEntity.ok(new ApiResponse(Boolean.TRUE, AppConstants.DELETE_SUCCESS, HttpStatus.OK));
	}
}