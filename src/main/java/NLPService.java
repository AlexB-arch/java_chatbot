import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class NLPService {
    private SentenceDetectorME sentenceDetector;
    private TokenizerME tokenizer;

    public NLPService() {
        try {
            // initialze basic models
            initializeModels();
        } catch (IOException e) {
            System.err.println("Error initializing NLP models: " + e.getMessage());
        }
    }

    private void initializeModels() throws IOException {
        // Load sentence detector model
        try (InputStream sentenceModelIn = getClass().getResourceAsStream("/models/en-sent.bin")) {
            if (sentenceModelIn != null) {
                SentenceModel sentenceModel = new SentenceModel(sentenceModelIn);
                sentenceDetector = new SentenceDetectorME(sentenceModel);
                System.out.println("Sentence detector model loaded successfully");
            } else {
                System.err.println("Error loading sentence detector model");

                // Try a different location
                try (FileInputStream fileIn = new FileInputStream("src/main/resources/models/en-sent.bin")) {
                    SentenceModel sentenceModel = new SentenceModel(fileIn);
                    sentenceDetector = new SentenceDetectorME(sentenceModel);
                    System.out.println("Sentence detector model loaded from file system.");
                } catch (IOException e) {
                    System.err.println("Error loading sentence detector model from file system: " + e.getMessage());
                }
            }
        }

        // Load tokenizer model
        try (InputStream tokenizerModelIn = getClass().getResourceAsStream("/models/en-token.bin")) {
            if (tokenizerModelIn != null) {
                TokenizerModel tokenizerModel = new TokenizerModel(tokenizerModelIn);
                tokenizer = new TokenizerME(tokenizerModel);
                System.out.println("Tokenizer model loaded successfully");
            } else {
                System.err.println("Error loading tokenizer model");

                // Try a different location
                try (FileInputStream fileIn = new FileInputStream("src/main/resources/models/en-token.bin")) {
                    TokenizerModel tokenizerModel = new TokenizerModel(fileIn);
                    tokenizer = new TokenizerME(tokenizerModel);
                    System.out.println("Tokenizer model loaded from file system.");
                } catch (IOException e) {
                    System.err.println("Error loading tokenizer model from file system: " + e.getMessage());
                }
            }
        }
    }

    // Method to unload sentence detector model
    public void unloadSentenceDetector() {
        sentenceDetector = null;
    }

    // Method to unload tokenizer model
    public void unloadTokenizer() {
        tokenizer = null;
    }

    // Methods to use models
    public String[] detectSentences(String text) {
        if (sentenceDetector == null) {
            // Fallback if model is not loaded
            return new String[]{text};
        }
        return sentenceDetector.sentDetect(text);
    }

    public String[] tokenize(String sentence) {
        if (tokenizer == null) {
            // Fallback to simple split
            return sentence.split("\\s+");
        }

        return tokenizer.tokenize(sentence);
    }

    public boolean modelIsLoaded() {
        return sentenceDetector != null && tokenizer != null;
    }
}
