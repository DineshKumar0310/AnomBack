package com.anonboard.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreatePostRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 300, message = "Title must be between 5 and 300 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(min = 10, max = 10000, message = "Content must be between 10 and 10000 characters")
    private String content;

    // Custom tags (replaces fixed categories)
    private List<String> tags = new ArrayList<>();

    private String imageUrl;
}
