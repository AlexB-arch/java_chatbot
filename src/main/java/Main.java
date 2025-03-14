import java.util.Scanner;

public class Main {
    
    public static void main(String[] args) {
        Chatbot chatbot = new Chatbot();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the Chatbot! Ask me anything.");
        System.out.println("Type 'exit' to quit.");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("exit")) {
                break;
            }

            String response = chatbot.processUserInput(input);
            System.out.println(response);
        }

        chatbot.shutdown();
        scanner.close();
        System.out.println("Goodbye!");
    }
}

// Project needs an abstract class
// Project needs a final class