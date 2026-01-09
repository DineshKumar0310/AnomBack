package com.anonboard.repository;

import com.anonboard.model.PendingVerification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PendingVerificationRepository extends MongoRepository<PendingVerification, String> {
}
