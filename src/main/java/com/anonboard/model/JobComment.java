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
@Document(collection = "job_comments")
public class JobComment {

    @Id
    private String id;

    @Indexed
    private String jobId;

    private String authorId;

    private String authorAnonymousName;

    private String content;

    @CreatedDate
    private Instant createdAt;
}
