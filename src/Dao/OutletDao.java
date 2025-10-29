package Dao;

import Model.Outlet;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OutletDao {

    private Connection connection;

    public OutletDao() {

        connection = KoneksiDatabase.getConnection();

    }

    public boolean insertOutlet(Outlet outlet) {

        String sql = "INSERT INTO outlet (nama, alamat, tlp) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, outlet.getNama());
            stmt.setString(2, outlet.getAlamat());
            stmt.setString(3, outlet.getTlp());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {

                    if (generatedKeys.next()) {

                        outlet.setId(generatedKeys.getInt(1));

                    }

                }

                return true;

            }

        } catch (SQLException e) {

            System.err.println("Error insert outlet: " + e.getMessage());

        }

        return false;

    }

    public List<Outlet> getAllOutlets() {

        List<Outlet> outlets = new ArrayList<>();
        String sql = "SELECT o.*, " +
                "COUNT(DISTINCT u.id) as total_users, " +
                "COUNT(DISTINCT t.id) as total_transaksi, " +
                "COUNT(DISTINCT p.id) as total_paket " +
                "FROM outlet o " +
                "LEFT JOIN users u ON o.id = u.id_outlet " +
                "LEFT JOIN transaksi t ON o.id = t.id_outlet " +
                "LEFT JOIN paket p ON o.id = p.id_outlet " +
                "GROUP BY o.id";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                Outlet outlet = resultSetToOutlet(rs);
                outlets.add(outlet);

            }

        } catch (SQLException e) {

            System.err.println("Error get all outlets: " + e.getMessage());

        }

        return outlets;

    }

    public Outlet getOutletById(int id) {

        String sql = "SELECT o.*, " +
                "COUNT(DISTINCT u.id) as total_users, " +
                "COUNT(DISTINCT t.id) as total_transaksi, " +
                "COUNT(DISTINCT p.id) as total_paket " +
                "FROM outlet o " +
                "LEFT JOIN users u ON o.id = u.id_outlet " +
                "LEFT JOIN transaksi t ON o.id = t.id_outlet " +
                "LEFT JOIN paket p ON o.id = p.id_outlet " +
                "WHERE o.id = ? " +
                "GROUP BY o.id";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    return resultSetToOutlet(rs);

                }

            }

        } catch (SQLException e) {

            System.err.println("Error get outlet by id: " + e.getMessage());

        }

        return null;

    }

    public boolean updateOutlet(Outlet outlet) {

        String sql = "UPDATE outlet SET nama = ?, alamat = ?, tlp = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, outlet.getNama());
            stmt.setString(2, outlet.getAlamat());
            stmt.setString(3, outlet.getTlp());
            stmt.setInt(4, outlet.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println("Error update outlet: " + e.getMessage());

        }

        return false;

    }

    public boolean deleteOutlet(int id) {

        String checkSql = "SELECT COUNT(*) as count FROM users WHERE id_outlet = ? " +
                "UNION ALL SELECT COUNT(*) FROM transaksi WHERE id_outlet = ?";
        String deleteSql = "DELETE FROM outlet WHERE id = ?";

        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {

            checkStmt.setInt(1, id);
            checkStmt.setInt(2, id);

            try (ResultSet rs = checkStmt.executeQuery()) {

                int total = 0;
                while (rs.next()) {

                    total += rs.getInt("count");

                }

                if (total > 0) {

                    System.err.println("Tidak dapat menghapus outlet yang memiliki users atau transaksi");
                    return false;

                }

            }

            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {

                deleteStmt.setInt(1, id);
                return deleteStmt.executeUpdate() > 0;

            }

        } catch (SQLException e) {

            System.err.println("Error delete outlet: " + e.getMessage());

        }

        return false;

    }

    public List<Outlet> searchOutlets(String keyword) {

        List<Outlet> outlets = new ArrayList<>();
        String sql = "SELECT o.*, " +
                "COUNT(DISTINCT u.id) as total_users, " +
                "COUNT(DISTINCT t.id) as total_transaksi, " +
                "COUNT(DISTINCT p.id) as total_paket " +
                "FROM outlet o " +
                "LEFT JOIN users u ON o.id = u.id_outlet " +
                "LEFT JOIN transaksi t ON o.id = t.id_outlet " +
                "LEFT JOIN paket p ON o.id = p.id_outlet " +
                "WHERE o.nama LIKE ? OR o.alamat LIKE ? OR o.tlp LIKE ? " +
                "GROUP BY o.id";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    Outlet outlet = resultSetToOutlet(rs);
                    outlets.add(outlet);

                }

            }

        } catch (SQLException e) {

            System.err.println("Error search outlets: " + e.getMessage());

        }

        return outlets;

    }

    public int getTotalOutlets() {

        String sql = "SELECT COUNT(*) as total FROM outlet";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {

                return rs.getInt("total");

            }

        } catch (SQLException e) {

            System.err.println("Error get total outlets: " + e.getMessage());

        }

        return 0;

    }

    private Outlet resultSetToOutlet(ResultSet rs) throws SQLException {

        Outlet outlet = new Outlet();
        outlet.setId(rs.getInt("id"));
        outlet.setNama(rs.getString("nama"));
        outlet.setAlamat(rs.getString("alamat"));
        outlet.setTlp(rs.getString("tlp"));
        outlet.setCreatedAt(rs.getTimestamp("created_at"));
        outlet.setUpdatedAt(rs.getTimestamp("updated_at"));

        return outlet;

    }

}
