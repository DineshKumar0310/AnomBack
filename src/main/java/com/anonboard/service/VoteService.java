package com.anonboard.service;

import com.anonboard.exception.BadRequestException;
import com.anonboard.model.Vote;
import com.anonboard.repository.CommentRepository;
import com.anonboard.repository.VoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class VoteService {

    private final VoteRepository voteRepository;
    private final CommentRepository commentRepository;

    public VoteService(VoteRepository voteRepository, CommentRepository commentRepository) {
        this.voteRepository = voteRepository;
        this.commentRepository = commentRepository;
    }

    // Voting is now only for comments, not posts
    @Transactional
    public VoteResult voteOnComment(String commentId, int voteType, String userId) {
        if (voteType != 1 && voteType != -1) {
            throw new BadRequestException("Vote type must be 1 (upvote) or -1 (downvote)");
        }

        if (!commentRepository.existsById(commentId)) {
            throw new BadRequestException("Comment not found");
        }

        Optional<Vote> existingVote = voteRepository.findByUserIdAndTargetTypeAndTargetId(
                userId, Vote.TargetType.COMMENT, commentId);

        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();

            if (vote.getVoteType() == voteType) {
                // Same vote again = remove vote
                voteRepository.delete(vote);
                commentRepository.incrementVoteCount(commentId, -voteType);
                return new VoteResult(VoteAction.REMOVED, null);
            } else {
                // Opposite vote = change vote (delta of 2)
                vote.setVoteType(voteType);
                voteRepository.save(vote);
                commentRepository.incrementVoteCount(commentId, voteType * 2);
                return new VoteResult(VoteAction.CHANGED, voteType);
            }
        } else {
            // New vote
            Vote newVote = Vote.builder()
                    .userId(userId)
                    .targetType(Vote.TargetType.COMMENT)
                    .targetId(commentId)
                    .voteType(voteType)
                    .build();
            voteRepository.save(newVote);
            commentRepository.incrementVoteCount(commentId, voteType);
            return new VoteResult(VoteAction.ADDED, voteType);
        }
    }

    @Transactional
    public void removeCommentVote(String commentId, String userId) {
        Optional<Vote> existingVote = voteRepository.findByUserIdAndTargetTypeAndTargetId(
                userId, Vote.TargetType.COMMENT, commentId);

        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();
            voteRepository.delete(vote);
            commentRepository.incrementVoteCount(commentId, -vote.getVoteType());
        }
    }

    public enum VoteAction {
        ADDED, CHANGED, REMOVED
    }

    public record VoteResult(VoteAction action, Integer currentVote) {
    }
}
