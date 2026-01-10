package com.anonboard.repository;

import com.anonboard.model.JobComment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobCommentRepository extends MongoRepository<JobComment, String> {
    List<JobComment> findByJobIdOrderByCreatedAtDesc(String jobId);
}
