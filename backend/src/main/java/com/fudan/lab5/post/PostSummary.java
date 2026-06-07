package com.fudan.lab5.post;

import java.time.LocalDateTime;
import java.util.List;

public record PostSummary(long id, long authorId, String content, LocalDateTime lastUpdatedAt, List<CommentSummary> comments) {
}
