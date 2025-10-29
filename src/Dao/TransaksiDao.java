package Dao;

import Model.Transaksi;
import Model.DetailTransaksi;
import Model.Member;
import Model.Outlet;
import Model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransaksiDao {

    private Connection connection;
    private DetailTransaksiDao detailTransaksiDao;

    public TransaksiDao() {

        connection = KoneksiDatabase.getConnection();
        detailTransaksiDao = new DetailTransaksiDao();

    }


    public boolean insertTransaksi(Transaksi transaksi) {

        String sql = "INSERT INTO transaksi (id_outlet, kode_invoice, id_member, tipe_pelanggan, " +
                "nama_pelanggan, tlp_pelanggan, tgl, batas_waktu, tgl_bayar, biaya_tambahan, " +
                "diskon, pajak, status, dibayar, id_user) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, transaksi.getIdOutlet());
            stmt.setString(2, transaksi.getKodeInvoice());

            if (transaksi.getIdMember() != null) {

                stmt.setInt(3, transaksi.getIdMember());

            } else {

                stmt.setNull(3, Types.INTEGER);

            }

            stmt.setString(4, transaksi.getTipePelanggan());
            stmt.setString(5, transaksi.getNamaPelanggan());
            stmt.setString(6, transaksi.getTlpPelanggan());
            stmt.setTimestamp(7, new Timestamp(transaksi.getTgl().getTime()));

            if (transaksi.getBatasWaktu() != null) {

                stmt.setTimestamp(8, new Timestamp(transaksi.getBatasWaktu().getTime()));

            } else {

                stmt.setNull(8, Types.TIMESTAMP);

            }

            if (transaksi.getTglBayar() != null) {

                stmt.setTimestamp(9, new Timestamp(transaksi.getTglBayar().getTime()));

            } else {

                stmt.setNull(9, Types.TIMESTAMP);

            }

            stmt.setInt(10, transaksi.getBiayaTambahan());
            stmt.setDouble(11, transaksi.getDiskon());
            stmt.setInt(12, transaksi.getPajak());
            stmt.setString(13, transaksi.getStatus());
            stmt.setString(14, transaksi.getDibayar());
            stmt.setInt(15, transaksi.getIdUser());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {

                    if (generatedKeys.next()) {

                        int transaksiId = generatedKeys.getInt(1);
                        transaksi.setId(transaksiId);

                        if (transaksi.getDetailTransaksi() != null) {

                            for (DetailTransaksi detail : transaksi.getDetailTransaksi()) {

                                detail.setIdTransaksi(transaksiId);
                                detailTransaksiDao.insertDetailTransaksi(detail);

                            }

                        }

                        return true;

                    }

                }

            }

        } catch (SQLException e) {

            System.err.println("Error insert transaksi: " + e.getMessage());

        }

        return false;

    }

    public List<Transaksi> getAllTransaksi() {

        List<Transaksi> transaksiList = new ArrayList<>();
        String sql = "SELECT t.*, o.nama as outlet_nama, m.nama as member_nama, u.name as user_nama " +
                "FROM transaksi t " +
                "LEFT JOIN outlet o ON t.id_outlet = o.id " +
                "LEFT JOIN member m ON t.id_member = m.id " +
                "LEFT JOIN users u ON t.id_user = u.id " +
                "ORDER BY t.tgl DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                Transaksi transaksi = resultSetToTransaksi(rs);
                transaksiList.add(transaksi);

            }

        } catch (SQLException e) {

            System.err.println("Error get all transaksi: " + e.getMessage());

        }

        return transaksiList;

    }

    public Transaksi getTransaksiById(int id) {

        String sql = "SELECT t.*, o.nama as outlet_nama, m.nama as member_nama, u.name as user_nama " +
                "FROM transaksi t " +
                "LEFT JOIN outlet o ON t.id_outlet = o.id " +
                "LEFT JOIN member m ON t.id_member = m.id " +
                "LEFT JOIN users u ON t.id_user = u.id " +
                "WHERE t.id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    Transaksi transaksi = resultSetToTransaksi(rs);

                    List<DetailTransaksi> detailList = detailTransaksiDao.getDetailByTransaksi(id);
                    transaksi.setDetailTransaksi(detailList);

                    return transaksi;

                }

            }

        } catch (SQLException e) {

            System.err.println("Error get transaksi by id: " + e.getMessage());

        }

        return null;

    }

    public Transaksi getTransaksiByKodeInvoice(String kodeInvoice) {

        String sql = "SELECT t.*, o.nama as outlet_nama, m.nama as member_nama, u.name as user_nama " +
                "FROM transaksi t " +
                "LEFT JOIN outlet o ON t.id_outlet = o.id " +
                "LEFT JOIN member m ON t.id_member = m.id " +
                "LEFT JOIN users u ON t.id_user = u.id " +
                "WHERE t.kode_invoice = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, kodeInvoice);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    Transaksi transaksi = resultSetToTransaksi(rs);

                    List<DetailTransaksi> detailList = detailTransaksiDao.getDetailByTransaksi(transaksi.getId());
                    transaksi.setDetailTransaksi(detailList);

                    return transaksi;

                }

            }

        } catch (SQLException e) {

            System.err.println("Error get transaksi by kode invoice: " + e.getMessage());

        }

        return null;

    }

    public List<Transaksi> getTransaksiByOutlet(int outletId) {

        List<Transaksi> transaksiList = new ArrayList<>();
        String sql = "SELECT t.*, o.nama as outlet_nama, m.nama as member_nama, u.name as user_nama " +
                "FROM transaksi t " +
                "LEFT JOIN outlet o ON t.id_outlet = o.id " +
                "LEFT JOIN member m ON t.id_member = m.id " +
                "LEFT JOIN users u ON t.id_user = u.id " +
                "WHERE t.id_outlet = ? " +
                "ORDER BY t.tgl DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, outletId);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    Transaksi transaksi = resultSetToTransaksi(rs);
                    transaksiList.add(transaksi);

                }

            }

        } catch (SQLException e) {

            System.err.println("Error get transaksi by outlet: " + e.getMessage());

        }

        return transaksiList;

    }

    public List<Transaksi> getTransaksiByStatus(String status) {

        List<Transaksi> transaksiList = new ArrayList<>();
        String sql = "SELECT t.*, o.nama as outlet_nama, m.nama as member_nama, u.name as user_nama " +
                "FROM transaksi t " +
                "LEFT JOIN outlet o ON t.id_outlet = o.id " +
                "LEFT JOIN member m ON t.id_member = m.id " +
                "LEFT JOIN users u ON t.id_user = u.id " +
                "WHERE t.status = ? " +
                "ORDER BY t.tgl DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, status);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    Transaksi transaksi = resultSetToTransaksi(rs);
                    transaksiList.add(transaksi);

                }

            }

        } catch (SQLException e) {

            System.err.println("Error get transaksi by status: " + e.getMessage());

        }

        return transaksiList;

    }

    public boolean updateTransaksi(Transaksi transaksi) {

        String sql = "UPDATE transaksi SET id_outlet = ?, kode_invoice = ?, id_member = ?, tipe_pelanggan = ?, " +
                "nama_pelanggan = ?, tlp_pelanggan = ?, tgl = ?, batas_waktu = ?, tgl_bayar = ?, " +
                "biaya_tambahan = ?, diskon = ?, pajak = ?, status = ?, dibayar = ?, id_user = ? " +
                "WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, transaksi.getIdOutlet());
            stmt.setString(2, transaksi.getKodeInvoice());

            if (transaksi.getIdMember() != null) {

                stmt.setInt(3, transaksi.getIdMember());

            } else {

                stmt.setNull(3, Types.INTEGER);

            }

            stmt.setString(4, transaksi.getTipePelanggan());
            stmt.setString(5, transaksi.getNamaPelanggan());
            stmt.setString(6, transaksi.getTlpPelanggan());
            stmt.setTimestamp(7, new Timestamp(transaksi.getTgl().getTime()));

            if (transaksi.getBatasWaktu() != null) {

                stmt.setTimestamp(8, new Timestamp(transaksi.getBatasWaktu().getTime()));

            } else {

                stmt.setNull(8, Types.TIMESTAMP);

            }

            if (transaksi.getTglBayar() != null) {

                stmt.setTimestamp(9, new Timestamp(transaksi.getTglBayar().getTime()));

            } else {

                stmt.setNull(9, Types.TIMESTAMP);

            }

            stmt.setInt(10, transaksi.getBiayaTambahan());
            stmt.setDouble(11, transaksi.getDiskon());
            stmt.setInt(12, transaksi.getPajak());
            stmt.setString(13, transaksi.getStatus());
            stmt.setString(14, transaksi.getDibayar());
            stmt.setInt(15, transaksi.getIdUser());
            stmt.setInt(16, transaksi.getId());

            boolean transaksiUpdated = stmt.executeUpdate() > 0;

            if (transaksiUpdated && transaksi.getDetailTransaksi() != null) {

                detailTransaksiDao.deleteDetailByTransaksi(transaksi.getId());

                for (DetailTransaksi detail : transaksi.getDetailTransaksi()) {

                    detail.setIdTransaksi(transaksi.getId());
                    detailTransaksiDao.insertDetailTransaksi(detail);

                }

            }

            return transaksiUpdated;

        } catch (SQLException e) {

            System.err.println("Error update transaksi: " + e.getMessage());

        }

        return false;

    }

    public boolean updateStatus(int id, String status) {

        String sql = "UPDATE transaksi SET status = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, id);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println("Error update status: " + e.getMessage());

        }

        return false;

    }

    public boolean updatePembayaran(int id, String dibayar, Date tglBayar) {

        String sql = "UPDATE transaksi SET dibayar = ?, tgl_bayar = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, dibayar);

            if (tglBayar != null) {

                stmt.setTimestamp(2, new Timestamp(tglBayar.getTime()));

            } else {

                stmt.setNull(2, Types.TIMESTAMP);

            }

            stmt.setInt(3, id);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println("Error update pembayaran: " + e.getMessage());

        }

        return false;

    }

    public boolean deleteTransaksi(int id) {

        detailTransaksiDao.deleteDetailByTransaksi(id);

        String sql = "DELETE FROM transaksi WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println("Error delete transaksi: " + e.getMessage());

        }

        return false;

    }

    public List<Transaksi> searchTransaksi(String keyword) {

        List<Transaksi> transaksiList = new ArrayList<>();
        String sql = "SELECT t.*, o.nama as outlet_nama, m.nama as member_nama, u.name as user_nama " +
                "FROM transaksi t " +
                "LEFT JOIN outlet o ON t.id_outlet = o.id " +
                "LEFT JOIN member m ON t.id_member = m.id " +
                "LEFT JOIN users u ON t.id_user = u.id " +
                "WHERE t.kode_invoice LIKE ? OR t.nama_pelanggan LIKE ? OR m.nama LIKE ? " +
                "ORDER BY t.tgl DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    Transaksi transaksi = resultSetToTransaksi(rs);
                    transaksiList.add(transaksi);

                }

            }

        } catch (SQLException e) {

            System.err.println("Error search transaksi: " + e.getMessage());

        }

        return transaksiList;

    }

    public int getTotalTransaksi() {

        String sql = "SELECT COUNT(*) as total FROM transaksi";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {

                return rs.getInt("total");

            }

        } catch (SQLException e) {

            System.err.println("Error get total transaksi: " + e.getMessage());

        }

        return 0;

    }

    public int getTotalTransaksiByOutlet(int outletId) {

        String sql = "SELECT COUNT(*) as total FROM transaksi WHERE id_outlet = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, outletId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    return rs.getInt("total");

                }

            }

        } catch (SQLException e) {

            System.err.println("Error get total transaksi by outlet: " + e.getMessage());

        }

        return 0;

    }

    public String generateKodeInvoice() {

        String sql = "SELECT COUNT(*) as count FROM transaksi WHERE DATE(tgl) = CURDATE()";
        String prefix = "INV/" + new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date()) + "/";


        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {

                int count = rs.getInt("count") + 1;
                return prefix + String.format("%04d", count);

            }

        } catch (SQLException e) {

            System.err.println("Error generate kode invoice: " + e.getMessage());

        }

        return prefix + "0001";

    }

    private Transaksi resultSetToTransaksi(ResultSet rs) throws SQLException {

        Transaksi transaksi = new Transaksi();
        transaksi.setId(rs.getInt("id"));
        transaksi.setIdOutlet(rs.getInt("id_outlet"));
        transaksi.setKodeInvoice(rs.getString("kode_invoice"));
        transaksi.setIdMember(rs.getInt("id_member"));
        if (rs.wasNull()) transaksi.setIdMember(null);
        transaksi.setTipePelanggan(rs.getString("tipe_pelanggan"));
        transaksi.setNamaPelanggan(rs.getString("nama_pelanggan"));
        transaksi.setTlpPelanggan(rs.getString("tlp_pelanggan"));
        transaksi.setTgl(rs.getTimestamp("tgl"));
        transaksi.setBatasWaktu(rs.getTimestamp("batas_waktu"));
        transaksi.setTglBayar(rs.getTimestamp("tgl_bayar"));
        transaksi.setBiayaTambahan(rs.getInt("biaya_tambahan"));
        transaksi.setDiskon(rs.getDouble("diskon"));
        transaksi.setPajak(rs.getInt("pajak"));
        transaksi.setStatus(rs.getString("status"));
        transaksi.setDibayar(rs.getString("dibayar"));
        transaksi.setIdUser(rs.getInt("id_user"));


        if (rs.getString("outlet_nama") != null) {

            Outlet outlet = new Outlet();
            outlet.setId(rs.getInt("id_outlet"));
            outlet.setNama(rs.getString("outlet_nama"));
            transaksi.setOutlet(outlet);

        }

        if (rs.getString("member_nama") != null) {

            Member member = new Member();
            member.setId(rs.getInt("id_member"));
            member.setNama(rs.getString("member_nama"));
            transaksi.setMember(member);

        }

        if (rs.getString("user_nama") != null) {

            User user = new User();
            user.setId(rs.getInt("id_user"));
            user.setName(rs.getString("user_nama"));
            transaksi.setUser(user);

        }

        return transaksi;

    }

}
