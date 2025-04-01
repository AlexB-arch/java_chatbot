import java.sql.*;
import java.util.*;

public class DatabaseService {
    private Connection conn;
    
    public DatabaseService() {
        try {
            // Connect to the SQLite database
            conn = DriverManager.getConnection("jdbc:sqlite:chatbotdb");
            System.out.println("Connected to database successfully");
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
    }
    
    public List<Map<String, Object>> executeQuery(String sql) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), rs.getObject(i));
                }
                results.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
            e.printStackTrace();
        }
        
        return results;
    }
    
    public void close() {
        try {
            if (conn != null) {
                conn.close();
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database: " + e.getMessage());
        }
    }

    public boolean isClosed() {
        try {
            return conn == null || conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Error checking connection status: " + e.getMessage());
            return true;
        }
    }
}