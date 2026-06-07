package com.fudan.lab5.post;

import com.fudan.lab5.common.ApiResponse;
import com.fudan.lab5.common.CurrentSession;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PostControllerTest {
    private final RecordingPostService postService = new RecordingPostService();
    private final PostController controller = new PostController(postService, new CurrentSession());

    @Test
    void returnsVisibilityRulesForCurrentUsersOwnPost() {
        MockHttpSession session = loggedInUser();
        PostVisibilityRule rule = new PostVisibilityRule(1L, 12L, "ALLOW", "GROUP", 7L, "项目组");
        postService.visibilityRules = List.of(rule);

        ApiResponse<List<PostVisibilityRule>> response = controller.visibility(12L, session);

        assertThat(response.success()).isTrue();
        assertThat(response.data()).containsExactly(rule);
        assertThat(postService.visibilityUserId).isEqualTo(42L);
        assertThat(postService.visibilityPostId).isEqualTo(12L);
    }

    @Test
    void updatesVisibilityRulesForCurrentUsersOwnPost() {
        MockHttpSession session = loggedInUser();
        PostVisibilityUpdateRequest request = new PostVisibilityUpdateRequest("DENY", "USER", List.of(2L));

        ApiResponse<Void> response = controller.updateVisibility(12L, request, session);

        assertThat(response.success()).isTrue();
        assertThat(postService.updateUserId).isEqualTo(42L);
        assertThat(postService.updatePostId).isEqualTo(12L);
        assertThat(postService.updateRequest).isEqualTo(request);
    }

    private MockHttpSession loggedInUser() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 42L);
        return session;
    }

    static class RecordingPostService extends PostService {
        long visibilityUserId;
        long visibilityPostId;
        long updateUserId;
        long updatePostId;
        PostVisibilityUpdateRequest updateRequest;
        List<PostVisibilityRule> visibilityRules = new ArrayList<>();

        RecordingPostService() {
            super(null);
        }

        @Override
        public List<PostVisibilityRule> visibilityRules(long userId, long postId) {
            visibilityUserId = userId;
            visibilityPostId = postId;
            return visibilityRules;
        }

        @Override
        public void updateVisibility(long userId, long postId, PostVisibilityUpdateRequest request) {
            updateUserId = userId;
            updatePostId = postId;
            updateRequest = request;
        }
    }
}
