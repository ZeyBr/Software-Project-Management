// src/LoanDAO.java (loanBook metodu)
import java.sql.*;
import java.time.LocalDate;

public class LoanDAO {
    // NOT: Bu kodun çalışması için MemberDAO ve BookDAO sınıflarının da olması gerekir.
// src/LoanDAO.java içine eklenecek

    // Aktif (İade Edilmemiş) Ödünçleri Listele
    // Not: Bu metot String döndürür, böylece ekrana kolayca basılabilir.
    public void listActiveLoans() throws SQLException {
        // Kitap ve Üye tablolarıyla birleştirerek (JOIN) detaylı bilgi alıyoruz
        String sql = "SELECT l.loan_id, b.title, m.first_name, m.last_name, l.loan_date " +
                "FROM Loans l " +
                "JOIN Books b ON l.book_id = b.book_id " +
                "JOIN Members m ON l.member_id = m.member_id " +
                "WHERE l.return_date IS NULL";

        Connection conn = null;
        try {
            conn = DatabaseManager.connect();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                System.out.println("\n--- Aktif Ödünç Listesi ---");
                System.out.printf("%-5s %-30s %-20s %-15s\n", "ID", "Kitap Başlığı", "Üye Adı", "Ödünç Tarihi");
                System.out.println("---------------------------------------------------------------------------");

                boolean hasRecords = false;
                while (rs.next()) {
                    hasRecords = true;
                    int id = rs.getInt("loan_id");
                    String title = rs.getString("title");
                    String memberName = rs.getString("first_name") + " " + rs.getString("last_name");
                    String date = rs.getString("loan_date");

                    // Tablo formatında yazdır (Başlık çok uzunsa kes)
                    if (title.length() > 28) title = title.substring(0, 25) + "...";

                    System.out.printf("%-5d %-30s %-20s %-15s\n", id, title, memberName, date);
                }

                if (!hasRecords) {
                    System.out.println("Şu anda ödünçte olan bir kitap bulunmamaktadır.");
                }
            }
        } finally {
            DatabaseManager.close(conn);
        }
    }
    /**
     * Kitabın müsait olup olmadığını (is_available = 1) kontrol eden yardımcı metot.
     * Bu metot, ana işleme dahil olmak üzere, aynı Connection (conn) üzerinde çalışmalıdır.
     */
    private boolean isBookAvailable(Connection conn, int bookId) throws SQLException {
        String sql = "SELECT is_available FROM Books WHERE book_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // is_available sütunu 1 (True) ise müsait demektir.
                    return rs.getBoolean("is_available");
                }
            }
        }
        return false; // Kitap ID'si yoksa veya müsait değilse
    }

    /**
     * Bir kitabi bir üyeye ödünç verir. JDBC Transaction yönetimi kullanılarak
     * veri bütünlüğü sağlanır.
     * @param bookId Ödünç verilecek kitabın ID'si.
     * @param memberId Ödünç alan üyenin ID'si.
     * @throws SQLException İşlem sırasında veritabanı hatası oluşursa.
     * @throws IllegalStateException Kitap müsait değilse.
     */
    public void loanBook(int bookId, int memberId) throws SQLException, IllegalStateException {
        Connection conn = null;
        try {
            conn = DatabaseManager.connect();
            // JDBC Transaction'ı başlatıyoruz: Otomatik commit'i kapat
            conn.setAutoCommit(false);

            // 1. Ürün Başarı Kriteri Kontrolü: Kitabın Müsaitliğini Kontrol Et
            if (!isBookAvailable(conn, bookId)) {
                // Mantıksal Hata Kontrolü: Bir kitabın aynı anda birden fazla kişiye ödünç verilmesini engeller.
                throw new IllegalStateException("HATA: Kitap ID " + bookId + " şu anda müsait değil.");
            }

            // 2. Loans Tablosuna Kayıt Ekle
            String loanSql = "INSERT INTO Loans (book_id, member_id, loan_date, due_date) VALUES (?, ?, ?, ?)";
            try (PreparedStatement loanStmt = conn.prepareStatement(loanSql)) {
                // Örnek tarih hesaplama
                String loanDate = LocalDate.now().toString();
                String dueDate = LocalDate.now().plusWeeks(2).toString(); // Örn: 2 hafta vade

                loanStmt.setInt(1, bookId);
                loanStmt.setInt(2, memberId);
                loanStmt.setString(3, loanDate);
                loanStmt.setString(4, dueDate);
                loanStmt.executeUpdate();
            }

            // 3. Books Tablosundaki Müsaitlik Durumunu Güncelle (0 = Ödünçte)
            String updateBookSql = "UPDATE Books SET is_available = 0 WHERE book_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateBookSql)) {
                updateStmt.setInt(1, bookId);
                int rowsAffected = updateStmt.executeUpdate();

                if (rowsAffected == 0) {
                    // Kitap durumu güncellenemezse, veri tutarsızlığını önlemek için rollback'i tetikler.
                    throw new SQLException("Kitap durumu güncellenemedi, İşlem geri alınıyor (Rollback).");
                }
            }

            // 4. İki işlem de başarılı: Commit et
            conn.commit();
            System.out.println("✅ Ödünç Verme Başarılı: Kitap ID " + bookId + " üyeye verildi.");

        } catch (Exception e) {
            // Hata oluşursa, tüm işlemleri geri al (Rollback)
            if (conn != null) {
                conn.rollback();
            }
            // Mantıksal hata veya SQL hatası, üst katmana fırlatılır.
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true); // Ayarı normale döndür
                DatabaseManager.close(conn);
            }
        }
    }

    public void returnBook(int bookId, int memberId) throws SQLException, IllegalStateException {
        Connection conn = null;
        try {
            conn = DatabaseManager.connect();
            conn.setAutoCommit(false); // 1. Transaction başlat

            // 1. Loans Tablosunda Aktif (İade edilmemiş) Kaydı Bul ve Güncelle
            // return_date IS NULL kontrolü, zaten iade edilmiş bir kaydı güncellememeyi sağlar.
            String loanUpdateSql = "UPDATE Loans SET return_date = ? WHERE book_id = ? AND member_id = ? AND return_date IS NULL";
            try (PreparedStatement loanStmt = conn.prepareStatement(loanUpdateSql)) {
                String returnDate = LocalDate.now().toString();

                loanStmt.setString(1, returnDate);
                loanStmt.setInt(2, bookId);
                loanStmt.setInt(3, memberId);

                int rowsAffected = loanStmt.executeUpdate();

                if (rowsAffected == 0) {
                    // Aktif ödünç kaydı bulunamazsa, işlemi geri al
                    throw new IllegalStateException("HATA: Bu kitap için aktif ödünç kaydı (veya ilgili üyede) bulunamadı.");
                }
            }

            // 2. Books Tablosundaki Müsaitlik Durumunu Güncelle (1 = Müsait)
            String bookUpdateSql = "UPDATE Books SET is_available = 1 WHERE book_id = ?";
            try (PreparedStatement bookStmt = conn.prepareStatement(bookUpdateSql)) {
                bookStmt.setInt(1, bookId);
                bookStmt.executeUpdate();
            }

            // 3. İki işlem de başarılı: Veritabanına kaydet
            conn.commit();
            System.out.println("✅ Başarılı: Kitap ID " + bookId + " başarıyla iade edildi.");

        } catch (Exception e) {
            // Hata oluşursa, tüm işlemleri geri al (Rollback)
            if (conn != null) {
                conn.rollback();
            }
            // Hata Kontrolü: Hata, üst katmana (UI) bildirilir.
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true); // Ayarı normale döndür
                DatabaseManager.close(conn);
            }
        }
    }
}