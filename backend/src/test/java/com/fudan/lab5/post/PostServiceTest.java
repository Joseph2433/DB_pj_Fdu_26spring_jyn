package com.fudan.lab5.post;

import org.apache.ibatis.annotations.Select;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
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
    void commentsSqlIncludesAuthorUsername() throws NoSuchMethodException {
        Method method = PostMapper.class.getMethod("selectComments", long.class);
        String sql = String.join("\n", method.getAnnotation(Select.class).value());

        assertThat(sql).contains("u.username AS author_username");
    }

    static class RecordingPostMapper implements PostMapper {
        String keyword;
        long friendViewerId;
        long friendAuthorId;

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
    }
}
