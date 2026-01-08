package com.anonboard.controller;

import com.anonboard.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping({ "/", "/health", "/api/health" })
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> status = Map.of(
                "status", "UP",
                "service", "AnonBoard API",
                "timestamp", Instant.now().toString());
        return ResponseEntity.ok(ApiResponse.success(status));
    }
}
