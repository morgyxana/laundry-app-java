package Dao;

import Model.DetailTransaksi;
import Model.Paket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DetailTransaksiDao {

    private Connection connection;

    public DetailTransaksiDao() {

        connection = KoneksiDatabase.getConnection();

    }

    public boolean insertDetailTransaksi(DetailTransaksi detail) {

        String sql = "INSERT INTO detail_transaksi (id_transaksi, id_paket, qty, berat, keterangan) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, detail.getIdTransaksi());
            stmt.setInt(2, detail.getIdPaket());
            stmt.setDouble(3, detail.getQty());
            stmt.setDouble(4, detail.getBerat());
            stmt.setString(5, detail.getKeterangan());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {

                    if (generatedKeys.next()) {

                        detail.setId(generatedKeys.getInt(1));

                    }

                }

                return true;

            }

        } catch (SQLException e) {

            System.err.println("Error insert detail transaksi: " + e.getMessage());

        }

        return false;
    }

    public List<DetailTransaksi> getDetailByTransaksi(int idTransaksi) {

        List<DetailTransaksi> detailList = new ArrayList<>();

        String sql = "SELECT dt.*, p.jenis, p.nama_paket, p.harga, p.id_outlet " +
                "FROM detail_transaksi dt " +
                "JOIN paket p ON dt.id_paket = p.id " +
                "WHERE dt.id_transaksi = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, idTransaksi);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    DetailTransaksi detail = resultSetToDetailTransaksi(rs);
                    detailList.add(detail);

                }

            }

        } catch (SQLException e) {

            System.err.println("Error get detail by transaksi: " + e.getMessage());

        }

        return detailList;

    }

    public DetailTransaksi getDetailById(int id) {

        String sql = "SELECT dt.*, p.jenis, p.nama_paket, p.harga, p.id_outlet " +
                "FROM detail_transaksi dt " +
                "JOIN paket p ON dt.id_paket = p.id " +
                "WHERE dt.id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    return resultSetToDetailTransaksi(rs);

                }

            }

        } catch (SQLException e) {

            System.err.println("Error get detail by id: " + e.getMessage());

        }

        return null;

    }

    public boolean updateDetailTransaksi(DetailTransaksi detail) {

        String sql = "UPDATE detail_transaksi SET id_paket = ?, qty = ?, berat = ?, keterangan = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, detail.getIdPaket());
            stmt.setDouble(2, detail.getQty());
            stmt.setDouble(3, detail.getBerat());
            stmt.setString(4, detail.getKeterangan());
            stmt.setInt(5, detail.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println("Error update detail transaksi: " + e.getMessage());

        }

        return false;

    }

    public boolean deleteDetailTransaksi(int id) {

        String sql = "DELETE FROM detail_transaksi WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println("Error delete detail transaksi: " + e.getMessage());

        }

        return false;

    }

    public boolean deleteDetailByTransaksi(int idTransaksi) {

        String sql = "DELETE FROM detail_transaksi WHERE id_transaksi = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, idTransaksi);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println("Error delete detail by transaksi: " + e.getMessage());

        }

        return false;
    }

    private DetailTransaksi resultSetToDetailTransaksi(ResultSet rs) throws SQLException {

        DetailTransaksi detail = new DetailTransaksi();
        detail.setId(rs.getInt("id"));
        detail.setIdTransaksi(rs.getInt("id_transaksi"));
        detail.setIdPaket(rs.getInt("id_paket"));
        detail.setQty(rs.getDouble("qty"));
        detail.setBerat(rs.getDouble("berat"));
        detail.setKeterangan(rs.getString("keterangan"));

        Paket paket = new Paket();
        paket.setId(rs.getInt("id_paket"));
        paket.setJenis(rs.getString("jenis"));
        paket.setNamaPaket(rs.getString("nama_paket"));
        paket.setHarga(rs.getInt("harga"));
        paket.setIdOutlet(rs.getInt("id_outlet"));
        detail.setPaket(paket);

        return detail;
    }
}