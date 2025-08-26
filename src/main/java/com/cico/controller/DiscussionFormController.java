package com.cico.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cico.payload.CommentReplyRequest;
import com.cico.payload.CreateCommentRequest;
import com.cico.payload.CreateDiscussionFormRequest;
import com.cico.payload.LikeRequest;
import com.cico.service.IdiscussionForm;
import com.cico.util.AppConstants;

@RestController
@CrossOrigin("*")
@RequestMapping("/discussionForm/")
@ResponseBody
public class DiscussionFormController {

	@Autowired
	private IdiscussionForm discussionFormService;

	@PostMapping("createDiscussionForm")
	public ResponseEntity<?> createDiscussionForm(@RequestParam("studentId") Integer studentId,
			@RequestParam(name = "file", required = false) MultipartFile file, @RequestParam("content") String content,
			@RequestParam(name = "audioFile", required = false) MultipartFile audioFile) {
		return discussionFormService.createDiscussionForm(studentId, file, content, audioFile);
	}

	@PostMapping("createComment")
	public ResponseEntity<?> createComment(@RequestParam("studentId") Integer studentId,
			@RequestParam("content") String content, @RequestParam("discussionFormId") Integer discussionFormId,
			@RequestParam(name = "file", required = false) MultipartFile file) {
		return discussionFormService.createComment(studentId, content, discussionFormId, file);
	}

	@GetMapping("getAllDiscussionForm")
	public ResponseEntity<?> getAllDiscussionForm(
			@RequestParam(name = AppConstants.STUDENT_ID, required = false) Integer studentId) {
		return this.discussionFormService.getAllDiscussionForm(studentId);
	}

	@GetMapping("getDiscussionFormById")
	public ResponseEntity<?> getDiscussionFormById(@RequestParam(name = AppConstants.ID) Integer id,
			@RequestParam(name = AppConstants.STUDENT_ID, required = false) Integer studentId) {
		return this.discussionFormService.getDiscussionFormById(id, studentId);
	}

	@PostMapping("addOrRemoveLike")
	public ResponseEntity<?> addLike(@RequestParam("studentId") Integer studentId,
			@RequestParam("discussionFormId") Integer discussionFormId) {
		return discussionFormService.addOrRemoveLike(studentId, discussionFormId);
	}

	@DeleteMapping("removeComment")
	public ResponseEntity<?> removeComment(
			@RequestParam(name = AppConstants.DISCUSSION_FORM_ID) Integer discussionFormId,
			@RequestParam(name = AppConstants.COMMENTS_ID) Integer commentsId) {
		return discussionFormService.removeComment(discussionFormId, commentsId);
	}

	@DeleteMapping("removeCommentReply")
	public ResponseEntity<?> removeCommentReply(
			@RequestParam(name = AppConstants.COMMENT_REPLY_ID) Integer commentReplyId,
			@RequestParam(name = AppConstants.COMMENTS_ID) Integer commentsId) {
		return discussionFormService.removeCommentReply(commentReplyId, commentsId);
	}

	@DeleteMapping("deletepost")
	public ResponseEntity<?> deleteAllPost() {
		return discussionFormService.deletePost();
	}

	@PostMapping("addCommentReply")
	public ResponseEntity<?> addCommentReply(@RequestParam("studentId") Integer studentId,
			@RequestParam("commentsId") Integer commentsId, @RequestParam("content") String content,
			@RequestParam(name = "file", required = false) MultipartFile file) {
		return discussionFormService.addCommentReply(studentId, commentsId, content, file);
	}

	@GetMapping("searchingDiscussionForm")
	public ResponseEntity<?> searchingDiscussionForm(@RequestParam(name = AppConstants.SEARCH) String search) {
		return this.discussionFormService.searchingDiscussionForm(search);
	}

	// ...................... NEW API'S .............................

	@PostMapping("v2/createDiscussionForm")
	public ResponseEntity<?> createDiscussionForm(
			@Valid @RequestBody CreateDiscussionFormRequest discussionFormRequest) {
		return discussionFormService.createDiscussionForm(discussionFormRequest);
	}

	@PostMapping("v2/createComment")
	public ResponseEntity<?> createComment(@Valid @RequestBody CreateCommentRequest commentRequest) {
		return discussionFormService.createComment(commentRequest);
	}

	@PostMapping("v2/addOrRemoveLike")
	public ResponseEntity<?> addLike(@Valid @RequestBody LikeRequest likeRequest) {
		return discussionFormService.addOrRemoveLike(likeRequest);
	}

	@PostMapping("v2/addCommentReply")
	public ResponseEntity<?> addCommentReply(@Valid @RequestBody CommentReplyRequest replyRequest) {
		return discussionFormService.addCommentReply(replyRequest);
	}

	@GetMapping("v2/getAllDiscussionForm")
	public ResponseEntity<?> getAllDiscussionFormNew(
			@RequestParam(name = AppConstants.STUDENT_ID, required = false) Integer studentId,
			@RequestParam(name = AppConstants.PAGE_SIZE, defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
			@RequestParam(name = AppConstants.PAGE_NUMBER, defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer pageNo) {
		return this.discussionFormService.getAllDiscussionFormNew(studentId, pageSize, pageNo);
	}

	@GetMapping("v2/searchingDiscussionFormByAllFields")
	public ResponseEntity<?> searchingDiscussionFormByAllFields(
			@RequestParam(name = AppConstants.SEARCH) String search) {
		return this.discussionFormService.searchingDiscussionFormByAllFields(search);
	}

}
