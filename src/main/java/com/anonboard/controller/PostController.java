package com.anonboard.controller;

import com.anonboard.dto.request.CreatePostRequest;
import com.anonboard.dto.request.EditPostRequest;
import com.anonboard.dto.request.ReportRequest;
import com.anonboard.dto.response.ApiResponse;
import com.anonboard.dto.response.PostResponse;
import com.anonboard.model.User;
import com.anonboard.service.AuthService;
import com.anonboard.service.ImageService;
import com.anonboard.service.PostService;
import com.anonboard.service.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final ReportService reportService;
    private final AuthService authService;
    private final ImageService imageService;
    private final ObjectMapper objectMapper;

    public PostController(PostService postService, ReportService reportService,
            AuthService authService, ImageService imageService) {
        this.postService = postService;
        this.reportService = reportService;
        this.authService = authService;
        this.imageService = imageService;
        this.objectMapper = new ObjectMapper();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getPosts(
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getUserByEmail(userDetails.getUsername());
        Page<PostResponse> posts = postService.getPosts(tag, sort, page, size, user.getId());
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getTrendingPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getUserByEmail(userDetails.getUsername());
        Page<PostResponse> posts = postService.getTrendingPosts(page, size, user.getId());
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> searchPosts(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getUserByEmail(userDetails.getUsername());
        Page<PostResponse> posts = postService.searchPosts(q, page, size, user.getId());
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getUserByEmail(userDetails.getUsername());
        PostResponse post = postService.getPostById(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success(post));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @RequestPart("post") String postJson,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        CreatePostRequest request = objectMapper.readValue(postJson, CreatePostRequest.class);
        User user = authService.getUserByEmail(userDetails.getUsername());

        String imageUrl = request.getImageUrl();
        if (image != null && !image.isEmpty()) {
            String uploadedUrl = imageService.uploadImage(image);
            if (uploadedUrl != null) {
                imageUrl = uploadedUrl;
            }
        }

        PostResponse post = postService.createPost(request, imageUrl, user.getId(), user.getAnonymousName(),
                user.getAvatar());
        return ResponseEntity.ok(ApiResponse.success(post, "Post created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> editPost(
            @PathVariable String id,
            @Valid @RequestBody EditPostRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getUserByEmail(userDetails.getUsername());
        PostResponse post = postService.editPost(id, request, user.getId());
        return ResponseEntity.ok(ApiResponse.success(post, "Post updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getUserByEmail(userDetails.getUsername());
        postService.deletePost(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Post deleted successfully"));
    }

    @GetMapping("/my-posts")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getUserByEmail(userDetails.getUsername());
        Page<PostResponse> posts = postService.getMyPosts(user.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<ApiResponse<Void>> sharePost(@PathVariable String id) {
        postService.incrementShareCount(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Share count updated"));
    }

    @PostMapping("/{id}/report")
    public ResponseEntity<ApiResponse<Void>> reportPost(
            @PathVariable String id,
            @Valid @RequestBody ReportRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getUserByEmail(userDetails.getUsername());
        reportService.reportPost(id, request, user.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Report submitted successfully"));
    }
}
