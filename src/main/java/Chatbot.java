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
    
    public void setSystemMessage(String message) {
        this.baseSystemMessage = message;
        updateSystemMessage();
    }
    
    public void setCurrentStudent(Student student) {
        this.currentStudent = student;
        updateSystemMessage();
    }
    
    private void updateSystemMessage() {
        conversationHistory.clear();
        
        // Create a system message that includes student context if available
        StringBuilder systemMessage = new StringBuilder(baseSystemMessage != null ? baseSystemMessage : "");
        
        if (currentStudent != null) {
            systemMessage.append("\n\nCurrent user information:");
            systemMessage.append("\nName: ").append(currentStudent.getName());
            systemMessage.append("\nStudent ID: ").append(currentStudent.getStudentId());
            systemMessage.append("\nMajor: ").append(currentStudent.getMajor());
            systemMessage.append("\nGPA: ").append(currentStudent.getGpa());
            systemMessage.append("\n\nPlease personalize responses for this student.");
        }
        
        conversationHistory.add(new SystemMessage(systemMessage.toString()));
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
        updateSystemMessage(); // Re-add the system message with current student context
    }
    
    public Student getCurrentStudent() {
        return currentStudent;
    }
    
    public void close() {
        // LangChain4j models typically don't require explicit shutdown
        // Method kept for API compatibility
    }
}