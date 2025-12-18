

public class Book {
    private int id;
    private String title;
    private String author;
    private String isbn; // UNIQUE
    private int publicationYear;
    private boolean isAvailable; // is_available sütununu temsil eder

    // Veritabanından okuma/güncelleme için Constructor
    public Book(int id, String title, String author, String isbn, int publicationYear, boolean isAvailable) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publicationYear = publicationYear;
        this.isAvailable = isAvailable;
    }

    // Yeni kitap eklemek için Constructor (ID ve isAvailable otomatik)
    public Book(String title, String author, String isbn, int publicationYear) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publicationYear = publicationYear;
        this.isAvailable = true;
    }

    // --- GETTERS VE SETTERS ---
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public int getPublicationYear() { return publicationYear; }
    public boolean isAvailable() { return isAvailable; }

    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setPublicationYear(int publicationYear) { this.publicationYear = publicationYear; }
    public void setIsAvailable(boolean available) { isAvailable = available; }
    public void setId(int id) { this.id = id; }
    // ... toString() metodu eklenebilir
}