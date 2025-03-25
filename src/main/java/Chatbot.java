import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.AiMessage;

import java.util.ArrayList;
import java.util.List;
import java.time.Duration;

public class Chatbot {
    private final ChatLanguageModel chatModel;
    private final List<ChatMessage> conversationHistory = new ArrayList<>();
    private Student currentStudent;
    private String baseSystemMessage;
    
    public Chatbot(String apiKey) {
        this.chatModel = OpenAiChatModel.builder()
            .apiKey(apiKey)
            .timeout(Duration.ofSeconds(30))
            .modelName("gpt-4o")
            .temperature(0.3)
            .maxTokens(500)
            .build();
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
    
    public void close() {
        // LangChain4j models typically don't require explicit shutdown
        // Method kept for API compatibility
    }
}