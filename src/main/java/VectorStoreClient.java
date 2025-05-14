import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Deprecated: Use ElasticsearchVectorStoreClient instead for vector storage.
 */
@Deprecated
public class VectorStoreClient {
    private final String apiUrl;
    private final ObjectMapper mapper = new ObjectMapper();

    public VectorStoreClient(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    // Adds a document chunk and its embedding to the vector store
    public void addEmbedding(String id, List<Double> embedding, String text) throws Exception {
        URL url = new URL(apiUrl + "/add");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        String payload = mapper.writeValueAsString(Map.of(
            "id", id,
            "embedding", embedding,
            "text", text
        ));
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }
        int code = conn.getResponseCode();
        if (code != 200) throw new RuntimeException("Failed to add embedding");
    }

    // Searches for top-k relevant chunks by query embedding
    public List<String> search(List<Double> embedding, int topK) throws Exception {
        URL url = new URL(apiUrl + "/search");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        String payload = mapper.writeValueAsString(Map.of(
            "embedding", embedding,
            "top_k", topK
        ));
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            Map resp = mapper.readValue(br, Map.class);
            return (List<String>) resp.get("results");
        }
    }
}
