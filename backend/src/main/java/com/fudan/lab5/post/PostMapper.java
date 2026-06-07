package com.fudan.lab5.post;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PostMapper {
    @Select("""
        SELECT
          p.id,
          p.author_id,
          p.content,
          p.last_updated_at
        FROM posts p
        LEFT JOIN friendships f ON f.owner_id = #{userId} AND f.friend_id = p.author_id
        LEFT JOIN comments c ON c.post_id = p.id
        WHERE p.status = 'VISIBLE'
          AND (p.author_id = #{userId} OR f.id IS NOT NULL)
          AND (
            #{keyword} IS NULL
            OR p.content LIKE CONCAT('%', #{keyword}, '%')
            OR c.content LIKE CONCAT('%', #{keyword}, '%')
          )
        GROUP BY p.id, p.author_id, p.content, p.last_updated_at, p.created_at
        ORDER BY p.created_at DESC
        """)
    List<PostRow> selectFriendFeedRows(@Param("userId") long userId, @Param("keyword") String keyword);

    @Select("""
        SELECT id, author_id, content, last_updated_at
        FROM posts
        WHERE author_id = #{userId} AND status = 'VISIBLE'
        ORDER BY created_at DESC
        """)
    List<PostRow> selectMyPostRows(long userId);

    @Select("""
        SELECT p.id, p.author_id, p.content, p.last_updated_at
        FROM posts p
        WHERE p.author_id = #{friendId}
          AND p.status = 'VISIBLE'
          AND EXISTS (
            SELECT 1
            FROM friendships f
            WHERE f.owner_id = #{userId}
              AND f.friend_id = #{friendId}
          )
        ORDER BY p.created_at DESC
        """)
    List<PostRow> selectFriendPostRows(@Param("userId") long userId, @Param("friendId") long friendId);

    @Select("""
        SELECT id, author_id, content, created_at
        FROM comments
        WHERE post_id = #{postId}
        ORDER BY created_at
        """)
    List<CommentSummary> selectComments(long postId);

    @Insert("INSERT INTO posts(author_id, content) VALUES (#{authorId}, #{content})")
    int insertPost(@Param("authorId") long authorId, @Param("content") String content);

    @Update("""
        UPDATE posts
        SET content = #{content}
        WHERE id = #{postId} AND author_id = #{authorId} AND status = 'VISIBLE'
        """)
    int updateOwnPost(@Param("postId") long postId, @Param("authorId") long authorId, @Param("content") String content);

    @Delete("DELETE FROM posts WHERE id = #{postId} AND author_id = #{authorId}")
    int deleteOwnPost(@Param("postId") long postId, @Param("authorId") long authorId);

    @Insert("INSERT INTO comments(post_id, author_id, content) VALUES (#{postId}, #{authorId}, #{content})")
    int insertComment(@Param("postId") long postId, @Param("authorId") long authorId, @Param("content") String content);

    @Select("""
        SELECT COUNT(*)
        FROM posts p
        WHERE p.id = #{postId}
          AND p.status = 'VISIBLE'
          AND (
            p.author_id = #{userId}
            OR EXISTS (
              SELECT 1 FROM friendships f
              WHERE f.owner_id = #{userId} AND f.friend_id = p.author_id
            )
          )
        """)
    int countVisibleToUser(@Param("postId") long postId, @Param("userId") long userId);
}
