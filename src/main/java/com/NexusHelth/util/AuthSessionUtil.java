package com.NexusHelth.util;

import com.NexusHelth.model.User;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class AuthSessionUtil {

    private AuthSessionUtil() {
    }

    public static User getUser(HttpSession session) {
        // Prefer per-tab token auth when present (works across multiple logins in different tabs)
        HttpServletRequest request = currentRequest();
        if (request != null) {
            String token = TabAuthStore.extractToken(request);
            if (token != null) {
                User tokenUser = TabAuthStore.getUser(token);
                // If a token is supplied but not valid, treat as unauthenticated.
                // Do NOT fall back to shared HttpSession, otherwise per-tab logout/isolation breaks.
                return tokenUser;
            }
        }

        // Fallback to regular HttpSession cookie auth
        if (session == null) {
            return null;
        }
        Object userObj = session.getAttribute("user");
        return (userObj instanceof User) ? (User) userObj : null;
    }

    public static Integer getUserId(HttpSession session) {
        User user = getUser(session);
        if (user != null) {
            return user.getId();
        }

        if (session == null) {
            return null;
        }
        Object userIdObj = session.getAttribute("userId");
        return (userIdObj instanceof Integer) ? (Integer) userIdObj : null;
    }

    private static HttpServletRequest currentRequest() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            return servletAttrs.getRequest();
        }
        return null;
    }
}
