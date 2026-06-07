package com.fudan.lab5.auth;

public record UserAccount(long id, String username, String passwordHash, String displayName, String status) {
}
