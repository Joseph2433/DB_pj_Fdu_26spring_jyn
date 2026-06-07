package com.fudan.lab5.user;

import com.fudan.lab5.common.ApiResponse;
import com.fudan.lab5.common.CurrentSession;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final CurrentSession currentSession;

    public UserController(UserService userService, CurrentSession currentSession) {
        this.userService = userService;
        this.currentSession = currentSession;
    }

    @GetMapping("/me")
    public ApiResponse<UserProfile> me(HttpSession session) {
        return ApiResponse.ok(userService.profile(currentSession.requireUserId(session)));
    }

    @PutMapping("/me")
    public ApiResponse<UserProfile> updateMe(@RequestBody ProfileUpdateRequest request, HttpSession session) {
        return ApiResponse.ok(userService.updateProfile(currentSession.requireUserId(session), request));
    }
}
