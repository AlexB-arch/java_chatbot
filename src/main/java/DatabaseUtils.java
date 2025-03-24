import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for working with database operations
 */
public final class DatabaseUtils {
    
    // Private constructor to prevent instantiation
    private DatabaseUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Extract student ID from a question
     * 
     * @param question The question to extract from
     * @return The extracted student ID or null if none found
     */
    public static String extractStudentId(String question) {
        String[] words = question.split("\\s+");
        for (int i = 0; i < words.length - 1; i++) {
            if ((words[i].equalsIgnoreCase("student") || 
                 words[i].equalsIgnoreCase("id")) && 
                 i + 1 < words.length && 
                 words[i + 1].matches("\\d+")) {
                return words[i + 1];
            }
        }
        
        for (String word : words) {
            if (word.matches("\\d+") && word.length() <= 3) {
                return word;
            }
        }
        
        return null;
    }
    
    /**
     * Extract course ID from a question
     * 
     * @param question The question to extract from
     * @return The extracted course ID or null if none found
     */
    public static String extractCourseId(String question) {
        String[] words = question.split("\\s+");
        for (String word : words) {
            if (word.matches("[A-Z]{2,4}\\d{3}")) {
                return word;
            }
        }
        return null;
    }
    
    /**
     * Extract major ID from a question
     * 
     * @param question The question to extract from
     * @return The extracted major ID or null if none found
     */
    public static String extractMajorId(String question) {
        String[] majors = {"CS", "IS", "DET", "ART", "EE", "ACCT", "MATH", "PHYS", "CHEM", "BIO", "ENG", "HIST", "PSYCH", "SOC", "ANTH", "MUS", "PHIL", "COMM", "ECON"};
        for (String major : majors) {
            if (question.toUpperCase().contains(major)) {
                return major;
            }
        }
        return null;
    }
    
    /**
     * Sanitize a string for SQL to prevent SQL injection
     * 
     * @param input The input to sanitize
     * @return Sanitized input
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return input.replace("'", "''");
    }
}