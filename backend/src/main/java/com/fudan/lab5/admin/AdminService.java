package com.fudan.lab5.admin;

import com.fudan.lab5.common.PasswordService;
import com.fudan.lab5.common.PasswordUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {
    private final AdminMapper adminMapper;
    private final PasswordService passwordService;

    public AdminService(AdminMapper adminMapper, PasswordService passwordService) {
        this.adminMapper = adminMapper;
        this.passwordService = passwordService;
    }

    public List<AdminPostReview> reviewPosts() {
        return adminMapper.selectReviewPostRows().stream()
            .map(row -> new AdminPostReview(
                row.postId(),
                row.authorId(),
                row.authorUsername(),
                row.content(),
                row.status(),
                row.createdAt(),
                row.lastUpdatedAt(),
                row.commentCount(),
                adminMapper.selectComments(row.postId())
            ))
            .toList();
    }

    public AdminProfile profile(long adminId) {
        AdminProfile profile = adminMapper.selectProfile(adminId);
        if (profile == null) {
            throw new IllegalStateException("管理员不存在");
        }
        return profile;
    }

    @Transactional
    public AdminProfile updateProfile(long adminId, AdminProfileUpdateRequest request) {
        if (request.displayName() == null || request.displayName().trim().isEmpty()) {
            throw new IllegalArgumentException("管理员姓名不能为空");
        }
        adminMapper.updateProfile(adminId, request.displayName().trim());
        return profile(adminId);
    }

    @Transactional
    public void deletePost(long adminId, long postId) {
        adminMapper.insertAuditLog(postId, adminId, "ADMIN_DELETE_POST", "管理员审核删除朋友圈");
        if (adminMapper.deletePost(postId) == 0) {
            throw new IllegalArgumentException("朋友圈不存在");
        }
    }

    @Transactional
    public void deleteUser(long adminId, long userId) {
        adminMapper.insertAuditLog(null, adminId, "ADMIN_DELETE_USER", "管理员注销用户 " + userId);
        if (adminMapper.deleteUser(userId) == 0) {
            throw new IllegalArgumentException("用户不存在");
        }
    }

    @Transactional
    public void deleteUserByUsername(long adminId, String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户账号不能为空");
        }
        String targetUsername = username.trim();
        adminMapper.insertAuditLog(null, adminId, "ADMIN_DELETE_USER", "管理员注销用户 " + targetUsername);
        if (adminMapper.deleteUserByUsername(targetUsername) == 0) {
            throw new IllegalArgumentException("用户不存在");
        }
    }

    @Transactional
    public void updatePassword(long adminId, PasswordUpdateRequest request) {
        if (request == null || request.currentPassword() == null || request.currentPassword().isBlank()) {
            throw new IllegalArgumentException("当前密码不能为空");
        }
        if (request.newPassword() == null || !request.newPassword().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("两次输入的新密码不一致");
        }
        String currentHash = adminMapper.selectPasswordHash(adminId);
        if (!passwordService.matches(request.currentPassword(), currentHash)) {
            throw new IllegalArgumentException("当前密码错误");
        }
        if (adminMapper.updatePasswordHash(adminId, passwordService.hash(request.newPassword())) == 0) {
            throw new IllegalArgumentException("管理员不存在");
        }
    }
}
