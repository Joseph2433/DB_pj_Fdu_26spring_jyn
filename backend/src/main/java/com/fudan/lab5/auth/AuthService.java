package com.fudan.lab5.auth;

import com.fudan.lab5.common.PasswordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final AuthMapper authMapper;
    private final PasswordService passwordService;

    public AuthService(AuthMapper authMapper, PasswordService passwordService) {
        this.authMapper = authMapper;
        this.passwordService = passwordService;
    }

    @Transactional
    public void register(RegisterRequest request) {
        String username = requireText(request.username(), "用户名不能为空");
        String displayName = requireText(request.displayName(), "姓名不能为空");
        if (authMapper.selectUserByUsername(username) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        authMapper.insertUser(username, passwordService.hash(request.password()), displayName);
    }

    public LoginResponse login(LoginRequest request) {
        UserAccount account = authMapper.selectUserByUsername(requireText(request.username(), "用户名不能为空"));
        if (account == null || !"ACTIVE".equals(account.status()) || !passwordService.matches(request.password(), account.passwordHash())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        return new LoginResponse(account.id(), account.username(), account.displayName(), "USER");
    }

    public LoginResponse adminLogin(LoginRequest request) {
        AdminAccount account = authMapper.selectAdminByUsername(requireText(request.username(), "管理员用户名不能为空"));
        if (account == null || !passwordService.matches(request.password(), account.passwordHash())) {
            throw new IllegalArgumentException("管理员用户名或密码错误");
        }
        return new LoginResponse(account.id(), account.username(), account.displayName(), "ADMIN");
    }

    private String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
