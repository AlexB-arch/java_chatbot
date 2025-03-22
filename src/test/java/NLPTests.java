import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

    // Test unloading models
    @Test
    public void testModelUnloading() {
        nlp.unloadModels();
        Assert.assertFalse(nlp.modelsAreLoaded());
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
    
    @Test
    public void testBasicCourseExtraction() {
        String input = "I'm taking CS375 this semester";
        String[] tokens = nlp.tokenize(input);
        Map<String, List<String>> entities = nlp.findEntities(tokens);
        
        assertTrue(entities.containsKey(NLPService.ENTITY_COURSE));
        // Ignore case
        assertEquals("cs375", entities.get(NLPService.ENTITY_COURSE).get(0));
    }
    
    @Test
    public void testMultipleCourseExtraction() {
        String input = "I need to register for CS101 and MATH200 next term";
        String[] tokens = nlp.tokenize(input);
        Map<String, List<String>> entities = nlp.findEntities(tokens);
        
        assertTrue(entities.containsKey(NLPService.ENTITY_COURSE));
        assertEquals(2, entities.get(NLPService.ENTITY_COURSE).size());
        assertTrue(entities.get(NLPService.ENTITY_COURSE).contains("cs101"));
        assertTrue(entities.get(NLPService.ENTITY_COURSE).contains("math200"));
    }
    
    @Test
    public void testFourLetterPrefix() {
        String input = "HIST101 is my favorite class";
        String[] tokens = nlp.tokenize(input);
        Map<String, List<String>> entities = nlp.findEntities(tokens);
        
        assertTrue("Should extract a course entity", entities.containsKey(NLPService.ENTITY_COURSE));
        assertEquals("Should extract HIST101", "hist101", entities.get(NLPService.ENTITY_COURSE).get(0));
    }
    
    @Test
    public void testCourseAtBeginning() {
        String input = "CS375 is challenging but interesting";
        String[] tokens = nlp.tokenize(input);
        Map<String, List<String>> entities = nlp.findEntities(tokens);
        
        assertTrue("Should extract a course entity", entities.containsKey(NLPService.ENTITY_COURSE));
        assertEquals("Should extract CS375", "cs375", entities.get(NLPService.ENTITY_COURSE).get(0));
    }
    
    @Test
    public void testCourseAtEnd() {
        String input = "My hardest class is PHYS301";
        String[] tokens = nlp.tokenize(input);
        Map<String, List<String>> entities = nlp.findEntities(tokens);
        
        assertTrue("Should extract a course entity", entities.containsKey(NLPService.ENTITY_COURSE));
        assertEquals("Should extract PHYS301", "phys301", entities.get(NLPService.ENTITY_COURSE).get(0));
    }

    @Test
    public void testTeacherBasicStructure() {
        String input = "Sarah Johnson is a professor at the university.";
        String[] tokens = nlp.tokenize(input);
        Map<String, List<String>> entities = nlp.findEntities(tokens);
        
        assertTrue("Should extract a teacher entity", entities.containsKey(NLPService.ENTITY_TEACHER));
        assertTrue("Should extract Sarah Johnson", 
                 entities.get(NLPService.ENTITY_TEACHER).stream()
                 .anyMatch(name -> name.equalsIgnoreCase("Sarah Johnson")));
    }
    
    @Test
    public void testTeacherPossessiveStructure() {
        String input = "Dr. Anderson's class is very challenging but informative.";
        String[] tokens = nlp.tokenize(input);
        Map<String, List<String>> entities = nlp.findEntities(tokens);
        
        assertTrue("Should extract a teacher entity", entities.containsKey(NLPService.ENTITY_TEACHER));

        System.out.println(entities.get(NLPService.ENTITY_TEACHER));

        assertTrue("Should extract Anderson", 
                 entities.get(NLPService.ENTITY_TEACHER).stream()
                 .anyMatch(name -> name.equalsIgnoreCase("Dr. Anderson")));
    }
    
    @Test
    public void testTeacherWithTitle() {
        String input = "Professor Williams teaches CS375 this semester.";
        String[] tokens = nlp.tokenize(input);
        Map<String, List<String>> entities = nlp.findEntities(tokens);
        
        assertTrue("Should extract a teacher entity", entities.containsKey(NLPService.ENTITY_TEACHER));

        System.out.println(entities.get(NLPService.ENTITY_TEACHER));

        assertTrue("Should extract Williams", 
                 entities.get(NLPService.ENTITY_TEACHER).stream()
                 .anyMatch(name -> name.equalsIgnoreCase("Professor Williams")));
    }
    
    @Test
    public void testTeacherAtEnd() {
        String input = "The department chair for Computer Science is Dr. Michael Thompson";
        String[] tokens = nlp.tokenize(input);
        Map<String, List<String>> entities = nlp.findEntities(tokens);
        
        assertTrue("Should extract a teacher entity", entities.containsKey(NLPService.ENTITY_TEACHER));
        assertTrue("Should extract Michael Thompson", 
                 entities.get(NLPService.ENTITY_TEACHER).stream()
                 .anyMatch(name -> name.equalsIgnoreCase("Michael Thompson")));
    }
    
    @Test
    public void testMultipleTeachers() {
        String input = "Jennifer Davis and Robert Wilson are teaching the course together.";
        String[] tokens = nlp.tokenize(input);
        Map<String, List<String>> entities = nlp.findEntities(tokens);
        
        assertTrue("Should extract teacher entities", entities.containsKey(NLPService.ENTITY_TEACHER));
        assertEquals("Should extract 2 teachers", 2, entities.get(NLPService.ENTITY_TEACHER).size());
        
        List<String> teacherNames = entities.get(NLPService.ENTITY_TEACHER);
        boolean hasJenniferDavis = teacherNames.stream()
            .anyMatch(name -> name.equalsIgnoreCase("Jennifer Davis"));
        boolean hasRobertWilson = teacherNames.stream()
            .anyMatch(name -> name.equalsIgnoreCase("Robert Wilson"));
            
        assertTrue("Should contain Jennifer Davis", hasJenniferDavis);
        assertTrue("Should contain Robert Wilson", hasRobertWilson);
    }
}