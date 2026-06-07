package com.fudan.lab5.admin;

import com.fudan.lab5.post.CommentSummary;

import java.time.LocalDateTime;
import java.util.List;

public record AdminPostReview(long postId, long authorId, String authorUsername, String content, String status,
                              LocalDateTime createdAt, LocalDateTime lastUpdatedAt, long commentCount,
                              List<CommentSummary> comments) {
}
