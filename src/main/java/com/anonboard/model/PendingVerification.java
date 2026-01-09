package com.anonboard.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "pending_verifications")
public class PendingVerification {

    @Id
    private String email;

    private String otp;

    @Indexed(expireAfterSeconds = 0) // Mongo TTL index
    private Instant expiresAt;
}
