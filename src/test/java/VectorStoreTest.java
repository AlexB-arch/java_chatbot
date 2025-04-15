import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class VectorStoreTest {
    // Initialize the vector store service
    private VectorStoreService vectorStoreService;
    private DatabaseService dbService;
    private ChatService chatService;

    // Load properties from config file
    private static final Properties properties = new Properties();
    static {
        try {
            InputStream input = VectorStoreTest.class.getClassLoader().getResourceAsStream("config.properties");
            if (input != null) {
                properties.load(input);
                input.close();
            } else {
                throw new RuntimeException("Unable to find config.properties");
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error loading properties file", ex);
        }
    }
    
    // Get the API key from the properties file
    private static final String TEST_API_KEY = properties.getProperty("openai.api.key");

    @Test
    public void testVectorStoreInitialization() {
        // Initialize the database service
        dbService = new DatabaseService();
        
        // Initialize the vector store service
        vectorStoreService = new VectorStoreService(TEST_API_KEY, dbService);
        
        // Test if the vector store is initialized correctly
        assertNotNull("Vector Store Service should not be null", vectorStoreService);
        
        // Test if the vector store is ready for semantic search
        boolean isReady = vectorStoreService.testVectorStore();
        assertNotNull("Vector Store should be ready for semantic search", isReady);
        assertTrue("Vector Store should be initialized successfully", isReady);
    }

    @Test
    public void printVectorStoreData() {
        // Initialize the database service
        dbService = new DatabaseService();
        
        // Initialize the vector store service
        vectorStoreService = new VectorStoreService(TEST_API_KEY, dbService);
        
        // Print the vector store data
        vectorStoreService.printVectorStoreData();
    }
}
