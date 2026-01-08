package com.anonboard.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VoteRequest {

    @NotNull(message = "Vote type is required")
    @Min(value = -1, message = "Vote type must be -1 or 1")
    @Max(value = 1, message = "Vote type must be -1 or 1")
    private Integer voteType; // 1 = upvote, -1 = downvote
}
