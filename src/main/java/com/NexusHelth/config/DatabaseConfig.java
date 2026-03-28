package com.NexusHelth.config;

import org.springframework.context.annotation.Configuration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Database configuration that ensures data persistence across project moves.
 * Stores the SQLite database in a fixed location in the user's home directory.
 */
@Configuration
public class DatabaseConfig {
    
    public static final String DB_DIR_NAME = ".clinic";
    public static final String DB_FILE_NAME = "clinic.db";
    
    /**
     * Gets the absolute path to the clinic database file in the project folder.
     * 
     * @return Absolute path to clinic.db
     */
    public static String getDatabasePath() {
        try {
            // Use project directory to store data
            Path dbDir = Paths.get(System.getProperty("user.dir"));
            
            Path dbFile = dbDir.resolve(DB_FILE_NAME);
            System.out.println("✅ Database path: " + dbFile);
            return dbFile.toString();
        } catch (Exception e) {
            System.err.println("❌ Error setting up database directory: " + e.getMessage());
            // Fallback to project directory
            return DB_FILE_NAME;
        }
    }
}

