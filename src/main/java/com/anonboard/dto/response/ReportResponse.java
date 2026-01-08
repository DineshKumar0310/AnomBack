package com.anonboard.dto.response;

import com.anonboard.model.Report;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private String id;
    private Report.TargetType targetType;
    private String targetId;
    private Report.ReportReason reason;
    private String description;
    private Report.ReportStatus status;
    private String contentSnapshot;
    private String authorAnonymousName;
    private String reviewedBy;
    private String reviewNotes;
    private Instant reviewedAt;
    private Instant createdAt;
}
