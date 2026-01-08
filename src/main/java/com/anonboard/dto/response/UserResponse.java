package com.anonboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String anonymousName;
    private String avatar;
    private String role;
    private String userType; // FREE or PREMIUM
    private boolean isPremium;
    private int totalPosts;
    private int postsRemaining; // -1 for unlimited (premium)
    private int freePostLimit;
}
