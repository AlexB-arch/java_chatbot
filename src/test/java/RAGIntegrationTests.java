import org.junit.Test;
import static org.junit.Assert.*;

public class RAGIntegrationTests {
    @Test
    public void testEmbeddingServiceConnection() {
        try {
            EmbeddingService embeddingService = new EmbeddingService(System.getenv("OPENAI_API_KEY"));
            // Should not throw on instantiation
        } catch (Exception e) {
            fail("Failed to instantiate EmbeddingService: " + e.getMessage());
        }
    }

    @Test
    public void testVectorStoreClientConnection() {
        try {
            VectorStoreClient vectorStoreClient = new VectorStoreClient("http://localhost:8000"); // Update if needed
            // Should not throw on instantiation
        } catch (Exception e) {
            fail("Failed to instantiate VectorStoreClient: " + e.getMessage());
        }
    }

    @Test
    public void testDocumentIngestorInstantiation() {
        try {
            EmbeddingService embeddingService = new EmbeddingService(System.getenv("OPENAI_API_KEY"));
            VectorStoreClient vectorStoreClient = new VectorStoreClient("http://localhost:8000");
            DocumentIngestor ingestor = new DocumentIngestor(embeddingService, vectorStoreClient);
            // Should not throw on instantiation
        } catch (Exception e) {
            fail("Failed to instantiate DocumentIngestor: " + e.getMessage());
        }
    }
}
