package com.fudan.lab5.admin;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
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

    @Test
    void auditLogsSqlIncludesAdminUsernameAndOrdersLatestFirst() throws NoSuchMethodException {
        Method method = AdminMapper.class.getMethod("selectAuditLogs");
        String sql = String.join("\n", method.getAnnotation(Select.class).value());

        assertThat(sql).contains("FROM post_audit_logs l");
        assertThat(sql).contains("a.username AS admin_username");
        assertThat(sql).contains("ORDER BY l.created_at DESC, l.id DESC");
    }

    @Test
    void deleteUserByUsernameSqlDeletesByBoundUsername() throws NoSuchMethodException {
        Method method = AdminMapper.class.getMethod("deleteUserByUsername", String.class);
        String sql = method.getAnnotation(Delete.class).value()[0];

        assertThat(sql).contains("username = #{username}");
    }

    @Test
    void passwordHashSqlReadsAdminPasswordHash() throws NoSuchMethodException {
        Method method = AdminMapper.class.getMethod("selectPasswordHash", long.class);
        String sql = method.getAnnotation(Select.class).value()[0];

        assertThat(sql).contains("password_hash");
        assertThat(sql).contains("id = #{adminId}");
    }

    @Test
    void passwordUpdateSqlUsesBoundAdminHash() throws NoSuchMethodException {
        Method method = AdminMapper.class.getMethod("updatePasswordHash", long.class, String.class);
        String sql = method.getAnnotation(Update.class).value()[0];

        assertThat(sql).contains("password_hash = #{passwordHash}");
        assertThat(sql).contains("id = #{adminId}");
    }
}
