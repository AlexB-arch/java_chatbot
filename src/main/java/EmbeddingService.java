import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Collectors;

public class EmbeddingService {
    private final String openAiApiKey;
    private final ObjectMapper mapper = new ObjectMapper();

    public EmbeddingService(String openAiApiKey) {
        this.openAiApiKey = openAiApiKey;
    }

    // Gets embedding for a given text from OpenAI API
    public List<Double> getEmbedding(String text) throws Exception {
        URL url = URI.create("https://api.openai.com/v1/embeddings").toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + openAiApiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        String payload = mapper.writeValueAsString(Map.of(
            "input", text,
            "model", "text-embedding-ada-002"
        ));
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            Map<String, Object> resp = mapper.readValue(br, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            Object dataObj = resp.get("data");
            if (!(dataObj instanceof List<?>)) {
                throw new RuntimeException("Expected a list for 'data'");
            }
            List<?> data = (List<?>) dataObj;
            if (data.isEmpty()) throw new RuntimeException("No embedding returned");
            Map<String, Object> embeddingObj = (Map<String, Object>) data.get(0);
            List<Double> embedding = ((List<?>) embeddingObj.get("embedding")).stream().map(x -> ((Number)x).doubleValue()).collect(Collectors.toList());
            return embedding;
        }
    }
}
