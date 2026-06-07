package com.fudan.lab5.auth;

public record AdminAccount(long id, String username, String passwordHash, String displayName) {
}
