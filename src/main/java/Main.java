import java.io.File;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.*;
import java.io.File;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static FileHandler globalFileHandler;
    private static boolean loggingEnabled = true; // Default value
    
    public static void main(String[] args) {
        // Process command line arguments
        processCommandLineArgs(args);
        
        // Only log if enabled
        if (loggingEnabled) {
            logger.info("Application starting...");
        }
        
        ChatService dbChatService = null;
        
        try {
            // Load configuration properties
            Properties properties = new Properties();
            properties.load(Main.class.getResourceAsStream("/config.properties"));
            
            // Check if logging is enabled in config (if not overridden by command line)
            if (!hasCommandLineLoggingFlag(args)) {
                loggingEnabled = Boolean.parseBoolean(
                    properties.getProperty("logging.enabled", "false"));
            }
            
            // Set up logging only if enabled
            if (loggingEnabled) {
                setupLogging();
                logger.info("Logging is enabled");
            } else {
                // Remove any existing handlers
                for (Handler handler : logger.getHandlers()) {
                    logger.removeHandler(handler);
                }
                logger.setLevel(Level.OFF);
            }
            
            // Load API Key
            String apiKey = properties.getProperty("openai.api.key");

            // Initialize the ChatService with vector store capabilities
            dbChatService = new ChatService(apiKey);
            System.out.println("Vector store initialized and ready for semantic search!");

            // Initialize the DatabaseService before using it
            DatabaseService databaseService = new DatabaseService();

            // In Main.java or your test class
            VectorStoreService vectorStoreService = new VectorStoreService(apiKey, databaseService);
            vectorStoreService.initializeVectorStore();
            vectorStoreService.printVectorStoreData();

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

            if (loggingEnabled) {
                logger.info("Chat session starting");
            }

            while (true) {
                System.out.print("You: ");
                String input = scanner.nextLine();
                
                if (input.equalsIgnoreCase("exit")) {
                    break;
                } else if (input.equalsIgnoreCase("toggle-logging")) {
                    loggingEnabled = !loggingEnabled;
                    if (loggingEnabled) {
                        setupLogging();
                        logger.info("Logging enabled");
                        System.out.println("Logging is now enabled");
                    } else {
                        if (globalFileHandler != null) {
                            logger.removeHandler(globalFileHandler);
                        }
                        logger.setLevel(Level.OFF);
                        System.out.println("Logging is now disabled");
                    }
                    continue;
                }
                
                // Log user input if logging is enabled
                if (loggingEnabled) {
                    logger.info("User: " + input);
                }
                
                String response = dbChatService.processQuery(input);
                System.out.println("Chatbot: " + response);
                
                // Log chatbot response if logging is enabled
                if (loggingEnabled) {
                    logger.info("Chatbot: " + response);
                }
            }

            if (loggingEnabled) {
                logger.info("Chat session ended");
            }
            
            dbChatService.close();
            scanner.close();
            
        } catch (Exception e) {
            if (loggingEnabled) {
                logger.severe("Error occurred: " + e.getMessage());
            }
            e.printStackTrace();
        } finally {
            // Ensure proper closure of resources
            if (dbChatService != null) {
                dbChatService.close();
            }
            if (loggingEnabled) {
                logger.info("Application shutting down");
            }
        }
    }
    
    private static void setupLogging() {
        try {
            // Create logs directory if it doesn't exist
            File logsDir = new File("logs");
            if (!logsDir.exists()) {
                logsDir.mkdirs();
            }

            // Set up logging with clean format
            System.setProperty("java.util.logging.SimpleFormatter.format", 
                              "[%1$tY-%1$tm-%1$td %1$tH:%1$tM-%1$tS] %4$s: %5$s%n");
            
            // Remove old handler if exists
            if (globalFileHandler != null) {
                logger.removeHandler(globalFileHandler);
                globalFileHandler.close();
            }
            
            globalFileHandler = new FileHandler("./logs/chatbot.log", true);
            globalFileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(globalFileHandler);
            logger.setLevel(Level.ALL);
        } catch (Exception e) {
            System.err.println("Error setting up logging: " + e.getMessage());
        }
    }
    
    private static void processCommandLineArgs(String[] args) {
        if (args != null) {
            for (String arg : args) {
                if (arg.equals("--no-logging")) {
                    loggingEnabled = false;
                } else if (arg.equals("--logging")) {
                    loggingEnabled = true;
                }
            }
        }
    }
    
    private static boolean hasCommandLineLoggingFlag(String[] args) {
        if (args != null) {
            for (String arg : args) {
                if (arg.equals("--no-logging") || arg.equals("--logging")) {
                    return true;
                }
            }
        }
        return false;
    }
}

// Project needs an abstract class
// Project needs a final class