package com.fudan.lab5.admin;

import com.fudan.lab5.common.ApiResponse;
import com.fudan.lab5.common.CurrentSession;
import com.fudan.lab5.common.PasswordUpdateRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;
    private final CurrentSession currentSession;

    public AdminController(AdminService adminService, CurrentSession currentSession) {
        this.adminService = adminService;
        this.currentSession = currentSession;
    }

    @GetMapping("/me")
    public ApiResponse<AdminProfile> me(HttpSession session) {
        return ApiResponse.ok(adminService.profile(currentSession.requireAdminId(session)));
    }

    @PutMapping("/me")
    public ApiResponse<AdminProfile> updateMe(@RequestBody AdminProfileUpdateRequest request, HttpSession session) {
        return ApiResponse.ok(adminService.updateProfile(currentSession.requireAdminId(session), request));
    }

    @PutMapping("/me/password")
    public ApiResponse<Void> updatePassword(@RequestBody PasswordUpdateRequest request, HttpSession session) {
        adminService.updatePassword(currentSession.requireAdminId(session), request);
        return ApiResponse.ok(null);
    }

    @GetMapping("/posts")
    public ApiResponse<List<AdminPostReview>> posts(HttpSession session) {
        currentSession.requireAdminId(session);
        return ApiResponse.ok(adminService.reviewPosts());
    }

    @GetMapping("/audit-logs")
    public ApiResponse<List<AdminAuditLog>> auditLogs(HttpSession session) {
        currentSession.requireAdminId(session);
        return ApiResponse.ok(adminService.auditLogs());
    }

    @DeleteMapping("/posts/{postId}")
    public ApiResponse<Void> deletePost(@PathVariable long postId, HttpSession session) {
        adminService.deletePost(currentSession.requireAdminId(session), postId);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/users/{userId}")
    public ApiResponse<Void> deleteUser(@PathVariable long userId, HttpSession session) {
        adminService.deleteUser(currentSession.requireAdminId(session), userId);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/users/by-username")
    public ApiResponse<Void> deleteUserByUsername(@RequestParam String username, HttpSession session) {
        adminService.deleteUserByUsername(currentSession.requireAdminId(session), username);
        return ApiResponse.ok(null);
    }
}
