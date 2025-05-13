import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
// RAG dependencies
import java.util.ArrayList;

public class ChatService extends BaseQueryProcessor {
    private static final String SQL_PATTERN = "```sql\\s+(.*?)\\s+```";
    private static final Logger logger = Logger.getLogger(ChatService.class.getName());
    
    private Chatbot chatService;
    private DatabaseService dbService;
    // RAG fields
    private EmbeddingService embeddingService;
    private VectorStoreClient vectorStoreClient;
    private static final int RAG_TOP_K = 3;

    public ChatService(String apiKey) {
        // Logging is configured via logging.properties
        this.chatService = new Chatbot(apiKey);
        this.dbService = new DatabaseService();
        // Initialize RAG components (update endpoint and key as needed)
        this.embeddingService = new EmbeddingService(System.getenv("OPENAI_API_KEY"));
        this.vectorStoreClient = new VectorStoreClient("http://localhost:8000"); // Update endpoint as needed
        // Initialize the context for the chatbot
        initializeContext();
    }
    
    private void initializeContext() {
        String systemPrompt = 
            "You are an AI assistant for an educational database system. " +
            "Answer questions by generating SQL queries that extract the needed information from these tables:\n\n" +
            
            "1. college (id, name)\n" +
            "2. student (id, firstname, lastname)\n" +
            "3. department (id, name, collegeID)\n" +
            "4. course (id, department, title, num, hrs)\n" +
            "5. major (id, title, deptID, reqtext, hrs, gpa)\n" +
            "6. teachers (id, firstname, lastname, departmentID, adjunct)\n" +
            "7. section (crn, max, room, courseID, term, startdate, enddate, days)\n" +
            "8. student_section (studentID, sectionID, grade)\n" +
            "9. major_class (majorID, classID)\n" +
            "10. and_prereq (course, prereq)\n" +
            "11. or_prereq (course, prereq)\n" +
            "12. coreq (course, prereq)\n" +
            "13. student_major (studentID, major)\n" +
            "14. concentration (id, major, title, reqtext)\n\n" +
            
            "After you get the results, explain them clearly and conversationally.";
            
        chatService.setSystemMessage(systemPrompt);
    }
    
    @Override
    public String processQuery(String userQuestion) {
        // --- RAG: Retrieve relevant document context ---
        List<String> retrievedContexts = new ArrayList<>();
        try {
            List<Double> queryEmbedding = embeddingService.getEmbedding(userQuestion);
            retrievedContexts = vectorStoreClient.search(queryEmbedding, RAG_TOP_K);
        } catch (Exception e) {
            logger.warning("RAG retrieval failed: " + e.getMessage());
        }
        String ragContext = String.join("\n", retrievedContexts);

        // --- Structured SQL retrieval as before ---
        String chatResponse = chatService.sendMessage(
            (ragContext.isEmpty() ? "" : ("Context:\n" + ragContext + "\n\n")) +
            "User question: \"" + userQuestion + "\"\n" +
            "Generate an appropriate SQL query to answer this question based on the database schema."
        );
        
        // Extract SQL query from the response
        Pattern sqlPattern = Pattern.compile(SQL_PATTERN, Pattern.DOTALL);
        Matcher sqlMatcher = sqlPattern.matcher(chatResponse);
        
        List<Map<String, Object>> results;
        
        if (sqlMatcher.find()) {
            String sqlQuery = sqlMatcher.group(1).trim();
            logger.info("Generated SQL query: " + sqlQuery);
            results = dbService.executeQuery(sqlQuery);
        } else {
            logger.warning("No SQL query found in the response. " +
                           "Response: " + chatResponse);
            return "I couldn't generate a proper SQL query for your question. " +
                   "Could you please rephrase your question?";
        }
        String formattedResults = formatResults(results);

        // --- RAG: Augment final prompt with retrieved context and SQL results ---
        return chatService.sendMessage(
            (ragContext.isEmpty() ? "" : ("Context:\n" + ragContext + "\n\n")) +
            "User question: \"" + userQuestion + "\"\n" +
            "Database results:\n" + formattedResults + 
            "\nPlease provide a helpful, conversational response based on these results and the context."
        );
    }
    
    // Format database results into a readable string
    protected String formatResults(List<Map<String, Object>> results) {
        if (results == null || results.isEmpty()) {
            return "No results found.";
        }
        
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> row : results) {
            sb.append("- ");
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(", ");
            }
            // Remove the trailing comma and space
            if (sb.length() > 2) {
                sb.setLength(sb.length() - 2);
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
    @Override
    public void close() {
        try {
            if (chatService != null) {
                chatService.close();
            }
            
            if (dbService != null) {
                dbService.close();
            }
        } catch (Exception e) {
            System.err.println("Error closing services: " + e.getMessage());
        }
    }
}