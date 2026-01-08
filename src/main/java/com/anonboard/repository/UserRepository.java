package com.anonboard.repository;

import com.anonboard.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByAnonymousName(String anonymousName);

    boolean existsByEmail(String email);

    boolean existsByAnonymousName(String anonymousName);

    long countByUserType(User.UserType userType);

    long countByIsBannedTrue();
}
