package com.fudan.lab5.admin;

import com.fudan.lab5.common.ApiResponse;
import com.fudan.lab5.common.CurrentSession;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AdminControllerTest {
    private final RecordingAdminService adminService = new RecordingAdminService();
    private final AdminController controller = new AdminController(adminService, new CurrentSession());

    @Test
    void returnsAuditLogsForCurrentAdmin() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("adminId", 99L);
        AdminAuditLog log = new AdminAuditLog(
            5L,
            8L,
            99L,
            "admin",
            "ADMIN_DELETE_POST",
            "管理员审核删除朋友圈",
            LocalDateTime.of(2026, 6, 7, 12, 30)
        );
        adminService.logs = List.of(log);

        ApiResponse<List<AdminAuditLog>> response = controller.auditLogs(session);

        assertThat(response.success()).isTrue();
        assertThat(response.data()).containsExactly(log);
        assertThat(adminService.auditLogsCalled).isTrue();
    }

    static class RecordingAdminService extends AdminService {
        boolean auditLogsCalled;
        List<AdminAuditLog> logs = List.of();

        RecordingAdminService() {
            super(null, null);
        }

        @Override
        public List<AdminAuditLog> auditLogs() {
            auditLogsCalled = true;
            return logs;
        }
    }
}
