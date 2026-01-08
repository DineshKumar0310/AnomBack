package com.anonboard.repository;

import com.anonboard.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {

    // Latest posts
    Page<Post> findByIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    // Trending posts (by view count)
    Page<Post> findByIsDeletedFalseOrderByViewCountDesc(Pageable pageable);

    // Filter by tag
    Page<Post> findByTagsContainingAndIsDeletedFalseOrderByCreatedAtDesc(String tag, Pageable pageable);

    Page<Post> findByTagsContainingAndIsDeletedFalseOrderByViewCountDesc(String tag, Pageable pageable);

    // User's posts
    Page<Post> findByAuthorIdAndIsDeletedFalseOrderByCreatedAtDesc(String authorId, Pageable pageable);

    // Search - title, content, or tags
    @Query("{ 'isDeleted': false, '$or': [ " +
            "{ 'title': { '$regex': ?0, '$options': 'i' } }, " +
            "{ 'content': { '$regex': ?0, '$options': 'i' } }, " +
            "{ 'tags': { '$regex': ?0, '$options': 'i' } } " +
            "] }")
    Page<Post> searchPosts(String searchTerm, Pageable pageable);

    @Query("{ '_id': ?0 }")
    @Update("{ '$inc': { 'viewCount': 1 } }")
    void incrementViewCount(String postId);

    @Query("{ '_id': ?0 }")
    @Update("{ '$inc': { 'shareCount': 1 } }")
    void incrementShareCount(String postId);

    @Query("{ '_id': ?0 }")
    @Update("{ '$inc': { 'commentCount': ?1 } }")
    void incrementCommentCount(String postId, int delta);

    // Count non-deleted posts
    long countByIsDeletedFalse();
}
