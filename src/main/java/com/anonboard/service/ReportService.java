package com.anonboard.service;

import com.anonboard.dto.request.ReportRequest;
import com.anonboard.dto.response.ReportResponse;
import com.anonboard.exception.BadRequestException;
import com.anonboard.exception.NotFoundException;
import com.anonboard.model.Comment;
import com.anonboard.model.Post;
import com.anonboard.model.Report;
import com.anonboard.repository.CommentRepository;
import com.anonboard.repository.PostRepository;
import com.anonboard.repository.ReportRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public ReportService(ReportRepository reportRepository, PostRepository postRepository,
            CommentRepository commentRepository) {
        this.reportRepository = reportRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    public void reportPost(String postId, ReportRequest request, String reporterId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        if (reportRepository.existsByReporterIdAndTargetTypeAndTargetId(reporterId, Report.TargetType.POST, postId)) {
            throw new BadRequestException("You have already reported this post");
        }

        Report report = Report.builder()
                .reporterId(reporterId)
                .targetType(Report.TargetType.POST)
                .targetId(postId)
                .reason(request.getReason())
                .description(request.getDescription())
                .status(Report.ReportStatus.PENDING)
                .contentSnapshot(post.getTitle() + "\n\n" + post.getContent())
                .authorAnonymousName(post.getAuthorAnonymousName())
                .build();

        reportRepository.save(report);
    }

    public void reportComment(String commentId, ReportRequest request, String reporterId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (reportRepository.existsByReporterIdAndTargetTypeAndTargetId(reporterId, Report.TargetType.COMMENT,
                commentId)) {
            throw new BadRequestException("You have already reported this comment");
        }

        Report report = Report.builder()
                .reporterId(reporterId)
                .targetType(Report.TargetType.COMMENT)
                .targetId(commentId)
                .reason(request.getReason())
                .description(request.getDescription())
                .status(Report.ReportStatus.PENDING)
                .contentSnapshot(comment.getContent())
                .authorAnonymousName(comment.getAuthorAnonymousName())
                .build();

        reportRepository.save(report);
    }

    public Page<ReportResponse> getReports(Report.ReportStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Report> reports;

        if (status != null) {
            reports = reportRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            reports = reportRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        return reports.map(this::toReportResponse);
    }

    public Page<ReportResponse> getPendingReports(int page, int size) {
        return getReports(Report.ReportStatus.PENDING, page, size);
    }

    public long getPendingReportCount() {
        return reportRepository.countByStatus(Report.ReportStatus.PENDING);
    }

    public void resolveReport(String reportId, String adminId, String notes, Report.ReportStatus newStatus) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Report not found"));

        report.setStatus(newStatus);
        report.setReviewedBy(adminId);
        report.setReviewNotes(notes);
        report.setReviewedAt(Instant.now());

        reportRepository.save(report);
    }

    // Resolve ALL reports for a specific target (post or comment)
    public void resolveAllReportsForTarget(Report.TargetType targetType, String targetId, String adminId,
            Report.ReportStatus newStatus) {
        List<Report> reports = reportRepository.findByTargetTypeAndTargetIdAndStatus(
                targetType, targetId, Report.ReportStatus.PENDING);

        Instant now = Instant.now();
        for (Report report : reports) {
            report.setStatus(newStatus);
            report.setReviewedBy(adminId);
            report.setReviewNotes("Bulk resolved - content removed");
            report.setReviewedAt(now);
        }

        reportRepository.saveAll(reports);
    }

    private ReportResponse toReportResponse(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .reason(report.getReason())
                .description(report.getDescription())
                .status(report.getStatus())
                .contentSnapshot(report.getContentSnapshot())
                .authorAnonymousName(report.getAuthorAnonymousName())
                .reviewedBy(report.getReviewedBy())
                .reviewNotes(report.getReviewNotes())
                .reviewedAt(report.getReviewedAt())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
