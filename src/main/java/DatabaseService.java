import java.sql.*;
import java.util.*;

public class DatabaseService {
    private DBConnection dbConnection;

    public DatabaseService() {
        this.dbConnection = new DBConnection();
        this.dbConnection.connect();
    }

    public List<Map<String, Object>> executeQuery(String query, Object... params) {
        List<Map<String, Object>> results = new ArrayList<>();

        if (!dbConnection.isConnected()) {
            System.err.println("Database connection not available");
            return results;
        }

        try {
            // Driver to talk to the database
            Connection connection = DriverManager.getConnection("jdbc:sqlite:chatbotdb");
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                // Set parameters
                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i + 1, params[i]);
                }

            // Execute and get results
            try (ResultSet resultSet = statement.executeQuery()) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (resultSet.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(metaData.getColumnName(i), resultSet.getObject(i));
                    }
                    results.add(row);
                }
            }
        } 
    } catch (SQLException e) {
        System.err.println("Error executing query: " + e.getMessage());
        }

        return results;
    }

    public boolean executeUpdate(String query, Object... params) {
        if (!dbConnection.isConnected()) {
            System.err.println("Database connection not available");
            return false;
        }

        try {
            // Driver to talk to the database
            Connection connection = DriverManager.getConnection("jdbc:sqlite:chatbotdb");
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                // Set parameters
                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i + 1, params[i]);
                }

                // Execute update
                int rowsAffected = statement.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error executing update: " + e.getMessage());
            return false;
        }
    }

    public void close() {
        dbConnection.closeConnection();
    }

    // Knowledge base queries
    public List<Map<String, Object>> findAnswer(String topic) {
        String query = "SELECT answer FROM knowledge_base WHERE topic LIKE ?";
        return executeQuery(query, "%" + topic + "%");
    }

    public List<Map<String, Object>> searchByKeywords(List<String> keywords) {
        if (keywords.isEmpty()) {
            return new ArrayList<>();
        }
    
        StringBuilder queryBuilder = new StringBuilder();
        // There is no table called knowledge base
    
        for (int i = 0; i < keywords.size(); i++) {
            if (i > 0) {
                queryBuilder.append(" OR ");
            }
            queryBuilder.append("topic LIKE ? OR content LIKE ?");
        }
    
        Object[] params = new Object[keywords.size() * 2];
        for (int i = 0; i < keywords.size(); i++) {
            params[i * 2] = "%" + keywords.get(i) + "%";
            params[i * 2 + 1] = "%" + keywords.get(i) + "%";
        }
    
        return executeQuery(queryBuilder.toString(), params);
    }
}
