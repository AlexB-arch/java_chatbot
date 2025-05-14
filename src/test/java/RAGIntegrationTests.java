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
            ElasticsearchVectorStoreClient vectorStoreClient = new ElasticsearchVectorStoreClient("localhost", 9200, "rag_vectors", 1536); // Updated for Elasticsearch
            // Should not throw on instantiation
        } catch (Exception e) {
            fail("Failed to instantiate VectorStoreClient: " + e.getMessage());
        }
    }

    @Test
    public void testDocumentIngestorInstantiation() {
        try {
            EmbeddingService embeddingService = new EmbeddingService(System.getenv("OPENAI_API_KEY"));
            ElasticsearchVectorStoreClient vectorStoreClient = new ElasticsearchVectorStoreClient("localhost", 9200, "rag_vectors", 1536);
            RAGPipeline ragPipeline = new RAGPipeline(embeddingService, vectorStoreClient, System.getenv("OPENAI_API_KEY"), 500);
            DocumentIngestor ingestor = new DocumentIngestor(ragPipeline);
            assertNotNull(ingestor);
        } catch (Exception e) {
            fail("Failed to instantiate DocumentIngestor: " + e.getMessage());
        }
    }

    @Test
public void testPdfIngestion() {
        try {
            EmbeddingService embeddingService = new EmbeddingService(System.getenv("OPENAI_API_KEY"));
            ElasticsearchVectorStoreClient vectorStoreClient = new ElasticsearchVectorStoreClient("localhost", 9200, "rag_vectors", 1536);
            RAGPipeline ragPipeline = new RAGPipeline(embeddingService, vectorStoreClient, System.getenv("OPENAI_API_KEY"), 500);
            DocumentIngestor ingestor = new DocumentIngestor(ragPipeline);
            ingestor.ingestPdfFile("dafi21-101.pdf");
            // No exception means success
        } catch (Exception e) {
            fail("Failed to ingest PDF: " + e.getMessage());
        }
    }
}
