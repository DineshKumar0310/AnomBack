package com.anonboard.service;

import com.anonboard.dto.request.CreateCommentRequest;
import com.anonboard.dto.response.CommentResponse;
import com.anonboard.exception.ForbiddenException;
import com.anonboard.exception.NotFoundException;
import com.anonboard.model.Comment;
import com.anonboard.model.Vote;
import com.anonboard.repository.CommentRepository;
import com.anonboard.repository.PostRepository;
import com.anonboard.repository.VoteRepository;
import com.anonboard.repository.UserRepository;
import com.anonboard.model.User;
import com.anonboard.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final VoteRepository voteRepository;

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Value("${app.comment.edit-window-minutes:10}")
    private int editWindowMinutes;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository,
            VoteRepository voteRepository, NotificationService notificationService, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.voteRepository = voteRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    public CommentResponse createComment(String postId, CreateCommentRequest request, String userId,
            String anonymousName, String avatar) {
        com.anonboard.model.Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        String parentId = request.getParentId();

        if (parentId != null) {
            Comment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new NotFoundException("Parent comment not found"));

            if (parent.getParentId() != null) {
                throw new ForbiddenException("Only single-level replies are allowed");
            }

            commentRepository.incrementReplyCount(parentId, 1);
        }

        Comment comment = Comment.builder()
                .postId(postId)
                .parentId(parentId)
                .authorId(userId)
                .authorAnonymousName(anonymousName)
                .authorAvatar(avatar)
                .content(request.getContent())
                .editableUntil(Instant.now().plus(editWindowMinutes, ChronoUnit.MINUTES))
                .build();

        comment = commentRepository.save(comment);

        if (parentId == null) {
            postRepository.incrementCommentCount(postId, 1);

            // Notify post author
            User actor = userRepository.findById(userId).orElseThrow();
            notificationService.createNotification(
                    post.getAuthorId(),
                    com.anonboard.model.Notification.NotificationType.POST_COMMENT,
                    "commented on your post: " + truncate(post.getTitle(), 30),
                    "/post/" + postId,
                    actor);
        } else {
            // Notify parent comment author
            commentRepository.findById(parentId).ifPresent(parent -> {
                User actor = userRepository.findById(userId).orElseThrow();
                notificationService.createNotification(
                        parent.getAuthorId(),
                        com.anonboard.model.Notification.NotificationType.COMMENT_REPLY,
                        "replied to your comment",
                        "/post/" + postId,
                        actor);
            });
        }

        return toCommentResponse(comment, userId);
    }

    private String truncate(String str, int length) {
        if (str == null)
            return "";
        return str.length() > length ? str.substring(0, length) + "..." : str;
    }

    // Returns ONLY top-level comments - replies are NOT loaded by default
    public Page<CommentResponse> getComments(String postId, String sortType, int page, int size, String userId) {
        Pageable pageable;

        if ("latest".equalsIgnoreCase(sortType)) {
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        } else {
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "voteCount"));
        }

        // Get ONLY top-level comments - do NOT load replies
        Page<Comment> comments = commentRepository.findByPostIdAndParentIdIsNullAndIsDeletedFalse(postId, pageable);

        return comments.map(comment -> toCommentResponse(comment, userId));
    }

    // Separate endpoint to fetch replies for a specific comment
    public List<CommentResponse> getReplies(String commentId, String userId) {
        return commentRepository.findByParentIdAndIsDeletedFalse(commentId, Sort.by(Sort.Direction.ASC, "createdAt"))
                .stream()
                .map(reply -> toCommentResponse(reply, userId))
                .collect(Collectors.toList());
    }

    public CommentResponse editComment(String commentId, CreateCommentRequest request, String userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (!comment.getAuthorId().equals(userId)) {
            throw new ForbiddenException("You can only edit your own comments");
        }

        // Strict 10-minute edit window
        if (Instant.now().isAfter(comment.getEditableUntil())) {
            throw new ForbiddenException(
                    "Edit window has expired. Comments can only be edited within " + editWindowMinutes + " minutes.");
        }

        comment.setContent(request.getContent());
        comment.setEdited(true);
        comment = commentRepository.save(comment);

        return toCommentResponse(comment, userId);
    }

    public void deleteComment(String commentId, String userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (!comment.getAuthorId().equals(userId)) {
            throw new ForbiddenException("You can only delete your own comments");
        }

        comment.setDeleted(true);
        commentRepository.save(comment);

        if (comment.getParentId() == null) {
            // Top-level comment - decrement post's comment count
            postRepository.incrementCommentCount(comment.getPostId(), -1);
        } else {
            // Reply - decrement parent's reply count
            commentRepository.incrementReplyCount(comment.getParentId(), -1);
        }
    }

    private CommentResponse toCommentResponse(Comment comment, String userId) {
        Instant now = Instant.now();
        boolean isAuthor = comment.getAuthorId().equals(userId);
        boolean canEdit = isAuthor && now.isBefore(comment.getEditableUntil());
        long editTimeRemaining = canEdit ? ChronoUnit.SECONDS.between(now, comment.getEditableUntil()) : 0;

        Integer userVote = voteRepository
                .findByUserIdAndTargetTypeAndTargetId(userId, Vote.TargetType.COMMENT, comment.getId())
                .map(Vote::getVoteType)
                .orElse(null);

        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .parentId(comment.getParentId())
                .authorAnonymousName(comment.getAuthorAnonymousName())
                .authorAvatar(comment.getAuthorAvatar())
                .content(comment.getContent())
                .voteCount(comment.getVoteCount())
                .userVote(userVote)
                .replyCount(comment.getReplyCount())
                .isEdited(comment.isEdited())
                .canEdit(canEdit)
                .editTimeRemainingSeconds(editTimeRemaining)
                .isAuthor(isAuthor)
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
