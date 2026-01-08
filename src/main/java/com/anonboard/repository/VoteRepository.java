package com.anonboard.repository;

import com.anonboard.model.Vote;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends MongoRepository<Vote, String> {

    Optional<Vote> findByUserIdAndTargetTypeAndTargetId(String userId, Vote.TargetType targetType, String targetId);

    List<Vote> findByUserIdAndTargetTypeAndTargetIdIn(String userId, Vote.TargetType targetType,
            List<String> targetIds);

    void deleteByUserIdAndTargetTypeAndTargetId(String userId, Vote.TargetType targetType, String targetId);
}
