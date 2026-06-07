package com.fudan.lab5.post;

import com.fudan.lab5.common.ApiResponse;
import com.fudan.lab5.common.CurrentSession;
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
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;
    private final CurrentSession currentSession;

    public PostController(PostService postService, CurrentSession currentSession) {
        this.postService = postService;
        this.currentSession = currentSession;
    }

    @GetMapping("/feed")
    public ApiResponse<List<PostSummary>> feed(@RequestParam(required = false) String keyword, HttpSession session) {
        return ApiResponse.ok(postService.feedForUser(currentSession.requireUserId(session), keyword));
    }

    @GetMapping("/mine")
    public ApiResponse<List<PostSummary>> mine(HttpSession session) {
        return ApiResponse.ok(postService.myPosts(currentSession.requireUserId(session)));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody PostCreateRequest request, HttpSession session) {
        postService.createPost(currentSession.requireUserId(session), request);
        return ApiResponse.ok(null);
    }

    @PutMapping("/{postId}")
    public ApiResponse<Void> update(@PathVariable long postId, @RequestBody PostUpdateRequest request, HttpSession session) {
        postService.updatePost(currentSession.requireUserId(session), postId, request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<Void> delete(@PathVariable long postId, HttpSession session) {
        postService.deletePost(currentSession.requireUserId(session), postId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{postId}/comments")
    public ApiResponse<Void> comment(@PathVariable long postId, @RequestBody CommentCreateRequest request, HttpSession session) {
        postService.comment(currentSession.requireUserId(session), postId, request);
        return ApiResponse.ok(null);
    }
}
