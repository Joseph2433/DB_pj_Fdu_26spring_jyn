package com.fudan.lab5.post;

import java.time.LocalDateTime;

public record CommentSummary(long id, long authorId, String content, LocalDateTime createdAt) {
}
