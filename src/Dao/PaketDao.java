package Dao;

import Model.Paket;
import Model.Outlet;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaketDao {

    private Connection connection;

    public PaketDao() {

        connection = KoneksiDatabase.getConnection();

    }

    public boolean insertPaket(Paket paket) {

        String sql = "INSERT INTO paket (id_outlet, jenis, nama_paket, harga) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, paket.getIdOutlet());
            stmt.setString(2, paket.getJenis());
            stmt.setString(3, paket.getNamaPaket());
            stmt.setInt(4, paket.getHarga());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {

                    if (generatedKeys.next()) {

                        paket.setId(generatedKeys.getInt(1));

                    }

                }

                return true;

            }

        } catch (SQLException e) {

            System.err.println("Error insert paket: " + e.getMessage());

        }

        return false;

    }

    public List<Paket> getAllPaket() {

        List<Paket> paketList = new ArrayList<>();
        String sql = "SELECT p.*, o.nama as outlet_nama, COUNT(dt.id) as total_transaksi " +
                "FROM paket p " +
                "LEFT JOIN outlet o ON p.id_outlet = o.id " +
                "LEFT JOIN detail_transaksi dt ON p.id = dt.id_paket " +
                "GROUP BY p.id";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                Paket paket = resultSetToPaket(rs);
                paketList.add(paket);

            }

        } catch (SQLException e) {

            System.err.println("Error get all paket: " + e.getMessage());

        }

        return paketList;

    }

    public Paket getPaketById(int id) {

        String sql = "SELECT p.*, o.nama as outlet_nama, COUNT(dt.id) as total_transaksi " +
                "FROM paket p " +
                "LEFT JOIN outlet o ON p.id_outlet = o.id " +
                "LEFT JOIN detail_transaksi dt ON p.id = dt.id_paket " +
                "WHERE p.id = ? " +
                "GROUP BY p.id";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    return resultSetToPaket(rs);

                }

            }

        } catch (SQLException e) {

            System.err.println("Error get paket by id: " + e.getMessage());

        }

        return null;

    }

    public List<Paket> getPaketByOutlet(int outletId) {

        List<Paket> paketList = new ArrayList<>();
        String sql = "SELECT p.*, o.nama as outlet_nama, COUNT(dt.id) as total_transaksi " +
                "FROM paket p " +
                "LEFT JOIN outlet o ON p.id_outlet = o.id " +
                "LEFT JOIN detail_transaksi dt ON p.id = dt.id_paket " +
                "WHERE p.id_outlet = ? " +
                "GROUP BY p.id";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, outletId);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    Paket paket = resultSetToPaket(rs);
                    paketList.add(paket);

                }

            }

        } catch (SQLException e) {

            System.err.println("Error get paket by outlet: " + e.getMessage());

        }

        return paketList;

    }

    public List<Paket> getPaketByJenis(int outletId, String jenis) {

        List<Paket> paketList = new ArrayList<>();
        String sql = "SELECT p.*, o.nama as outlet_nama, COUNT(dt.id) as total_transaksi " +
                "FROM paket p " +
                "LEFT JOIN outlet o ON p.id_outlet = o.id " +
                "LEFT JOIN detail_transaksi dt ON p.id = dt.id_paket " +
                "WHERE p.id_outlet = ? AND p.jenis = ? " +
                "GROUP BY p.id";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, outletId);
            stmt.setString(2, jenis);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    Paket paket = resultSetToPaket(rs);
                    paketList.add(paket);

                }

            }

        } catch (SQLException e) {

            System.err.println("Error get paket by jenis: " + e.getMessage());

        }

        return paketList;

    }

    public boolean updatePaket(Paket paket) {

        String sql = "UPDATE paket SET id_outlet = ?, jenis = ?, nama_paket = ?, harga = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, paket.getIdOutlet());
            stmt.setString(2, paket.getJenis());
            stmt.setString(3, paket.getNamaPaket());
            stmt.setInt(4, paket.getHarga());
            stmt.setInt(5, paket.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println("Error update paket: " + e.getMessage());

        }

        return false;

    }

    public boolean deletePaket(int id) {

        String checkSql = "SELECT COUNT(*) as count FROM detail_transaksi WHERE id_paket = ?";
        String deleteSql = "DELETE FROM paket WHERE id = ?";

        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {

            checkStmt.setInt(1, id);

            try (ResultSet rs = checkStmt.executeQuery()) {

                if (rs.next() && rs.getInt("count") > 0) {

                    System.err.println("Tidak dapat menghapus paket yang digunakan dalam transaksi");
                    return false;

                }

            }

            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {

                deleteStmt.setInt(1, id);
                return deleteStmt.executeUpdate() > 0;

            }

        } catch (SQLException e) {

            System.err.println("Error delete paket: " + e.getMessage());

        }

        return false;

    }

    public List<Paket> searchPaket(String keyword) {

        List<Paket> paketList = new ArrayList<>();
        String sql = "SELECT p.*, o.nama as outlet_nama, COUNT(dt.id) as total_transaksi " +
                "FROM paket p " +
                "LEFT JOIN outlet o ON p.id_outlet = o.id " +
                "LEFT JOIN detail_transaksi dt ON p.id = dt.id_paket " +
                "WHERE p.nama_paket LIKE ? OR p.jenis LIKE ? OR o.nama LIKE ? " +
                "GROUP BY p.id";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    Paket paket = resultSetToPaket(rs);
                    paketList.add(paket);

                }

            }

        } catch (SQLException e) {

            System.err.println("Error search paket: " + e.getMessage());

        }

        return paketList;

    }

    public int getTotalPaketByOutlet(int outletId) {

        String sql = "SELECT COUNT(*) as total FROM paket WHERE id_outlet = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, outletId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    return rs.getInt("total");

                }

            }

        } catch (SQLException e) {

            System.err.println("Error get total paket by outlet: " + e.getMessage());

        }

        return 0;

    }

    private Paket resultSetToPaket(ResultSet rs) throws SQLException {

        Paket paket = new Paket();
        paket.setId(rs.getInt("id"));
        paket.setIdOutlet(rs.getInt("id_outlet"));
        paket.setJenis(rs.getString("jenis"));
        paket.setNamaPaket(rs.getString("nama_paket"));
        paket.setHarga(rs.getInt("harga"));

        if (rs.getString("outlet_nama") != null) {

            Outlet outlet = new Outlet();
            outlet.setId(rs.getInt("id_outlet"));
            outlet.setNama(rs.getString("outlet_nama"));
            paket.setOutlet(outlet);

        }

        return paket;

    }

}