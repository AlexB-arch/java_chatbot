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
    // RAG pipeline
    private RAGPipeline ragPipeline;
    private static final int RAG_TOP_K = 3;

    public ChatService(String apiKey) {
        // Logging is configured via logging.properties
        this.chatService = new Chatbot(apiKey);
        this.dbService = new DatabaseService();
        EmbeddingService embeddingService = new EmbeddingService(System.getenv("OPENAI_API_KEY"));
        ElasticsearchVectorStoreClient vectorStoreClient = new ElasticsearchVectorStoreClient("localhost", 9200, "rag_vectors", 1536);
        this.ragPipeline = new RAGPipeline(embeddingService, vectorStoreClient, System.getenv("OPENAI_API_KEY"), 500);
        // Example: Ingest a PDF file from the root folder
        // DocumentIngestor ingestor = new DocumentIngestor(this.ragPipeline);
        // ingestor.ingestPdfFile("dafi21-101.pdf");
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
        try {
            return ragPipeline.query(userQuestion, RAG_TOP_K);
        } catch (Exception e) {
            logger.warning("RAGPipeline query failed: " + e.getMessage());
            return "Sorry, I couldn't process your question due to an internal error.";
        }
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