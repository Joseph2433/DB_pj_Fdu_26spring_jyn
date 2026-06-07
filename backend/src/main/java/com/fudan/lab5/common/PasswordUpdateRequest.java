package com.fudan.lab5.common;

public record PasswordUpdateRequest(String currentPassword, String newPassword, String confirmPassword) {
}
