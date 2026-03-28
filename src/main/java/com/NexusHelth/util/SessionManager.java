package com.NexusHelth.util;

import com.NexusHelth.model.User;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {
    private static final Map<String, User> activeSessions = new HashMap<>();
    
    public static String createSession(User user) {
        String sessionId = UUID.randomUUID().toString();
        activeSessions.put(sessionId, user);
        return sessionId;
    }
    
    public static User getUser(String sessionId) {
        return activeSessions.get(sessionId);
    }
    
    public static boolean isValidSession(String sessionId) {
        return activeSessions.containsKey(sessionId);
    }
    
    public static void removeSession(String sessionId) {
        activeSessions.remove(sessionId);
    }
    
    public static void clearAllSessions() {
        activeSessions.clear();
    }
}
