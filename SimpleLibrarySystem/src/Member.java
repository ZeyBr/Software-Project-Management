    // src/Member.java
    public class Member {
        private int id;
        private String firstName;
        private String lastName;
        private String memberCode; // UNIQUE
        private String phoneNumber;
        private String email;

        // Veritabanından okuma/güncelleme için Constructor
        public Member(int id, String firstName, String lastName, String memberCode, String phoneNumber, String email) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.memberCode = memberCode;
            this.phoneNumber = phoneNumber;
            this.email = email;
        }

        // Yeni üye eklemek için Constructor (ID otomatik)
        public Member(String firstName, String lastName, String memberCode, String phoneNumber, String email) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.memberCode = memberCode;
            this.phoneNumber = phoneNumber;
            this.email = email;
        }

        // --- GETTERS VE SETTERS ---
        public int getId() { return id; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getMemberCode() { return memberCode; }
        public String getPhoneNumber() { return phoneNumber; }
        public String getEmail() { return email; }

        public void setFirstName(String firstName) { this.firstName = firstName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public void setEmail(String email) { this.email = email; }
    }

