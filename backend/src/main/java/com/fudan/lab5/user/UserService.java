package com.fudan.lab5.user;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
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
}
