package com.anonboard.controller;

import com.anonboard.dto.response.ApiResponse;
import com.anonboard.dto.response.ReportResponse;
import com.anonboard.model.Report;
import com.anonboard.model.User;
import com.anonboard.service.AdminService;
import com.anonboard.service.AuthService;
import com.anonboard.service.ReportService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final ReportService reportService;
    private final AuthService authService;

    public AdminController(AdminService adminService, ReportService reportService, AuthService authService) {
        this.adminService = adminService;
        this.reportService = reportService;
        this.authService = authService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<Page<ReportResponse>>> getReports(
            @RequestParam(required = false) Report.ReportStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<ReportResponse> reports = reportService.getReports(status, page, size);
        return ResponseEntity.ok(ApiResponse.success(reports));
    }

    @PutMapping("/reports/{id}/resolve")
    public ResponseEntity<ApiResponse<Void>> resolveReport(
            @PathVariable String id,
            @RequestParam Report.ReportStatus status,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getUserByEmail(userDetails.getUsername());
        reportService.resolveReport(id, user.getId(), notes, status);
        return ResponseEntity.ok(ApiResponse.success(null, "Report updated"));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<Map<String, Object>>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Map<String, Object>> users = adminService.getAllUsers(page, size);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserDetails(@PathVariable String id) {
        Map<String, Object> identity = adminService.getUserRealIdentity(id);
        return ResponseEntity.ok(ApiResponse.success(identity));
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<Void>> removePost(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getUserByEmail(userDetails.getUsername());
        adminService.removePost(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Post removed"));
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<Void>> removeComment(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getUserByEmail(userDetails.getUsername());
        adminService.removeComment(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Comment removed"));
    }

    @PostMapping("/users/{id}/ban")
    public ResponseEntity<ApiResponse<Void>> banUser(
            @PathVariable String id,
            @RequestParam String reason,
            @RequestParam(required = false) Integer durationDays) {

        adminService.banUser(id, reason, durationDays);
        return ResponseEntity.ok(ApiResponse.success(null, "User banned"));
    }

    @PostMapping("/users/{id}/unban")
    public ResponseEntity<ApiResponse<Void>> unbanUser(@PathVariable String id) {
        adminService.unbanUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User unbanned"));
    }

    @PutMapping("/users/{id}/type")
    public ResponseEntity<ApiResponse<Void>> updateUserType(
            @PathVariable String id,
            @RequestParam User.UserType userType) {

        adminService.updateUserType(id, userType);
        return ResponseEntity.ok(ApiResponse.success(null, "User type updated to " + userType));
    }
}
