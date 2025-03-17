import java.util.List;
import java.util.Map;

import org.junit.*;

public class DatabaseServiceTests {
    private DatabaseService databaseService;

    @Before
    public void setUp() {
        databaseService = new DatabaseService();
    }

    @Test
    public void testExecuteQuery() {
        List<Map<String, Object>> results = databaseService.executeQuery("SELECT * FROM teachers");
        results.forEach(System.out::println);
        Assert.assertTrue(results.size() > 0);
    }

    @Test
    public void searchForCollege() {
        List<Map<String, Object>> results = databaseService.executeQuery("SELECT * FROM college");
        results.forEach(System.out::println);
        Assert.assertTrue(results.size() > 0);
    }

    @Test
    public void outputCollegeNameColumn() {
        List<Map<String, Object>> results = databaseService.executeQuery("SELECT name FROM college");
        results.forEach(System.out::println);
        Assert.assertTrue(results.size() > 0);

        // Now display the college names without the column name
        results.forEach(row -> System.out.println(row.get("name")));
    }

    @Test
    public void searchForAccountingMajor() {
        List<Map<String, Object>> results = databaseService.executeQuery("SELECT * FROM major WHERE title = 'Accounting'");
        results.forEach(System.out::println);
        Assert.assertTrue(results.size() == 1);
    }

    @Test
    public void searchForAccountingMajorUsingPreparedStatement() {
        List<Map<String, Object>> results = databaseService.executeQuery("SELECT * FROM major WHERE title = ?", "Accounting");
        results.forEach(System.out::println);
        Assert.assertTrue(results.size() == 1);
    }

    @Test
    public void searchByKeywords() {
        List<Map<String, Object>> results = databaseService.executeQuery("SELECT * FROM major WHERE title LIKE ?", "%Accounting%");
        results.forEach(System.out::println);
        Assert.assertTrue(results.size() == 1);
    }

    @Test
    public void dynamicSelectGrades() {
        List<Map<String, Object>> results = databaseService.executeQuery("SELECT id, CASE WHEN id = ? THEN ? ELSE 'Unknown' END AS RequiredGrade FROM course WHERE id = ?;", "ACCT210", "C", "ACCT210");
        results.forEach(System.out::println);
        Assert.assertTrue(results.size() == 1);
    }
}
