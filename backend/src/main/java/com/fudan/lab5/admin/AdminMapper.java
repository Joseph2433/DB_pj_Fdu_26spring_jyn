package com.fudan.lab5.admin;

import com.fudan.lab5.post.CommentSummary;
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
        SELECT
          v.post_id,
          v.author_id,
          u.username AS author_username,
          v.content,
          v.status,
          v.created_at,
          v.last_updated_at,
          v.comment_count
        FROM admin_post_review_view v
        JOIN users u ON u.id = v.author_id
        ORDER BY v.created_at DESC
        """)
    List<AdminPostReviewRow> selectReviewPostRows();

    @Select("""
        SELECT c.id, c.author_id, u.username AS author_username, c.content, c.created_at
        FROM comments c
        JOIN users u ON u.id = c.author_id
        WHERE c.post_id = #{postId}
        ORDER BY c.created_at
        """)
    List<CommentSummary> selectComments(long postId);

    @Delete("DELETE FROM posts WHERE id = #{postId}")
    int deletePost(long postId);

    @Delete("DELETE FROM users WHERE id = #{userId}")
    int deleteUser(long userId);

    @Delete("DELETE FROM users WHERE username = #{username}")
    int deleteUserByUsername(@Param("username") String username);

    @Select("""
        SELECT
          l.id,
          l.post_id,
          l.admin_id,
          a.username AS admin_username,
          l.action,
          l.reason,
          l.created_at
        FROM post_audit_logs l
        LEFT JOIN admins a ON a.id = l.admin_id
        ORDER BY l.created_at DESC, l.id DESC
        """)
    List<AdminAuditLog> selectAuditLogs();

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

    @Select("SELECT password_hash FROM admins WHERE id = #{adminId}")
    String selectPasswordHash(long adminId);

    @Update("UPDATE admins SET password_hash = #{passwordHash} WHERE id = #{adminId}")
    int updatePasswordHash(@Param("adminId") long adminId, @Param("passwordHash") String passwordHash);
}
