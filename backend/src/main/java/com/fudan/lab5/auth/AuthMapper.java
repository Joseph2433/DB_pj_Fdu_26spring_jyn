package com.fudan.lab5.auth;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AuthMapper {
    @Select("SELECT id, username, password_hash, display_name, status FROM users WHERE username = #{username}")
    UserAccount selectUserByUsername(String username);

    @Select("SELECT id, username, password_hash, display_name FROM admins WHERE username = #{username}")
    AdminAccount selectAdminByUsername(String username);

    @Insert("""
        INSERT INTO users(username, password_hash, display_name)
        VALUES (#{username}, #{passwordHash}, #{displayName})
        """)
    int insertUser(@Param("username") String username, @Param("passwordHash") String passwordHash, @Param("displayName") String displayName);
}
