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
@Document(collection = "reports")
@CompoundIndexes({
        @CompoundIndex(name = "status_created", def = "{'status': 1, 'createdAt': -1}")
})
public class Report {

    @Id
    private String id;

    @Indexed
    private String reporterId;

    private TargetType targetType;

    @Indexed
    private String targetId;

    private ReportReason reason;

    private String description;

    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

    private String reviewedBy;

    private String reviewNotes;

    private Instant reviewedAt;

    // Snapshot of reported content for admin review
    private String contentSnapshot;
    private String authorAnonymousName;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public enum TargetType {
        POST, COMMENT
    }

    public enum ReportReason {
        SPAM,
        ABUSE,
        FAKE_INFORMATION,
        HARASSMENT,
        OTHER
    }

    public enum ReportStatus {
        PENDING, REVIEWED, RESOLVED, DISMISSED
    }
}
