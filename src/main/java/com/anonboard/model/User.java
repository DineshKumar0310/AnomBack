package com.anonboard.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;

    @Indexed(unique = true)
    private String anonymousName;

    @Builder.Default
    private String avatar = "avatar_01";

    @Builder.Default
    private Role role = Role.USER;

    @Builder.Default
    private UserType userType = UserType.FREE;

    @Builder.Default
    private boolean isVerified = false;

    @Builder.Default
    private boolean isBanned = false;

    private String banReason;

    private Instant bannedUntil;

    @Builder.Default
    private int totalPosts = 0;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public enum Role {
        USER, MODERATOR, ADMIN
    }

    public enum UserType {
        FREE, PREMIUM
    }

    public boolean isPremium() {
        return userType == UserType.PREMIUM;
    }
}
