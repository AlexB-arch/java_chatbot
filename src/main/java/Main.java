import java.util.Properties;
import java.util.Scanner;
import java.util.logging.*;
import java.io.File;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());
    
    public static void main(String[] args) {
        logger.info("Application starting...");
        ChatService dbChatService = null;
        
        try {
            // Create logs directory if it doesn't exist
            File logsDir = new File("logs");
            if (!logsDir.exists()) {
                logsDir.mkdirs();
            }

            // Logging configuration is loaded from logging.properties
            String apiKey = System.getenv("OPENAI_API_KEY");
            dbChatService = new ChatService(apiKey);
            logger.setLevel(Level.ALL);
            logger.info("Chatbot started.");
          
            // Load API Key
            Properties properties = new Properties();
            properties.load(Main.class.getResourceAsStream("/config.properties"));
            apiKey = properties.getProperty("openai.api.key");

            // Add apikey from .zshrc
            String zshApiKey = System.getenv("OPENAI_API_KEY");
            if (zshApiKey != null && !zshApiKey.isEmpty()) {
                apiKey = zshApiKey;
            }

            // Initialize ChatService (no need to pass FileHandler)
            dbChatService = new ChatService(apiKey);
            
            Scanner scanner = new Scanner(System.in);

            System.out.println("Welcome to the Educational Database Chatbot!");
            System.out.println("You can ask questions about students, courses, majors, and more.");
            System.out.println("Type 'exit' to quit.");

            logger.info("Chat session starting");

            while (true) {
                System.out.print("You: ");
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("exit")) {
                    break;
                }
                
                // Log user input
                logger.info("User: " + input);
                
                String response = dbChatService.processQuery(input);
                System.out.println("Chatbot: " + response);
                
                // Log chatbot response
                logger.info("Chatbot: " + response);
            }

            logger.info("Chat session ended");
            
            dbChatService.close();
            scanner.close();
            
        } catch (Exception e) {
            logger.severe("Error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Ensure proper closure of resources
            if (dbChatService != null) {
                dbChatService.close();
            }
            logger.info("Application shutting down");
        }
    }
}

// Project needs an abstract class
// Project needs a final class