package com.anonboard.repository;

import com.anonboard.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends MongoRepository<Report, String> {

    Page<Report> findByStatusOrderByCreatedAtDesc(Report.ReportStatus status, Pageable pageable);

    Page<Report> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Report> findByTargetTypeAndTargetId(Report.TargetType targetType, String targetId);

    List<Report> findByTargetTypeAndTargetIdAndStatus(Report.TargetType targetType, String targetId,
            Report.ReportStatus status);

    boolean existsByReporterIdAndTargetTypeAndTargetId(String reporterId, Report.TargetType targetType,
            String targetId);

    long countByStatus(Report.ReportStatus status);
}
