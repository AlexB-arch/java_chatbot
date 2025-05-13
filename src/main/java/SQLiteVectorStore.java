import java.sql.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class SQLiteVectorStore {
    private final String dbUrl;
    private final String vecExtensionPath;

    public SQLiteVectorStore(String dbUrl, String vecExtensionPath) {
        this.dbUrl = dbUrl;
        this.vecExtensionPath = vecExtensionPath;
    }

    public Connection connect() throws SQLException {
        Connection conn = DriverManager.getConnection(dbUrl);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("SELECT load_extension('" + vecExtensionPath + "')");
        } catch (SQLException e) {
            throw new SQLException("Failed to load sqlite-vec extension: " + e.getMessage(), e);
        }
        return conn;
    }

    public void createTableAndIndex(int embeddingDim) throws SQLException {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS rag_docs (id INTEGER PRIMARY KEY AUTOINCREMENT, content TEXT, embedding BLOB)");
            stmt.execute("SELECT vec_index('rag_docs', 'embedding', 'dim=" + embeddingDim + "')");
        }
    }

    public void insertDocument(String content, float[] embedding) throws SQLException {
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO rag_docs (content, embedding) VALUES (?, ?)")) {
            ps.setString(1, content);
            ps.setBytes(2, floatArrayToBlob(embedding));
            ps.executeUpdate();
        }
    }

    public List<String> searchSimilar(float[] embedding, int topK) throws SQLException {
        List<String> results = new ArrayList<>();
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(
                // Use L2 distance for similarity (or switch to vec_cos for cosine)
                "SELECT content FROM rag_docs ORDER BY vec_l2(embedding, ?) ASC LIMIT ?")) {
            ps.setBytes(1, floatArrayToBlob(embedding));
            ps.setInt(2, topK);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(rs.getString("content"));
                }
            }
        }
        return results;
    }

    private byte[] floatArrayToBlob(float[] arr) {
        ByteBuffer bb = ByteBuffer.allocate(arr.length * 4);
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(arr);
        return bb.array();
    }

    // (Optional) Convert BLOB back to float[] if needed
    public static float[] blobToFloatArray(byte[] blob) {
        FloatBuffer fb = ByteBuffer.wrap(blob).asFloatBuffer();
        float[] arr = new float[fb.capacity()];
        fb.get(arr);
        return arr;
    }
}
