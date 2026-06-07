package com.fudan.lab5.friend;

import com.fudan.lab5.user.UserSearchResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FriendServiceTest {
    @Test
    void sendingFriendRequestDoesNotInsertFriendshipImmediately() {
        RecordingFriendMapper mapper = new RecordingFriendMapper();
        FriendService service = new FriendService(mapper);

        service.addFriend(1L, new FriendRequest(2L, 7L));

        assertThat(mapper.friendshipInserts).isEmpty();
        assertThat(mapper.insertedRequesterId).isEqualTo(1L);
        assertThat(mapper.insertedReceiverId).isEqualTo(2L);
        assertThat(mapper.insertedRequesterGroupId).isEqualTo(7L);
    }

    @Test
    void acceptingFriendRequestCreatesFriendshipsForBothUsers() {
        RecordingFriendMapper mapper = new RecordingFriendMapper();
        mapper.pendingRequest = new FriendRequestRow(20L, 2L, 1L, 8L);
        FriendService service = new FriendService(mapper);

        service.acceptFriendRequest(1L, 20L);

        assertThat(mapper.friendshipInserts).containsExactly(
            new FriendshipInsert(2L, 1L, 8L),
            new FriendshipInsert(1L, 2L, null)
        );
        assertThat(mapper.acceptedRequestId).isEqualTo(20L);
    }

    record FriendshipInsert(long ownerId, long friendId, Long groupId) {
    }

    static class RecordingFriendMapper implements FriendMapper {
        long insertedRequesterId;
        long insertedReceiverId;
        Long insertedRequesterGroupId;
        long acceptedRequestId;
        FriendRequestRow pendingRequest;
        List<FriendshipInsert> friendshipInserts = new ArrayList<>();

        @Override
        public List<FriendGroup> selectGroups(long userId) {
            return List.of();
        }

        @Override
        public int insertGroup(long userId, String name) {
            return 1;
        }

        @Override
        public int updateGroup(long userId, long groupId, String name) {
            return 1;
        }

        @Override
        public int deleteGroup(long userId, long groupId) {
            return 1;
        }

        @Override
        public List<FriendView> selectFriends(long userId) {
            return List.of();
        }

        @Override
        public List<UserSearchResult> searchUsers(long userId, String keyword) {
            return List.of();
        }

        @Override
        public int insertFriend(long ownerId, long friendId, Long groupId) {
            friendshipInserts.add(new FriendshipInsert(ownerId, friendId, groupId));
            return 1;
        }

        @Override
        public int deleteFriend(long ownerId, long friendId) {
            return 1;
        }

        @Override
        public int moveFriend(long ownerId, long friendId, Long groupId) {
            return 1;
        }

        @Override
        public int countOwnedGroup(long userId, long groupId) {
            return 1;
        }

        @Override
        public int countActiveUser(long userId) {
            return 1;
        }

        @Override
        public List<FriendRequestView> selectPendingRequests(long receiverId) {
            return List.of();
        }

        @Override
        public int insertFriendRequest(long requesterId, long receiverId, Long requesterGroupId) {
            insertedRequesterId = requesterId;
            insertedReceiverId = receiverId;
            insertedRequesterGroupId = requesterGroupId;
            return 1;
        }

        @Override
        public int countFriendship(long ownerId, long friendId) {
            return 0;
        }

        @Override
        public int countPendingRequest(long requesterId, long receiverId) {
            return 0;
        }

        @Override
        public FriendRequestRow selectPendingRequestForReceiver(long requestId, long receiverId) {
            return pendingRequest;
        }

        @Override
        public int markRequestAccepted(long requestId) {
            acceptedRequestId = requestId;
            return 1;
        }
    }
}
