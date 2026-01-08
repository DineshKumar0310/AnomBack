package com.anonboard.service;

import com.anonboard.dto.request.CreatePostRequest;
import com.anonboard.dto.request.EditPostRequest;
import com.anonboard.dto.response.PostResponse;
import com.anonboard.exception.ForbiddenException;
import com.anonboard.exception.NotFoundException;
import com.anonboard.model.Post;
import com.anonboard.model.PostView;
import com.anonboard.model.User;
import com.anonboard.repository.PostRepository;
import com.anonboard.repository.PostViewRepository;
import com.anonboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostViewRepository postViewRepository;

    @Value("${app.post.edit-window-minutes:10}")
    private int editWindowMinutes;

    @Value("${app.free-user.post-limit:5}")
    private int freePostLimit;

    public PostService(PostRepository postRepository, UserRepository userRepository,
            PostViewRepository postViewRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postViewRepository = postViewRepository;
    }

    public PostResponse createPost(CreatePostRequest request, String imageUrl, String userId, String anonymousName,
            String avatar) {
        User user = userRepository.findById(userId).orElseThrow();

        if (user.isBanned()) {
            throw new ForbiddenException("Your account has been banned");
        }

        if (!user.isPremium() && user.getTotalPosts() >= freePostLimit) {
            throw new ForbiddenException("Free users can only create " + freePostLimit +
                    " posts. Upgrade to premium for unlimited posts.");
        }

        List<String> tags = request.getTags().stream()
                .map(tag -> tag.toLowerCase().trim())
                .filter(tag -> !tag.isEmpty())
                .distinct()
                .limit(5)
                .collect(Collectors.toList());

        Post post = Post.builder()
                .authorId(userId)
                .authorAnonymousName(anonymousName)
                .authorAvatar(avatar)
                .title(request.getTitle())
                .content(request.getContent())
                .tags(tags)
                .imageUrl(imageUrl)
                .editableUntil(Instant.now().plus(editWindowMinutes, ChronoUnit.MINUTES))
                .build();

        post = postRepository.save(post);

        user.setTotalPosts(user.getTotalPosts() + 1);
        userRepository.save(user);

        return toPostResponse(post, userId);
    }

    public Page<PostResponse> getPosts(String tag, String sort, int page, int size, String userId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts;

        if (tag != null && !tag.isEmpty()) {
            String normalizedTag = tag.toLowerCase().trim();
            if ("trending".equalsIgnoreCase(sort) || "top".equalsIgnoreCase(sort)) {
                posts = postRepository.findByTagsContainingAndIsDeletedFalseOrderByViewCountDesc(normalizedTag,
                        pageable);
            } else {
                posts = postRepository.findByTagsContainingAndIsDeletedFalseOrderByCreatedAtDesc(normalizedTag,
                        pageable);
            }
        } else {
            if ("trending".equalsIgnoreCase(sort) || "top".equalsIgnoreCase(sort)) {
                posts = postRepository.findByIsDeletedFalseOrderByViewCountDesc(pageable);
            } else {
                posts = postRepository.findByIsDeletedFalseOrderByCreatedAtDesc(pageable);
            }
        }

        return posts.map(post -> toPostResponse(post, userId));
    }

    public Page<PostResponse> getTrendingPosts(int page, int size, String userId) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findByIsDeletedFalseOrderByViewCountDesc(pageable)
                .map(post -> toPostResponse(post, userId));
    }

    public Page<PostResponse> searchPosts(String query, int page, int size, String userId) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.searchPosts(query, pageable)
                .map(post -> toPostResponse(post, userId));
    }

    public PostResponse getPostById(String postId, String userId) {
        Post post = postRepository.findById(postId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new NotFoundException("Post not found"));

        // UNIQUE VIEW: Only increment if this user hasn't viewed this post before
        if (!postViewRepository.existsByUserIdAndPostId(userId, postId)) {
            postViewRepository.save(PostView.builder()
                    .userId(userId)
                    .postId(postId)
                    .build());
            postRepository.incrementViewCount(postId);
            post.setViewCount(post.getViewCount() + 1);
        }

        return toPostResponse(post, userId);
    }

    public void incrementShareCount(String postId) {
        postRepository.incrementShareCount(postId);
    }

    public PostResponse editPost(String postId, EditPostRequest request, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        if (!post.getAuthorId().equals(userId)) {
            throw new ForbiddenException("You can only edit your own posts");
        }

        // Strict 10-minute edit window
        if (Instant.now().isAfter(post.getEditableUntil())) {
            throw new ForbiddenException(
                    "Edit window has expired. Posts can only be edited within " + editWindowMinutes + " minutes.");
        }

        post.setContent(request.getContent());
        post.setEdited(true);
        post = postRepository.save(post);

        return toPostResponse(post, userId);
    }

    public void deletePost(String postId, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        if (!post.getAuthorId().equals(userId)) {
            throw new ForbiddenException("You can only delete your own posts");
        }

        post.setDeleted(true);
        postRepository.save(post);
    }

    public Page<PostResponse> getMyPosts(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findByAuthorIdAndIsDeletedFalseOrderByCreatedAtDesc(userId, pageable)
                .map(post -> toPostResponse(post, userId));
    }

    public Post getPostEntity(String postId) {
        return postRepository.findById(postId).orElse(null);
    }

    private PostResponse toPostResponse(Post post, String userId) {
        Instant now = Instant.now();
        boolean isAuthor = post.getAuthorId().equals(userId);
        boolean canEdit = isAuthor && now.isBefore(post.getEditableUntil());
        long editTimeRemaining = canEdit ? ChronoUnit.SECONDS.between(now, post.getEditableUntil()) : 0;

        return PostResponse.builder()
                .id(post.getId())
                .authorAnonymousName(post.getAuthorAnonymousName())
                .authorAvatar(post.getAuthorAvatar())
                .title(post.getTitle())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .tags(post.getTags())
                .viewCount(post.getViewCount())
                .shareCount(post.getShareCount())
                .commentCount(post.getCommentCount())
                .isEdited(post.isEdited())
                .canEdit(canEdit)
                .editTimeRemainingSeconds(editTimeRemaining)
                .isAuthor(isAuthor)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
