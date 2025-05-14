import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.transport.ElasticsearchTransport; // Correct import for ElasticsearchTransport
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A vector store client for Elasticsearch, supporting addEmbedding and search operations for RAG pipelines.
 * Requires Elasticsearch 8.x with dense_vector support.
 */

public class ElasticsearchVectorStoreClient {
    private final ElasticsearchClient client;
    private final String indexName;
    private final int dims;

    public ElasticsearchVectorStoreClient(String hostname, int port, String indexName, int dims) {
        RestClient restClient = RestClient.builder(new HttpHost(hostname, port)).build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        this.client = new ElasticsearchClient(transport);
        this.indexName = indexName;
        this.dims = dims;
    }

    public void addEmbedding(String id, List<Float> embedding, String text) throws Exception {
        Map<String, Object> doc = new HashMap<>();
        doc.put("embedding", embedding);
        doc.put("text", text);

        IndexRequest<Map<String, Object>> request = new IndexRequest.Builder<Map<String, Object>>()
                .index(indexName)
                .id(id)
                .document(doc)
                .build();

        client.index(request);
    }

    public List<String> search(List<Float> queryEmbedding, int topK) throws Exception {
        Map<String, co.elastic.clients.json.JsonData> params = new HashMap<>();
        params.put("query_vector", co.elastic.clients.json.JsonData.of(queryEmbedding));

        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(indexName)
                .size(topK)
                .query(q -> q
                        .scriptScore(ss -> ss
                                .query(q2 -> q2.matchAll(ma -> ma))
                                .script(s -> s
                                        .inline(new co.elastic.clients.elasticsearch._types.InlineScript.Builder().source("cosineSimilarity(params.query_vector, 'embedding') + 1.0").params(params).build())
                                )
                        )
                )
                .build();

        SearchResponse<Map<String, Object>> response = client.search(searchRequest, (Class<Map<String, Object>>) (Class<?>) Map.class);

        List<String> results = new ArrayList<>();
        for (Hit<Map<String, Object>> hit : response.hits().hits()) {
            Object text = hit.source().get("text");
            if (text != null) results.add(text.toString());
        }
        return results;
    }
}
