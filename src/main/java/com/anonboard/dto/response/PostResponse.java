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
public class PostResponse {
    private String id;
    private String authorAnonymousName;
    private String authorAvatar;
    private String title;
    private String content;
    private String imageUrl;
    private List<String> tags;
    private int viewCount;
    private int shareCount;
    private int commentCount;

    @JsonProperty("isEdited")
    private boolean isEdited;

    @JsonProperty("canEdit")
    private boolean canEdit;

    private long editTimeRemainingSeconds;

    @JsonProperty("isAuthor")
    private boolean isAuthor;

    private Instant createdAt;
    private Instant updatedAt;
}
