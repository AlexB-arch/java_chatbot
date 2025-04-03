public class Student {
    private String name;
    private int studentId;
    private String major;
    private double gpa;

    public Student() {
        name = "Student McStudentface";
        studentId = 80;
        major = "Accounting";
        gpa = 3.95;
    }
    
    // Add constructor with parameters
    public Student(String name, int studentId, String major, double gpa) {
        this.name = name;
        this.studentId = studentId;
        this.major = major;
        this.gpa = gpa;
    }
    
    // Add getters
    public String getName() {
        return name;
    }
    
    public int getStudentId() {
        return studentId;
    }
    
    public String getMajor() {
        return major;
    }
    
    public double getGpa() {
        return gpa;
    }
}