import java.sql.*;

public class DBConnection {

    private Connection connection = null;

    public void connect() {
        // Connect to the database
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:chatbotdb");
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database: " + e.getMessage());
            System.exit(1);
        }
    }

    // Close the database connection
    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.err.println("Failed to close the database connection: " + e.getMessage());
        } finally {
            connection = null;
        }
    }

    // Verify if the connection is successful
    public boolean isConnected() {
        return connection != null;
    }
}