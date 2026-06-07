package com.fudan.lab5.admin;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AdminMapper {
    @Select("""
        SELECT post_id, author_id, content, status, created_at, last_updated_at, comment_count
        FROM admin_post_review_view
        ORDER BY last_updated_at DESC
        """)
    List<AdminPostReview> selectReviewPosts();

    @Delete("DELETE FROM posts WHERE id = #{postId}")
    int deletePost(long postId);

    @Delete("DELETE FROM users WHERE id = #{userId}")
    int deleteUser(long userId);

    @Insert("""
        INSERT INTO post_audit_logs(post_id, admin_id, action, reason)
        VALUES (#{postId}, #{adminId}, #{action}, #{reason})
        """)
    int insertAuditLog(@Param("postId") Long postId, @Param("adminId") long adminId,
                       @Param("action") String action, @Param("reason") String reason);

    @Select("SELECT id, username, display_name FROM admins WHERE id = #{adminId}")
    AdminProfile selectProfile(long adminId);

    @Update("UPDATE admins SET display_name = #{displayName} WHERE id = #{adminId}")
    int updateProfile(@Param("adminId") long adminId, @Param("displayName") String displayName);
}
