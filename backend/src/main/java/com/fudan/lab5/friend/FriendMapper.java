package com.fudan.lab5.friend;

import com.fudan.lab5.user.UserSearchResult;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface FriendMapper {
    @Select("SELECT id, user_id, name FROM friend_groups WHERE user_id = #{userId} ORDER BY name")
    List<FriendGroup> selectGroups(long userId);

    @Insert("INSERT INTO friend_groups(user_id, name) VALUES (#{userId}, #{name})")
    int insertGroup(@Param("userId") long userId, @Param("name") String name);

    @Update("UPDATE friend_groups SET name = #{name} WHERE id = #{groupId} AND user_id = #{userId}")
    int updateGroup(@Param("userId") long userId, @Param("groupId") long groupId, @Param("name") String name);

    @Delete("DELETE FROM friend_groups WHERE id = #{groupId} AND user_id = #{userId}")
    int deleteGroup(@Param("userId") long userId, @Param("groupId") long groupId);

    @Select("""
        SELECT
          f.id AS friendship_id,
          u.id AS friend_id,
          u.username,
          u.display_name,
          g.id AS group_id,
          g.name AS group_name
        FROM friendships f
        JOIN users u ON u.id = f.friend_id
        LEFT JOIN friend_groups g ON g.id = f.group_id
        WHERE f.owner_id = #{userId} AND u.status = 'ACTIVE'
        ORDER BY COALESCE(g.name, '未分组'), u.username
        """)
    List<FriendView> selectFriends(long userId);

    @Select("""
        SELECT id, username, display_name
        FROM users
        WHERE status = 'ACTIVE'
          AND id <> #{userId}
          AND NOT EXISTS (
            SELECT 1 FROM friendships f
            WHERE (f.owner_id = #{userId} AND f.friend_id = users.id)
               OR (f.owner_id = users.id AND f.friend_id = #{userId})
          )
          AND NOT EXISTS (
            SELECT 1 FROM friend_requests fr
            WHERE fr.status = 'PENDING'
              AND (
                (fr.requester_id = #{userId} AND fr.receiver_id = users.id)
                OR (fr.requester_id = users.id AND fr.receiver_id = #{userId})
              )
          )
          AND (username LIKE CONCAT('%', #{keyword}, '%') OR display_name LIKE CONCAT('%', #{keyword}, '%'))
        ORDER BY username
        LIMIT 20
        """)
    List<UserSearchResult> searchUsers(@Param("userId") long userId, @Param("keyword") String keyword);

    @Select("""
        SELECT
          fr.id,
          fr.requester_id,
          u.username,
          u.display_name,
          fr.created_at
        FROM friend_requests fr
        JOIN users u ON u.id = fr.requester_id
        WHERE fr.receiver_id = #{receiverId}
          AND fr.status = 'PENDING'
          AND u.status = 'ACTIVE'
        ORDER BY fr.created_at DESC
        """)
    List<FriendRequestView> selectPendingRequests(long receiverId);

    @Insert("""
        INSERT INTO friend_requests(requester_id, receiver_id, requester_group_id)
        VALUES (#{requesterId}, #{receiverId}, #{requesterGroupId})
        """)
    int insertFriendRequest(
        @Param("requesterId") long requesterId,
        @Param("receiverId") long receiverId,
        @Param("requesterGroupId") Long requesterGroupId
    );

    @Insert("""
        INSERT INTO friendships(owner_id, friend_id, group_id)
        VALUES (#{ownerId}, #{friendId}, #{groupId})
        """)
    int insertFriend(@Param("ownerId") long ownerId, @Param("friendId") long friendId, @Param("groupId") Long groupId);

    @Delete("DELETE FROM friendships WHERE owner_id = #{ownerId} AND friend_id = #{friendId}")
    int deleteFriend(@Param("ownerId") long ownerId, @Param("friendId") long friendId);

    @Update("UPDATE friendships SET group_id = #{groupId} WHERE owner_id = #{ownerId} AND friend_id = #{friendId}")
    int moveFriend(@Param("ownerId") long ownerId, @Param("friendId") long friendId, @Param("groupId") Long groupId);

    @Select("SELECT COUNT(*) FROM friend_groups WHERE id = #{groupId} AND user_id = #{userId}")
    int countOwnedGroup(@Param("userId") long userId, @Param("groupId") long groupId);

    @Select("SELECT COUNT(*) FROM users WHERE id = #{userId} AND status = 'ACTIVE'")
    int countActiveUser(long userId);

    @Select("SELECT COUNT(*) FROM friendships WHERE owner_id = #{ownerId} AND friend_id = #{friendId}")
    int countFriendship(@Param("ownerId") long ownerId, @Param("friendId") long friendId);

    @Select("""
        SELECT COUNT(*)
        FROM friend_requests
        WHERE requester_id = #{requesterId}
          AND receiver_id = #{receiverId}
          AND status = 'PENDING'
        """)
    int countPendingRequest(@Param("requesterId") long requesterId, @Param("receiverId") long receiverId);

    @Select("""
        SELECT id, requester_id, receiver_id, requester_group_id
        FROM friend_requests
        WHERE id = #{requestId}
          AND receiver_id = #{receiverId}
          AND status = 'PENDING'
        """)
    FriendRequestRow selectPendingRequestForReceiver(@Param("requestId") long requestId, @Param("receiverId") long receiverId);

    @Update("""
        UPDATE friend_requests
        SET status = 'ACCEPTED', responded_at = CURRENT_TIMESTAMP
        WHERE id = #{requestId} AND status = 'PENDING'
        """)
    int markRequestAccepted(long requestId);
}
