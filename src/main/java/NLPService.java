import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class NLPService {
    private SentenceDetectorME sentenceDetector;
    private TokenizerME tokenizer;
    private POSTaggerME posTagger;
    @SuppressWarnings("unused")
    private NameFinderME nameFinder;
    
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

        // Load POS tagger model
        try (InputStream posModelIn = getClass().getResourceAsStream("/models/en-pos-maxent.bin")) {
            if (posModelIn != null) {
                POSModel posModel = new POSModel(posModelIn);
                posTagger = new POSTaggerME(posModel);
                System.out.println("POS tagger model loaded successfully");
            } else {
                // Fallback option
                System.err.println("Error loading POS tagger model");
            }
        }

        // Load named entity recognition model
        try (InputStream nerModelIn = getClass().getResourceAsStream("/models/en-ner-person.bin")) {
            if (nerModelIn != null) {
                TokenNameFinderModel nerModel = new TokenNameFinderModel(nerModelIn);
                nameFinder = new NameFinderME(nerModel);
                System.out.println("Named entity recognition model loaded successfully");
            } else {
                // Fallback option
                System.err.println("Error loading named entity recognition model");
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

    // Method to unload POSTagger
    public void unloadPOSTagger() {
        posTagger = null;
    }

    // Method to unload NameFinder
    public void unloadNameFinder() {
        nameFinder = null;
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

    // Adds POS Tagging capability
    public String[] tagPOS(String[] tokens) {
        if (posTagger == null) {
            // Fallback to simple tagging
            return tokens;
        }

        try {
            // Apply the POS tagger to get the most likely tag sequence
            String[] tags = posTagger.tag(tokens);
            
            /*  Optionally log the tagging results for debugging
            if (tokens.length > 0 && tags.length > 0) {
                System.out.println("POS Tagging example: " + tokens[0] + " -> " + tags[0]);
            }
            */
            
            return tags;
        } catch (Exception e) {
            System.err.println("Error during POS tagging: " + e.getMessage());
            // Return original tokens as fallback in case of error
            return tokens;
        }
    }

    // Add named entity recognition
    public Map<String, List<String>> findEntities(String tokens) {
        Map<String, List<String>> entities = new HashMap<>();

        return entities;
    }

    public boolean modelIsLoaded() {
        return sentenceDetector != null && tokenizer != null;
    }
}
