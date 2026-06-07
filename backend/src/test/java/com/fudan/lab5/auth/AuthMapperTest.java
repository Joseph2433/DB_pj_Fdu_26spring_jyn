package com.fudan.lab5.auth;

import org.apache.ibatis.annotations.Update;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class AuthMapperTest {
    @Test
    void userPasswordResetSqlUsesUsernameAndActiveStatus() throws NoSuchMethodException {
        Method method = AuthMapper.class.getMethod("updateUserPasswordHashByUsername", String.class, String.class);
        String sql = String.join("\n", method.getAnnotation(Update.class).value());

        assertThat(sql).contains("password_hash = #{passwordHash}");
        assertThat(sql).contains("username = #{username}");
        assertThat(sql).contains("status = 'ACTIVE'");
    }

    @Test
    void adminPasswordResetSqlUsesUsername() throws NoSuchMethodException {
        Method method = AuthMapper.class.getMethod("updateAdminPasswordHashByUsername", String.class, String.class);
        String sql = String.join("\n", method.getAnnotation(Update.class).value());

        assertThat(sql).contains("password_hash = #{passwordHash}");
        assertThat(sql).contains("username = #{username}");
    }
}
