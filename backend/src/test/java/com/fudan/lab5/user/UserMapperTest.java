package com.fudan.lab5.user;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {
    @Test
    void passwordHashSqlReadsOnlyActiveUser() throws NoSuchMethodException {
        Method method = UserMapper.class.getMethod("selectPasswordHash", long.class);
        String sql = String.join("\n", method.getAnnotation(Select.class).value());

        assertThat(sql).contains("password_hash");
        assertThat(sql).contains("status = 'ACTIVE'");
    }

    @Test
    void passwordUpdateSqlUsesBoundHashAndActiveUser() throws NoSuchMethodException {
        Method method = UserMapper.class.getMethod("updatePasswordHash", long.class, String.class);
        String sql = String.join("\n", method.getAnnotation(Update.class).value());

        assertThat(sql).contains("password_hash = #{passwordHash}");
        assertThat(sql).contains("status = 'ACTIVE'");
    }
}
