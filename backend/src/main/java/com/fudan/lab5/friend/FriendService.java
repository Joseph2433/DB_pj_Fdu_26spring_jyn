package com.fudan.lab5.friend;

import com.fudan.lab5.user.UserSearchResult;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FriendService {
    private final FriendMapper friendMapper;

    public FriendService(FriendMapper friendMapper) {
        this.friendMapper = friendMapper;
    }

    public List<UserSearchResult> searchUsers(long userId, String keyword) {
        String normalized = keyword == null || keyword.trim().isEmpty() ? "" : keyword.trim();
        return friendMapper.searchUsers(userId, normalized);
    }

    public List<FriendView> friends(long userId) {
        return friendMapper.selectFriends(userId);
    }

    public List<FriendGroup> groups(long userId) {
        return friendMapper.selectGroups(userId);
    }

    public List<FriendRequestView> friendRequests(long userId) {
        return friendMapper.selectPendingRequests(userId);
    }

    @Transactional
    public void createGroup(long userId, GroupRequest request) {
        try {
            friendMapper.insertGroup(userId, requireName(request.name()));
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException("分组名已存在");
        }
    }

    @Transactional
    public void updateGroup(long userId, long groupId, GroupRequest request) {
        if (friendMapper.updateGroup(userId, groupId, requireName(request.name())) == 0) {
            throw new IllegalArgumentException("好友分组不存在");
        }
    }

    @Transactional
    public void deleteGroup(long userId, long groupId) {
        if (friendMapper.deleteGroup(userId, groupId) == 0) {
            throw new IllegalArgumentException("好友分组不存在");
        }
    }

    @Transactional
    public void addFriend(long userId, FriendRequest request) {
        if (request.friendId() == userId) {
            throw new IllegalArgumentException("不能添加自己为好友");
        }
        if (friendMapper.countActiveUser(request.friendId()) == 0) {
            throw new IllegalArgumentException("好友用户不存在");
        }
        validateGroup(userId, request.groupId());
        if (friendMapper.countFriendship(userId, request.friendId()) > 0
            || friendMapper.countFriendship(request.friendId(), userId) > 0) {
            throw new IllegalArgumentException("已经是好友");
        }
        if (friendMapper.countPendingRequest(request.friendId(), userId) > 0) {
            throw new IllegalArgumentException("对方已发送好友申请，请先处理申请");
        }
        if (friendMapper.countPendingRequest(userId, request.friendId()) > 0) {
            throw new IllegalArgumentException("好友申请已发送");
        }
        try {
            friendMapper.insertFriendRequest(userId, request.friendId(), request.groupId());
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException("好友申请已发送");
        }
    }

    @Transactional
    public void acceptFriendRequest(long userId, long requestId) {
        FriendRequestRow request = friendMapper.selectPendingRequestForReceiver(requestId, userId);
        if (request == null) {
            throw new IllegalArgumentException("好友申请不存在");
        }
        insertFriendIfMissing(request.requesterId(), userId, request.requesterGroupId());
        insertFriendIfMissing(userId, request.requesterId(), null);
        if (friendMapper.markRequestAccepted(requestId) == 0) {
            throw new IllegalArgumentException("好友申请不存在");
        }
    }

    @Transactional
    public void deleteFriend(long userId, long friendId) {
        if (friendMapper.deleteFriend(userId, friendId) == 0) {
            throw new IllegalArgumentException("好友关系不存在");
        }
        friendMapper.deleteFriend(friendId, userId);
    }

    @Transactional
    public void moveFriend(long userId, long friendId, MoveFriendRequest request) {
        validateGroup(userId, request.groupId());
        if (friendMapper.moveFriend(userId, friendId, request.groupId()) == 0) {
            throw new IllegalArgumentException("好友关系不存在");
        }
    }

    private void validateGroup(long userId, Long groupId) {
        if (groupId != null && friendMapper.countOwnedGroup(userId, groupId) == 0) {
            throw new IllegalArgumentException("好友分组不存在");
        }
    }

    private void insertFriendIfMissing(long ownerId, long friendId, Long groupId) {
        try {
            friendMapper.insertFriend(ownerId, friendId, groupId);
        } catch (DuplicateKeyException ignored) {
            // Existing one-way rows can remain when accepting a request made before the flow changed.
        }
    }

    private String requireName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("分组名不能为空");
        }
        return name.trim();
    }
}
