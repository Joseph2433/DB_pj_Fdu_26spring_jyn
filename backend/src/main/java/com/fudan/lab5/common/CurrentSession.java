package com.fudan.lab5.common;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class CurrentSession {
    public long requireUserId(HttpSession session) {
        Object value = session.getAttribute("userId");
        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new IllegalStateException("请先登录用户账号");
    }

    public long requireAdminId(HttpSession session) {
        Object value = session.getAttribute("adminId");
        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new IllegalStateException("请先登录管理员账号");
    }
}
