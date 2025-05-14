import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.data.message.UserMessage;
import java.util.*;

public class RAGPipeline {
    private final EmbeddingService embeddingService;
    private final VectorStoreClient vectorStoreClient;
    private final ChatLanguageModel llm;
    private final int chunkSize;

    public RAGPipeline(EmbeddingService embeddingService, VectorStoreClient vectorStoreClient, String openAiApiKey, int chunkSize) {
        this.embeddingService = embeddingService;
        this.vectorStoreClient = vectorStoreClient;
        this.llm = OpenAiChatModel.withApiKey(openAiApiKey);
        this.chunkSize = chunkSize;
    }

    // Ingest a document (string content)
    public void ingestDocument(String content) throws Exception {
        List<String> chunks = chunkText(content, chunkSize);
        for (String chunk : chunks) {
            List<Double> embedding = embeddingService.getEmbedding(chunk);
            String id = UUID.randomUUID().toString();
            vectorStoreClient.addEmbedding(id, embedding, chunk);
        }
    }

    // Query with retrieval and generation
    public String query(String userQuestion, int topK) throws Exception {
        List<Double> queryEmbedding = embeddingService.getEmbedding(userQuestion);
        List<String> retrievedContexts = vectorStoreClient.search(queryEmbedding, topK);
        String ragContext = String.join("\n", retrievedContexts);

        String prompt = (ragContext.isEmpty() ? "" : ("Context:\n" + ragContext + "\n\n")) +
            "User question: \"" + userQuestion + "\"\n" +
            "Please answer the question using the provided context.";
        
        // Call LLM (LangChain4J)
        ChatResponse response = llm.chat(Collections.singletonList(new UserMessage(prompt)));
        return response.aiMessage().text();
    }

    // Utility: chunk text
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
