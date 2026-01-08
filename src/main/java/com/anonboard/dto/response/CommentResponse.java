package com.anonboard.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private String id;
    private String postId;
    private String parentId;
    private String authorAnonymousName;
    private String authorAvatar;
    private String content;
    private int voteCount;
    private Integer userVote;
    private int replyCount;

    @JsonProperty("isEdited")
    private boolean isEdited;

    @JsonProperty("canEdit")
    private boolean canEdit;

    private long editTimeRemainingSeconds;

    @JsonProperty("isAuthor")
    private boolean isAuthor;

    private Instant createdAt;

    private List<CommentResponse> replies;
}
