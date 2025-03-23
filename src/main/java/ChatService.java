import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import java.util.List;
import java.util.ArrayList;

public class ChatService {
    private OpenAiService openAiService;
    private final List<ChatMessage> conversationHistory = new ArrayList<>();

    public ChatService(String apiKey) {
        openAiService = new OpenAiService(apiKey);
    }

    public String sendMessage(String message) {
        // Add user message to conversation history
        conversationHistory.add(new ChatMessage("user", message));

        // Create request
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-4o")
                .messages(conversationHistory)
                .maxTokens(300)
                .temperature(0.5)
                .build();

        // Get response
        ChatCompletionResult response = openAiService.createChatCompletion(request);
        String responseContent = response.getChoices().get(0).getMessage().getContent();

        // Add chatbot response to conversation history
        conversationHistory.add(new ChatMessage("chatbot", responseContent));

        return responseContent;
    }

    public void clearConversation() {
        conversationHistory.clear();
    }
}
