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
@Document(collection = "job_comments")
@CompoundIndexes({
        @CompoundIndex(name = "job_created", def = "{'jobId': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "parent_created", def = "{'parentId': 1, 'createdAt': -1}")
})
public class JobComment {

    @Id
    private String id;

    @Indexed
    private String jobId;

    // Parent comment ID for replies (null for top-level comments)
    @Indexed
    private String parentId;

    private String authorId;

    private String authorAnonymousName;

    private String authorAvatar;

    private String content;

    @Builder.Default
    private int replyCount = 0;

    @Builder.Default
    private boolean isEdited = false;

    private Instant editableUntil;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
