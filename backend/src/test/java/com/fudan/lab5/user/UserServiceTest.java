package com.fudan.lab5.user;

import com.fudan.lab5.common.PasswordService;
import com.fudan.lab5.common.PasswordUpdateRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceTest {
    private final PasswordService passwordService = new PasswordService();

    @Test
    void updatePasswordVerifiesCurrentPasswordAndStoresNewHash() {
        RecordingUserMapper mapper = new RecordingUserMapper(passwordService.hash("oldpass"));
        UserService service = new UserService(mapper, passwordService);

        service.updatePassword(7L, new PasswordUpdateRequest("oldpass", "newpass", "newpass"));

        assertThat(mapper.updatedUserId).isEqualTo(7L);
        assertThat(passwordService.matches("newpass", mapper.updatedPasswordHash)).isTrue();
    }

    @Test
    void updatePasswordRejectsWrongCurrentPassword() {
        RecordingUserMapper mapper = new RecordingUserMapper(passwordService.hash("oldpass"));
        UserService service = new UserService(mapper, passwordService);

        assertThatThrownBy(() -> service.updatePassword(7L, new PasswordUpdateRequest("wrong", "newpass", "newpass")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("当前密码错误");
    }

    @Test
    void updatePasswordRequiresMatchingConfirmation() {
        RecordingUserMapper mapper = new RecordingUserMapper(passwordService.hash("oldpass"));
        UserService service = new UserService(mapper, passwordService);

        assertThatThrownBy(() -> service.updatePassword(7L, new PasswordUpdateRequest("oldpass", "newpass", "otherpass")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("两次输入的新密码不一致");
    }

    static class RecordingUserMapper implements UserMapper {
        private final String passwordHash;
        long updatedUserId;
        String updatedPasswordHash;

        RecordingUserMapper(String passwordHash) {
            this.passwordHash = passwordHash;
        }

        @Override
        public UserProfile selectProfile(long userId) {
            return new UserProfile(userId, "alice", "Alice", "", LocalDate.of(2000, 1, 1), 26);
        }

        @Override
        public int updateProfile(long userId, String displayName, String gender, LocalDate birthday, Integer age) {
            return 1;
        }

        @Override
        public List<UserSearchResult> searchUsers(long userId, String keyword) {
            return List.of();
        }

        @Override
        public String selectPasswordHash(long userId) {
            return passwordHash;
        }

        @Override
        public int updatePasswordHash(long userId, String passwordHash) {
            updatedUserId = userId;
            updatedPasswordHash = passwordHash;
            return 1;
        }
    }
}
