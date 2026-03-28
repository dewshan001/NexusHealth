package com.NexusHelth.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import com.NexusHelth.config.DatabaseConfig;

public class DatabaseConnection {
    private static final String DATABASE_URL = "jdbc:sqlite:" + DatabaseConfig.getDatabasePath();
    
    public static Connection getConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(DATABASE_URL);
            // Enable autocommit to ensure data is persisted immediately
            conn.setAutoCommit(true);
            return conn;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
