package com.fudan.lab5.post;

import java.util.List;

public record PostVisibilityUpdateRequest(String ruleType, String targetType, List<Long> targetIds) {
}
