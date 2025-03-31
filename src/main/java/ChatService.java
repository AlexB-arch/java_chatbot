import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.*;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.AiMessage;

import java.util.ArrayList;
import java.util.List;
import java.time.Duration;

public class ChatService {
    private final ChatLanguageModel chatModel;
    private final List<ChatMessage> conversationHistory = new ArrayList<>();
    
    public ChatService(String apiKey) {
        this.chatModel = OpenAiChatModel.builder()
            .apiKey(apiKey)
            .timeout(Duration.ofSeconds(30))
            .modelName("gpt-4o")
            .temperature(0.3)
            .maxTokens(500)
            .build();
    }
    
    public void setSystemMessage(String message) {
        conversationHistory.clear();
        conversationHistory.add(new SystemMessage(message));
    }
    
    public String sendMessage(String userMessage) {
        // Add user message to conversation history
        conversationHistory.add(new UserMessage(userMessage));
        
        // Get response from the model
        ChatResponse response = chatModel.chat(conversationHistory);
        
        // Extract the AI message from the response
        AiMessage aiMessage = response.aiMessage();
        
        // Add AI response to conversation history
        conversationHistory.add(aiMessage);
        
        // Return the response text
        return aiMessage.text();
    }
    
    public void clearConversation() {
        conversationHistory.clear();
    }
    
    public void close() {
        // LangChain4j models typically don't require explicit shutdown
        // Method kept for API compatibility
    }
}
