package com.anonboard.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "votes")
@CompoundIndex(name = "unique_vote", def = "{'userId': 1, 'targetType': 1, 'targetId': 1}", unique = true)
public class Vote {

    @Id
    private String id;

    private String userId;

    private TargetType targetType;

    private String targetId;

    private int voteType; // 1 = upvote, -1 = downvote

    @CreatedDate
    private Instant createdAt;

    public enum TargetType {
        POST, COMMENT
    }
}
