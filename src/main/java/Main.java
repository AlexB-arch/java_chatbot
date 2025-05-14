import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            // Get API key from environment variable
            String apiKey = System.getenv("OPENAI_API_KEY");
            if (apiKey == null || apiKey.isEmpty()) {
                System.err.println("OPENAI_API_KEY environment variable not set.");
                return;
            }

            // Elasticsearch config (adjust if needed)
            String esHost = "localhost";
            int esPort = 9200;
            String esIndex = "rag_docs";
            int embeddingDims = 1536; // for text-embedding-ada-002
            int chunkSize = 1000;

            // Initialize services
            EmbeddingService embeddingService = new EmbeddingService(apiKey);
            ElasticsearchVectorStoreClient vectorStore = new ElasticsearchVectorStoreClient(esHost, esPort, esIndex, embeddingDims);
            RAGPipeline rag = new RAGPipeline(embeddingService, vectorStore, apiKey, chunkSize);

            // Simple CLI
            Scanner scanner = new Scanner(System.in);
            System.out.println("RAG Pipeline Demo. Type 'exit' to quit.");
            while (true) {
                System.out.print("Enter a document to ingest (or just press Enter to skip): ");
                String doc = scanner.nextLine();
                if (doc.equalsIgnoreCase("exit")) break;
                if (!doc.isBlank()) {
                    try {
                        rag.ingestDocument(doc);
                        System.out.println("Document ingested.");
                    } catch (Exception e) {
                        System.err.println("Error ingesting document: " + e.getMessage());
                    }
                }
                System.out.print("Ask a question: ");
                String question = scanner.nextLine();
                if (question.equalsIgnoreCase("exit")) break;
                if (!question.isBlank()) {
                    try {
                        String answer = rag.query(question, 3);
                        System.out.println("Answer: " + answer);
                    } catch (Exception e) {
                        System.err.println("Error answering question: " + e.getMessage());
                    }
                }
            }
            scanner.close();
            System.out.println("Session ended.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
