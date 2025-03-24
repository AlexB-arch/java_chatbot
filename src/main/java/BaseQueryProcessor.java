import java.util.List;
import java.util.Map;

/**
 * Abstract base class for processing database queries
 */
public abstract class BaseQueryProcessor {
    
    /**
     * Process a user query and return a response
     * 
     * @param userQuery The query to process
     * @return A response to the query
     */
    public abstract String processQuery(String userQuery);
    
    /**
     * Format database results into a readable string
     * 
     * @param results The database results to format
     * @return A formatted string representation of the results
     */
    protected String formatResults(List<Map<String, Object>> results) {
        if (results == null || results.isEmpty()) {
            return "No results found.";
        }
        
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> row : results) {
            sb.append("- ");
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(", ");
            }
            // Remove the trailing comma and space
            if (sb.length() > 2) {
                sb.setLength(sb.length() - 2);
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
    /**
     * Close any resources used by the processor
     */
    public abstract void close();
}