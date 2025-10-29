package Dao;

import Model.Member;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MemberDao {

    private Connection connection;

    public MemberDao() {

        connection = KoneksiDatabase.getConnection();

    }

    public boolean insertMember(Member member) {

        String sql = "INSERT INTO member (nama, alamat, jenis_kelamin, tlp) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, member.getNama());
            stmt.setString(2, member.getAlamat());
            stmt.setString(3, member.getJenisKelamin());
            stmt.setString(4, member.getTlp());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {

                    if (generatedKeys.next()) {

                        member.setId(generatedKeys.getInt(1));

                    }

                }

                return true;

            }

        } catch (SQLException e) {

            System.err.println("Error insert member: " + e.getMessage());

        }

        return false;

    }

    public List<Member> getAllMembers() {

        List<Member> members = new ArrayList<>();
        String sql = "SELECT m.*, COUNT(t.id) as total_transaksi FROM member m " +
                "LEFT JOIN transaksi t ON m.id = t.id_member " +
                "GROUP BY m.id";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                Member member = resultSetToMember(rs);
                members.add(member);

            }

        } catch (SQLException e) {

            System.err.println("Error get all members: " + e.getMessage());

        }

        return members;

    }

    public Member getMemberById(int id) {

        String sql = "SELECT m.*, COUNT(t.id) as total_transaksi FROM member m " +
                "LEFT JOIN transaksi t ON m.id = t.id_member " +
                "WHERE m.id = ? " +
                "GROUP BY m.id";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    return resultSetToMember(rs);

                }

            }

        } catch (SQLException e) {

            System.err.println("Error get member by id: " + e.getMessage());

        }

        return null;

    }

    public boolean updateMember(Member member) {

        String sql = "UPDATE member SET nama = ?, alamat = ?, jenis_kelamin = ?, tlp = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, member.getNama());
            stmt.setString(2, member.getAlamat());
            stmt.setString(3, member.getJenisKelamin());
            stmt.setString(4, member.getTlp());
            stmt.setInt(5, member.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println("Error update member: " + e.getMessage());

        }

        return false;

    }

    public boolean deleteMember(int id) {

        String checkSql = "SELECT COUNT(*) as count FROM transaksi WHERE id_member = ?";
        String deleteSql = "DELETE FROM member WHERE id = ?";

        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {

            checkStmt.setInt(1, id);

            try (ResultSet rs = checkStmt.executeQuery()) {

                if (rs.next() && rs.getInt("count") > 0) {

                    System.err.println("Tidak dapat menghapus member yang memiliki transaksi");
                    return false;

                }

            }

            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {

                deleteStmt.setInt(1, id);
                return deleteStmt.executeUpdate() > 0;

            }

        } catch (SQLException e) {

            System.err.println("Error delete member: " + e.getMessage());

        }

        return false;

    }

    public List<Member> searchMembers(String keyword) {

        List<Member> members = new ArrayList<>();
        String sql = "SELECT m.*, COUNT(t.id) as total_transaksi FROM member m " +
                "LEFT JOIN transaksi t ON m.id = t.id_member " +
                "WHERE m.nama LIKE ? OR m.tlp LIKE ? OR m.alamat LIKE ? " +
                "GROUP BY m.id";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    Member member = resultSetToMember(rs);
                    members.add(member);

                }

            }

        } catch (SQLException e) {

            System.err.println("Error search members: " + e.getMessage());

        }

        return members;

    }

    public int getTotalMembers() {

        String sql = "SELECT COUNT(*) as total FROM member";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {

                return rs.getInt("total");

            }

        } catch (SQLException e) {

            System.err.println("Error get total members: " + e.getMessage());

        }

        return 0;

    }

    private Member resultSetToMember(ResultSet rs) throws SQLException {

        Member member = new Member();
        member.setId(rs.getInt("id"));
        member.setNama(rs.getString("nama"));
        member.setAlamat(rs.getString("alamat"));
        member.setJenisKelamin(rs.getString("jenis_kelamin"));
        member.setTlp(rs.getString("tlp"));
        member.setTotalTransaksi(rs.getInt("total_transaksi"));

        return member;

    }

}