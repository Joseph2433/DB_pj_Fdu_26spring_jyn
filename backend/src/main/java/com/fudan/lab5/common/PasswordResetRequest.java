package com.fudan.lab5.common;

public record PasswordResetRequest(String username, String newPassword, String confirmPassword) {
}
