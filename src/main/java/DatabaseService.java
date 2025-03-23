import java.sql.*;
import java.util.*;

public class DatabaseService {
    private DBConnection dbConnection;

    public DatabaseService() {
        this.dbConnection = new DBConnection();
        this.dbConnection.connect();
    }

    public List<Map<String, Object>> executeQuery(String query, Object... params) {
        List<Map<String, Object>> results = new ArrayList<>();

        if (!dbConnection.isConnected()) {
            System.err.println("Database connection not available");
            return results;
        }

        try {
            // Driver to talk to the database
            Connection connection = DriverManager.getConnection("jdbc:sqlite:chatbotdb");
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                // Set parameters
                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i + 1, params[i]);
                }

            // Execute and get results
            try (ResultSet resultSet = statement.executeQuery()) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (resultSet.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(metaData.getColumnName(i), resultSet.getObject(i));
                    }
                    results.add(row);
                }
            }
        } 
    } catch (SQLException e) {
        System.err.println("Error executing query: " + e.getMessage());
        }

        return results;
    }

    public boolean executeUpdate(String query, Object... params) {
        if (!dbConnection.isConnected()) {
            System.err.println("Database connection not available");
            return false;
        }

        try {
            // Driver to talk to the database
            Connection connection = DriverManager.getConnection("jdbc:sqlite:chatbotdb");
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                // Set parameters
                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i + 1, params[i]);
                }

                // Execute update
                int rowsAffected = statement.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error executing update: " + e.getMessage());
            return false;
        }
    }

    public void close() {
        dbConnection.closeConnection();
    }

    public List<Map<String, Object>> getMajorConcentrations(String major) {
        if (major != null) {
            return executeQuery("SELECT * FROM v_major_concentrations WHERE major = ?", major);
        } else {
            return executeQuery("SELECT * FROM v_major_concentrations");
        }
    }

    // 1. Get required grade for a course
    public List<Map<String, Object>> getRequiredGrade(String courseId) {
        return executeQuery("SELECT * FROM v_required_grade WHERE id = ?", courseId);
    }

    // 3. Get remaining hours for a major
    public List<Map<String, Object>> getHoursRemainingForMajor(String studentId, String majorId) {
        return executeQuery("SELECT * FROM v_hours_remaining_major WHERE studentID = ? AND major = ?", 
                           studentId, majorId);
    }

    // 4. Get all classes a student is in
    public List<Map<String, Object>> getStudentClasses(String studentId) {
        return executeQuery("SELECT * FROM v_classes_in WHERE studentID = ?", studentId);
    }

    // 5. Get total credit hours a student is taking
    public List<Map<String, Object>> getTotalHoursTaking(String studentId) {
        return executeQuery("SELECT * FROM v_total_hours_taking WHERE studentID = ?", studentId);
    }

    // 6. Get department of student's major
    public List<Map<String, Object>> getMajorDepartment(String studentId) {
        return executeQuery("SELECT * FROM v_major_department WHERE studentID = ?", studentId);
    }

    // 7. Get professor for a specific course
    public List<Map<String, Object>> getProfessorForCourse(String studentId, String courseId) {
        return executeQuery("SELECT * FROM v_professor_cs375 WHERE studentID = ? AND c.id = ?", 
                           studentId, courseId);
    }

    // 8. Get all majors a student is enrolled in
    public List<Map<String, Object>> getStudentMajors(String studentId) {
        return executeQuery("SELECT * FROM v_student_majors WHERE studentID = ?", studentId);
    }

    // 9. Get classes needed to graduate
    public List<Map<String, Object>> getClassesNeededToGraduate(List<String> requiredCourses, String studentId) {
        // Converting List to appropriate format for SQL IN clause
        String inClause = String.join("','", requiredCourses);
        inClause = "('" + inClause + "')";
        
        // Using string replacement for IN clause and prepared statement for studentId
        String query = "SELECT * FROM v_classes_needed_graduate WHERE c.id IN " + inClause + 
                      " AND studentID = ?";
        
        return executeQuery(query, studentId);
    }

    // 10. Get department whose classes student attends most
    public List<Map<String, Object>> getMostAttendedDepartment(String studentId) {
        return executeQuery("SELECT * FROM v_most_departments_class WHERE studentID = ?", studentId);
    }

    // 11. Get classes student is currently in
    public List<Map<String, Object>> getCurrentClasses(String studentId) {
        return executeQuery("SELECT * FROM v_classes_current WHERE studentID = ?", studentId);
    }

    // 12. Get total hours needed to graduate
    public List<Map<String, Object>> getHoursRemaining(String studentId) {
        return executeQuery("SELECT * FROM v_hours_remaining WHERE studentID = ?", studentId);
    }

    // 13. Get count of teachers in a department
    public List<Map<String, Object>> getTeacherCountInDepartment(String departmentId) {
        return executeQuery("SELECT * FROM v_teachers_sitc_count WHERE departmentID = ?", departmentId);
    }

    // 14. Get names of teachers in a department
    public List<Map<String, Object>> getTeacherNamesInDepartment(String departmentId) {
        return executeQuery("SELECT * FROM v_teachers_sitc_names WHERE departmentID = ?", departmentId);
    }

}