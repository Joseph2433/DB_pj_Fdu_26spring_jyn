package com.fudan.lab5.admin;

import java.time.LocalDateTime;

public record AdminAuditLog(long id, Long postId, Long adminId, String adminUsername, String action, String reason,
                            LocalDateTime createdAt) {
}
