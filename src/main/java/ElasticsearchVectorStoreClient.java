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

import java.util.logging.Logger;

public class ElasticsearchVectorStoreClient {
    // ... existing fields and constructor ...

    /**
     * Returns the number of documents in the index.
     */
    public long countDocuments() {
        try {
            co.elastic.clients.elasticsearch.core.CountRequest countRequest = new co.elastic.clients.elasticsearch.core.CountRequest.Builder()
                .index(indexName)
                .build();
            co.elastic.clients.elasticsearch.core.CountResponse response = client.count(countRequest);
            logger.info("Elasticsearch index '" + indexName + "' contains " + response.count() + " documents.");
            return response.count();
        } catch (Exception e) {
            logger.severe("Failed to count documents in index '" + indexName + "': " + e.getMessage());
            return -1;
        }
    }
    private static final Logger logger = Logger.getLogger(ElasticsearchVectorStoreClient.class.getName());
    private final ElasticsearchClient client;
    private final String indexName;
    private final int dims;

    public ElasticsearchVectorStoreClient(String hostname, int port, String indexName, int dims) {
        RestClient restClient = RestClient.builder(new HttpHost(hostname, port)).build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        this.client = new ElasticsearchClient(transport);
        this.indexName = indexName;
        this.dims = dims;
        try {
            createIndexIfNotExists();
        } catch (Exception e) {
            logger.severe("Failed to create or verify index: " + e.getMessage());
        }
    }

    // Create the index with dense_vector mapping if it doesn't exist
    private void createIndexIfNotExists() throws Exception {
        boolean exists = client.indices().exists(b -> b.index(indexName)).value();
        if (!exists) {
            logger.info("Index '" + indexName + "' does not exist. Creating with dense_vector mapping...");
            client.indices().create(c -> c
                .index(indexName)
                .mappings(m -> m
                    .properties("embedding", p -> p
                        .denseVector(dv -> dv.dims(dims))
                    )
                    .properties("text", p -> p.text(t -> t)
                    )
                )
            );
            logger.info("Index '" + indexName + "' created.");
        } else {
            logger.info("Index '" + indexName + "' already exists.");
        }
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
        try {
            client.index(request);
            logger.info("Successfully indexed document with id: " + id);
        } catch (Exception e) {
            logger.severe("Failed to index document with id: " + id + ". Error: " + e.getMessage());
            throw e;
        }
    }

    public List<String> search(List<Float> queryEmbedding, int topK) throws Exception {
        logger.info("Elasticsearch search: embedding size = " + queryEmbedding.size() + ", topK = " + topK);
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

        try {
            SearchResponse<Map<String, Object>> response = client.search(searchRequest, (Class<Map<String, Object>>) (Class<?>) Map.class);

            List<String> results = new ArrayList<>();
            for (Hit<Map<String, Object>> hit : response.hits().hits()) {
                Object text = hit.source().get("text");
                if (text != null) results.add(text.toString());
            }
            logger.info("Elasticsearch search returned " + results.size() + " results.");
            return results;
        } catch (Exception e) {
            logger.severe("Elasticsearch search failed: " + e.getMessage());
            throw e;
        }
    }
}
