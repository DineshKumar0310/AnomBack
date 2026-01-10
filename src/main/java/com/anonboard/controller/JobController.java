package com.anonboard.controller;

import com.anonboard.model.Job;
import com.anonboard.model.JobComment;
import com.anonboard.model.User;
import com.anonboard.repository.JobCommentRepository;
import com.anonboard.repository.UserRepository;
import com.anonboard.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final JobCommentRepository jobCommentRepository;
    private final UserRepository userRepository;

    // Public Endpoints
    @GetMapping("/jobs")
    public ResponseEntity<List<Job>> getJobs(@RequestParam(required = false) Job.JobType type) {
        return ResponseEntity.ok(jobService.getActiveJobs(type));
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<Job> getJob(@PathVariable String id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    // Job Comments
    @GetMapping("/jobs/{jobId}/comments")
    public ResponseEntity<List<JobComment>> getJobComments(@PathVariable String jobId) {
        return ResponseEntity.ok(jobCommentRepository.findByJobIdOrderByCreatedAtDesc(jobId));
    }

    @PostMapping("/jobs/{jobId}/comments")
    public ResponseEntity<JobComment> createJobComment(@PathVariable String jobId,
            @RequestBody Map<String, String> body) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        JobComment comment = JobComment.builder()
                .jobId(jobId)
                .authorId(user.getId())
                .authorAnonymousName(user.getAnonymousName())
                .content(body.get("content"))
                .build();
        return ResponseEntity.ok(jobCommentRepository.save(comment));
    }

    @DeleteMapping("/jobs/comments/{commentId}")
    public ResponseEntity<Void> deleteJobComment(@PathVariable String commentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        JobComment comment = jobCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // Only author or admin can delete
        if (!comment.getAuthorId().equals(user.getId()) && user.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(403).build();
        }

        jobCommentRepository.delete(comment);
        return ResponseEntity.noContent().build();
    }

    // Admin Endpoints
    @GetMapping("/admin/jobs")
    public ResponseEntity<List<Job>> getAllJobsForAdmin() {
        return ResponseEntity.ok(jobService.getAllJobsForAdmin());
    }

    @PostMapping("/admin/jobs")
    public ResponseEntity<Job> createJob(@RequestBody Job job) {
        return ResponseEntity.ok(jobService.createJob(job));
    }

    @PutMapping("/admin/jobs/{id}")
    public ResponseEntity<Job> updateJob(@PathVariable String id, @RequestBody Job job) {
        return ResponseEntity.ok(jobService.updateJob(id, job));
    }

    @DeleteMapping("/admin/jobs/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable String id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }
}
