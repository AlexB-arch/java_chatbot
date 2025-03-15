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
}
