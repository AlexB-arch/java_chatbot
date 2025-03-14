public class Chatbot {
    private NLPService nlpService;
    private DatabaseService dbService;
    private QuestionAnalyzer questionAnalyzer;

    public Chatbot() {
        nlpService = new NLPService();
        dbService = new DatabaseService();
        questionAnalyzer = new QuestionAnalyzer(nlpService, dbService);
    }

    public String processUserInput(String input) {
        input = input.trim();
        if (input.isEmpty()) {
            return "Please enter a valid question.";
        }

        String[] sentences = nlpService.detectSentences(input);
        if (sentences.length == 0) {
            return "I don't understand your question. Could you rephrase?";
        }

        // Process the first sentence as a question
        return questionAnalyzer.processQuestion(sentences[0]);
    }
}
