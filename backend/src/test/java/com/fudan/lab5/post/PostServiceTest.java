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

        assertThat(sql).contains("p.author_id = #{userId}");
        assertThat(sql).contains("ORDER BY p.created_at DESC");
    }

    static class RecordingPostMapper implements PostMapper {
        String keyword;

        @Override
        public List<PostRow> selectFriendFeedRows(long userId, String keyword) {
            this.keyword = keyword;
            return List.of(new PostRow(9L, 2L, "AI assistant draft", LocalDateTime.now()));
        }

        @Override
        public List<PostRow> selectMyPostRows(long userId) {
            return List.of();
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
