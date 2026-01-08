package com.anonboard.dto.request;

import com.anonboard.model.Report;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportRequest {

    @NotNull(message = "Reason is required")
    private Report.ReportReason reason;

    private String description;
}
