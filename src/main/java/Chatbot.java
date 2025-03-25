import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class Chatbot {
    private NLPService nlpService;
    private DatabaseService dbService;
    
    private Student student = new Student();

    public Chatbot() {
        nlpService = new NLPService();
        dbService = new DatabaseService();
    }

    public String processText(String text) {
        // Detect intent
        String intent = nlpService.detectIntent(text);

        // Detect sentences
        String[] sentences = nlpService.detectSentences(text);
        StringBuilder response = new StringBuilder();

        for (String sentence : sentences) {
            // Tokenize each sentence
            String[] tokens = nlpService.tokenize(sentence);
            
       // POS tagging
            String[] tags = nlpService.tagPOS(tokens);
            
            // Entity extraction
            //Map<String, List<String>> entities = nlpService.findEntities(tokens);
            
            // Process based on intent and entities
            //String sentenceResponse = generateResponse(intent, sentence, entities);
            if (response.length() > 0) {
                response.append("\n");
            }
            //response.append(sentenceResponse);
        }
        
        String responseText = response.toString();
        System.out.println("Chatbot: " + responseText);
        return responseText;
    }

    public void shutdown() {
        dbService.close();
    }
}
