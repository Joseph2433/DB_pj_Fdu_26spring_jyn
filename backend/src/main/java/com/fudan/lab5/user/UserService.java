package com.fudan.lab5.user;

import com.fudan.lab5.common.PasswordService;
import com.fudan.lab5.common.PasswordUpdateRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final PasswordService passwordService;

    public UserService(UserMapper userMapper, PasswordService passwordService) {
        this.userMapper = userMapper;
        this.passwordService = passwordService;
    }

    public UserProfile profile(long userId) {
        UserProfile profile = userMapper.selectProfile(userId);
        if (profile == null) {
            throw new IllegalStateException("用户不存在或已注销");
        }
        return profile;
    }

    public UserProfile updateProfile(long userId, ProfileUpdateRequest request) {
        String displayName = request.displayName() == null || request.displayName().trim().isEmpty()
            ? profile(userId).displayName()
            : request.displayName().trim();
        if (request.age() != null && (request.age() < 0 || request.age() > 150)) {
            throw new IllegalArgumentException("年龄必须在 0 到 150 之间");
        }
        userMapper.updateProfile(userId, displayName, request.gender(), request.birthday(), request.age());
        return profile(userId);
    }

    public List<UserSearchResult> search(long userId, String keyword) {
        String normalized = keyword == null || keyword.trim().isEmpty() ? "" : keyword.trim();
        return userMapper.searchUsers(userId, normalized);
    }

    public void updatePassword(long userId, PasswordUpdateRequest request) {
        if (request == null || request.currentPassword() == null || request.currentPassword().isBlank()) {
            throw new IllegalArgumentException("当前密码不能为空");
        }
        if (request.newPassword() == null || !request.newPassword().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("两次输入的新密码不一致");
        }
        String currentHash = userMapper.selectPasswordHash(userId);
        if (!passwordService.matches(request.currentPassword(), currentHash)) {
            throw new IllegalArgumentException("当前密码错误");
        }
        String newHash = passwordService.hash(request.newPassword());
        if (userMapper.updatePasswordHash(userId, newHash) == 0) {
            throw new IllegalArgumentException("用户不存在或已注销");
        }
    }
}
