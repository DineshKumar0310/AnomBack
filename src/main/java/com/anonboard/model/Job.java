package com.anonboard.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "jobs")
public class Job {

    @Id
    private String id;

    private String companyName;

    private String logoUrl;

    private String title;

    @Builder.Default
    private JobType type = JobType.JOB;

    private String duration; // Only for internships

    private String location; // "Remote", "Onsite", "Hybrid - City"

    // Enhanced eligibility fields
    @Builder.Default
    private List<String> eligibleDegrees = new ArrayList<>(); // B.E., B.Tech, BCS, MCA, etc.

    @Builder.Default
    private List<String> eligibleBranches = new ArrayList<>(); // CSE, IT, ECE, etc.

    private String eligibleBatches; // "2024-2026"

    private String experienceLevel; // "Fresher", "0-2 years", "2-5 years"

    // Legacy field kept for backward compatibility
    private String eligibility;

    private Instant lastDate;

    private String description;

    private String applyLink;

    @Builder.Default
    private boolean active = true; // Renamed from isActive to fix lombok getter issue

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @CreatedDate
    @Indexed
    private Instant postedDate;

    @LastModifiedDate
    private Instant updatedAt;

    public enum JobType {
        JOB, INTERNSHIP
    }
}
