// src/BookDAO.java

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    // CREATE: Yeni Kitap Ekleme
    public void addBook(Book book) throws SQLException {
        // is_available sütunu, veritabanında DEFAULT 1 olduğu için sorguya dahil edilmedi.
        String sql = "INSERT INTO Books (title, author, isbn, publication_year) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DatabaseManager.connect();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, book.getTitle());
                pstmt.setString(2, book.getAuthor());
                pstmt.setString(3, book.getIsbn());
                pstmt.setInt(4, book.getPublicationYear());
                pstmt.executeUpdate();
            }
        } finally {
            DatabaseManager.close(conn);
        }
    }

    // READ: Tüm Kitapları Listeleme
    public List<Book> getAllBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM Books";
        Connection conn = null;
        try {
            conn = DatabaseManager.connect();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    Book book = new Book(
                            rs.getInt("book_id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("isbn"),
                            rs.getInt("publication_year"),
                            rs.getBoolean("is_available")
                    );
                    books.add(book);
                }
            }
        } finally {
            DatabaseManager.close(conn);
        }
        return books;
    }

    // READ: ID ile Kitap Bulma
    public Book getBookById(int id) throws SQLException {
        String sql = "SELECT * FROM Books WHERE book_id = ?";
        Connection conn = null;
        try {
            conn = DatabaseManager.connect();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return new Book(
                                rs.getInt("book_id"),
                                rs.getString("title"),
                                rs.getString("author"),
                                rs.getString("isbn"),
                                rs.getInt("publication_year"),
                                rs.getBoolean("is_available")
                        );
                    }
                }
            }
        } finally {
            DatabaseManager.close(conn);
        }
        return null;
    }

    // UPDATE: Kitap Bilgilerini Güncelleme
    public boolean updateBook(Book book) throws SQLException {
        // is_available sütunu, Book/Member CRUD'un parçası olarak dahil edildi.
        String sql = "UPDATE Books SET title = ?, author = ?, publication_year = ?, is_available = ? WHERE book_id = ?";
        Connection conn = null;
        try {
            conn = DatabaseManager.connect();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, book.getTitle());
                pstmt.setString(2, book.getAuthor());
                pstmt.setInt(3, book.getPublicationYear());
                pstmt.setBoolean(4, book.isAvailable());
                pstmt.setInt(5, book.getId());
                return pstmt.executeUpdate() > 0;
            }
        } finally {
            DatabaseManager.close(conn);
        }
    }

    // DELETE: Kitabı Silme
    public boolean deleteBook(int id) throws SQLException {
        String sql = "DELETE FROM Books WHERE book_id = ?";
        Connection conn = null;
        try {
            conn = DatabaseManager.connect();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                return pstmt.executeUpdate() > 0;
            }
        } finally {
            DatabaseManager.close(conn);
        }
    }
}