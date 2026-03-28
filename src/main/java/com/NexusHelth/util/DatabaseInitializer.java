package com.NexusHelth.util;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.sql.Connection;
import java.sql.Statement;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class DatabaseInitializer implements CommandLineRunner {
    
    @Override
    public void run(String... args) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Read schema.sql from classpath (works both in development and JAR)
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("schema.sql");
            
            if (inputStream == null) {
                System.err.println("❌ Error: schema.sql not found in classpath");
                return;
            }
            
            String schema = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            
            // Execute schema
            String[] statements = schema.split(";");
            for (String sql : statements) {
                String trimmed = sql.trim();
                if (!trimmed.isEmpty()) {
                    try {
                        stmt.execute(trimmed);
                    } catch (Exception e) {
                        // Ignore "table already exists" errors
                        if (!e.getMessage().contains("already exists")) {
                            System.err.println("⚠️  SQL Error: " + e.getMessage());
                        }
                    }
                }
            }
            
            System.out.println("✅ Database schema initialized successfully");
            
            // Initialize default admin account
            AdminInitializer.initializeAdminAccount();
        } catch (Exception e) {
            System.err.println("❌ Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

