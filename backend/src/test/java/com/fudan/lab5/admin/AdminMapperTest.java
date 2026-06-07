package com.fudan.lab5.admin;

import org.apache.ibatis.annotations.Select;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class AdminMapperTest {
    @Test
    void reviewPostsSqlIncludesAuthorUsername() throws NoSuchMethodException {
        Method method = AdminMapper.class.getMethod("selectReviewPostRows");
        String sql = String.join("\n", method.getAnnotation(Select.class).value());

        assertThat(sql).contains("u.username AS author_username");
    }

    @Test
    void reviewPostsSqlOrdersByCreatedAtForFeedDisplay() throws NoSuchMethodException {
        Method method = AdminMapper.class.getMethod("selectReviewPostRows");
        String sql = String.join("\n", method.getAnnotation(Select.class).value());

        assertThat(sql).contains("ORDER BY v.created_at DESC");
    }

    @Test
    void reviewCommentsSqlIncludesAuthorUsername() throws NoSuchMethodException {
        Method method = AdminMapper.class.getMethod("selectComments", long.class);
        String sql = String.join("\n", method.getAnnotation(Select.class).value());

        assertThat(sql).contains("u.username AS author_username");
    }
}
