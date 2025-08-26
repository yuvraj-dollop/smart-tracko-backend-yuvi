package com.cico.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cico.payload.CommentReplyRequest;
import com.cico.payload.CreateCommentRequest;
import com.cico.payload.CreateDiscussionFormRequest;
import com.cico.payload.LikeRequest;

@Component
public interface IdiscussionForm {

	ResponseEntity<?> createDiscussionForm(Integer studentId, MultipartFile file, String content,
			MultipartFile audioFile);

	ResponseEntity<?> createComment(Integer studentId, String content, Integer discussionFormId, MultipartFile file);

	ResponseEntity<?> getAllDiscussionForm(Integer studentId);

	ResponseEntity<?> getDiscussionFormById(Integer id, Integer studentId);

	ResponseEntity<?> addOrRemoveLike(Integer studentId, Integer discussionFormId);

	ResponseEntity<?> removeComment(Integer discussionFormId, Integer commentsId);

	ResponseEntity<?> deletePost();

	ResponseEntity<?> addCommentReply(Integer studentId, Integer commentsId, String content, MultipartFile file);

	ResponseEntity<?> removeCommentReply(Integer commentReplyId, Integer commentsId);

	ResponseEntity<?> searchingDiscussionForm(String search);

	// ......................... NEW METHOD'S ............................

	ResponseEntity<?> createDiscussionForm(CreateDiscussionFormRequest discussionFormRequest);

	ResponseEntity<?> createComment(CreateCommentRequest commentRequest);

	ResponseEntity<?> addOrRemoveLike(LikeRequest likeRequest);

	ResponseEntity<?> addCommentReply(CommentReplyRequest replyRequest);

	ResponseEntity<?> getAllDiscussionFormNew(Integer studentId, Integer pageSize, Integer pageNo);

	ResponseEntity<?> searchingDiscussionFormByAllFields(String search);
}
