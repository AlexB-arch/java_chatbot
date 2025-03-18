import java.util.*;

public class QuestionAnalyzer {
    private NLPService nlpService;
    private DatabaseService dbService;

    public QuestionAnalyzer(NLPService nlpService, DatabaseService dbService) {
        this.nlpService = nlpService;
        this.dbService = dbService;
    }

    // Inner class for structured query results
    public class QueryResult {
        private String originalText;
        private String intent;
        private Map<String, List<String>> entities;
        private List<String> keywords;
        
        public QueryResult(String originalText, String intent, 
                          Map<String, List<String>> entities, List<String> keywords) {
            this.originalText = originalText;
            this.intent = intent;
            this.entities = entities;
            this.keywords = keywords;
        }
        
        // Getters
        public String getOriginalText() { return originalText; }
        public String getIntent() { return intent; }
        public Map<String, List<String>> getEntities() { return entities; }
        public List<String> getKeywords() { return keywords; }
        
        // Helper methods
        public boolean hasEntityType(String type) {
            return entities.containsKey(type) && !entities.get(type).isEmpty();
        }
        
        public String getFirstEntity(String type) {
            if (hasEntityType(type)) {
                return entities.get(type).get(0);
            }
            return null;
        }
        
        @Override
        public String toString() {
            return "QueryResult{" +
                    "intent='" + intent + '\'' +
                    ", entities=" + entities +
                    ", keywords=" + keywords +
                    '}';
        }
    }

    public String processQuestion(String question) {
        // Create a complete query result with intent and entities
        QueryResult queryResult = analyzeQuestion(question);
        
        // Handle response based on intent and entities
        return generateResponse(queryResult);
    }
    
    private QueryResult analyzeQuestion(String question) {
        // Step 1: Tokenize the question
        String[] tokens = nlpService.tokenize(question);

        // Step 2: Analyze POS tags if available
        String[] posTags = {};
        try {
            posTags = nlpService.tagPOS(tokens);
        } catch (Exception e) {
            System.err.println("Error while tagging POS: " + e.getMessage());
        }

        // Step 3: Extract named entities
        Map<String, List<String>> entities = nlpService.findEntities(tokens);
        
        // Step 4: Detect intent
        String intent = nlpService.detectIntent(question);
        
        // Step 5: Extract keywords for database search
        List<String> keywords = extractKeywords(tokens, posTags);
        
        return new QueryResult(question, intent, entities, keywords);
    }

    private String generateResponse(QueryResult query) {
        // Different handling based on detected intent
        switch (query.getIntent()) {
            case NLPService.INTENT_QUESTION:
                return handleQuestionIntent(query);
                
            case NLPService.INTENT_SEARCH:
                return handleSearchIntent(query);
                
            case NLPService.INTENT_INFO:
                return handleInformationIntent(query);
                
            case NLPService.INTENT_ENROLL:
                return handleEnrollmentIntent(query);
                
            case NLPService.INTENT_SCHEDULE:
                return handleScheduleIntent(query);
                
            default:
                // Fallback to general keyword search
                return handleGeneralQuery(query);
        }
    }
    
    private String handleQuestionIntent(QueryResult query) {
        // Check for specific entity types and construct targeted database queries
        if (query.hasEntityType(NLPService.ENTITY_COURSE)) {
            String courseId = query.getFirstEntity(NLPService.ENTITY_COURSE);
            String originalText = query.getOriginalText().toLowerCase();
            
            // Different queries based on the question context
            if (originalText.contains("prerequisite") || originalText.contains("require")) {
                List<Map<String, Object>> results = dbService.executeQuery(
                    "SELECT prerequisites FROM courses WHERE course_id = ?", courseId);
                return formatCoursePrerequisites(courseId, results);
            } 
            else if (originalText.contains("professor") || originalText.contains("teacher") || 
                    originalText.contains("instructor")) {
                List<Map<String, Object>> results = dbService.executeQuery(
                    "SELECT instructor FROM course_sections WHERE course_id = ?", courseId);
                return formatCourseInstructors(courseId, results);
            }
            else {
                // General course info
                List<Map<String, Object>> results = dbService.executeQuery(
                    "SELECT * FROM courses WHERE course_id = ?", courseId);
                return formatCourseInfo(results);
            }
        }
        else if (query.hasEntityType(NLPService.ENTITY_TEACHER)) {
            // Query for teacher information
            String teacher = query.getFirstEntity(NLPService.ENTITY_TEACHER);
            List<Map<String, Object>> results = dbService.executeQuery(
                "SELECT * FROM faculty WHERE name LIKE ?", "%" + teacher + "%");
            return formatTeacherInfo(results);
        }
        
        // If no specific entities found, fall back to keyword search
        return handleGeneralQuery(query);
    }
    
