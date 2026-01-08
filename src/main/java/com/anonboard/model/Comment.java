package com.anonboard.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "comments")
@CompoundIndexes({
        @CompoundIndex(name = "post_created", def = "{'postId': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "post_votes", def = "{'postId': 1, 'voteCount': -1}"),
        @CompoundIndex(name = "parent_created", def = "{'parentId': 1, 'createdAt': -1}")
})
public class Comment {

    @Id
    private String id;

    @Indexed
    private String postId;

    // Parent comment ID for replies (null for top-level comments)
    @Indexed
    private String parentId;

    private String authorId;

    private String authorAnonymousName;

    // Author's avatar for display
    private String authorAvatar;

    private String content;

    @Builder.Default
    private int voteCount = 0;

    // Count of replies to this comment
    @Builder.Default
    private int replyCount = 0;

    @Builder.Default
    private boolean isEdited = false;

    private Instant editableUntil;

    @Builder.Default
    private boolean isDeleted = false;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
