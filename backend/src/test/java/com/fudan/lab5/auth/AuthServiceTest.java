package com.fudan.lab5.auth;

import com.fudan.lab5.common.PasswordResetRequest;
import com.fudan.lab5.common.PasswordService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthServiceTest {
    private final PasswordService passwordService = new PasswordService();

    @Test
    void resetUserPasswordStoresNewHashByTrimmedUsername() {
        RecordingAuthMapper mapper = new RecordingAuthMapper();
        AuthService service = new AuthService(mapper, passwordService);

        service.resetUserPassword(new PasswordResetRequest("  alice  ", "newpass", "newpass"));

        assertThat(mapper.updatedUserUsername).isEqualTo("alice");
        assertThat(passwordService.matches("newpass", mapper.updatedUserPasswordHash)).isTrue();
    }

    @Test
    void resetAdminPasswordStoresNewHashByTrimmedUsername() {
        RecordingAuthMapper mapper = new RecordingAuthMapper();
        AuthService service = new AuthService(mapper, passwordService);

        service.resetAdminPassword(new PasswordResetRequest("  admin  ", "newpass", "newpass"));

        assertThat(mapper.updatedAdminUsername).isEqualTo("admin");
        assertThat(passwordService.matches("newpass", mapper.updatedAdminPasswordHash)).isTrue();
    }

    @Test
    void resetPasswordRequiresMatchingConfirmation() {
        AuthService service = new AuthService(new RecordingAuthMapper(), passwordService);

        assertThatThrownBy(() -> service.resetUserPassword(new PasswordResetRequest("alice", "newpass", "otherpass")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("两次输入的新密码不一致");
    }

    static class RecordingAuthMapper implements AuthMapper {
        String updatedUserUsername;
        String updatedUserPasswordHash;
        String updatedAdminUsername;
        String updatedAdminPasswordHash;

        @Override
        public UserAccount selectUserByUsername(String username) {
            return null;
        }

        @Override
        public AdminAccount selectAdminByUsername(String username) {
            return null;
        }

        @Override
        public int insertUser(String username, String passwordHash, String displayName) {
            return 1;
        }

        @Override
        public int updateUserPasswordHashByUsername(String username, String passwordHash) {
            updatedUserUsername = username;
            updatedUserPasswordHash = passwordHash;
            return 1;
        }

        @Override
        public int updateAdminPasswordHashByUsername(String username, String passwordHash) {
            updatedAdminUsername = username;
            updatedAdminPasswordHash = passwordHash;
            return 1;
        }
    }
}
