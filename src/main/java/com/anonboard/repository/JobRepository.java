package com.anonboard.repository;

import com.anonboard.model.Job;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends MongoRepository<Job, String> {
    List<Job> findByActiveTrueOrderByPostedDateDesc();

    List<Job> findByActiveTrueAndTypeOrderByPostedDateDesc(Job.JobType type);
}
