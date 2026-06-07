package com.fudan.lab5.admin;

import com.fudan.lab5.post.CommentSummary;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AdminServiceTest {
    @Test
    void reviewPostsIncludeCommentsForCardDisplay() {
        RecordingAdminMapper mapper = new RecordingAdminMapper();
        AdminService service = new AdminService(mapper);

        List<AdminPostReview> posts = service.reviewPosts();

        assertThat(posts).hasSize(1);
        assertThat(posts.getFirst().authorUsername()).isEqualTo("alice");
        assertThat(posts.getFirst().comments()).containsExactly(
            new CommentSummary(3L, 2L, "bob", "Looks good", LocalDateTime.of(2026, 6, 7, 12, 10))
        );
    }

    static class RecordingAdminMapper implements AdminMapper {
        @Override
        public List<AdminPostReviewRow> selectReviewPostRows() {
            return List.of(new AdminPostReviewRow(
                8L,
                1L,
                "alice",
                "Need review",
                "VISIBLE",
                LocalDateTime.of(2026, 6, 7, 12, 0),
                LocalDateTime.of(2026, 6, 7, 12, 0),
                1L
            ));
        }

        @Override
        public List<CommentSummary> selectComments(long postId) {
            return List.of(new CommentSummary(3L, 2L, "bob", "Looks good", LocalDateTime.of(2026, 6, 7, 12, 10)));
        }

        @Override
        public int deletePost(long postId) {
            return 1;
        }

        @Override
        public int deleteUser(long userId) {
            return 1;
        }

        @Override
        public int insertAuditLog(Long postId, long adminId, String action, String reason) {
            return 1;
        }

        @Override
        public AdminProfile selectProfile(long adminId) {
            return new AdminProfile(adminId, "admin", "System Admin");
        }

        @Override
        public int updateProfile(long adminId, String displayName) {
            return 1;
        }
    }
}
