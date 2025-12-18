// src/LibraryApp.java

import java.util.Scanner;
import java.sql.SQLException;

public class LibraryApp {

    // DAO sınıflarını burada tanımlıyoruz, böylece tüm menü metotları kullanabilir.
    private static BookDAO bookDAO = new BookDAO();
    private static MemberDAO memberDAO = new MemberDAO();
    private static LoanDAO loanDAO = new LoanDAO();
    private static Scanner scanner = new Scanner(System.in);


    public static void main(String[] args) {

        // 1. Veritabanı Başlatma (Milestone 1)
        DatabaseManager.initializeDatabase();

        System.out.println("=================================================");
        System.out.println("     BASİT KÜTÜPHANE YÖNETİM SİSTEMİ (CLI)     ");
        System.out.println("=================================================");

        runMainMenu();
    }

    /**
     * Ana Menü Döngüsü
     */
    private static void runMainMenu() {
        int choice;
        do {
            System.out.println("\n--- ANA MENÜ ---");
            System.out.println("1. Kitap İşlemleri");
            System.out.println("2. Üye İşlemleri");
            System.out.println("3. Ödünç/İade İşlemleri");
            System.out.println("0. Çıkış");
            System.out.print("Seçiminiz: ");

            try {
                choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1:
                        handleBookMenu();
                        break;
                    case 2:
                        handleMemberMenu();
                        break;
                    case 3:
                        handleLoanMenu();
                        break;
                    case 0:
                        System.out.println("Sistem kapatılıyor. Hoşça kalın.");
                        break;
                    default:
                        System.out.println("Geçersiz seçim. Lütfen 0-3 arasında bir değer girin.");
                }
            } catch (NumberFormatException e) {
                System.err.println("HATA: Lütfen sayısal bir değer giriniz.");
                choice = -1; // Döngüyü devam ettir
            } catch (Exception e) {
                System.err.println("Beklenmeyen bir hata oluştu: " + e.getMessage());
                choice = -1;
            }
        } while (choice != 0);

        scanner.close(); // Uygulama kapanırken Scanner'ı kapat
    }

    // --- ÜYE METOTLARI (LibraryApp.java içine ekleyin) ---

    private static void listAllMembers() {
        System.out.println("\n--- Üye Listesi ---");
        try {
            var members = memberDAO.getAllMembers();
            if (members.isEmpty()) {
                System.out.println("Sistemde kayıtlı üye bulunmamaktadır.");
            } else {
                System.out.printf("%-5s %-15s %-15s %-15s\n", "ID", "Ad", "Soyad", "Üye Kodu");
                System.out.println("-------------------------------------------------------");
                for (Member m : members) {
                    System.out.printf("%-5d %-15s %-15s %-15s\n",
                            m.getId(), m.getFirstName(), m.getLastName(), m.getMemberCode());
                }
            }
        } catch (SQLException e) {
            System.err.println("Listeleme hatası: " + e.getMessage());
        }
    }