    private String handleSearchIntent(QueryResult query) {
        // More focused search based on entity types
        List<Map<String, Object>> results;
        
        if (query.hasEntityType(NLPService.ENTITY_COURSE)) {
            String courseId = query.getFirstEntity(NLPService.ENTITY_COURSE);
            results = dbService.executeQuery(
                "SELECT * FROM courses WHERE course_id = ?", courseId);
        } else if (query.hasEntityType(NLPService.ENTITY_MAJOR)) {
            String major = query.getFirstEntity(NLPService.ENTITY_MAJOR);
            results = dbService.executeQuery(
                "SELECT * FROM majors WHERE name LIKE ?", "%" + major + "%");
        } else {
            // General keyword search
            results = dbService.searchByKeywords(query.getKeywords());
        }
        
        return formatSearchResults(results);
    }
    
    private String handleInformationIntent(QueryResult query) {
        // Similar to question intent but with more emphasis on detailed information
        return handleQuestionIntent(query);
    }
    
    private String handleEnrollmentIntent(QueryResult query) {
        if (query.hasEntityType(NLPService.ENTITY_COURSE)) {
            String courseId = query.getFirstEntity(NLPService.ENTITY_COURSE);
            List<Map<String, Object>> results = dbService.executeQuery(
                "SELECT enrollment_status, available_seats FROM course_sections WHERE course_id = ?", courseId);
            return formatEnrollmentInfo(courseId, results);
        } else {
            // General enrollment information
            return "To enroll in courses, you need to access the student portal and select 'Course Registration'. " +
                  "Registration for the upcoming semester usually opens 4 weeks before the semester starts.";
        }
    }
    
    private String handleScheduleIntent(QueryResult query) {
        // Information about schedules
        if (query.hasEntityType(NLPService.ENTITY_COURSE)) {
            String courseId = query.getFirstEntity(NLPService.ENTITY_COURSE);
            List<Map<String, Object>> results = dbService.executeQuery(
                "SELECT section_id, days, start_time, end_time, room FROM course_sections WHERE course_id = ?", 
                courseId);
            return formatCourseSchedule(courseId, results);
        }
        
        return "The academic calendar and course schedules can be accessed through the student portal. " +
               "If you're looking for a specific course schedule, please mention the course ID.";
    }
    
    private String handleGeneralQuery(QueryResult query) {
        // Default handling with keyword search
        List<Map<String, Object>> results = dbService.searchByKeywords(query.getKeywords());
        return formatResponse(results, query.getOriginalText());
    }

    private List<String> extractKeywords(String[] tokens, String[] posTags) {
        List<String> keywords = new ArrayList<>();

        // If we don't have POS tags, just use important words based on length
        if (posTags == null || posTags.length == 0) {
            for (String token : tokens) {
                // Skip common short words and common stopwords
                if (token.length() > 3 && !isStopWord(token)) {
                    keywords.add(token.toLowerCase());
                }
            }
            return keywords;
        }

        // If we do have POS tags, extract nouns and verbs
        for (int i = 0; i < tokens.length && i < posTags.length; i++) {
            // Usually NN* are nouns and VB* are verbs
            if ((posTags[i].startsWith("NN") || posTags[i].startsWith("VB")) && !isStopWord(tokens[i])){
                keywords.add(tokens[i].toLowerCase());
            }
        }

        return keywords;
    }

    private boolean isStopWord(String word) {
        String[] stopWords = {"the", "a", "an", "and", "or", "but", "is", "are", "was",
                            "were", "be", "been", "being", "in", "on", "at", "to", "for",
                            "with", "by", "about", "like", "through", "over", "before",
                            "between", "after", "since", "without", "under", "within"};
        
        for (String stopWord : stopWords) {
            if (stopWord.equalsIgnoreCase(word)) {
                return true;
            }
        }
        return false;
    }

    private String formatResponse(List<Map<String, Object>> results, String question) {
        if (results.isEmpty()) {
            return "I don't have information about that. Could you ask something else?";
        }

        if (results.get(0).containsKey("answer")) {
            return results.get(0).get("answer").toString();
        } else if (results.get(0).containsKey("content")) {
            return results.get(0).get("content").toString();
        }

        StringBuilder response = new StringBuilder("Here's what I found:\n");
        for (Map<String, Object> result : results) {
            response.append("- ");
            // Use the first column as the response
            response.append(result.values().iterator().next());
            response.append("\n");
        }

        return response.toString();
    }
    
