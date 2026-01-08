package com.anonboard.service;

import com.anonboard.exception.NotFoundException;
import com.anonboard.model.Comment;
import com.anonboard.model.Post;
import com.anonboard.model.Report;
import com.anonboard.model.User;
import com.anonboard.repository.CommentRepository;
import com.anonboard.repository.PostRepository;
import com.anonboard.repository.ReportRepository;
import com.anonboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;

    @Value("${app.free-user.post-limit:5}")
    private int freePostLimit;

    public AdminService(UserRepository userRepository, PostRepository postRepository,
            CommentRepository commentRepository, ReportRepository reportRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.reportRepository = reportRepository;
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUsers", userRepository.count());
        stats.put("totalPosts", postRepository.countByIsDeletedFalse());
        stats.put("totalComments", commentRepository.countByIsDeletedFalse());
        stats.put("totalPremiumUsers", userRepository.countByUserType(User.UserType.PREMIUM));
        stats.put("totalBannedUsers", userRepository.countByIsBannedTrue());
        stats.put("pendingReports", reportRepository.countByStatus(Report.ReportStatus.PENDING));
        stats.put("resolvedReports", reportRepository.countByStatus(Report.ReportStatus.RESOLVED));

        return stats;
    }

    public void banUser(String userId, String reason, Integer durationDays) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setBanned(true);
        user.setBanReason(reason);

        if (durationDays != null && durationDays > 0) {
            user.setBannedUntil(Instant.now().plus(durationDays, ChronoUnit.DAYS));
        } else {
            user.setBannedUntil(null);
        }

        userRepository.save(user);
    }

    public void unbanUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setBanned(false);
        user.setBanReason(null);
        user.setBannedUntil(null);

        userRepository.save(user);
    }

    // Admin delete post - cascades to comments and reports
    public void removePost(String postId, String adminId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        // Soft delete the post
        post.setDeleted(true);
        postRepository.save(post);

        // Resolve all reports for this post
        resolveReportsForTarget(Report.TargetType.POST, postId, adminId);
    }

    // Admin delete comment - cascades to reports
    public void removeComment(String commentId, String adminId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        // Soft delete the comment
        comment.setDeleted(true);
        commentRepository.save(comment);

        // Decrement counts properly
        if (comment.getParentId() == null) {
            // Top-level comment - decrement post's comment count
            postRepository.incrementCommentCount(comment.getPostId(), -1);
        } else {
            // Reply - decrement parent's reply count
            commentRepository.incrementReplyCount(comment.getParentId(), -1);
        }

        // Resolve all reports for this comment
        resolveReportsForTarget(Report.TargetType.COMMENT, commentId, adminId);
    }

    private void resolveReportsForTarget(Report.TargetType targetType, String targetId, String adminId) {
        List<Report> reports = reportRepository.findByTargetTypeAndTargetIdAndStatus(
                targetType, targetId, Report.ReportStatus.PENDING);

        Instant now = Instant.now();
        for (Report report : reports) {
            report.setStatus(Report.ReportStatus.RESOLVED);
            report.setReviewedBy(adminId);
            report.setReviewNotes("Content removed by admin");
            report.setReviewedAt(now);
        }

        if (!reports.isEmpty()) {
            reportRepository.saveAll(reports);
        }
    }

    public Map<String, Object> getUserRealIdentity(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Map<String, Object> identity = new HashMap<>();
        identity.put("id", user.getId());
        identity.put("email", user.getEmail());
        identity.put("anonymousName", user.getAnonymousName());
        identity.put("avatar", user.getAvatar());
        identity.put("role", user.getRole().name());
        identity.put("userType", user.getUserType().name());
        identity.put("isPremium", user.isPremium());
        identity.put("isBanned", user.isBanned());
        identity.put("banReason", user.getBanReason());
        identity.put("totalPosts", user.getTotalPosts());
        identity.put("createdAt", user.getCreatedAt());

        return identity;
    }

    public void updateUserType(String userId, User.UserType newType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setUserType(newType);
        userRepository.save(user);
    }

    public void promoteToAdmin(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setRole(User.Role.ADMIN);
        userRepository.save(user);
    }

    public Page<Map<String, Object>> getAllUsers(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size))
                .map(user -> {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("id", user.getId());
                    userData.put("email", user.getEmail());
                    userData.put("anonymousName", user.getAnonymousName());
                    userData.put("avatar", user.getAvatar());
                    userData.put("role", user.getRole().name());
                    userData.put("userType", user.getUserType().name());
                    userData.put("isPremium", user.isPremium());
                    userData.put("isBanned", user.isBanned());
                    userData.put("totalPosts", user.getTotalPosts());
                    userData.put("createdAt", user.getCreatedAt());
                    return userData;
                });
    }
}