// --- İADE METODU  ---

    private static void returnBook() {
        System.out.println("\n--- Kitap İade Al ---");
        try {
            System.out.print("İade Edilen Kitap ID: ");
            int bookId = Integer.parseInt(scanner.nextLine());

            System.out.print("İade Eden Üye ID: ");
            int memberId = Integer.parseInt(scanner.nextLine());

            // LoanDAO.returnBook metodu çağrılıyor. (Transaction yönetimi burada devreye girer)
            loanDAO.returnBook(bookId, memberId);

            // Başarı mesajı DAO içinde (System.out.println) verildiği için burası boş kalabilir
            // veya ek bir onay mesajı yazılabilir.

        } catch (NumberFormatException e) {
            System.err.println("HATA: ID'ler için geçersiz sayı formatı.");
        } catch (IllegalStateException e) {
            // DAO'dan fırlatılan mantıksal hata (Örn: Aktif ödünç kaydı yoksa)
            System.err.println(e.getMessage());
        } catch (SQLException e) {
            System.err.println("Veritabanı hatası: İşlem başarısız oldu. " + e.getMessage());
        }
    }
    // Üye silme metodu (Menüde case 4 için)
    private static void deleteMember() {
        System.out.println("\n--- Üye Sil ---");
        try {
            System.out.print("Silinecek Üye ID: ");
            int id = Integer.parseInt(scanner.nextLine());

            boolean isDeleted = memberDAO.deleteMember(id);
            if (isDeleted) {
                System.out.println("✅ Üye başarıyla silindi.");
            } else {
                System.out.println("❌ HATA: Bu ID'ye sahip bir üye bulunamadı.");
            }
        } catch (NumberFormatException e) {
            System.err.println("HATA: ID sayısal olmalıdır.");
        } catch (SQLException e) {
            System.err.println("Silme işlemi başarısız: " + e.getMessage());
        }
    }
    // --- KİTAP METOTLARI (LibraryApp.java içine ekleyin) ---

    private static void listAllBooks() {
        System.out.println("\n--- Kitap Listesi ---");
        try {
            var books = bookDAO.getAllBooks();
            if (books.isEmpty()) {
                System.out.println("Sistemde kayıtlı kitap bulunmamaktadır.");
            } else {
                // Tablo başlığı
                System.out.printf("%-5s %-30s %-20s %-15s %-10s\n", "ID", "Başlık", "Yazar", "ISBN", "Durum");
                System.out.println("-------------------------------------------------------------------------------------");
                for (Book b : books) {
                    String status = b.isAvailable() ? "Müsait" : "Ödünçte";
                    // Verileri formatlı yazdır
                    System.out.printf("%-5d %-30s %-20s %-15s %-10s\n",
                            b.getId(),
                            limitString(b.getTitle(), 28),
                            limitString(b.getAuthor(), 18),
                            b.getIsbn(),
                            status);
                }
            }
        } catch (SQLException e) {
            System.err.println("Listeleme hatası: " + e.getMessage());
        }
    }
    private static void updateBook() {
        System.out.println("\n--- Kitap Güncelle ---");
        try {
            System.out.print("Güncellenecek Kitap ID: ");
            int id = Integer.parseInt(scanner.nextLine());

            Book existing = bookDAO.getBookById(id);
            if (existing == null) {
                System.out.println("❌ HATA: Kitap bulunamadı.");
                return;
            }

            System.out.println("Bulunan: " + existing.getTitle());
            System.out.print("Yeni Başlık: "); String title = scanner.nextLine();
            System.out.print("Yeni Yazar: "); String author = scanner.nextLine();
            System.out.print("Yeni Yıl: "); int year = Integer.parseInt(scanner.nextLine());

            // Mevcut ISBN ve Durumu koruyarak güncelle
            Book updated = new Book(id, title, author, existing.getIsbn(), year, existing.isAvailable());

            if (bookDAO.updateBook(updated)) {
                System.out.println("✅ Kitap güncellendi.");
            } else {
                System.out.println("❌ Güncelleme başarısız.");
            }
        } catch (Exception e) {
            System.err.println("Hata: " + e.getMessage());
        }
    }
    private static void deleteBook() {
        System.out.println("\n--- Kitap Sil ---");
        try {
            System.out.print("Silinecek Kitap ID: ");
            int id = Integer.parseInt(scanner.nextLine());

            boolean isDeleted = bookDAO.deleteBook(id);
            if (isDeleted) {
                System.out.println("✅ Kitap başarıyla silindi.");
            } else {
                System.out.println("❌ HATA: Bu ID'ye sahip bir kitap bulunamadı.");
            }
        } catch (NumberFormatException e) {
            System.err.println("HATA: ID sayısal olmalıdır.");
        } catch (SQLException e) {
            System.err.println("Silme işlemi başarısız: " + e.getMessage());
        }
    }

    // Tablo düzeni için yardımcı metot (Metin çok uzunsa keser)
    private static String limitString(String str, int limit) {
        if (str.length() > limit) {
            return str.substring(0, limit - 3) + "...";
        }
        return str;
    }
    // src/LibraryApp.java (loanBook metodu)

    private static void loanBook() {
        System.out.println("\n--- Kitap Ödünç Ver ---");
        try {
            System.out.print("Kitap ID: ");
            int bookId = Integer.parseInt(scanner.nextLine());

            System.out.print("Üye ID: ");
            int memberId = Integer.parseInt(scanner.nextLine());

            // LoanDAO.loanBook metodu çağrılıyor. (Transaction yönetimi burada devreye girer)
            loanDAO.loanBook(bookId, memberId);

            // Başarı mesajı DAO içinde yazdırıldığı için burada ek mesaj gerekmeyebilir.

        } catch (NumberFormatException e) {
            System.err.println("HATA: ID'ler için geçersiz sayı formatı.");
        } catch (IllegalStateException e) {
            // DAO'dan fırlatılan mantıksal hata (Kitabın müsait olmaması gibi)
            System.err.println(e.getMessage());
        } catch (SQLException e) {
            System.err.println("Veritabanı hatası: İşlem başarısız oldu. Lütfen ID'lerin doğru olduğundan emin olun. " + e.getMessage());
        }
    }
