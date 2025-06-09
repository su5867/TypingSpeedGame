import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/typing_game";
    private static final String USER = "root";
    private static final String PASS = "supriya@2058";
    
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }

    public static void saveScore(String username, int wpm, double accuracy, Difficulty difficulty) {
        String sql = "INSERT INTO user_scores (username, wpm, accuracy, difficulty, date) VALUES (?, ?, ?, ?, NOW())";

        try (Connection conn = getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setInt(2, wpm);
            stmt.setDouble(3, accuracy);
            stmt.setString(4, difficulty.name());
            stmt.executeUpdate();
            System.out.println("✅ Score saved successfully for user: " + username);
        } catch (SQLException e) {
            System.err.println("❌ Error saving score: " + e.getMessage());
        }
    }


    public static List<ScoreRecord> getHighScores(Difficulty difficulty, int limit) {
        List<ScoreRecord> scores = new ArrayList<>();
        String sql = "SELECT username, wpm, accuracy, date FROM user_scores WHERE difficulty = ? ORDER BY wpm DESC LIMIT ?";


        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, difficulty.name());
            stmt.setInt(2, limit);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                scores.add(new ScoreRecord(
                    rs.getString("username"),
                    rs.getInt("wpm"),
                    rs.getDouble("accuracy"),
                    rs.getTimestamp("date").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error loading high scores: " + e.getMessage());
        }
        return scores;
    }

    public static class ScoreRecord {
        public final String username;
        public final int wpm;
        public final double accuracy;
        public final LocalDateTime date;

        public ScoreRecord(String username, int wpm, double accuracy, LocalDateTime date) {
            this.username = username;
            this.wpm = wpm;
            this.accuracy = accuracy;
            this.date = date;
        }
    }
}