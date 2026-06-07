package com.fudan.lab5.post;

import org.apache.ibatis.annotations.Select;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostServiceTest {
    @Test
    void trimsKeywordAndDelegatesNullForBlankKeyword() {
        RecordingPostMapper mapper = new RecordingPostMapper();
        PostService service = new PostService(mapper);

        service.feedForUser(1L, "   ");

        assertThat(mapper.keyword).isNull();
    }

    @Test
    void trimsKeywordBeforeSearchingFeed() {
        RecordingPostMapper mapper = new RecordingPostMapper();
        PostService service = new PostService(mapper);

        service.feedForUser(1L, "  AI assistant  ");

        assertThat(mapper.keyword).isEqualTo("AI assistant");
    }

    @Test
    void rejectsPostContentOverLimit() {
        PostService service = new PostService(new RecordingPostMapper());

        assertThatThrownBy(() -> service.createPost(1L, new PostCreateRequest("a".repeat(281))))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void feedSqlIncludesOwnPostsAndOrdersByCreatedAt() throws NoSuchMethodException {
        Method method = PostMapper.class.getMethod("selectFriendFeedRows", long.class, String.class);
        String sql = String.join("\n", method.getAnnotation(Select.class).value());

        assertThat(sql).contains("u.username AS author_username");
        assertThat(sql).contains("p.author_id = #{userId}");
        assertThat(sql).contains("ORDER BY p.created_at DESC");
    }

    @Test
    void loadsVisiblePostsForOneFriendWithComments() {
        RecordingPostMapper mapper = new RecordingPostMapper();
        PostService service = new PostService(mapper);

        List<PostSummary> result = service.friendPosts(1L, 2L);

        assertThat(mapper.friendViewerId).isEqualTo(1L);
        assertThat(mapper.friendAuthorId).isEqualTo(2L);
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().authorId()).isEqualTo(2L);
        assertThat(result.getFirst().authorUsername()).isEqualTo("bob");
        assertThat(result.getFirst().comments()).isEmpty();
    }

    @Test
    void friendPostsSqlRequiresFriendshipAndOrdersByCreatedAt() throws NoSuchMethodException {
        Method method = PostMapper.class.getMethod("selectFriendPostRows", long.class, long.class);
        String sql = String.join("\n", method.getAnnotation(Select.class).value());

        assertThat(sql).contains("p.author_id = #{friendId}");
        assertThat(sql).contains("u.username AS author_username");
        assertThat(sql).contains("f.owner_id = #{userId}");
        assertThat(sql).contains("f.friend_id = #{friendId}");
        assertThat(sql).contains("ORDER BY p.created_at DESC");
    }

    @Test
    void feedSqlAppliesPostVisibilityRules() throws NoSuchMethodException {
        Method method = PostMapper.class.getMethod("selectFriendFeedRows", long.class, String.class);
        String sql = String.join("\n", method.getAnnotation(Select.class).value());

        assertThat(sql).contains("post_visibility_rules");
        assertThat(sql).contains("rule_type = 'DENY'");
        assertThat(sql).contains("rule_type = 'ALLOW'");
    }

    @Test
    void updatesVisibilityRulesForOwnedPostTargets() {
        RecordingPostMapper mapper = new RecordingPostMapper();
        PostService service = new PostService(mapper);

        service.updateVisibility(1L, 12L, new PostVisibilityUpdateRequest("ALLOW", "GROUP", List.of(7L, 8L)));

        assertThat(mapper.deletedVisibilityRuleType).isEqualTo("ALLOW");
        assertThat(mapper.deletedVisibilityTargetType).isEqualTo("GROUP");
        assertThat(mapper.insertedVisibilityTargetIds).containsExactly(7L, 8L);
    }

    @Test
    void rejectsVisibilityUpdateForSomeoneElsesPost() {
        RecordingPostMapper mapper = new RecordingPostMapper();
        mapper.ownPostCount = 0;
        PostService service = new PostService(mapper);

        assertThatThrownBy(() -> service.updateVisibility(1L, 12L, new PostVisibilityUpdateRequest("DENY", "USER", List.of(2L))))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("只能设置自己的朋友圈可见范围");
    }

    @Test
    void rejectsOppositeVisibilityModeWhenPostAlreadyHasRules() {
        RecordingPostMapper mapper = new RecordingPostMapper();
        mapper.oppositeVisibilityRuleCount = 1;
        PostService service = new PostService(mapper);

        assertThatThrownBy(() -> service.updateVisibility(1L, 12L, new PostVisibilityUpdateRequest("ALLOW", "USER", List.of(2L))))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("一条动态只能设置可见或不可见其中一种");

        assertThat(mapper.oppositeCheckPostId).isEqualTo(12L);
        assertThat(mapper.oppositeCheckAuthorId).isEqualTo(1L);
        assertThat(mapper.oppositeCheckRuleType).isEqualTo("DENY");
    }

    @Test
    void commentsSqlIncludesAuthorUsername() throws NoSuchMethodException {
        Method method = PostMapper.class.getMethod("selectComments", long.class);
        String sql = String.join("\n", method.getAnnotation(Select.class).value());

        assertThat(sql).contains("u.username AS author_username");
    }

    static class RecordingPostMapper implements PostMapper {
        String keyword;
        long friendViewerId;
        long friendAuthorId;
        int ownPostCount = 1;
        int oppositeVisibilityRuleCount = 0;
        long oppositeCheckPostId;
        long oppositeCheckAuthorId;
        String oppositeCheckRuleType;
        String deletedVisibilityRuleType;
        String deletedVisibilityTargetType;
        List<Long> insertedVisibilityTargetIds = new ArrayList<>();

        @Override
        public List<PostRow> selectFriendFeedRows(long userId, String keyword) {
            this.keyword = keyword;
            return List.of(new PostRow(9L, 2L, "bob", "AI assistant draft", LocalDateTime.now()));
        }

        @Override
        public List<PostRow> selectMyPostRows(long userId) {
            return List.of();
        }

        @Override
        public List<PostRow> selectFriendPostRows(long userId, long friendId) {
            this.friendViewerId = userId;
            this.friendAuthorId = friendId;
            return List.of(new PostRow(11L, friendId, "bob", "Friend post", LocalDateTime.now()));
        }

        @Override
        public List<CommentSummary> selectComments(long postId) {
            return List.of();
        }

        @Override
        public int insertPost(long authorId, String content) {
            return 1;
        }

        @Override
        public int updateOwnPost(long postId, long authorId, String content) {
            return 1;
        }

        @Override
        public int deleteOwnPost(long postId, long authorId) {
            return 1;
        }

        @Override
        public int insertComment(long postId, long authorId, String content) {
            return 1;
        }

        @Override
        public int countVisibleToUser(long postId, long userId) {
            return 1;
        }

        @Override
        public int countOwnVisiblePost(long postId, long authorId) {
            return ownPostCount;
        }

        @Override
        public int countOwnedGroupTargets(long authorId, List<Long> targetIds) {
            return targetIds.size();
        }

        @Override
        public int countFriendTargets(long authorId, List<Long> targetIds) {
            return targetIds.size();
        }

        @Override
        public List<PostVisibilityRule> selectVisibilityRules(long postId, long authorId) {
            return List.of();
        }

        public int countVisibilityRulesByRuleType(long postId, long authorId, String ruleType) {
            oppositeCheckPostId = postId;
            oppositeCheckAuthorId = authorId;
            oppositeCheckRuleType = ruleType;
            return oppositeVisibilityRuleCount;
        }

        @Override
        public int deleteVisibilityRules(long postId, String ruleType, String targetType) {
            deletedVisibilityRuleType = ruleType;
            deletedVisibilityTargetType = targetType;
            return 1;
        }

        @Override
        public int insertVisibilityRules(long postId, String ruleType, String targetType, List<Long> targetIds) {
            insertedVisibilityTargetIds = targetIds;
            return targetIds.size();
        }
    }
}
