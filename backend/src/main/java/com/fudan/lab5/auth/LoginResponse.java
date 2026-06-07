package com.fudan.lab5.auth;

public record LoginResponse(long id, String username, String displayName, String role) {
}
