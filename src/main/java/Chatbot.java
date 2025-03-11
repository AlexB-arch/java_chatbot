
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

    public static void main(String[] args) {
        // Connect to the database
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:chatbot.db");
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database: " + e.getMessage());
            System.exit(1);
        }

        // Create a scanner to read user input
        Scanner scanner = new Scanner(System.in);

        // Main loop
        while (true) {
            // Print the list of views
            System.out.println("Available views:");
            for (int i = 0; i < VIEWS.length; i++) {
                System.out.println((i + 1) + ". " + VIEWS[i]);
            }

            // Ask the user to select a view
            System.out.print("Enter the number of the view you would like to query (or 0 to exit): ");
            int viewIndex = scanner.nextInt();
            if (viewIndex == 0) {
                break;
            } else if (viewIndex < 1 || viewIndex > VIEWS.length) {
                System.out.println("Invalid view number.");
                continue;
            }

            // Get the view name
            String viewName = VIEWS[viewIndex - 1];

            // Query the view
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM " + viewName);
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnCount = rsmd.getColumnCount();

                // Print the column names
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(rsmd.getColumnName(i));
                    if (i < columnCount) {
                        System.out.print(", ");
                    }
                }
                System.out.println();

                // Print the rows
                while (rs.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        System.out.print(rs.getString(i));
                        if (i < columnCount) {
                            System.out.print(", ");
                        }
                    }
                    System.out.println();
                }

                rs.close();
                stmt.close();
            } catch (SQLException e) {
                System.err.println("Failed to query the view: " + e.getMessage());
            }
        }

        // Close the scanner
        scanner.close();

        // Close the database connection
        try {
            conn.close();
        } catch (SQLException e) {
            System.err.println("Failed to close the database connection: " + e.getMessage());
        }
    }
}