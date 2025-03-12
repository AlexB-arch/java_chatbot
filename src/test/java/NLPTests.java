
import org.junit.*;

public class NLPTests {

    NLPService nlp = new NLPService();

    // Test NLPService model loading
    @Test
    public void testModelLoading() {
        Assert.assertTrue(nlp.modelIsLoaded());
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
}
