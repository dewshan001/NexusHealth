
import java.sql.*;
public class test_delete {
    public static void main(String[] args) throws Exception {
        String dbUrl = "jdbc:sqlite:clinic.db";
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                pstmt.setInt(1, 4);
                int rows = pstmt.executeUpdate();
                System.out.println("Rows deleted: " + rows);
            }
        }
    }
}

