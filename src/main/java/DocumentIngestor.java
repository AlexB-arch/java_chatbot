import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

public class DocumentIngestor {
    private final EmbeddingService embeddingService;
    private final VectorStoreClient vectorStoreClient;
    private final int chunkSize = 500; // characters per chunk

    public DocumentIngestor(EmbeddingService embeddingService, VectorStoreClient vectorStoreClient) {
        this.embeddingService = embeddingService;
        this.vectorStoreClient = vectorStoreClient;
    }

    // Ingests a text file: splits into chunks, embeds, stores in vector DB
    public void ingestTextFile(String filePath) throws Exception {
        String content = Files.readString(Paths.get(filePath));
        List<String> chunks = chunkText(content, chunkSize);
        for (String chunk : chunks) {
            List<Double> embedding = embeddingService.getEmbedding(chunk);
            String id = UUID.randomUUID().toString();
            vectorStoreClient.addEmbedding(id, embedding, chunk);
        }
    }

    private List<String> chunkText(String text, int size) {
        List<String> chunks = new ArrayList<>();
        int i = 0;
        while (i < text.length()) {
            int end = Math.min(i + size, text.length());
            chunks.add(text.substring(i, end));
            i = end;
        }
        return chunks;
    }
}
