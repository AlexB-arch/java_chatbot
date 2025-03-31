import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseChatService extends BaseQueryProcessor {
    private static final String SQL_PATTERN = "```sql\\s+(.*?)\\s+```";
    
    private ChatService chatService;
    private DatabaseService dbService;
    
    public DatabaseChatService(String apiKey) {
        this.dbService = new DatabaseService();
        this.chatService = new ChatService(apiKey);
        this.initializeContext();
    }
    
    private void initializeContext() {
        // Provide detailed context about the database schema and available methods
        String systemPrompt = 
            "You are an AI assistant for an educational database system. " +
            "Answer questions by generating SQL queries that extract the needed information from these tables:\n\n" +
            
            "1. college (id, name)\n" +
            "2. student (id, firstname, lastname)\n" +
            "3. department (id, name, collegeID)\n" +
            "4. course (id, department, title, num, hrs)\n" +
            "5. major (id, title, deptID, reqtext, hrs, gpa)\n" +
            "6. teachers (id, firstname, lastname, departmentID, adjunct)\n" +
            "7. section (crn, max, room, courseID, term, startdate, enddate, days)\n" +
            "8. student_section (studentID, sectionID, grade)\n" +
            "9. major_class (majorID, classID)\n" +
            "10. and_prereq (course, prereq)\n" +
            "11. or_prereq (course, prereq)\n" +
            "12. coreq (course, prereq)\n" +
            "13. student_major (studentID, major)\n" +
            "14. concentration (id, major, title, reqtext)\n\n" +
            
<<<<<<< HEAD
            "When asked a question about the database, you have two options:\n\n" +
            
            "OPTION 1: Generate a SQL query to answer the question directly. " + 
            "Format your response with a SQL query between ```sql tags like this:\n" +
            "```sql\n" +
            "SELECT * FROM your_query_here;\n" +
            "```\n\n" +
            
            "OPTION 2: Call an existing database service method if appropriate. " +
            "Format your response like this:\n" +
            "METHOD: methodName PARAMS: param1, param2, ...\n\n" +
            
            "Available database methods:\n" +
            "- getStudentClasses(studentId) - Get classes a student is taking\n" +
            "- getStudentMajors(studentId) - Get majors a student is enrolled in\n" +
            "- getMajorConcentrations(majorId) - Get concentrations for a major\n" +
            "- getHoursRemaining(studentId) - Get remaining credit hours for graduation\n" +
            "- getProfessorForCourse(studentId, courseId) - Get professor info for a course\n" +
            "- getMajorDepartment(studentId) - Get department info for student's major\n" +
            "- getTeacherNamesInDepartment(departmentId) - Get names of teachers in a department\n\n" +
            
=======
>>>>>>> 30f77a5 (Some more changes)
            "After you get the results, explain them clearly and conversationally.";
            
        chatService.setSystemMessage(systemPrompt);
    }
    
    @Override
    public String processQuery(String userQuestion) {
<<<<<<< HEAD
        // Ask ChatGPT how to handle this query
        String chatResponse = chatService.sendMessage(
            "User question: \"" + userQuestion + "\"\n" +
            "Based on this question, either generate an SQL query or suggest a database method call to get the information."
        );
        
        List<Map<String, Object>> results = null;
        
        // Check if the response contains a SQL query
=======
        // Ask the LLM to generate a SQL query for the question
        String chatResponse = chatService.sendMessage(
            "User question: \"" + userQuestion + "\"\n" +
            "Generate an appropriate SQL query to answer this question based on the database schema."
        );
        
        // Extract SQL query from the response
>>>>>>> 30f77a5 (Some more changes)
        Pattern sqlPattern = Pattern.compile(SQL_PATTERN, Pattern.DOTALL);
        Matcher sqlMatcher = sqlPattern.matcher(chatResponse);
        
        List<Map<String, Object>> results;
        
        if (sqlMatcher.find()) {
            // Extract the SQL query
            String sqlQuery = sqlMatcher.group(1).trim();
            
            // Execute the SQL query directly
            results = dbService.executeQuery(sqlQuery);
        } else {
<<<<<<< HEAD
            // Check if the response suggests a method call
            Pattern methodPattern = Pattern.compile(METHOD_PATTERN);
            Matcher methodMatcher = methodPattern.matcher(chatResponse);
            
            if (methodMatcher.find()) {
                String methodName = methodMatcher.group(1);
                String paramsString = methodMatcher.group(2);
                String[] params = paramsString.split(",\\s*");
                
                // Call the appropriate method based on the suggestion
                switch (methodName) {
                    case "getStudentClasses":
                        results = dbService.getStudentClasses(params[0]);
                        break;
                    case "getStudentMajors":
                        results = dbService.getStudentMajors(params[0]);
                        break;
                    case "getMajorConcentrations":
                        results = dbService.getMajorConcentrations(params[0]);
                        break;
                    case "getHoursRemaining":
                        results = dbService.getHoursRemaining(params[0]);
                        break;
                    case "getProfessorForCourse":
                        results = dbService.getProfessorForCourse(params[0], params[1]);
                        break;
                    case "getMajorDepartment":
                        results = dbService.getMajorDepartment(params[0]);
                        break;
                    case "getTeacherNamesInDepartment":
                        results = dbService.getTeacherNamesInDepartment(params[0]);
                        break;
                    default:
                        return "I'm not sure how to process that request. Method not recognized: " + methodName;
                }
            } else {
                // If no SQL or method call is found, just return the original response
                return chatResponse;
            }
=======
            // Fallback if no SQL was found
            return "I couldn't generate a proper SQL query for your question. " +
                   "Could you please rephrase your question?";
>>>>>>> 30f77a5 (Some more changes)
        }
        
        // Format the results
        String formattedResults = formatResults(results);
        
<<<<<<< HEAD
        // Ask ChatGPT to interpret the results
=======
        // Ask the LLM to generate a conversational response based on the results
>>>>>>> 30f77a5 (Some more changes)
        return chatService.sendMessage(
            "User question: \"" + userQuestion + "\"\n" +
            "Database results:\n" + formattedResults + 
            "\nPlease provide a helpful, conversational response based on these results."
        );
    }
    
    // Format database results into a readable string
        protected String formatResults(List<Map<String, Object>> results) {
            if (results == null || results.isEmpty()) {
                return "No results found.";
            }
            
            StringBuilder sb = new StringBuilder();
            for (Map<String, Object> row : results) {
                sb.append("- ");
                for (Map.Entry<String, Object> entry : row.entrySet()) {
                    sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(", ");
                }
                // Remove the trailing comma and space
                if (sb.length() > 2) {
                    sb.setLength(sb.length() - 2);
                }
                sb.append("\n");
            }
            return sb.toString();
        }
    
    @Override
    public void close() {
        try {
            // Close the chat service
            if (chatService != null) {
                chatService.close();
            }
            
            // Close the database service
            if (dbService != null) {
                dbService.close();
            }
        } catch (Exception e) {
            System.err.println("Error closing services: " + e.getMessage());
        }
    }
}