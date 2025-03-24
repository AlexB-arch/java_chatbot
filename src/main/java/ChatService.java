import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.service.OpenAiService;
import java.util.ArrayList;
import java.util.List;
import java.time.Duration;

public class ChatService {
    private final OpenAiService openAiService;
    private final List<ChatMessage> conversationHistory = new ArrayList<>();
    
    public ChatService(String apiKey) {
        // Set a timeout to ensure connections don't hang
        this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(30));
    }
    
    public void setSystemMessage(String message) {
        // Clear history and add system message
        conversationHistory.clear();
        conversationHistory.add(new ChatMessage("system", message));
    }
    
    public String sendMessage(String message) {
        // Add user message to history
        conversationHistory.add(new ChatMessage("user", message));
        
        // Create the request
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-4") // or gpt-3.5-turbo
                .messages(conversationHistory)
                .maxTokens(500)
                .temperature(0.3)
                .build();
        
        // Get response
        ChatCompletionResult response = openAiService.createChatCompletion(request);
        String responseContent = response.getChoices().get(0).getMessage().getContent();
        
        // Add assistant response to history
        conversationHistory.add(new ChatMessage("assistant", responseContent));
        
        return responseContent;
    }
    
    public void clearConversation() {
        conversationHistory.clear();
    }
    
    /**
     * Properly close the OpenAI service to prevent thread leaks
     */
    public void close() {
        try {
            // The OpenAiService has a shutdown method to close its resources
            openAiService.shutdownExecutor();
        } catch (Exception e) {
            System.err.println("Error shutting down OpenAI service: " + e.getMessage());
        }
    }
}
