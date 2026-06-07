package com.fudan.lab5.post;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PostService {
    private static final int POST_LIMIT = 280;
    private static final int COMMENT_LIMIT = 160;

    private final PostMapper postMapper;

    public PostService(PostMapper postMapper) {
        this.postMapper = postMapper;
    }

    public List<PostSummary> feedForUser(long userId, String keyword) {
        String normalized = keyword == null || keyword.trim().isEmpty() ? null : keyword.trim();
        return withComments(postMapper.selectFriendFeedRows(userId, normalized));
    }

    public List<PostSummary> myPosts(long userId) {
        return withComments(postMapper.selectMyPostRows(userId));
    }

    public List<PostSummary> friendPosts(long userId, long friendId) {
        return withComments(postMapper.selectFriendPostRows(userId, friendId));
    }

    @Transactional
    public void createPost(long userId, PostCreateRequest request) {
        postMapper.insertPost(userId, validateText(request.content(), POST_LIMIT, "朋友圈内容"));
    }

    @Transactional
    public void updatePost(long userId, long postId, PostUpdateRequest request) {
        if (postMapper.updateOwnPost(postId, userId, validateText(request.content(), POST_LIMIT, "朋友圈内容")) == 0) {
            throw new IllegalArgumentException("只能修改自己的朋友圈");
        }
    }

    @Transactional
    public void deletePost(long userId, long postId) {
        if (postMapper.deleteOwnPost(postId, userId) == 0) {
            throw new IllegalArgumentException("只能删除自己的朋友圈");
        }
    }

    @Transactional
    public void comment(long userId, long postId, CommentCreateRequest request) {
        if (postMapper.countVisibleToUser(postId, userId) == 0) {
            throw new IllegalArgumentException("朋友圈不存在或不可见");
        }
        postMapper.insertComment(postId, userId, validateText(request.content(), COMMENT_LIMIT, "评论内容"));
    }

    private List<PostSummary> withComments(List<PostRow> rows) {
        return rows.stream()
            .map(row -> new PostSummary(row.id(), row.authorId(), row.authorUsername(), row.content(), row.lastUpdatedAt(), postMapper.selectComments(row.id())))
            .toList();
    }

    private String validateText(String content, int limit, String label) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException(label + "不能为空");
        }
        String normalized = content.trim();
        if (normalized.length() > limit) {
            throw new IllegalArgumentException(label + "不能超过 " + limit + " 字");
        }
        return normalized;
    }
}
