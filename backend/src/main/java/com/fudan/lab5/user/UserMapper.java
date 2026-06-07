package com.fudan.lab5.user;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserMapper {
    @Select("SELECT id, username, display_name, gender, birthday, age FROM users WHERE id = #{userId} AND status = 'ACTIVE'")
    UserProfile selectProfile(long userId);

    @Update("""
        UPDATE users
        SET display_name = #{displayName}, gender = #{gender}, birthday = #{birthday}, age = #{age}
        WHERE id = #{userId} AND status = 'ACTIVE'
        """)
    int updateProfile(@Param("userId") long userId, @Param("displayName") String displayName,
                      @Param("gender") String gender, @Param("birthday") java.time.LocalDate birthday,
                      @Param("age") Integer age);

    @Select("""
        SELECT id, username, display_name
        FROM users
        WHERE status = 'ACTIVE'
          AND id <> #{userId}
          AND (username LIKE CONCAT('%', #{keyword}, '%') OR display_name LIKE CONCAT('%', #{keyword}, '%'))
        ORDER BY username
        LIMIT 20
        """)
    List<UserSearchResult> searchUsers(@Param("userId") long userId, @Param("keyword") String keyword);
}
