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

    public List<Map<String, Object>> getMajorConcentrations(String major) {
        if (major != null) {
            return executeQuery("SELECT * FROM v_major_concentrations WHERE major = ?", major);
        } else {
            return executeQuery("SELECT * FROM v_major_concentrations");
        }
    }

}
