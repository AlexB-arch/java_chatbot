public class Chatbot {
    private NLPService nlpService;
    private DatabaseService dbService;
    
    public Chatbot() {
        nlpService = new NLPService();
        dbService = new DatabaseService();
    }

    public void processText(String text) {
        // Detect sentences
        String[] sentences = nlpService.detectSentences(text);
        for (String sentence : sentences) {
            // Tokenize each sentence
            String[] tokens = nlpService.tokenize(sentence);
            for (String token : tokens) {
                // Process each token
                System.out.println("Processing token: " + token);
            }
        }
    }

    public void shutdown() {
        dbService.close();
    }
}
