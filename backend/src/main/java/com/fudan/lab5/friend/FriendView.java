package com.fudan.lab5.friend;

public record FriendView(long friendshipId, long friendId, String username, String displayName, Long groupId, String groupName) {
}
