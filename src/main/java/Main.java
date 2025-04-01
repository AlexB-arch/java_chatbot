import java.util.Properties;
import java.util.Scanner;
import java.util.logging.*;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());
    
    public static void main(String[] args) {
        ChatService dbChatService = null;
        
        try {
            // Check if database exists and initialize if needed
            // [existing code...]
            
            // Load API Key
            Properties properties = new Properties();
            properties.load(Main.class.getResourceAsStream("/config.properties"));
            String apiKey = properties.getProperty("openai.api.key");

            // Create database chat service
            dbChatService = new ChatService(apiKey);
            
            // Add a shutdown hook to ensure clean closure
            final ChatService finalDbChatService = dbChatService;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down services...");
                if (finalDbChatService != null) {
                    finalDbChatService.close();
                }
            }));
            
            Scanner scanner = new Scanner(System.in);

            System.out.println("Welcome to the Educational Database Chatbot!");
            System.out.println("You can ask questions about students, courses, majors, and more.");
            System.out.println("Type 'exit' to quit.");

            while (true) {
                System.out.print("You: ");
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("exit")) {
                    break;
                }
                
                String response = dbChatService.processQuery(input);
                System.out.println("Chatbot: " + response);
            }
            
            dbChatService.close();
            scanner.close();
            
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Ensure proper closure of resources
            if (dbChatService != null) {
                dbChatService.close();
            }
        }
    }
}

// Project needs an abstract class
// Project needs a final class