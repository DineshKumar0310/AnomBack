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

    private String eligibility;

    private Instant lastDate;

    // Use a string or structure for rich text description if needed, effectively
    // simpler for now
    private String description;

    private String applyLink;

    @Builder.Default
    private boolean isActive = true;

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
