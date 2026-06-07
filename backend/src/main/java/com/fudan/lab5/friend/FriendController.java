package com.fudan.lab5.friend;

import com.fudan.lab5.common.ApiResponse;
import com.fudan.lab5.common.CurrentSession;
import com.fudan.lab5.user.UserSearchResult;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FriendController {
    private final FriendService friendService;
    private final CurrentSession currentSession;

    public FriendController(FriendService friendService, CurrentSession currentSession) {
        this.friendService = friendService;
        this.currentSession = currentSession;
    }

    @GetMapping("/api/friends/search")
    public ApiResponse<List<UserSearchResult>> search(@RequestParam(defaultValue = "") String keyword, HttpSession session) {
        return ApiResponse.ok(friendService.searchUsers(currentSession.requireUserId(session), keyword));
    }

    @GetMapping("/api/friends")
    public ApiResponse<List<FriendView>> friends(HttpSession session) {
        return ApiResponse.ok(friendService.friends(currentSession.requireUserId(session)));
    }

    @PostMapping("/api/friends")
    public ApiResponse<Void> addFriend(@RequestBody FriendRequest request, HttpSession session) {
        friendService.addFriend(currentSession.requireUserId(session), request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/api/friends/{friendId}")
    public ApiResponse<Void> deleteFriend(@PathVariable long friendId, HttpSession session) {
        friendService.deleteFriend(currentSession.requireUserId(session), friendId);
        return ApiResponse.ok(null);
    }

    @PutMapping("/api/friends/{friendId}/group")
    public ApiResponse<Void> moveFriend(@PathVariable long friendId, @RequestBody MoveFriendRequest request, HttpSession session) {
        friendService.moveFriend(currentSession.requireUserId(session), friendId, request);
        return ApiResponse.ok(null);
    }

    @GetMapping("/api/friend-groups")
    public ApiResponse<List<FriendGroup>> groups(HttpSession session) {
        return ApiResponse.ok(friendService.groups(currentSession.requireUserId(session)));
    }

    @PostMapping("/api/friend-groups")
    public ApiResponse<Void> createGroup(@RequestBody GroupRequest request, HttpSession session) {
        friendService.createGroup(currentSession.requireUserId(session), request);
        return ApiResponse.ok(null);
    }

    @PutMapping("/api/friend-groups/{groupId}")
    public ApiResponse<Void> updateGroup(@PathVariable long groupId, @RequestBody GroupRequest request, HttpSession session) {
        friendService.updateGroup(currentSession.requireUserId(session), groupId, request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/api/friend-groups/{groupId}")
    public ApiResponse<Void> deleteGroup(@PathVariable long groupId, HttpSession session) {
        friendService.deleteGroup(currentSession.requireUserId(session), groupId);
        return ApiResponse.ok(null);
    }
}
