import org.junit.*;

public class DBConnectionTests {
    
    DBConnection db = new DBConnection();

    // Test DBConnection connection
    @Test
    public void testConnection() {
        db.connect();
        Assert.assertTrue(db.isConnected());
    }

    // Test DBConnection disconnection
    @Test
    public void testDisconnection() {
        db.connect();
        db.closeConnection();
        Assert.assertFalse(db.isConnected());
    }
}
