import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class NLPService {
    // Models for NLP tasks
    private SentenceDetectorME sentenceDetector;
    private TokenizerME tokenizer;
    private POSTaggerME posTagger;
    private NameFinderME nameFinder;

    // Entity types
    public static final String ENTITY_COURSE = "course";
    public static final String ENTITY_COLLEGE = "college";
    public static final String ENTITY_TEACHER = "teacher";
    public static final String ENTITY_DEPARTMENT = "department";
    public static final String ENTITY_STUDENT = "student";
    public static final String ENTITY_MAJOR = "major";
    public static final String ENTITY_MINOR = "minor";
    public static final String ENTITY_CONCENTRATION = "concentration";
    public static final String ENTITY_SECTION = "section";

    // Intent types
    public static final String INTENT_QUESTION = "question";
    public static final String INTENT_SEARCH = "search";
    public static final String INTENT_INFO = "information";
    public static final String INTENT_ENROLL = "enrollment";
    public static final String INTENT_SCHEDULE = "schedule";
    public static final String INTENT_UNKNOWN = "unknown";

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
                //System.out.println("Sentence detector model loaded successfully");
            } else {
                System.err.println("Error loading sentence detector model");

                // Try a different location
                try (FileInputStream fileIn = new FileInputStream("src/main/resources/models/en-sent.bin")) {
                    SentenceModel sentenceModel = new SentenceModel(fileIn);
                    sentenceDetector = new SentenceDetectorME(sentenceModel);
                    //System.out.println("Sentence detector model loaded from file system.");
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
                //System.out.println("Tokenizer model loaded successfully");
            } else {
                System.err.println("Error loading tokenizer model");

                // Try a different location
                try (FileInputStream fileIn = new FileInputStream("src/main/resources/models/en-token.bin")) {
                    TokenizerModel tokenizerModel = new TokenizerModel(fileIn);
                    tokenizer = new TokenizerME(tokenizerModel);
                    //System.out.println("Tokenizer model loaded from file system.");
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
                //System.out.println("POS tagger model loaded successfully");
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
                //System.out.println("Named entity recognition model loaded successfully");
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

    // Unload all models at once
    public void unloadModels() {
        unloadSentenceDetector();
        unloadTokenizer();
        unloadPOSTagger();
        unloadNameFinder();
    }

    // Check if models are loaded
    public boolean modelsAreLoaded() {
        return sentenceDetector != null && tokenizer != null && posTagger != null && nameFinder != null;
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
            
            return tags;
        } catch (Exception e) {
            System.err.println("Error during POS tagging: " + e.getMessage());
            // Return original tokens as fallback in case of error
            return tokens;
        }
    }

    // Add named entity recognition
    public Map<String, List<String>> findEntities(String[] tokens) {
        Map<String, List<String>> entities = new HashMap<>();
    
        // Use existing person NER model
        if (nameFinder != null) {
            try {
                Span[] personSpans = nameFinder.find(tokens);
                for (Span span : personSpans) {
                    String entity = String.join(" ", Arrays.copyOfRange(tokens, span.getStart(), span.getEnd()));
                    entities.computeIfAbsent(ENTITY_TEACHER, _ -> new ArrayList<>()).add(entity);
                }

                nameFinder.clearAdaptiveData(); // Clear adaptive data between documents
            } catch (Exception e) {
                System.err.println("Error in named entity recognition: " + e.getMessage());
            }
        }
    
        // Add rule-based entity recognition for academic domain
        String sentence = String.join(" ", tokens).toLowerCase();
        
        findPattern(sentence, "\\b(?:dr|professor|prof)\\.?\\s+([A-Za-z]+)(?:'s)?\\b", ENTITY_TEACHER, entities);
        
        // Course detection (e.g., CS101, MATH200)
        findPattern(sentence, "\\b[A-Z]{2,4}\\d{3}\\b", ENTITY_COURSE, entities);
        
        // Department detection
        findPattern(sentence, "\\bdepartment of [a-z]+\\b", ENTITY_DEPARTMENT, entities);
    
        // Major/Minor detection
        if (sentence.contains(" major") || sentence.contains("majoring")) {
            findSurroundingContextEntities(tokens, "major", ENTITY_MAJOR, entities);
        }

        if (sentence.contains(" minor") || sentence.contains("minoring")) {
            findSurroundingContextEntities(tokens, "minor", ENTITY_MINOR, entities);
        }

        // College detection
        findPattern(sentence, "\\bcollege of [a-z]+\\b", ENTITY_COLLEGE, entities);

        // Concentration detection
        findPattern(sentence, "\\bconcentration in [a-z]+\\b", ENTITY_CONCENTRATION, entities);

        // Section detection
        findPattern(sentence, "\\bsection [A-Z]\\d{3}\\b", ENTITY_SECTION, entities);
    
        return entities;
    }

    // Add intent detection method
    public String detectIntent(String text) {
        text = text.toLowerCase();
        
        // Question intent
        if (text.contains("?") || 
            text.startsWith("who") || text.startsWith("what") || 
            text.startsWith("when") || text.startsWith("where") || 
            text.startsWith("why") || text.startsWith("how") ||
            text.contains("tell me") || text.contains("show me")) {
            return INTENT_QUESTION;
        }
        
        // Search intent
        if (text.contains("find") || text.contains("search") || 
            text.contains("look for") || text.contains("locate") ||
            text.contains("where is") || text.contains("where are")) {
            return INTENT_SEARCH;
        }
        
        // Schedule intent
        if (text.contains("schedule") || text.contains("timetable") || 
            text.contains("when") || text.contains("time") || 
            text.contains("semester") || text.contains("class time")) {
            return INTENT_SCHEDULE;
        }
        
        // Enrollment intent
        if (text.contains("enroll") || text.contains("register") || 
            text.contains("sign up") || text.contains("take class") ||
            text.contains("join") || text.contains("prerequisites")) {
            return INTENT_ENROLL;
        }
        
        // Information intent (most general, check last)
        if (text.contains("information") || text.contains("details") || 
            text.contains("describe") || text.contains("explain") ||
            text.contains("about")) {
            return INTENT_INFO;
        }
        
        return INTENT_UNKNOWN;
    }

    // Helper method to find patterns in text
    private void findPattern(String text, String regex, String entityType, Map<String, List<String>> entities) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex, java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {
            String entity = matcher.group().trim();
            entities.computeIfAbsent(entityType, k -> new ArrayList<>()).add(entity);
        }
    }

    // Helper method to find entities based on context words
    private void findSurroundingContextEntities(String[] tokens, String contextWord, String entityType, Map<String, List<String>> entities) {
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].toLowerCase().contains(contextWord)) {
                // Check for entity before the context word (e.g., "Computer Science major")
                if (i > 0 && Character.isUpperCase(tokens[i-1].charAt(0))) {
                    String entity = tokens[i-1];
                    // Include multi-word entities (e.g., "Computer Science")
                    int j = i-2;
                    while (j >= 0 && Character.isUpperCase(tokens[j].charAt(0))) {
                        entity = tokens[j] + " " + entity;
                        j--;
                    }
                    entities.computeIfAbsent(entityType, k -> new ArrayList<>()).add(entity);
                }
                
                // Check for "in" pattern (e.g., "major in Computer Science")
                if (i < tokens.length - 2 && tokens[i+1].equalsIgnoreCase("in")) {
                    StringBuilder entity = new StringBuilder(tokens[i+2]);
                    // Include multi-word entities
                    for (int j = i+3; j < tokens.length && Character.isUpperCase(tokens[j].charAt(0)); j++) {
                        entity.append(" ").append(tokens[j]);
                    }
                    entities.computeIfAbsent(entityType, k -> new ArrayList<>()).add(entity.toString());
                }
            }
        }
    }
}
