package com.fudan.lab5.post;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class PostService {
    private static final int POST_LIMIT = 280;
    private static final int COMMENT_LIMIT = 160;

    private final PostMapper postMapper;

    public PostService(PostMapper postMapper) {
        this.postMapper = postMapper;
    }

    public List<PostSummary> feedForUser(long userId, String keyword) {
        String normalized = keyword == null || keyword.trim().isEmpty() ? null : keyword.trim();
        return withComments(postMapper.selectFriendFeedRows(userId, normalized));
    }

    public List<PostSummary> myPosts(long userId) {
        return withComments(postMapper.selectMyPostRows(userId));
    }

    public List<PostSummary> friendPosts(long userId, long friendId) {
        return withComments(postMapper.selectFriendPostRows(userId, friendId));
    }

    public List<PostVisibilityRule> visibilityRules(long userId, long postId) {
        ensureOwnPost(userId, postId);
        return postMapper.selectVisibilityRules(postId, userId);
    }

    @Transactional
    public void createPost(long userId, PostCreateRequest request) {
        postMapper.insertPost(userId, validateText(request.content(), POST_LIMIT, "朋友圈内容"));
    }

    @Transactional
    public void updatePost(long userId, long postId, PostUpdateRequest request) {
        if (postMapper.updateOwnPost(postId, userId, validateText(request.content(), POST_LIMIT, "朋友圈内容")) == 0) {
            throw new IllegalArgumentException("只能修改自己的朋友圈");
        }
    }

    @Transactional
    public void deletePost(long userId, long postId) {
        if (postMapper.deleteOwnPost(postId, userId) == 0) {
            throw new IllegalArgumentException("只能删除自己的朋友圈");
        }
    }

    @Transactional
    public void comment(long userId, long postId, CommentCreateRequest request) {
        if (postMapper.countVisibleToUser(postId, userId) == 0) {
            throw new IllegalArgumentException("朋友圈不存在或不可见");
        }
        postMapper.insertComment(postId, userId, validateText(request.content(), COMMENT_LIMIT, "评论内容"));
    }

    @Transactional
    public void updateVisibility(long userId, long postId, PostVisibilityUpdateRequest request) {
        ensureOwnPost(userId, postId);
        String ruleType = normalizeOption(request.ruleType(), List.of("ALLOW", "DENY"), "可见性规则");
        String targetType = normalizeOption(request.targetType(), List.of("GROUP", "USER"), "可见性对象");
        List<Long> targetIds = normalizeTargetIds(request.targetIds());
        validateTargets(userId, targetType, targetIds);
        validateSingleVisibilityMode(postId, userId, ruleType, targetIds);

        postMapper.deleteVisibilityRules(postId, ruleType, targetType);
        if (!targetIds.isEmpty()) {
            postMapper.insertVisibilityRules(postId, ruleType, targetType, targetIds);
        }
    }

    private List<PostSummary> withComments(List<PostRow> rows) {
        return rows.stream()
            .map(row -> new PostSummary(row.id(), row.authorId(), row.authorUsername(), row.content(), row.lastUpdatedAt(), postMapper.selectComments(row.id())))
            .toList();
    }

    private String validateText(String content, int limit, String label) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException(label + "不能为空");
        }
        String normalized = content.trim();
        if (normalized.length() > limit) {
            throw new IllegalArgumentException(label + "不能超过 " + limit + " 字");
        }
        return normalized;
    }

    private void ensureOwnPost(long userId, long postId) {
        if (postMapper.countOwnVisiblePost(postId, userId) == 0) {
            throw new IllegalArgumentException("只能设置自己的朋友圈可见范围");
        }
    }

    private String normalizeOption(String value, List<String> allowed, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + "不能为空");
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!allowed.contains(normalized)) {
            throw new IllegalArgumentException(label + "不合法");
        }
        return normalized;
    }

    private List<Long> normalizeTargetIds(List<Long> targetIds) {
        if (targetIds == null) {
            return List.of();
        }
        return targetIds.stream()
            .filter(id -> id != null && id > 0)
            .distinct()
            .toList();
    }

    private void validateTargets(long userId, String targetType, List<Long> targetIds) {
        if (targetIds.isEmpty()) {
            return;
        }
        int count = "GROUP".equals(targetType)
            ? postMapper.countOwnedGroupTargets(userId, targetIds)
            : postMapper.countFriendTargets(userId, targetIds);
        if (count != targetIds.size()) {
            throw new IllegalArgumentException("只能选择自己的分组或好友");
        }
    }

    private void validateSingleVisibilityMode(long postId, long userId, String ruleType, List<Long> targetIds) {
        if (targetIds.isEmpty()) {
            return;
        }
        String oppositeRuleType = "ALLOW".equals(ruleType) ? "DENY" : "ALLOW";
        if (postMapper.countVisibilityRulesByRuleType(postId, userId, oppositeRuleType) > 0) {
            throw new IllegalArgumentException("一条动态只能设置可见或不可见其中一种");
        }
    }
}
