import java.util.*;

public class QuestionAnalyzer {
    private NLPService nlpService;
    private DatabaseService dbService;

    public QuestionAnalyzer(NLPService nlpService, DatabaseService dbService) {
        this.nlpService = nlpService;
        this.dbService = dbService;
    }

    public String processQuestion(String question) {
        // Step 1: Tokenize the question
        String[] tokens = nlpService.tokenize(question);

        // Step 2: Analyze POS tags if available
        String[] posTags = {};
        try {
            posTags = nlpService.tagPOS(tokens);
        } catch (Exception e) {
            // Log the error
            System.err.println("Error while tagging POS: " + e.getMessage());
        }

        // Step 3: Extract named keywords for nouns and entities
        List<String> keywords = extractKeywords(tokens, posTags);

        // Step 4: Query the database with the keywords
        List<Map<String, Object>> results = dbService.searchByKeywords(keywords);

        // Step 5: Format the response
        return formatResponse(results, question);
    }

    private List<String> extractKeywords(String[] tokens, String[] posTags) {
        List<String> keywords = new ArrayList<>();

        // If we don't have POS tags, just use important words based on length
        if (posTags == null || posTags.length == 0) {
            for (String token : tokens) {
                // Skip common short words and common stopwords
                if (token.length() > 3 && !isStopWord(token)) {
                    keywords.add(token.toLowerCase());
                }
            }
            return keywords;
        }

        // If we do have POS tags, extract nouns and verbs
        for (int i = 0; i < tokens.length && i < posTags.length; i++) {
            // Usually NN* are nouns and VB* are verbs
            if ((posTags[i].startsWith("NN") || posTags[i].startsWith("VB")) && !isStopWord(tokens[i])){
                keywords.add(tokens[i].toLowerCase());
            }
        }

        return keywords;
    }

    private boolean isStopWord(String word) {
        // A simple list of stop words
        String[] stopWords = {"the", "a", "an", "and", "or", "but", "is", "are", "was",
                            "were", "be", "been", "being", "in", "on", "at", "to", "for",
                            "with", "by", "about", "like", "through", "over", "before",
                            "between", "after", "since", "without", "under", "within"};
        
        for (String stopWord : stopWords) {
            if (stopWord.equalsIgnoreCase(word)) {
                return true;
            }
        }
        return false;
    }

    private String formatResponse(List<Map<String, Object>> results, String question) {
        if (results.isEmpty()) {
            return "I don't have information about that. Could you ask something else?";
        }

        if (results.get(0).containsKey("answer")) {
            return results.get(0).get("answer").toString();
        } else if (results.get(0).containsKey("content")) {
            return results.get(0).get("content").toString();
        }

        StringBuilder response = new StringBuilder("Here's what I found:\n");
        for (Map<String, Object> result : results) {
            response.append("- ");
            // Use the first column as the response
            response.append(result.values().iterator().next());
            response.append("\n");
        }

        return response.toString();
    }
}
