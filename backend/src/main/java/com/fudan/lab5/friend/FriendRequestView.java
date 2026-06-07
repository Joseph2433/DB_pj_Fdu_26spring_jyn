package com.fudan.lab5.friend;

import java.time.LocalDateTime;

public record FriendRequestView(long id, long requesterId, String username, String displayName, LocalDateTime createdAt) {
}
