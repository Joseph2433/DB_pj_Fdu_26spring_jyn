package com.fudan.lab5.admin;

import com.fudan.lab5.common.PasswordService;
import com.fudan.lab5.common.PasswordUpdateRequest;
import com.fudan.lab5.post.CommentSummary;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminServiceTest {
    private final PasswordService passwordService = new PasswordService();

    @Test
    void reviewPostsIncludeCommentsForCardDisplay() {
        RecordingAdminMapper mapper = new RecordingAdminMapper();
        AdminService service = new AdminService(mapper, passwordService);

        List<AdminPostReview> posts = service.reviewPosts();

        assertThat(posts).hasSize(1);
        assertThat(posts.getFirst().authorUsername()).isEqualTo("alice");
        assertThat(posts.getFirst().comments()).containsExactly(
            new CommentSummary(3L, 2L, "bob", "Looks good", LocalDateTime.of(2026, 6, 7, 12, 10))
        );
    }

    @Test
    void deleteUserByUsernameUsesTrimmedAccountName() {
        RecordingAdminMapper mapper = new RecordingAdminMapper();
        AdminService service = new AdminService(mapper, passwordService);

        service.deleteUserByUsername(99L, "  bob  ");

        assertThat(mapper.deletedUsername).isEqualTo("bob");
        assertThat(mapper.auditReason).isEqualTo("管理员注销用户 bob");
    }

    @Test
    void deleteUserByUsernameRejectsBlankAccountName() {
        AdminService service = new AdminService(new RecordingAdminMapper(), passwordService);

        assertThatThrownBy(() -> service.deleteUserByUsername(99L, "  "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("用户账号不能为空");
    }

    @Test
    void updatePasswordVerifiesCurrentPasswordAndStoresNewHash() {
        RecordingAdminMapper mapper = new RecordingAdminMapper();
        mapper.passwordHash = passwordService.hash("oldpass");
        AdminService service = new AdminService(mapper, passwordService);

        service.updatePassword(99L, new PasswordUpdateRequest("oldpass", "newpass", "newpass"));

        assertThat(mapper.updatedAdminId).isEqualTo(99L);
        assertThat(passwordService.matches("newpass", mapper.updatedPasswordHash)).isTrue();
    }

    @Test
    void updatePasswordRejectsWrongCurrentPassword() {
        RecordingAdminMapper mapper = new RecordingAdminMapper();
        mapper.passwordHash = passwordService.hash("oldpass");
        AdminService service = new AdminService(mapper, passwordService);

        assertThatThrownBy(() -> service.updatePassword(99L, new PasswordUpdateRequest("wrong", "newpass", "newpass")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("当前密码错误");
    }

    static class RecordingAdminMapper implements AdminMapper {
        String deletedUsername;
        String auditReason;
        String passwordHash;
        long updatedAdminId;
        String updatedPasswordHash;

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
        public int deleteUserByUsername(String username) {
            deletedUsername = username;
            return 1;
        }

        @Override
        public int insertAuditLog(Long postId, long adminId, String action, String reason) {
            auditReason = reason;
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

        @Override
        public String selectPasswordHash(long adminId) {
            return passwordHash;
        }

        @Override
        public int updatePasswordHash(long adminId, String passwordHash) {
            updatedAdminId = adminId;
            updatedPasswordHash = passwordHash;
            return 1;
        }
    }
}
