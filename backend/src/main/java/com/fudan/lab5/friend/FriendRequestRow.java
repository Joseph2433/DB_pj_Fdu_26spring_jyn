package com.fudan.lab5.friend;

public record FriendRequestRow(long id, long requesterId, long receiverId, Long requesterGroupId) {
}
