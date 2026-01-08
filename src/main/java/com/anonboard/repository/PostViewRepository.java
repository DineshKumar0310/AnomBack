package com.anonboard.repository;

import com.anonboard.model.PostView;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostViewRepository extends MongoRepository<PostView, String> {

    boolean existsByUserIdAndPostId(String userId, String postId);
}
