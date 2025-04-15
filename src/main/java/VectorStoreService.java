import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;

// Updated import for OpenAI model
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class VectorStoreService {
    private static final Logger logger = Logger.getLogger(VectorStoreService.class.getName());
    
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final DatabaseService databaseService;
    
    public VectorStoreService(String openAiApiKey, DatabaseService databaseService) {
        this.databaseService = databaseService;
        
        // Initialize embedding model using OpenAI
        this.embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(openAiApiKey)
                .modelName("text-embedding-3-small")
                .build();
                
        // Use in-memory store instead of Chroma to simplify debugging
        this.embeddingStore = new InMemoryEmbeddingStore<>();
        logger.info("Initialized in-memory embedding store");
    }
    
    public VectorStoreService() {
        this.embeddingModel = null;
        this.embeddingStore = null;
        this.databaseService = new DatabaseService();
    }

    public boolean testVectorStore() {
        try {
            // Test a simple embedding and retrieval
            Embedding testEmbedding = embeddingModel.embed("test query").content();
            logger.info("Successfully created test embedding");
            return true;
        } catch (Exception e) {
            logger.severe("Vector store test failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public void initializeVectorStore() {
        logger.info("Initializing vector store with educational data...");
        
        try {
            // Load course descriptions
            embedCourseData();
            
            // Load major information
            embedMajorData();
            
            // Load concentration information
            embedConcentrationData();
            
            logger.info("Vector store initialization complete");
        } catch (Exception e) {
            logger.severe("Error initializing vector store: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void embedCourseData() throws SQLException {
        List<Map<String, Object>> courses = databaseService.executeQuery(
                "SELECT id, department, title, num, hrs FROM course");
        
        for (Map<String, Object> course : courses) {
            try {
                String id = convertToString(course.get("id"));
                String department = convertToString(course.get("department"));
                String title = convertToString(course.get("title"));
                int num = convertToInt(course.get("num"));
                int hrs = convertToInt(course.get("hrs"));
                
                String content = String.format("Course ID: %s\nDepartment: %s\nTitle: %s\nNumber: %d\nCredit Hours: %d",
                        id, department, title, num, hrs);
                
                // Create text segment with proper metadata type
                Metadata metadata = createMetadata("course", id);
                TextSegment segment = TextSegment.from(content, metadata);
                
                // Embed and store directly
                Embedding embedding = embeddingModel.embed(segment.text()).content();
                embeddingStore.add(embedding, segment);
                
                logger.fine("Added course: " + id);
            } catch (Exception e) {
                logger.warning("Error processing course data: " + e.getMessage());
            }
        }
        
        logger.info("Embedded course data completed");
    }
    
    private void embedMajorData() throws SQLException {
        List<Map<String, Object>> majors = databaseService.executeQuery(
                "SELECT id, title, deptID, reqtext, hrs, gpa FROM major");
        
        for (Map<String, Object> major : majors) {
            try {
                String id = convertToString(major.get("id"));
                String title = convertToString(major.get("title"));
                String deptID = convertToString(major.get("deptID"));
                String reqtext = convertToString(major.get("reqtext"));
                int hrs = convertToInt(major.get("hrs"));
                double gpa = convertToDouble(major.get("gpa"));
                
                String content = String.format("Major ID: %s\nTitle: %s\nDepartment: %s\nRequirements: %s\n" +
                        "Total Hours: %d\nMinimum GPA: %.2f", id, title, deptID, reqtext, hrs, gpa);
                
                // Create text segment with metadata instead of document
                Metadata metadata = createMetadata("major", id);
                TextSegment segment = TextSegment.from(content, metadata);
                
                // Embed and store directly
                Embedding embedding = embeddingModel.embed(segment.text()).content();
                embeddingStore.add(embedding, segment);
                
                logger.fine("Added major: " + id);
            } catch (Exception e) {
                logger.warning("Error processing major data: " + e.getMessage());
            }
        }
        
        logger.info("Embedded major data completed");
    }
    
    private void embedConcentrationData() throws SQLException {
        List<Map<String, Object>> concentrations = databaseService.executeQuery(
                "SELECT id, major, title, reqtext FROM concentration");
        
        for (Map<String, Object> concentration : concentrations) {
            try {
                String id = convertToString(concentration.get("id"));
                String major = convertToString(concentration.get("major"));
                String title = convertToString(concentration.get("title"));
                String reqtext = convertToString(concentration.get("reqtext"));
                
                String content = String.format("Concentration ID: %s\nMajor: %s\nTitle: %s\nRequirements: %s",
                        id, major, title, reqtext);
                
                // Create text segment with metadata instead of document
                Metadata metadata = createMetadata("concentration", id);
                TextSegment segment = TextSegment.from(content, metadata);
                
                // Embed and store directly
                Embedding embedding = embeddingModel.embed(segment.text()).content();
                embeddingStore.add(embedding, segment);
                
                logger.fine("Added concentration: " + id);
            } catch (Exception e) {
                logger.warning("Error processing concentration data: " + e.getMessage());
            }
        }
        
        logger.info("Embedded concentration data completed");
    }
    
    private Metadata createMetadata(String type, String id) {
        return Metadata.from(Map.of("type", type, "id", id));
    }
    
    public List<TextSegment> findRelevantContent(String query, int maxResults) {
        try {
            // Generate embedding for the query
            Embedding queryEmbedding = embeddingModel.embed(query).content();
            
            // Create a proper search request object
            EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(maxResults)
                .build();
                
            // Execute the search with the request object
            List<EmbeddingMatch<TextSegment>> matches = (List<EmbeddingMatch<TextSegment>>) embeddingStore.search(request);
            
            if (matches.isEmpty()) {
                logger.warning("No matches found for query: " + query);
                return new ArrayList<>();
            }
            logger.info("Found " + matches.size() + " matches for query: " + query);
                        
            // Extract just the text segments
            List<TextSegment> results = new ArrayList<>();
            for (EmbeddingMatch<TextSegment> match : matches) {
                results.add(match.embedded());
            }
            
            logger.info("Found " + results.size() + " relevant segments for query: " + query);
            return results;
        } catch (Exception e) {
            logger.severe("Error finding relevant content: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public void close() {
        logger.info("Closing vector store service");
        
        // Add actual resource cleanup if needed
        if (embeddingModel instanceof AutoCloseable) {
            try {
                ((AutoCloseable) embeddingModel).close();
            } catch (Exception e) {
                logger.warning("Error closing embedding model: " + e.getMessage());
            }
        }
    }
    
    // Helper methods for safe type conversion
    private String convertToString(Object obj) {
        return obj != null ? obj.toString() : "";
    }
    
    private int convertToInt(Object obj) {
        if (obj instanceof Integer) return (Integer) obj;
        if (obj instanceof Long) return ((Long) obj).intValue();
        if (obj instanceof String) return Integer.parseInt((String) obj);
        return 0;
    }
    
    private double convertToDouble(Object obj) {
        if (obj instanceof Double) return (Double) obj;
        if (obj instanceof Float) return ((Float) obj).doubleValue();
        if (obj instanceof Integer) return ((Integer) obj).doubleValue();
        if (obj instanceof String) return Double.parseDouble((String) obj);
        return 0.0;
    }

    /**
     * Prints information about the data currently stored in the vector store.
     * Useful for debugging and verifying that data was properly ingested.
     */
    public void printVectorStoreData() {
        logger.info("Printing vector store data summary...");
        
        try {
            // Generate a generic embedding to find all content
            Embedding genericEmbedding = embeddingModel.embed("information").content();
            
            // Retrieve a large number of segments to get a comprehensive view
            EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(genericEmbedding)
                .maxResults(100) // Get up to 100 items
                .build();
                
            List<EmbeddingMatch<TextSegment>> matches = (List<EmbeddingMatch<TextSegment>>) embeddingStore.search(request);
            
            if (matches.isEmpty()) {
                System.out.println("No data found in vector store.");
                return;
            }
            
            // Count by type for summary
            int courseCount = 0;
            int majorCount = 0;
            int concentrationCount = 0;
            int otherCount = 0;
            
            // Print header
            System.out.println("\n======= VECTOR STORE CONTENTS =======");
            System.out.println("Total items found: " + matches.size());
            
            // Group by type
            for (EmbeddingMatch<TextSegment> match : matches) {
                TextSegment segment = match.embedded();
                Metadata metadata = segment.metadata();
                String type = convertToString(metadata.get("type"));
                String id = convertToString(metadata.get("id"));
                
                if ("course".equals(type)) {
                    courseCount++;
                } else if ("major".equals(type)) {
                    majorCount++;
                } else if ("concentration".equals(type)) {
                    concentrationCount++;
                } else {
                    otherCount++;
                }
                
                // Print each item
                System.out.println("Type: " + type + ", ID: " + id);
                System.out.println(segment.text());
                System.out.println("-----------------------------------");
            }
            
            // Print summary
            System.out.println("\nSummary:");
            System.out.println("Courses: " + courseCount);
            System.out.println("Majors: " + majorCount);
            System.out.println("Concentrations: " + concentrationCount);
            System.out.println("Other: " + otherCount);
        } catch (Exception e) {
            logger.severe("Error printing vector store data: " + e.getMessage());
            e.printStackTrace();
        }
    }

}