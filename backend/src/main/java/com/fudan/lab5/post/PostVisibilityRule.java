package com.fudan.lab5.post;

public record PostVisibilityRule(long id, long postId, String ruleType, String targetType, long targetId,
                                 String targetName) {
}
