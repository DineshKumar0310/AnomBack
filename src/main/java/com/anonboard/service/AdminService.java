package com.anonboard.service;

import com.anonboard.exception.NotFoundException;
import com.anonboard.model.Comment;
import com.anonboard.model.Post;
import com.anonboard.model.Report;
import com.anonboard.model.User;
import com.anonboard.repository.CommentRepository;
import com.anonboard.repository.PostRepository;
import com.anonboard.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReportService reportService;

    public AdminService(UserRepository userRepository, PostRepository postRepository,
            CommentRepository commentRepository, ReportService reportService) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.reportService = reportService;
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalPosts", postRepository.countByIsDeletedFalse());
        stats.put("totalComments", commentRepository.countByIsDeletedFalse());
        stats.put("pendingReports", reportService.getPendingReportCount());
        stats.put("premiumUsers", userRepository.countByUserType(User.UserType.PREMIUM));
        stats.put("bannedUsers", userRepository.countByIsBannedTrue());
        return stats;
    }

    public Page<Map<String, Object>> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable).map(this::mapUserToAdminView);
    }

    public Map<String, Object> getUserRealIdentity(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Map<String, Object> identity = new HashMap<>();
        identity.put("id", user.getId());
        identity.put("email", user.getEmail());
        identity.put("anonymousName", user.getAnonymousName());
        identity.put("createdAt", user.getCreatedAt());
        identity.put("role", user.getRole());
        identity.put("isBanned", user.isBanned());

        return identity;
    }

    @Transactional
    public void removePost(String postId, String adminId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        post.setDeleted(true);
        postRepository.save(post);

        reportService.resolveAllReportsForTarget(Report.TargetType.POST, postId, adminId, Report.ReportStatus.RESOLVED);
    }

    @Transactional
    public void removeComment(String commentId, String adminId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        comment.setDeleted(true);
        commentRepository.save(comment);

        reportService.resolveAllReportsForTarget(Report.TargetType.COMMENT, commentId, adminId,
                Report.ReportStatus.RESOLVED);
    }

    public void banUser(String userId, String reason, Integer durationDays) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setBanned(true);
        user.setBanReason(reason);
        if (durationDays != null && durationDays > 0) {
            user.setBannedUntil(Instant.now().plus(durationDays, ChronoUnit.DAYS));
        } else {
            user.setBannedUntil(null); // Permanent
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

    public void updateUserType(String userId, User.UserType userType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setUserType(userType);
        userRepository.save(user);
    }

    private Map<String, Object> mapUserToAdminView(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("anonymousName", user.getAnonymousName());
        map.put("role", user.getRole());
        map.put("userType", user.getUserType());
        map.put("isBanned", user.isBanned());
        map.put("totalPosts", user.getTotalPosts());
        map.put("createdAt", user.getCreatedAt());
        return map;
    }
}
