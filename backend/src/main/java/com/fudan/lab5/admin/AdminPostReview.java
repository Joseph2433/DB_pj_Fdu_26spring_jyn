package com.fudan.lab5.admin;

import java.time.LocalDateTime;

public record AdminPostReview(long postId, long authorId, String content, String status,
                              LocalDateTime createdAt, LocalDateTime lastUpdatedAt, long commentCount) {
}
