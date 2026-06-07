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
        try {
            friendMapper.insertFriend(userId, request.friendId(), request.groupId());
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException("已经添加过该好友");
        }
    }

    @Transactional
    public void deleteFriend(long userId, long friendId) {
        if (friendMapper.deleteFriend(userId, friendId) == 0) {
            throw new IllegalArgumentException("好友关系不存在");
        }
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

    private String requireName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("分组名不能为空");
        }
        return name.trim();
    }
}
