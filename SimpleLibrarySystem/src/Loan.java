// src/Loan.java

public class Loan {
    private int id;
    private int bookId;
    private int memberId;
    private String loanDate; // YYYY-MM-DD
    private String dueDate;  // YYYY-MM-DD
    private String returnDate; // YYYY-MM-DD (iade edilmediyse NULL)

    // --- 1. Veritabanından Okuma (READ) Constructor'ı ---
    // Tüm alanları alır (ID dahil)
    public Loan(int id, int bookId, int memberId, String loanDate, String dueDate, String returnDate) {
        this.id = id;
        this.bookId = bookId;
        this.memberId = memberId;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
    }

    // --- 2. Yeni Kayıt (CREATE) Constructor'ı ---
    // ID ve returnDate hariç, yeni ödünç kaydı oluşturmak için.
    public Loan(int bookId, int memberId, String loanDate, String dueDate) {
        this.bookId = bookId;
        this.memberId = memberId;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.returnDate = null; // Başlangıçta iade edilmedi
    }

    // --- GETTERS (DAO'lar tarafından okuma için gereklidir) ---
    public int getId() { return id; }
    public int getBookId() { return bookId; }
    public int getMemberId() { return memberId; }
    public String getLoanDate() { return loanDate; }
    public String getDueDate() { return dueDate; }
    public String getReturnDate() { return returnDate; }

    // --- SETTERS (Güncelleme için gereklidir) ---
    // Bu metot, genellikle sadece raporlama veya iade tarihi manuel ayarı için kullanılır.
    public void setReturnDate(String returnDate) { this.returnDate = returnDate; }
    public void setId(int id) { this.id = id; } // Nadiren kullanılır.
}