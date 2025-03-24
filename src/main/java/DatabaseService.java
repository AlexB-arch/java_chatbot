import java.sql.*;
import java.util.*;

public class DatabaseService {
    private Connection conn;
    
    public DatabaseService() {
        try {
            // Connect to the SQLite database
            conn = DriverManager.getConnection("jdbc:sqlite:chatbotdb");
            System.out.println("Connected to database successfully");
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
    }
    
    /**
     * Execute a SQL query directly
     */
    public List<Map<String, Object>> executeQuery(String sql) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), rs.getObject(i));
                }
                results.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
            e.printStackTrace();
        }
        
        return results;
    }
    
    /**
     * Get classes a student is taking
     */
    public List<Map<String, Object>> getStudentClasses(String studentId) {
        String sql = 
            "SELECT c.id, c.title, c.hrs, s.room, s.days " +
            "FROM student_section ss " +
            "JOIN section s ON ss.sectionID = s.crn " +
            "JOIN course c ON s.courseID = c.id " +
            "WHERE ss.studentID = ?";
        
        return executeParameterizedQuery(sql, studentId);
    }
    
    /**
     * Get majors a student is enrolled in
     */
    public List<Map<String, Object>> getStudentMajors(String studentId) {
        String sql = 
            "SELECT m.id, m.title, m.hrs, m.gpa, d.name as department " +
            "FROM student_major sm " +
            "JOIN major m ON sm.major = m.id " +
            "JOIN department d ON m.deptID = d.id " +
            "WHERE sm.studentID = ?";
        
        return executeParameterizedQuery(sql, studentId);
    }
    
    /**
     * Get concentrations available for a major
     */
    public List<Map<String, Object>> getMajorConcentrations(String majorId) {
        String sql = 
            "SELECT id, title, reqtext " +
            "FROM concentration " +
            "WHERE major = ?";
        
        return executeParameterizedQuery(sql, majorId);
    }
    
    /**
     * Get remaining credit hours for graduation
     */
    public List<Map<String, Object>> getHoursRemaining(String studentId) {
        // This is a more complex calculation that may require multiple queries
        // For this example, we'll use a simplified version
        String sql = 
            "SELECT m.hrs as total_required, SUM(c.hrs) as completed, " +
            "(m.hrs - SUM(c.hrs)) as remaining " +
            "FROM student_major sm " +
            "JOIN major m ON sm.major = m.id " +
            "JOIN student_section ss ON ss.studentID = sm.studentID " +
            "JOIN section s ON ss.sectionID = s.crn " +
            "JOIN course c ON s.courseID = c.id " +
            "WHERE sm.studentID = ? " +
            "GROUP BY m.id";
        
        return executeParameterizedQuery(sql, studentId);
    }
    
    /**
     * Get professor information for a course
     */
    public List<Map<String, Object>> getProfessorForCourse(String studentId, String courseId) {
        String sql = 
            "SELECT t.firstname, t.lastname, d.name as department " +
            "FROM teachers t " +  // Notice "teachers" not "teacher"
            "JOIN section s ON t.id = s.instructor " +  // Note: The section table in the schema doesn't have an instructor column
            "JOIN department d ON t.departmentID = d.id " +
            "WHERE s.courseID = ? AND s.crn IN " +
            "(SELECT sectionID FROM student_section WHERE studentID = ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, courseId);
            pstmt.setString(2, studentId);
            return getResultList(pstmt);
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Get department info for student's major
     */
    public List<Map<String, Object>> getMajorDepartment(String studentId) {
        String sql = 
            "SELECT d.id, d.name, c.name as college " +
            "FROM student_major sm " +
            "JOIN major m ON sm.major = m.id " +
            "JOIN department d ON m.deptID = d.id " +
            "JOIN college c ON d.collegeID = c.id " +
            "WHERE sm.studentID = ?";
        
        return executeParameterizedQuery(sql, studentId);
    }
    
    /**
     * Get names of teachers in a department
     */
    public List<Map<String, Object>> getTeacherNamesInDepartment(String departmentId) {
        String sql = 
            "SELECT firstname, lastname, " +
            "CASE WHEN adjunct = 1 THEN 'Yes' ELSE 'No' END as is_adjunct " +
            "FROM teachers " +  // Notice "teachers" not "teacher"
            "WHERE departmentID = ?";
        
        return executeParameterizedQuery(sql, departmentId);
    }
    
    /**
     * Helper method for parameterized queries
     */
    private List<Map<String, Object>> executeParameterizedQuery(String sql, String param) {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, param);
            return getResultList(pstmt);
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Helper method to convert ResultSet to List of Maps
     */
    private List<Map<String, Object>> getResultList(PreparedStatement pstmt) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (ResultSet rs = pstmt.executeQuery()) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), rs.getObject(i));
                }
                results.add(row);
            }
        }
        
        return results;
    }
    
    /**
     * Close the database connection
     */
    public void close() {
        try {
            if (conn != null) {
                conn.close();
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database: " + e.getMessage());
        }
    }
}