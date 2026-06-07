package com.fudan.lab5.post;

import java.time.LocalDateTime;

public record PostRow(long id, long authorId, String authorUsername, String content, LocalDateTime lastUpdatedAt) {
}
