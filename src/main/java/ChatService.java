import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.service.OpenAiService;
import java.util.ArrayList;
import java.util.List;
import java.time.Duration;

public class ChatService {
    private final OpenAiService openAiService;
    private final List<ChatMessage> conversationHistory = new ArrayList<>();
    
    public ChatService(String apiKey) {
        this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(30));
    }
    
    public void setSystemMessage(String message) {
        conversationHistory.clear();
        conversationHistory.add(new ChatMessage("system", message));
    }
    
    public String sendMessage(String message) {
        conversationHistory.add(new ChatMessage("user", message));
        
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-4o")
                .messages(conversationHistory)
                .maxTokens(500)
                .temperature(0.3)
                .build();
        
        ChatCompletionResult response = openAiService.createChatCompletion(request);
        String responseContent = response.getChoices().get(0).getMessage().getContent();
        
        conversationHistory.add(new ChatMessage("assistant", responseContent));
        
        return responseContent;
    }
    
    public void clearConversation() {
        conversationHistory.clear();
    }
    
    public void close() {
        try {
            openAiService.shutdownExecutor();
        } catch (Exception e) {
            System.err.println("Error shutting down OpenAI service: " + e.getMessage());
        }
    }
}
