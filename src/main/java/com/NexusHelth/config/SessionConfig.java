package com.NexusHelth.config;

import org.springframework.context.annotation.Configuration;

/**
 * Session Configuration for Security and Cookie Management
 * 
 * Session cookie properties are configured via application.properties:
 * - server.servlet.session.timeout=30m (30 minute timeout)
 * - server.servlet.session.cookie.http-only=true (XSS protection)
 * - server.servlet.session.cookie.same-site=lax (CSRF protection)
 * - server.servlet.session.cookie.name=JSESSIONID
 * 
 * This class serves as a marker for session configuration
 */
@Configuration
public class SessionConfig {
    
    // Session cookie configuration is handled via application.properties
    // Spring Boot automatically applies these settings to all session cookies
    
}




