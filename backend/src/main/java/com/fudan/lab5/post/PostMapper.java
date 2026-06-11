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
          u.username AS author_username,
          p.content,
          p.last_updated_at
        FROM posts p
        JOIN users u ON u.id = p.author_id
        LEFT JOIN friendships f ON f.owner_id = #{userId} AND f.friend_id = p.author_id
        LEFT JOIN comments c ON c.post_id = p.id
        WHERE p.status = 'VISIBLE'
          AND (
            p.author_id = #{userId}
            OR (
              f.id IS NOT NULL
              AND NOT EXISTS (
                SELECT 1
                FROM post_visibility_rules dvr
                WHERE dvr.post_id = p.id
                  AND dvr.rule_type = 'DENY'
                  AND (
                    (dvr.target_type = 'USER' AND dvr.target_id = #{userId})
                    OR (
                      dvr.target_type = 'GROUP'
                      AND EXISTS (
                        SELECT 1
                        FROM friendships author_group
                        WHERE author_group.owner_id = p.author_id
                          AND author_group.friend_id = #{userId}
                          AND author_group.group_id = dvr.target_id
                      )
                    )
                  )
              )
              AND (
                NOT EXISTS (
                  SELECT 1
                  FROM post_visibility_rules avr
                  WHERE avr.post_id = p.id
                    AND avr.rule_type = 'ALLOW'
                )
                OR EXISTS (
                  SELECT 1
                  FROM post_visibility_rules avr
                  WHERE avr.post_id = p.id
                    AND avr.rule_type = 'ALLOW'
                    AND (
                      (avr.target_type = 'USER' AND avr.target_id = #{userId})
                      OR (
                        avr.target_type = 'GROUP'
                        AND EXISTS (
                          SELECT 1
                          FROM friendships author_group
                          WHERE author_group.owner_id = p.author_id
                            AND author_group.friend_id = #{userId}
                            AND author_group.group_id = avr.target_id
                        )
                      )
                    )
                )
              )
            )
          )
          AND (
            #{keyword} IS NULL
            OR p.content LIKE CONCAT('%', #{keyword}, '%')
            OR c.content LIKE CONCAT('%', #{keyword}, '%')
          )
        GROUP BY p.id, p.author_id, u.username, p.content, p.last_updated_at, p.created_at
        ORDER BY p.created_at DESC
        """)
    List<PostRow> selectFriendFeedRows(@Param("userId") long userId, @Param("keyword") String keyword);

    @Select("""
        SELECT p.id, p.author_id, u.username AS author_username, p.content, p.last_updated_at
        FROM posts p
        JOIN users u ON u.id = p.author_id
        WHERE p.author_id = #{userId} AND p.status = 'VISIBLE'
        ORDER BY p.created_at DESC
        """)
    List<PostRow> selectMyPostRows(long userId);

    @Select("""
        SELECT p.id, p.author_id, u.username AS author_username, p.content, p.last_updated_at
        FROM posts p
        JOIN users u ON u.id = p.author_id
        WHERE p.author_id = #{friendId}
          AND p.status = 'VISIBLE'
          AND EXISTS (
            SELECT 1
            FROM friendships f
            WHERE f.owner_id = #{userId}
              AND f.friend_id = #{friendId}
          )
          AND NOT EXISTS (
            SELECT 1
            FROM post_visibility_rules dvr
            WHERE dvr.post_id = p.id
              AND dvr.rule_type = 'DENY'
              AND (
                (dvr.target_type = 'USER' AND dvr.target_id = #{userId})
                OR (
                  dvr.target_type = 'GROUP'
                  AND EXISTS (
                    SELECT 1
                    FROM friendships author_group
                    WHERE author_group.owner_id = p.author_id
                      AND author_group.friend_id = #{userId}
                      AND author_group.group_id = dvr.target_id
                  )
                )
              )
          )
          AND (
            NOT EXISTS (
              SELECT 1
              FROM post_visibility_rules avr
              WHERE avr.post_id = p.id
                AND avr.rule_type = 'ALLOW'
            )
            OR EXISTS (
              SELECT 1
              FROM post_visibility_rules avr
              WHERE avr.post_id = p.id
                AND avr.rule_type = 'ALLOW'
                AND (
                  (avr.target_type = 'USER' AND avr.target_id = #{userId})
                  OR (
                    avr.target_type = 'GROUP'
                    AND EXISTS (
                      SELECT 1
                      FROM friendships author_group
                      WHERE author_group.owner_id = p.author_id
                        AND author_group.friend_id = #{userId}
                        AND author_group.group_id = avr.target_id
                    )
                  )
                )
            )
          )
        ORDER BY p.created_at DESC
        """)
    List<PostRow> selectFriendPostRows(@Param("userId") long userId, @Param("friendId") long friendId);

    @Select("""
        SELECT c.id, c.author_id, u.username AS author_username, c.content, c.created_at
        FROM comments c
        JOIN users u ON u.id = c.author_id
        WHERE c.post_id = #{postId}
        ORDER BY c.created_at
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

    @Delete("""
        DELETE c
        FROM comments c
        JOIN posts p ON p.id = c.post_id
        WHERE c.id = #{commentId}
          AND c.post_id = #{postId}
          AND (c.author_id = #{userId} OR p.author_id = #{userId})
        """)
    int deleteManageableComment(@Param("postId") long postId, @Param("commentId") long commentId,
                                @Param("userId") long userId);

    @Select("""
        SELECT COUNT(*)
        FROM posts p
        WHERE p.id = #{postId}
          AND p.status = 'VISIBLE'
          AND (
            p.author_id = #{userId}
            OR (
              EXISTS (
                SELECT 1 FROM friendships f
                WHERE f.owner_id = #{userId} AND f.friend_id = p.author_id
              )
              AND NOT EXISTS (
                SELECT 1
                FROM post_visibility_rules dvr
                WHERE dvr.post_id = p.id
                  AND dvr.rule_type = 'DENY'
                  AND (
                    (dvr.target_type = 'USER' AND dvr.target_id = #{userId})
                    OR (
                      dvr.target_type = 'GROUP'
                      AND EXISTS (
                        SELECT 1
                        FROM friendships author_group
                        WHERE author_group.owner_id = p.author_id
                          AND author_group.friend_id = #{userId}
                          AND author_group.group_id = dvr.target_id
                      )
                    )
                  )
              )
              AND (
                NOT EXISTS (
                  SELECT 1
                  FROM post_visibility_rules avr
                  WHERE avr.post_id = p.id
                    AND avr.rule_type = 'ALLOW'
                )
                OR EXISTS (
                  SELECT 1
                  FROM post_visibility_rules avr
                  WHERE avr.post_id = p.id
                    AND avr.rule_type = 'ALLOW'
                    AND (
                      (avr.target_type = 'USER' AND avr.target_id = #{userId})
                      OR (
                        avr.target_type = 'GROUP'
                        AND EXISTS (
                          SELECT 1
                          FROM friendships author_group
                          WHERE author_group.owner_id = p.author_id
                            AND author_group.friend_id = #{userId}
                            AND author_group.group_id = avr.target_id
                        )
                      )
                    )
                )
              )
            )
          )
        """)
    int countVisibleToUser(@Param("postId") long postId, @Param("userId") long userId);

    @Select("SELECT COUNT(*) FROM posts WHERE id = #{postId} AND author_id = #{authorId} AND status = 'VISIBLE'")
    int countOwnVisiblePost(@Param("postId") long postId, @Param("authorId") long authorId);

    @Select("""
        <script>
        SELECT COUNT(*)
        FROM friend_groups
        WHERE user_id = #{authorId}
          AND id IN
          <foreach collection="targetIds" item="targetId" open="(" separator="," close=")">
            #{targetId}
          </foreach>
        </script>
        """)
    int countOwnedGroupTargets(@Param("authorId") long authorId, @Param("targetIds") List<Long> targetIds);

    @Select("""
        <script>
        SELECT COUNT(*)
        FROM friendships
        WHERE owner_id = #{authorId}
          AND friend_id IN
          <foreach collection="targetIds" item="targetId" open="(" separator="," close=")">
            #{targetId}
          </foreach>
        </script>
        """)
    int countFriendTargets(@Param("authorId") long authorId, @Param("targetIds") List<Long> targetIds);

    @Select("""
        SELECT
          r.id,
          r.post_id,
          r.rule_type,
          r.target_type,
          r.target_id,
          CASE
            WHEN r.target_type = 'GROUP' THEN g.name
            WHEN r.target_type = 'USER' THEN u.username
            ELSE ''
          END AS target_name
        FROM post_visibility_rules r
        JOIN posts p ON p.id = r.post_id AND p.author_id = #{authorId}
        LEFT JOIN friend_groups g ON r.target_type = 'GROUP' AND g.id = r.target_id AND g.user_id = #{authorId}
        LEFT JOIN users u ON r.target_type = 'USER' AND u.id = r.target_id
        WHERE r.post_id = #{postId}
        ORDER BY r.rule_type, r.target_type, r.target_id
        """)
    List<PostVisibilityRule> selectVisibilityRules(@Param("postId") long postId, @Param("authorId") long authorId);

    @Select("""
        SELECT COUNT(*)
        FROM post_visibility_rules r
        JOIN posts p ON p.id = r.post_id AND p.author_id = #{authorId}
        WHERE r.post_id = #{postId}
          AND r.rule_type = #{ruleType}
        """)
    int countVisibilityRulesByRuleType(@Param("postId") long postId, @Param("authorId") long authorId,
                                       @Param("ruleType") String ruleType);

    @Delete("""
        DELETE FROM post_visibility_rules
        WHERE post_id = #{postId}
          AND rule_type = #{ruleType}
          AND target_type = #{targetType}
        """)
    int deleteVisibilityRules(@Param("postId") long postId, @Param("ruleType") String ruleType, @Param("targetType") String targetType);

    @Insert("""
        <script>
        INSERT INTO post_visibility_rules(post_id, rule_type, target_type, target_id)
        VALUES
        <foreach collection="targetIds" item="targetId" separator=",">
          (#{postId}, #{ruleType}, #{targetType}, #{targetId})
        </foreach>
        </script>
        """)
    int insertVisibilityRules(@Param("postId") long postId, @Param("ruleType") String ruleType,
                              @Param("targetType") String targetType, @Param("targetIds") List<Long> targetIds);
}
