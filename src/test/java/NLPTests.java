
import java.util.List;
import java.util.Map;

import org.junit.*;

public class NLPTests {

    NLPService nlp = new NLPService();

    // Test NLPService model loading
    @Test
    public void testModelLoading() {
        Assert.assertTrue(nlp.modelsAreLoaded());
    }

    // Test sentence detection
    @Test
    public void testSentenceDetection() {
        String text = "Hello there. How are you doing today? This is a test.";
        String[] sentences = nlp.detectSentences(text);

        // Output a string to the console to verify the test
        System.out.println("Detected " + sentences.length + " sentences:");
        for (String sentence : sentences) {
            System.out.println("- " + sentence);
        }

        Assert.assertEquals(3, sentences.length);
    }

    // Test tokenization
    @Test
    public void testTokenization() {
        String sentence = "This is a test sentence.";
        String[] tokens = nlp.tokenize(sentence);

        // Output a string to the console to verify the test
        System.out.println("Tokenized sentence:");
        for (String token : tokens) {
            System.out.println("- " + token);
        }

        Assert.assertEquals(6, tokens.length);
    }

    // Test fallback behavior
    @Test
    public void testFallback() {
        // Unload the models
        nlp.unloadSentenceDetector();
        nlp.unloadTokenizer();

        // Test sentence detection
        String text = "Hello there. How are you doing today? This is a test.";
        String[] sentences = nlp.detectSentences(text);

        // Output a string to the console to verify the test
        System.out.println("Detected " + sentences.length + " sentences:");
        for (String sentence : sentences) {
            System.out.println("- " + sentence);
        }

        Assert.assertEquals(1, sentences.length);

        // Test tokenization
        String sentence = "This is a test sentence.";
        String[] tokens = nlp.tokenize(sentence);

        // Output a string to the console to verify the test
        System.out.println("Tokenized sentence:");
        for (String token : tokens) {
            System.out.println("- " + token);
        }

        Assert.assertEquals(5, tokens.length);
    }

    // Test POS tagging
    @Test
    public void testPOSTagging() {
        String sentence = "This is a test sentence.";
        String[] tokens = nlp.tokenize(sentence);
        String[] tags = nlp.tagPOS(tokens);

        // Output a string to the console to verify the test
        System.out.println("POS Tagging example:");
        for (int i = 0; i < tokens.length; i++) {
            System.out.println(tokens[i] + " -> " + tags[i]);
        }

        Assert.assertEquals(6, tags.length);
    }

    // Test Name Entity Recognition
    @Test
    public void testNameEntityRecognition() {
        String sentence = "John Smith is a professor at the university.";
        String[] tokens = nlp.tokenize(sentence);
        Map<String, List<String>> entities = nlp.findEntities(tokens);

        for (String type : entities.keySet()) {
            List<String> names = entities.get(type);
            for (String name : names) {
                System.out.println(type + ": " + name);
            }
        }
        
        // Check if the model correctly identified "John Smith" as a person
        boolean hasPersonEntity = entities.containsKey("person") && 
                                 entities.get("person").stream()
                                 .anyMatch(name -> name.contains("John"));
        
        Assert.assertTrue("Should recognize 'John Smith' as a person entity", hasPersonEntity);
    }

    // Test unloading models
    @Test
    public void testModelUnloading() {
        nlp.unloadModels();
        Assert.assertFalse(nlp.modelsAreLoaded());
    }
}