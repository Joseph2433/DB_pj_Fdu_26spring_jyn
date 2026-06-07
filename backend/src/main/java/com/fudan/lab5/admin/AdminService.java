package com.fudan.lab5.admin;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {
    private final AdminMapper adminMapper;

    public AdminService(AdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }

    public List<AdminPostReview> reviewPosts() {
        return adminMapper.selectReviewPosts();
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
}
