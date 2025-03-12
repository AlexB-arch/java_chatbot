import java.sql.*;
import java.util.*;

public class DatabaseService {
    private DBConnection dbConnection;

    public DatabaseService() {
        this.dbConnection = new DBConnection();
        this.dbConnection.connect();
    }
}
