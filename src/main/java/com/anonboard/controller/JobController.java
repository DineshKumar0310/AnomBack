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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final JobCommentRepository jobCommentRepository;
    private final UserRepository userRepository;
    private final com.anonboard.service.NotificationService notificationService;

    private static final int EDIT_WINDOW_MINUTES = 5;

    // ==================== JOB ENDPOINTS ====================

    @GetMapping("/jobs")
    public ResponseEntity<List<Job>> getJobs(@RequestParam(required = false) Job.JobType type) {
        return ResponseEntity.ok(jobService.getActiveJobs(type));
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<Job> getJob(@PathVariable String id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    // ==================== JOB COMMENT ENDPOINTS ====================

    @GetMapping("/jobs/{jobId}/comments")
    public ResponseEntity<List<Map<String, Object>>> getJobComments(@PathVariable String jobId) {
        User currentUser = getCurrentUser();
        List<JobComment> comments = jobCommentRepository.findByJobIdAndParentIdIsNullOrderByCreatedAtDesc(jobId);

        List<Map<String, Object>> enrichedComments = comments.stream().map(c -> enrichComment(c, currentUser)).toList();
        return ResponseEntity.ok(enrichedComments);
    }

    @GetMapping("/jobs/comments/{commentId}/replies")
    public ResponseEntity<List<Map<String, Object>>> getCommentReplies(@PathVariable String commentId) {
        User currentUser = getCurrentUser();
        List<JobComment> replies = jobCommentRepository.findByParentIdOrderByCreatedAtAsc(commentId);

        List<Map<String, Object>> enrichedReplies = replies.stream().map(c -> enrichComment(c, currentUser)).toList();
        return ResponseEntity.ok(enrichedReplies);
    }

    @PostMapping("/jobs/{jobId}/comments")
    public ResponseEntity<Map<String, Object>> createJobComment(
            @PathVariable String jobId,
            @RequestBody Map<String, String> body) {
        User user = getCurrentUser();

        String parentId = body.get("parentId");

        // If replying, increment parent's reply count
        if (parentId != null) {
            JobComment parent = jobCommentRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            parent.setReplyCount(parent.getReplyCount() + 1);
            jobCommentRepository.save(parent);
        }

        JobComment comment = JobComment.builder()
                .jobId(jobId)
                .parentId(parentId)
                .authorId(user.getId())
                .authorAnonymousName(user.getAnonymousName())
                .authorAvatar(user.getAvatar())
                .content(body.get("content"))
                .editableUntil(Instant.now().plus(EDIT_WINDOW_MINUTES, ChronoUnit.MINUTES))
                .build();

        JobComment saved = jobCommentRepository.save(comment);

        if (parentId != null) {
            // Notify parent comment author
            jobCommentRepository.findById(parentId).ifPresent(parent -> {
                com.anonboard.model.Notification.NotificationType type = com.anonboard.model.Notification.NotificationType.JOB_REPLY;
                String message = "replied to your question on a job";
                String link = "/jobs/" + jobId;
                notificationService.createNotification(parent.getAuthorId(), type, message, link, user);
            });
        }

        return ResponseEntity.ok(enrichComment(saved, user));
    }

    @PutMapping("/jobs/comments/{commentId}")
    public ResponseEntity<?> editJobComment(
            @PathVariable String commentId,
            @RequestBody Map<String, String> body) {
        User user = getCurrentUser();

        JobComment comment = jobCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // Check ownership
        if (!comment.getAuthorId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("error", "You can only edit your own comments"));
        }

        // Check edit window
        if (comment.getEditableUntil() == null || Instant.now().isAfter(comment.getEditableUntil())) {
            return ResponseEntity.status(400).body(Map.of("error", "Edit window has expired"));
        }

        comment.setContent(body.get("content"));
        comment.setEdited(true);

        JobComment saved = jobCommentRepository.save(comment);
        return ResponseEntity.ok(enrichComment(saved, user));
    }

    @DeleteMapping("/jobs/comments/{commentId}")
    public ResponseEntity<Void> deleteJobComment(@PathVariable String commentId) {
        User user = getCurrentUser();

        JobComment comment = jobCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // Only author or admin can delete
        if (!comment.getAuthorId().equals(user.getId()) && user.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(403).build();
        }

        // If this comment has a parent, decrement parent's reply count
        if (comment.getParentId() != null) {
            jobCommentRepository.findById(comment.getParentId()).ifPresent(parent -> {
                parent.setReplyCount(Math.max(0, parent.getReplyCount() - 1));
                jobCommentRepository.save(parent);
            });
        }

        jobCommentRepository.delete(comment);
        return ResponseEntity.noContent().build();
    }

    // ==================== ADMIN ENDPOINTS ====================

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

    // ==================== HELPER METHODS ====================

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Map<String, Object> enrichComment(JobComment comment, User currentUser) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", comment.getId());
        map.put("jobId", comment.getJobId());
        map.put("parentId", comment.getParentId());
        map.put("authorId", comment.getAuthorId());
        map.put("authorAnonymousName", comment.getAuthorAnonymousName());
        map.put("authorAvatar", comment.getAuthorAvatar());
        map.put("content", comment.getContent());
        map.put("replyCount", comment.getReplyCount());
        map.put("isEdited", comment.isEdited());
        map.put("createdAt", comment.getCreatedAt());

        boolean isAuthor = currentUser != null && comment.getAuthorId().equals(currentUser.getId());
        map.put("isAuthor", isAuthor);

        boolean canEdit = isAuthor && comment.getEditableUntil() != null
                && Instant.now().isBefore(comment.getEditableUntil());
        map.put("canEdit", canEdit);

        if (canEdit && comment.getEditableUntil() != null) {
            long secondsRemaining = ChronoUnit.SECONDS.between(Instant.now(), comment.getEditableUntil());
            map.put("editTimeRemainingSeconds", Math.max(0, secondsRemaining));
        } else {
            map.put("editTimeRemainingSeconds", 0);
        }

        return map;
    }
}
