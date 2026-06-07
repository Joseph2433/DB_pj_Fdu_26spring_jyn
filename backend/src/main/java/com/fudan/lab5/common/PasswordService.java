package com.fudan.lab5.common;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class PasswordService {
    public String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < 4) {
            throw new IllegalArgumentException("密码至少需要 4 位");
        }
        return "{sha256}" + sha256(rawPassword);
    }

    public boolean matches(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }
        if (storedHash.startsWith("{noop}")) {
            return storedHash.substring("{noop}".length()).equals(rawPassword);
        }
        if (storedHash.startsWith("{sha256}")) {
            return storedHash.equals(hash(rawPassword));
        }
        return false;
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 不可用", ex);
        }
    }
}
