package com.anonboard.controller;

import com.anonboard.dto.request.CreateCommentRequest;
import com.anonboard.dto.request.ReportRequest;
import com.anonboard.dto.request.VoteRequest;
import com.anonboard.dto.response.ApiResponse;
import com.anonboard.dto.response.CommentResponse;
import com.anonboard.model.User;
import com.anonboard.service.AuthService;
import com.anonboard.service.CommentService;
import com.anonboard.service.ReportService;
import com.anonboard.service.VoteService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;
    private final VoteService voteService;
    private final ReportService reportService;
    private final AuthService authService;

    public CommentController(CommentService commentService, VoteService voteService,
            ReportService reportService, AuthService authService) {
        this.commentService = commentService;
        this.voteService = voteService;
        this.reportService = reportService;
        this.authService = authService;
    }

    // Get top-level comments only
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getComments(
            @PathVariable String postId,
            @RequestParam(defaultValue = "top") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getUserByEmail(userDetails.getUsername());
        Page<CommentResponse> comments = commentService.getComments(postId, sort, page, size, user.getId());
        return ResponseEntity.ok(ApiResponse.success(comments));
    }

    // Get replies for a specific comment
    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getReplies(
            @PathVariable String commentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getUserByEmail(userDetails.getUsername());
        List<CommentResponse> replies = commentService.getReplies(commentId, user.getId());
        return ResponseEntity.ok(ApiResponse.success(replies));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable String postId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getUserByEmail(userDetails.getUsername());
        CommentResponse comment = commentService.createComment(postId, request, user.getId(), user.getAnonymousName(),
                user.getAvatar());
        return ResponseEntity.ok(ApiResponse.success(comment, "Comment added successfully"));
    }

    @PutMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<CommentResponse>> editComment(
            @PathVariable String id,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getUserByEmail(userDetails.getUsername());
        CommentResponse comment = commentService.editComment(id, request, user.getId());
        return ResponseEntity.ok(ApiResponse.success(comment, "Comment updated successfully"));
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getUserByEmail(userDetails.getUsername());
        commentService.deleteComment(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Comment deleted successfully"));
    }

    @PostMapping("/comments/{id}/vote")
    public ResponseEntity<ApiResponse<VoteService.VoteResult>> voteOnComment(
            @PathVariable String id,
            @Valid @RequestBody VoteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getUserByEmail(userDetails.getUsername());
        VoteService.VoteResult result = voteService.voteOnComment(id, request.getVoteType(), user.getId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @DeleteMapping("/comments/{id}/vote")
    public ResponseEntity<ApiResponse<Void>> removeCommentVote(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getUserByEmail(userDetails.getUsername());
        voteService.removeCommentVote(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Vote removed"));
    }

    @PostMapping("/comments/{id}/report")
    public ResponseEntity<ApiResponse<Void>> reportComment(
            @PathVariable String id,
            @Valid @RequestBody ReportRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getUserByEmail(userDetails.getUsername());
        reportService.reportComment(id, request, user.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Report submitted successfully"));
    }
}
