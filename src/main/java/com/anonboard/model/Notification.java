package com.anonboard.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    @Indexed
    private String recipientId;

    private NotificationType type;

    private String message;

    private String link; // URL to redirect to (e.g., /post/123)

    private String actorAnonymousName; // Name of person who performed action

    private String actorAvatar;

    @Builder.Default
    private boolean isRead = false;

    @CreatedDate
    @Indexed
    private Instant createdAt;

    public enum NotificationType {
        POST_COMMENT,
        COMMENT_REPLY,
        JOB_REPLY,
        SYSTEM
    }
}
