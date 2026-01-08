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
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "posts")
@CompoundIndexes({
        @CompoundIndex(name = "tags_created", def = "{'tags': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "trending", def = "{'viewCount': -1, 'shareCount': -1}")
})
public class Post {

    @Id
    private String id;

    @Indexed
    private String authorId;

    private String authorAnonymousName;

    private String authorAvatar;

    private String title;

    private String content;

    private String imageUrl;

    @Indexed
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    // View count - incremented when post detail is opened
    @Builder.Default
    private int viewCount = 0;

    // Share count - incremented when share button is clicked
    @Builder.Default
    private int shareCount = 0;

    @Builder.Default
    private int commentCount = 0;

    @Builder.Default
    private boolean isEdited = false;

    private Instant editableUntil;

    @Builder.Default
    private boolean isDeleted = false;

    @CreatedDate
    @Indexed
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