// src/LibraryApp.java (handleLoanMenu metodu)

    private static void handleLoanMenu() {
        int choice;
        do {
            System.out.println("\n--- ÖDÜNÇ/İADE İŞLEMLERİ ---");
            System.out.println("1. Kitap Ödünç Ver");
            System.out.println("2. Kitap İade Al");
            System.out.println("3. Aktif Ödünç Kayıtlarını Listele");
            System.out.println("0. Ana Menüye Dön");
            System.out.print("Seçiminiz: ");

            try {
                choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1:
                        loanBook();
                        break;
                    case 2:
                        returnBook();
                        break;
                    case 3:
                         loanDAO.listActiveLoans();//
                        break;
                    case 0:
                        System.out.println("Ana Menüye dönülüyor...");
                        break;
                    default:
                        System.out.println("Geçersiz seçim.");
                }
            } catch (NumberFormatException e) {
                System.err.println("HATA: Lütfen sayısal bir değer giriniz.");
                choice = -1;
            } catch (Exception e) {
                System.err.println("Beklenmeyen bir hata oluştu: " + e.getMessage());
                choice = -1;
            }
        } while (choice != 0);
    }

    // src/LibraryApp.java içine

    private static void updateMember() {
        System.out.println("\n--- Üye Güncelle ---");
        try {
            System.out.print("Güncellenecek Üye ID: ");
            int id = Integer.parseInt(scanner.nextLine());

            // Önce üyenin var olup olmadığını kontrol edelim
            Member existingMember = memberDAO.getMemberById(id);
            if (existingMember == null) {
                System.out.println("❌ HATA: Bu ID'ye sahip bir üye bulunamadı.");
                return;
            }

            System.out.println("Bulunan Üye: " + existingMember.getFirstName() + " " + existingMember.getLastName());
            System.out.println("Lütfen yeni bilgileri giriniz:");

            System.out.print("Yeni Ad: ");
            String firstName = scanner.nextLine();

            System.out.print("Yeni Soyad: ");
            String lastName = scanner.nextLine();

            // Üye Kodu (Member Code) genellikle değiştirilmez, mevcut olanı koruyoruz.
            String memberCode = existingMember.getMemberCode();

            System.out.print("Yeni Telefon: ");
            String phone = scanner.nextLine();

            System.out.print("Yeni E-posta: ");
            String email = scanner.nextLine();

            // Güncelleme için yeni nesne oluştur (ID'yi vermeyi unutma!)
            Member updatedMember = new Member(id, firstName, lastName, memberCode, phone, email);

            boolean isUpdated = memberDAO.updateMember(updatedMember);

            if (isUpdated) {
                System.out.println("✅ Üye başarıyla güncellendi.");
            } else {
                System.out.println("❌ Güncelleme başarısız oldu.");
            }

        } catch (NumberFormatException e) {
            System.err.println("HATA: ID sayısal olmalıdır.");
        } catch (SQLException e) {
            System.err.println("Veritabanı hatası: " + e.getMessage());
        }
    }

    // src/LibraryApp.java (addMember metodu)

    private static void addMember() {
        System.out.println("\n--- Yeni Üye Ekle ---");
        try {
            System.out.print("Adı: ");
            String firstName = scanner.nextLine();

            System.out.print("Soyadı: ");
            String lastName = scanner.nextLine();

            System.out.print("Üye Kodu (Benzersiz): ");
            String memberCode = scanner.nextLine();

            System.out.print("Telefon Numarası: ");
            String phone = scanner.nextLine();

            System.out.print("E-posta: ");
            String email = scanner.nextLine();

            Member newMember = new Member(firstName, lastName, memberCode, phone, email);
            memberDAO.addMember(newMember);

            System.out.println("✅ Üye başarıyla eklendi.");

        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed: Members.member_code")) {
                System.err.println("HATA: Girdiğiniz Üye Kodu zaten sistemde mevcut.");
            } else {
                System.err.println("Veritabanı hatası: Üye eklenemedi. " + e.getMessage());
            }
        }
    }
    // src/LibraryApp.java (handleMemberMenu metodu)

    private static void handleMemberMenu() {
        int choice;
        do {
            System.out.println("\n--- ÜYE İŞLEMLERİ ---");
            System.out.println("1. Yeni Üye Ekle");
            System.out.println("2. Tüm Üyeleri Listele");
            System.out.println("3. Üye Güncelle");
            System.out.println("4. Üye Sil");
            System.out.println("0. Ana Menüye Dön");
            System.out.print("Seçiminiz: ");

            try {
                choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1:
                        addMember();
                        break;
                    case 2:
                        listAllMembers();
                        break;
                    case 3:
                        updateMember(); // Detaylı metotlar sonra eklenecek
                        break;
                    case 4:
                        deleteMember(); // Detaylı metotlar sonra eklenecek
                        break;
                    case 0:
                        System.out.println("Ana Menüye dönülüyor...");
                        break;
                    default:
                        System.out.println("Geçersiz seçim.");
                }
            } catch (NumberFormatException e) {
                System.err.println("HATA: Lütfen sayısal bir değer giriniz.");
                choice = -1;
            } catch (Exception e) {
                System.err.println("Bir hata oluştu: " + e.getMessage());
                choice = -1;
            }
        } while (choice != 0);
    }
    // src/LibraryApp.java (addBook metodu)

    private static void addBook() {
        System.out.println("\n--- Yeni Kitap Ekle ---");
        try {
            System.out.print("Başlık: ");
            String title = scanner.nextLine();

            System.out.print("Yazar: ");
            String author = scanner.nextLine();

            System.out.print("ISBN (Benzersiz): ");
            String isbn = scanner.nextLine();

            System.out.print("Yayın Yılı: ");
            int year = Integer.parseInt(scanner.nextLine());

            // Yeni Book nesnesini oluştur ve DAO metoduyla kaydet
            Book newBook = new Book(title, author, isbn, year);
            bookDAO.addBook(newBook);

            System.out.println("✅ Kitap başarıyla eklendi.");

        } catch (NumberFormatException e) {
            System.err.println("HATA: Yayın yılı için geçersiz sayı formatı.");
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed: Books.isbn")) {
                System.err.println("HATA: Girdiğiniz ISBN zaten sistemde mevcut. Lütfen kontrol edin.");
            } else {
                System.err.println("Veritabanı hatası: Kitap eklenemedi. " + e.getMessage());
            }
        }
    }

    // src/LibraryApp.java (handleBookMenu metodu)
    private static void handleBookMenu() {
        int choice;
        do {
            System.out.println("\n--- KİTAP İŞLEMLERİ ---");
            System.out.println("1. Kitap Ekle");
            System.out.println("2. Tüm Kitapları Listele");
            System.out.println("3. Kitap Güncelle");
            System.out.println("4. Kitap Sil");
            System.out.println("0. Ana Menüye Dön");
            System.out.print("Seçiminiz: ");

            try {
                choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1:
                        // Kitap Ekleme (CREATE)
                        addBook();
                        break;
                    case 2:
                        // Kitap Listeleme (READ)
                        listAllBooks();
                        break;
                    case 3:
                        // Kitap Güncelleme (UPDATE)
                         updateBook(); // Bu metot bir sonraki adımda kodlanacak
                        break;
                    case 4:
                        // Kitap Silme (DELETE)
                         deleteBook(); // Bu metot bir sonraki adımda kodlanacak
                        break;
                    case 0:
                        System.out.println("Ana Menüye dönülüyor...");
                        break;
                    default:
                        System.out.println("Geçersiz seçim.");
                }
            } catch (NumberFormatException e) {
                System.err.println("HATA: Lütfen sayısal bir değer giriniz.");
                choice = -1;
            } catch (Exception e) {
                System.err.println("Bir hata oluştu: " + e.getMessage());
                choice = -1;
            }
        } while (choice != 0);
    }
    // --- Kitap, Üye ve Ödünç/İade Menüleri Buraya Gelecektir ---

    // Bu metotları bir sonraki adımda detaylıca kodlayalım.
}