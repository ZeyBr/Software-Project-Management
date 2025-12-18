// src/MemberDAO.java
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MemberDAO {

    // CREATE: Yeni Üye Ekleme
    public void addMember(Member member) throws SQLException {
        String sql = "INSERT INTO Members (first_name, last_name, member_code, phone_number, email) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DatabaseManager.connect();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, member.getFirstName());
                pstmt.setString(2, member.getLastName());
                pstmt.setString(3, member.getMemberCode());
                pstmt.setString(4, member.getPhoneNumber());
                pstmt.setString(5, member.getEmail());
                pstmt.executeUpdate();
            }
        } finally {
            DatabaseManager.close(conn);
        }
    }

    // READ: Tüm Üyeleri Listeleme
    public List<Member> getAllMembers() throws SQLException {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM Members";
        Connection conn = null;
        try {
            conn = DatabaseManager.connect();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    Member member = new Member(
                            rs.getInt("member_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("member_code"),
                            rs.getString("phone_number"),
                            rs.getString("email")
                    );
                    members.add(member);
                }
            }
        } finally {
            DatabaseManager.close(conn);
        }
        return members;
    }

    // READ: ID ile Üye Bulma
    public Member getMemberById(int id) throws SQLException {
        String sql = "SELECT * FROM Members WHERE member_id = ?";
        Connection conn = null;
        try {
            conn = DatabaseManager.connect();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return new Member(
                                rs.getInt("member_id"),
                                rs.getString("first_name"),
                                rs.getString("last_name"),
                                rs.getString("member_code"),
                                rs.getString("phone_number"),
                                rs.getString("email")
                        );
                    }
                }
            }
        } finally {
            DatabaseManager.close(conn);
        }
        return null;
    }

    // UPDATE: Üye Bilgilerini Güncelleme
    public boolean updateMember(Member member) throws SQLException {
        String sql = "UPDATE Members SET first_name = ?, last_name = ?, phone_number = ?, email = ? WHERE member_id = ?";
        Connection conn = null;
        try {
            conn = DatabaseManager.connect();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, member.getFirstName());
                pstmt.setString(2, member.getLastName());
                pstmt.setString(3, member.getPhoneNumber());
                pstmt.setString(4, member.getEmail());
                pstmt.setInt(5, member.getId());
                return pstmt.executeUpdate() > 0;
            }
        } finally {
            DatabaseManager.close(conn);
        }
    }

    // DELETE: Üyeyi Silme
    public boolean deleteMember(int id) throws SQLException {
        String sql = "DELETE FROM Members WHERE member_id = ?";
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