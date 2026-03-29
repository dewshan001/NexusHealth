package com.NexusHelth.util;

import com.NexusHelth.model.User;
import jakarta.servlet.http.HttpServletRequest;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory per-tab authentication tokens.
 *
 * Token is expected to be stored in browser tab's sessionStorage and sent on each API call
 * as the {@code X-Tab-Token} header (or as a {@code tabToken} request parameter for non-AJAX form posts).
 */
public final class TabAuthStore {

    public static final String HEADER_NAME = "X-Tab-Token";
    public static final String PARAM_NAME = "tabToken";

    private static final Duration TOKEN_TTL = Duration.ofHours(12);
    private static final Duration CLEANUP_INTERVAL = Duration.ofMinutes(5);

    private static final SecureRandom RNG = new SecureRandom();
    private static final ConcurrentMap<String, Entry> TOKENS = new ConcurrentHashMap<>();
    private static final AtomicLong lastCleanupEpochMillis = new AtomicLong(0);

    private TabAuthStore() {
    }

    public static String issueToken(User user) {
        Objects.requireNonNull(user, "user");

        maybeCleanup();

        String token = generateToken();
        Instant now = Instant.now();
        User safeUser = sanitize(user);
        TOKENS.put(token, new Entry(safeUser, now.plus(TOKEN_TTL), now));
        return token;
    }

    public static User getUser(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        maybeCleanup();

        Entry entry = TOKENS.get(token);
        if (entry == null) {
            return null;
        }

        Instant now = Instant.now();
        if (entry.expiresAt.isBefore(now)) {
            TOKENS.remove(token);
            return null;
        }

        // Sliding expiration: keep active tabs alive.
        entry.lastSeen = now;
        entry.expiresAt = now.plus(TOKEN_TTL);
        return entry.user;
    }

    public static void revoke(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        TOKENS.remove(token);
    }

    public static String extractToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String headerValue = request.getHeader(HEADER_NAME);
        if (headerValue != null && !headerValue.isBlank()) {
            return headerValue.trim();
        }

        String paramValue = request.getParameter(PARAM_NAME);
        if (paramValue != null && !paramValue.isBlank()) {
            return paramValue.trim();
        }

        return null;
    }

    private static void maybeCleanup() {
        long nowMs = System.currentTimeMillis();
        long lastMs = lastCleanupEpochMillis.get();
        if (nowMs - lastMs < CLEANUP_INTERVAL.toMillis()) {
            return;
        }
        if (!lastCleanupEpochMillis.compareAndSet(lastMs, nowMs)) {
            return;
        }

        Instant now = Instant.now();
        TOKENS.entrySet().removeIf(e -> e.getValue().expiresAt.isBefore(now));
    }

    private static String generateToken() {
        byte[] bytes = new byte[32];
        RNG.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static User sanitize(User user) {
        User safe = new User();
        safe.setId(user.getId());
        safe.setFullName(user.getFullName());
        safe.setEmail(user.getEmail());
        safe.setRole(user.getRole());
        safe.setStatus(user.getStatus());
        safe.setPhone(user.getPhone());
        safe.setProfilePicture(user.getProfilePicture());
        safe.setPassword(null);
        return safe;
    }

    private static final class Entry {
        private final User user;
        private volatile Instant expiresAt;
        private volatile Instant lastSeen;

        private Entry(User user, Instant expiresAt, Instant lastSeen) {
            this.user = user;
            this.expiresAt = expiresAt;
            this.lastSeen = lastSeen;
        }
    }
}