    // Add new formatting methods for specific entity types
    private String formatCourseInfo(List<Map<String, Object>> results) {
        if (results.isEmpty()) {
            return "I couldn't find information about that course.";
        }
        
        Map<String, Object> course = results.get(0);
        StringBuilder response = new StringBuilder();
        
        response.append("Course: ").append(course.get("course_id")).append(" - ")
               .append(course.get("title")).append("\n");
        
        if (course.containsKey("description")) {
            response.append("Description: ").append(course.get("description")).append("\n");
        }
        
        if (course.containsKey("credits")) {
            response.append("Credits: ").append(course.get("credits")).append("\n");
        }
        
        return response.toString();
    }
    
    private String formatCoursePrerequisites(String courseId, List<Map<String, Object>> results) {
        if (results.isEmpty() || results.get(0).get("prerequisites") == null) {
            return "Course " + courseId + " doesn't have any prerequisites.";
        }
        
        return "Prerequisites for " + courseId + ": " + results.get(0).get("prerequisites");
    }
    
    private String formatCourseInstructors(String courseId, List<Map<String, Object>> results) {
        if (results.isEmpty()) {
            return "I couldn't find instructor information for " + courseId + ".";
        }
        
        Set<String> instructors = new HashSet<>();
        for (Map<String, Object> row : results) {
            instructors.add(row.get("instructor").toString());
        }
        
        return "Course " + courseId + " is taught by: " + String.join(", ", instructors);
    }
    
    private String formatTeacherInfo(List<Map<String, Object>> results) {
        if (results.isEmpty()) {
            return "I couldn't find information about that instructor.";
        }
        
        Map<String, Object> teacher = results.get(0);
        StringBuilder response = new StringBuilder();
        
        response.append(teacher.get("name")).append("\n");
        
        if (teacher.containsKey("title")) {
            response.append("Title: ").append(teacher.get("title")).append("\n");
        }
        
        if (teacher.containsKey("department")) {
            response.append("Department: ").append(teacher.get("department")).append("\n");
        }
        
        if (teacher.containsKey("email")) {
            response.append("Email: ").append(teacher.get("email")).append("\n");
        }
        
        return response.toString();
    }
    
    private String formatEnrollmentInfo(String courseId, List<Map<String, Object>> results) {
        if (results.isEmpty()) {
            return "I couldn't find enrollment information for " + courseId + ".";
        }
        
        StringBuilder response = new StringBuilder("Enrollment information for " + courseId + ":\n");
        
        for (int i = 0; i < results.size(); i++) {
            Map<String, Object> section = results.get(i);
            response.append("Section ").append(i + 1).append(": ");
            
            if ("open".equalsIgnoreCase(section.get("enrollment_status").toString())) {
                response.append("Open with ").append(section.get("available_seats")).append(" seats available");
            } else {
                response.append("Closed/Full");
            }
            
            response.append("\n");
        }
        
        return response.toString();
    }
    
    private String formatCourseSchedule(String courseId, List<Map<String, Object>> results) {
        if (results.isEmpty()) {
            return "I couldn't find schedule information for " + courseId + ".";
        }
        
        StringBuilder response = new StringBuilder("Schedule for " + courseId + ":\n");
        
        for (Map<String, Object> section : results) {
            response.append("Section ").append(section.get("section_id"))
                   .append(": ").append(section.get("days"))
                   .append(" ").append(section.get("start_time"))
                   .append("-").append(section.get("end_time"))
                   .append(" in ").append(section.get("room"))
                   .append("\n");
        }
        
        return response.toString();
    }
    
    private String formatSearchResults(List<Map<String, Object>> results) {
        if (results.isEmpty()) {
            return "No matching records found.";
        }
        
        StringBuilder response = new StringBuilder("Search results:\n");
        
        for (Map<String, Object> result : results) {
            response.append("- ");
            
            // Try to construct a meaningful summary based on available fields
            if (result.containsKey("title")) {
                if (result.containsKey("course_id")) {
                    response.append(result.get("course_id")).append(": ");
                }
                response.append(result.get("title"));
            } else {
                // Just use the first value
                response.append(result.values().iterator().next());
            }
            
            response.append("\n");
        }
        
        return response.toString();
    }
}