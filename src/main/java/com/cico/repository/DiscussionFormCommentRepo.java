package com.cico.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cico.model.DiscussionFormComment;

@Repository
public interface DiscussionFormCommentRepo extends JpaRepository<DiscussionFormComment, Integer> {

	

}
