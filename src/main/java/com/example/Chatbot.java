package com.example;
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
        try {
            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            // Connect to the database
            Connection connection = DriverManager.getConnection("jdbc:sqlite:abcdb.db");

            Scanner scanner = new Scanner(System.in);
            String input = "";
            System.out.println("Welcome to the ACU Chatbot!");
            System.out.println("Type your question (in comments) to select a view,");
            System.out.println("or type 'list' to see available views, or 'quit' to exit.");

            while (true) {
                System.out.print("\nEnter your question or command: ");
                input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("quit")) {
                    System.out.println("Goodbye!");
                    break;
                } else if (input.equalsIgnoreCase("list")) {
                    System.out.println("Available views:");
                    for (String view : VIEWS) {
                        System.out.println(" - " + view);
                    }
                    continue;
                }

                // Map natural language questions to view names
                String viewName = "";
                String lowerInput = input.toLowerCase();
                if (lowerInput.equals("what is my major") || lowerInput.equals("what majors am i in")) {
                    viewName = "v_student_majors";
                } else if (lowerInput.contains("required grade") && lowerInput.contains("cs375")) {
                    viewName = "v_required_grade";
                } else if (lowerInput.contains("concentrations") && lowerInput.contains("computer science")) {
                    viewName = "v_major_concentrations";
                } else if (lowerInput.contains("hours") && lowerInput.contains("complete") && lowerInput.contains("major")) {
                    viewName = "v_hours_remaining_major";
                } else if (lowerInput.contains("classes") && lowerInput.contains("am in") && !lowerInput.contains("currently")) {
                    viewName = "v_classes_in";
                } else if (lowerInput.contains("hours") && lowerInput.contains("taking")) {
                    viewName = "v_total_hours_taking";
                } else if (lowerInput.contains("department") && lowerInput.contains("major")) {
                    viewName = "v_major_department";
                } else if (lowerInput.contains("professor") && lowerInput.contains("cs375")) {
                    viewName = "v_professor_cs375";
                } else if (lowerInput.contains("classes") && lowerInput.contains("need to graduate")) {
                    viewName = "v_classes_needed_graduate";
                } else if (lowerInput.contains("most") && lowerInput.contains("department")) {
                    viewName = "v_most_departments_class";
                } else if (lowerInput.contains("currently in")) {
                    viewName = "v_classes_current";
                } else if (lowerInput.contains("hours more") && lowerInput.contains("graduate")) {
                    viewName = "v_hours_remaining";
                } else if (lowerInput.contains("teachers") && lowerInput.contains("sitc") && lowerInput.contains("count")) {
                    viewName = "v_teachers_sitc_count";
                } else if (lowerInput.contains("teachers") && lowerInput.contains("sitc") && lowerInput.contains("name")) {
                    viewName = "v_teachers_sitc_names";
                } else {
                    // Fallback: if the input exactly matches one of our views
                    boolean valid = false;
                    for (String view : VIEWS) {
                        if (view.equalsIgnoreCase(input)) {
                            viewName = view;
                            valid = true;
                            break;
                        }
                    }
                    if (!valid) {
                        System.out.println("Invalid view name or question. Please type 'list' to see available views.");
                        continue;
                    }
                }

                // Execute the query for the mapped view
                String query = "SELECT * FROM " + viewName;
                System.out.println("\nExecuting query on view: " + viewName);
                try (Statement statement = connection.createStatement();
                     ResultSet rs = statement.executeQuery(query)) {

                    ResultSetMetaData rsmd = rs.getMetaData();
                    int columnCount = rsmd.getColumnCount();

                    // Print column headers
                    for (int i = 1; i <= columnCount; i++) {
                        System.out.print(rsmd.getColumnName(i));
                        if (i < columnCount) {
                            System.out.print(" | ");
                        }
                    }
                    System.out.println("\n" + "-".repeat(40));

                    // Print each row of the result set
                    while (rs.next()) {
                        for (int i = 1; i <= columnCount; i++) {
                            System.out.print(rs.getString(i));
                            if (i < columnCount) {
                                System.out.print(" | ");
                            }
                        }
                        System.out.println();
                    }
                } catch (SQLException e) {
                    System.out.println("Error executing query: " + e.getMessage());
                }
            }

            connection.close();
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}