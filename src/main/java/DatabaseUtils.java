import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for working with database operations
 */
public final class DatabaseUtils {
    
    // Regex patterns for extracting entities from questions
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("student\\s+(\\d+)|student\\s+id\\s*(\\d+)|student\\s*#\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern COURSE_ID_PATTERN = Pattern.compile("([A-Z]{2,4})\\s*(\\d{3,4})", Pattern.CASE_INSENSITIVE);
    private static final Pattern MAJOR_ID_PATTERN = Pattern.compile("(CS|IS|DET|ART|EE|ACCT|BUS|MATH|HIST|ENG)", Pattern.CASE_INSENSITIVE);
    
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
        Matcher matcher = STUDENT_ID_PATTERN.matcher(question);
        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                if (matcher.group(i) != null) {
                    return matcher.group(i);
                }
            }
        }
        
        // Just look for numbers that could be IDs
        Pattern simplePattern = Pattern.compile("\\b(\\d{1,3})\\b");
        Matcher simpleMatcher = simplePattern.matcher(question);
        if (simpleMatcher.find()) {
            return simpleMatcher.group(1);
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
        Matcher matcher = COURSE_ID_PATTERN.matcher(question);
        if (matcher.find()) {
            String dept = matcher.group(1).toUpperCase();
            String num = matcher.group(2);
            return dept + num;
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
        Matcher matcher = MAJOR_ID_PATTERN.matcher(question);
        if (matcher.find()) {
            return matcher.group(1).toUpperCase();
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