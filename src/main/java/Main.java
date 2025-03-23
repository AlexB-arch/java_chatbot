import java.util.Properties;
import java.util.Scanner;

public class Main {
    
    public static void main(String[] args) {
        try {
            // Load API Key
            Properties properties = new Properties();
            properties.load(Main.class.getResourceAsStream("/config.properties"));
            String apiKey = properties.getProperty("openai.api.key");

            // Create service
            ChatService chatService = new ChatService(apiKey);
            //Chatbot chatbot = new Chatbot();
            Scanner scanner = new Scanner(System.in);

            System.out.println("Welcome to the Chatbot! Ask me anything.");
            System.out.println("Type 'exit' to quit.");

            while (true) {
                System.out.print("You: ");
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("exit")) {
                    break;
                }
                //chatbot.processText(input);
                String response = chatService.sendMessage(input);
                System.out.println("Chatbot: " + response);
            }
            
            //chatbot.shutdown();
            scanner.close();
            System.out.println("Goodbye!");
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }
}

// Project needs an abstract class
// Project needs a final class