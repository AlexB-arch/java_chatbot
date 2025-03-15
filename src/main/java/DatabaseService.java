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
        String query = "SELECT answer FROM ? WHERE topic LIKE ?";
        return executeQuery(query, "%" + topic + "%");
    }

    public List<Map<String, Object>> searchByKeywords(List<String> keywords) {
        if (keywords.isEmpty()) {
            return new ArrayList<>();
        }

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT * FROM ? WHERE ");
        for (int i = 0; i < keywords.size(); i++) {
            if (i > 0) {
                queryBuilder.append(" OR ");
            }
            queryBuilder.append("content LIKE ? OR question LIKE ?");
        }
        
        Object[] params = new Object[keywords.size() * 3 + 1];
        params[0] = "knowledge_base";
        for (int i = 0; i < keywords.size(); i++) {
            params[i * 3 + 1] = "%" + keywords.get(i) + "%";
            params[i * 3 + 2] = "%" + keywords.get(i) + "%";
            params[i * 3 + 3] = "%" + keywords.get(i) + "%";
        }
        
        return executeQuery(queryBuilder.toString(), params);
    }

    /**
     * Intelligently searches the knowledge base based on user input
     * @param userQuery The natural language query from the user
     * @param relevantKeywords List of extracted keywords from NLP analysis
     * @return List of potential answers
     */
    public List<Map<String, Object>> intelligentSearch(String userQuery, List<String> relevantKeywords) {
        // First try exact match
        List<Map<String, Object>> exactMatches = executeQuery(
            "SELECT * FROM knowledge_base WHERE topic = ? OR question = ?", 
            userQuery, userQuery
        );
        
        if (!exactMatches.isEmpty()) {
            return exactMatches;
        }
        
        // Next try keyword-based search with relevance ranking
        if (!relevantKeywords.isEmpty()) {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT *, (");
            
            // Add relevance score calculation based on keyword matches
            for (int i = 0; i < relevantKeywords.size(); i++) {
                if (i > 0) {
                    queryBuilder.append(" + ");
                }
                queryBuilder.append("(CASE WHEN topic LIKE ? THEN 3 ELSE 0 END)");
                queryBuilder.append(" + (CASE WHEN question LIKE ? THEN 2 ELSE 0 END)");
                queryBuilder.append(" + (CASE WHEN content LIKE ? THEN 1 ELSE 0 END)");
            }
            
            queryBuilder.append(") AS relevance_score FROM knowledge_base ");
            queryBuilder.append("WHERE ");
            
            for (int i = 0; i < relevantKeywords.size(); i++) {
                if (i > 0) {
                    queryBuilder.append(" OR ");
                }
                queryBuilder.append("topic LIKE ? OR question LIKE ? OR content LIKE ?");
            }
            
            queryBuilder.append(" ORDER BY relevance_score DESC LIMIT 5");
            
            Object[] params = new Object[relevantKeywords.size() * 6];
            for (int i = 0; i < relevantKeywords.size(); i++) {
                // For relevance scoring
                params[i * 6] = "%" + relevantKeywords.get(i) + "%";
                params[i * 6 + 1] = "%" + relevantKeywords.get(i) + "%";
                params[i * 6 + 2] = "%" + relevantKeywords.get(i) + "%";
                
                // For WHERE clause
                params[i * 6 + 3] = "%" + relevantKeywords.get(i) + "%";
                params[i * 6 + 4] = "%" + relevantKeywords.get(i) + "%";
                params[i * 6 + 5] = "%" + relevantKeywords.get(i) + "%";
            }
            
            return executeQuery(queryBuilder.toString(), params);
        }
        
        // Fallback to fuzzy search
        return executeQuery(
            "SELECT * FROM knowledge_base WHERE topic LIKE ? OR content LIKE ?",
            "%" + userQuery + "%", "%" + userQuery + "%"
        );
    }
    
    /**
     * Query by entity type and value (e.g., find answers about a specific person)
     * @param entityType Type of entity ("person", "location", etc.)
     * @param entityValue The entity name or value
     * @return Matching results
     */
    public List<Map<String, Object>> queryByEntity(String entityType, String entityValue) {
        return executeQuery(
            "SELECT * FROM knowledge_base WHERE entity_type = ? AND content LIKE ?",
            entityType, "%" + entityValue + "%"
        );
    }
    
    /**
     * Handle question-answer matching
     * @param question The user's question
     * @return Best matching answers
     */
    public List<Map<String, Object>> answerQuestion(String question) {
        // First try direct question match
        List<Map<String, Object>> directMatches = executeQuery(
            "SELECT * FROM knowledge_base WHERE question LIKE ?",
            "%" + question + "%"
        );
        
        if (!directMatches.isEmpty()) {
            return directMatches;
        }
        
        // Extract potential topic from question
        String simplifiedQuestion = question.replaceAll("(?i)what is|how do|can you tell me about|who is|where is", "").trim();
        
        return executeQuery(
            "SELECT * FROM knowledge_base WHERE topic LIKE ? OR content LIKE ?",
            "%" + simplifiedQuestion + "%", "%" + simplifiedQuestion + "%"
        );
    }
    
    /**
     * Find similar content based on previous interactions
     * @param userInput Current user input
     * @param previousContext Previous conversation context
     * @return Contextually relevant answers
     */
    public List<Map<String, Object>> contextualSearch(String userInput, String previousContext) {
        // Combine current input with previous context for better matching
        return executeQuery(
            "SELECT *, " +
            "(CASE WHEN content LIKE ? THEN 3 ELSE 0 END) + " +
            "(CASE WHEN content LIKE ? THEN 1 ELSE 0 END) AS relevance " +
            "FROM knowledge_base " +
            "WHERE content LIKE ? OR content LIKE ? " +
            "ORDER BY relevance DESC LIMIT 3",
            "%" + userInput + "%", "%" + previousContext + "%",
            "%" + userInput + "%", "%" + previousContext + "%"
        );
    }
}
