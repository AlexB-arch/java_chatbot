<<<<<<< HEAD
<<<<<<< HEAD
=======
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.AiMessage;

import java.util.ArrayList;
>>>>>>> 1ea2aac (Adds langchain for openAI API support)
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import dev.langchain4j.data.segment.TextSegment;

<<<<<<< HEAD
public class ChatService extends BaseQueryProcessor {
    private static final String SQL_PATTERN = "```sql\\s+(.*?)\\s+```";
    private static final Logger logger = Logger.getLogger(ChatService.class.getName());
=======
public class ChatService {
    private final ChatLanguageModel chatModel;
    private final List<ChatMessage> conversationHistory = new ArrayList<>();
>>>>>>> 1ea2aac (Adds langchain for openAI API support)
=======
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatService extends BaseQueryProcessor {
    private static final String SQL_PATTERN = "```sql\\s+(.*?)\\s+```";
    
    private Chatbot chatService;
    private DatabaseService dbService;
>>>>>>> 4bf41d9 (Refactoring and cleanup)
    
    private Chatbot chatService;
    private DatabaseService dbService;
    private VectorStoreService vectorStoreService; // Add this field
    
    public ChatService(String apiKey, FileHandler fileHandler) {
        // Add the file handler to this class's logger
        logger.addHandler(fileHandler);
        
        this.chatService = new Chatbot(apiKey);
        this.dbService = new DatabaseService();
        this.vectorStoreService = new VectorStoreService(apiKey, this.dbService); // Initialize it
        this.vectorStoreService.initializeVectorStore(); // Load data
        
        // Initialize the context for the chatbot
        initializeContext();
    }
    
    // Also provide the original constructor for backward compatibility
    public ChatService(String apiKey) {
<<<<<<< HEAD
<<<<<<< HEAD
        this.chatService = new Chatbot(apiKey);
        this.dbService = new DatabaseService();
        
        try {
            this.vectorStoreService = new VectorStoreService(apiKey, this.dbService);
            if (this.vectorStoreService.testVectorStore()) {
                this.vectorStoreService.initializeVectorStore();
                logger.info("Vector store initialized successfully");
            } else {
                logger.warning("Vector store test failed, proceeding without vector search capability");
            }
        } catch (Exception e) {
            logger.severe("Error initializing vector store: " + e.getMessage());
            e.printStackTrace();
        }
        
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
        // Add semantic search capability here
        if (isSemanticSearchQuery(userQuestion)) {
            return handleSemanticSearch(userQuestion);
        }
        
        // Ask the LLM to generate a SQL query for the question
        String chatResponse = chatService.sendMessage(
            "User question: \"" + userQuestion + "\"\n" +
            "Generate an appropriate SQL query to answer this question based on the database schema."
        );
        
        // Extract SQL query from the response
        Pattern sqlPattern = Pattern.compile(SQL_PATTERN, Pattern.DOTALL);
        Matcher sqlMatcher = sqlPattern.matcher(chatResponse);
        
        List<Map<String, Object>> results;
        
        if (sqlMatcher.find()) {
            String sqlQuery = sqlMatcher.group(1).trim();
            
            // Log the generated SQL query to the logs file
            logger.info("Generated SQL query: " + sqlQuery);
            
            results = dbService.executeQuery(sqlQuery);
        } else {
            // Fallback if no SQL was found
            logger.warning("No SQL query found in the response. " +
                           "Response: " + chatResponse);
            return "I couldn't generate a proper SQL query for your question. " +
                   "Could you please rephrase your question?";
        }
        
        // Format the results
        String formattedResults = formatResults(results);
        
        // Ask the LLM to generate a conversational response based on the results
        return chatService.sendMessage(
            "User question: \"" + userQuestion + "\"\n" +
            "Database results:\n" + formattedResults + 
            "\nPlease provide a helpful, conversational response based on these results."
        );
=======
        this.chatModel = OpenAiChatModel.builder()
            .apiKey(apiKey)
            .timeout(Duration.ofSeconds(30))
            .modelName("gpt-4o")
            .temperature(0.3)
            .maxTokens(500)
            .build();
=======
        this.chatService = new Chatbot(apiKey);
        this.dbService = new DatabaseService();
        
        // Initialize the context for the chatbot
        initializeContext();
>>>>>>> 4bf41d9 (Refactoring and cleanup)
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
        // Ask the LLM to generate a SQL query for the question
        String chatResponse = chatService.sendMessage(
            "User question: \"" + userQuestion + "\"\n" +
            "Generate an appropriate SQL query to answer this question based on the database schema."
        );
        
        // Extract SQL query from the response
        Pattern sqlPattern = Pattern.compile(SQL_PATTERN, Pattern.DOTALL);
        Matcher sqlMatcher = sqlPattern.matcher(chatResponse);
        
        List<Map<String, Object>> results;
        
        if (sqlMatcher.find()) {
            String sqlQuery = sqlMatcher.group(1).trim();
            results = dbService.executeQuery(sqlQuery);
        } else {
            // Fallback if no SQL was found
            return "I couldn't generate a proper SQL query for your question. " +
                   "Could you please rephrase your question?";
        }
        
<<<<<<< HEAD
        // Return the response text
        return aiMessage.text();
>>>>>>> 1ea2aac (Adds langchain for openAI API support)
    }
    
    private boolean isSemanticSearchQuery(String query) {
        String lowerQuery = query.toLowerCase();
        return lowerQuery.contains("similar") || 
               lowerQuery.contains("like") || 
               lowerQuery.contains("related to") ||
               lowerQuery.contains("about");
    }

    private String handleSemanticSearch(String query) {
        List<TextSegment> relevantSegments = vectorStoreService.findRelevantContent(query, 3);
        
        if (relevantSegments.isEmpty()) {
            return "I couldn't find any information related to your query.";
        }
        
        StringBuilder response = new StringBuilder("Here's what I found:\n\n");
        for (TextSegment segment : relevantSegments) {
            response.append(segment.text()).append("\n\n");
        }
        
        return response.toString();
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
<<<<<<< HEAD
=======
        // Format the results
        String formattedResults = formatResults(results);
        
        // Ask the LLM to generate a conversational response based on the results
        return chatService.sendMessage(
            "User question: \"" + userQuestion + "\"\n" +
            "Database results:\n" + formattedResults + 
            "\nPlease provide a helpful, conversational response based on these results."
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
>>>>>>> 4bf41d9 (Refactoring and cleanup)
        try {
            if (chatService != null) {
                chatService.close();
            }
            
            if (dbService != null) {
                dbService.close();
            }
<<<<<<< HEAD
            
            // Add this check to close the vector store service
            if (vectorStoreService != null) {
                vectorStoreService.close();
            }
        } catch (Exception e) {
            System.err.println("Error closing services: " + e.getMessage());
        }
=======
        // LangChain4j models typically don't require explicit shutdown
        // Method kept for API compatibility
>>>>>>> 1ea2aac (Adds langchain for openAI API support)
=======
        } catch (Exception e) {
            System.err.println("Error closing services: " + e.getMessage());
        }
>>>>>>> 4bf41d9 (Refactoring and cleanup)
    }
}