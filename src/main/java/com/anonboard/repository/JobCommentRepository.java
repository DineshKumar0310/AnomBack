package com.anonboard.repository;

import com.anonboard.model.JobComment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobCommentRepository extends MongoRepository<JobComment, String> {
    // Top-level comments (no parent)
    List<JobComment> findByJobIdAndParentIdIsNullOrderByCreatedAtDesc(String jobId);

    // Replies to a comment
    List<JobComment> findByParentIdOrderByCreatedAtAsc(String parentId);

    // Count replies for a parent
    int countByParentId(String parentId);
}
