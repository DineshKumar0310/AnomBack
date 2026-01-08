package com.anonboard.repository;

import com.anonboard.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {

    // Top-level comments (no parent)
    Page<Comment> findByPostIdAndParentIdIsNullAndIsDeletedFalse(String postId, Pageable pageable);

    // Replies to a comment
    List<Comment> findByParentIdAndIsDeletedFalse(String parentId, Sort sort);

    // All comments for a post
    Page<Comment> findByPostIdAndIsDeletedFalse(String postId, Pageable pageable);

    @Query("{ '_id': ?0 }")
    @Update("{ '$inc': { 'voteCount': ?1 } }")
    void incrementVoteCount(String commentId, int delta);

    @Query("{ '_id': ?0 }")
    @Update("{ '$inc': { 'replyCount': ?1 } }")
    void incrementReplyCount(String commentId, int delta);

    // Count non-deleted comments
    long countByIsDeletedFalse();
}
