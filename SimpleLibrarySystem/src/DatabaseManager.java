// src/DatabaseManager.java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement; // Statement sınıfı için gerekli

public class DatabaseManager {
    // SQLite veritabanı dosyasının yolu
    private static final String DB_URL = "jdbc:sqlite:library_system.db";

    /**
     * Veritabanına yeni bir bağlantı açar.
     */
    public static Connection connect() throws SQLException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
            return conn;
        } catch (SQLException e) {
            System.err.println("Veritabanı bağlantı hatası: " + e.getMessage());
            // Bağlantı hatası durumunda, driver'ın projeye eklendiğinden emin olun.
            throw e;
        }
    }

    /**
     * Veritabanı bağlantısını güvenli bir şekilde kapatır.
     */
    public static void close(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("Bağlantı kapatma hatası: " + e.getMessage());
        }
    }

    /**
     * Veritabanı dosyasını ve Books, Members, Loans tablolarını oluşturur (yoksa).
     */
    public static void initializeDatabase() {

        // --- 1. Kitaplar Tablosu ---
        String sqlBooks =
                "CREATE TABLE IF NOT EXISTS Books (\n" +
                        "    book_id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                        "    title TEXT NOT NULL,\n" +
                        "    author TEXT NOT NULL,\n" +
                        "    isbn TEXT UNIQUE NOT NULL,\n" +
                        "    publication_year INTEGER,\n" +
                        "    is_available BOOLEAN NOT NULL DEFAULT 1\n" +
                        ");";

        // --- 2. Üyeler Tablosu ---
        String sqlMembers =
                "CREATE TABLE IF NOT EXISTS Members (\n" +
                        "    member_id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                        "    first_name TEXT NOT NULL,\n" +
                        "    last_name TEXT NOT NULL,\n" +
                        "    member_code TEXT UNIQUE NOT NULL,\n" +
                        "    phone_number TEXT,\n" +
                        "    email TEXT\n" +
                        ");";

        // --- 3. Ödünç/İade İşlemleri Tablosu ---
        String sqlLoans =
                "CREATE TABLE IF NOT EXISTS Loans (\n" +
                        "    loan_id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                        "    book_id INTEGER NOT NULL,\n" +
                        "    member_id INTEGER NOT NULL,\n" +
                        "    loan_date TEXT NOT NULL,\n" +
                        "    due_date TEXT NOT NULL,\n" +
                        "    return_date TEXT,\n" +
                        "    FOREIGN KEY (book_id) REFERENCES Books(book_id) ON DELETE CASCADE,\n" +
                        "    FOREIGN KEY (member_id) REFERENCES Members(member_id) ON DELETE CASCADE\n" +
                        ");";

        String[] sqlStatements = {sqlBooks, sqlMembers, sqlLoans};

        // try-with-resources ile Connection ve Statement otomatik kapanır
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            for (String sql : sqlStatements) {
                stmt.execute(sql);
            }
            System.out.println("✅ Veritabanı ve tablolar başarıyla oluşturuldu/kontrol edildi.");
        } catch (SQLException e) {
            System.err.println("❌ Veritabanı oluşturma/tablo hatası: " + e.getMessage());
        }
    }
}