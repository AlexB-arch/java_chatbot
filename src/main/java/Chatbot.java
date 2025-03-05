
import java.sql.*;
import java.util.Scanner;

public class Chatbot {
    // List of views available for querying
    private static final String[] VIEWS = {
        "v_required_grade",
        "v_major_concentrations",
        "v_hours_remaining_major",
        "v_classes_in",
        "v_total_hours_taking",
        "v_major_department",
        "v_professor_cs375",
        "v_student_majors",
        "v_classes_needed_graduate",
        "v_most_departments_class",
        "v_classes_current",
        "v_hours_remaining",
        "v_teachers_sitc_count",
        "v_teachers_sitc_names"
    };
}