package Dao;

import Model.TbLog;
import Model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TbLogDao {

    private Connection connection;

    public TbLogDao() {

        connection = KoneksiDatabase.getConnection();

    }

    public boolean insertLog(TbLog log) {

        String sql = "INSERT INTO tb_log (id_user, aktivitas, tanggal, data_terkait) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, log.getIdUser());
            stmt.setString(2, log.getAktivitas());
            stmt.setTimestamp(3, new Timestamp(log.getTanggal().getTime()));
            stmt.setString(4, log.getDataTerkait());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {

                    if (generatedKeys.next()) {

                        log.setIdLog(generatedKeys.getInt(1));

                    }

                }

                return true;

            }

        } catch (SQLException e) {

            System.err.println("Error insert log: " + e.getMessage());

        }

        return false;

    }

    public List<TbLog> getAllLogs() {

        List<TbLog> logs = new ArrayList<>();
        String sql = "SELECT l.*, u.name as user_name, u.username " +
                "FROM tb_log l " +
                "JOIN users u ON l.id_user = u.id " +
                "ORDER BY l.tanggal DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                TbLog log = resultSetToLog(rs);
                logs.add(log);
            }

        } catch (SQLException e) {

            System.err.println("Error get all logs: " + e.getMessage());

        }

        return logs;

    }

    public TbLog getLogById(int id) {
        String sql = "SELECT l.*, u.name as user_name, u.username " +
                "FROM tb_log l " +
                "JOIN users u ON l.id_user = u.id " +
                "WHERE l.id_log = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    return resultSetToLog(rs);

                }

            }

        } catch (SQLException e) {

            System.err.println("Error get log by id: " + e.getMessage());

        }

        return null;

    }

    public List<TbLog> getLogsByUser(int userId) {

        List<TbLog> logs = new ArrayList<>();
        String sql = "SELECT l.*, u.name as user_name, u.username " +
                "FROM tb_log l " +
                "JOIN users u ON l.id_user = u.id " +
                "WHERE l.id_user = ? " +
                "ORDER BY l.tanggal DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    TbLog log = resultSetToLog(rs);
                    logs.add(log);

                }

            }

        } catch (SQLException e) {

            System.err.println("Error get logs by user: " + e.getMessage());

        }

        return logs;

    }

    public List<TbLog> getLogsByDateRange(Date startDate, Date endDate) {

        List<TbLog> logs = new ArrayList<>();
        String sql = "SELECT l.*, u.name as user_name, u.username " +
                "FROM tb_log l " +
                "JOIN users u ON l.id_user = u.id " +
                "WHERE l.tanggal BETWEEN ? AND ? " +
                "ORDER BY l.tanggal DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setTimestamp(1, new Timestamp(startDate.getTime()));
            stmt.setTimestamp(2, new Timestamp(endDate.getTime()));

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    TbLog log = resultSetToLog(rs);
                    logs.add(log);

                }

            }

        } catch (SQLException e) {

            System.err.println("Error get logs by date range: " + e.getMessage());

        }

        return logs;

    }

    public boolean deleteLog(int id) {

        String sql = "DELETE FROM tb_log WHERE id_log = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println("Error delete log: " + e.getMessage());

        }

        return false;

    }

    public boolean deleteLogsByUser(int userId) {

        String sql = "DELETE FROM tb_log WHERE id_user = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println("Error delete logs by user: " + e.getMessage());

        }

        return false;

    }

    public int getTotalLogs() {

        String sql = "SELECT COUNT(*) as total FROM tb_log";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {

                return rs.getInt("total");

            }

        } catch (SQLException e) {

            System.err.println("Error get total logs: " + e.getMessage());

        }

        return 0;

    }

    public int getTotalLogsByUser(int userId) {

        String sql = "SELECT COUNT(*) as total FROM tb_log WHERE id_user = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    return rs.getInt("total");

                }

            }

        } catch (SQLException e) {

            System.err.println("Error get total logs by user: " + e.getMessage());

        }

        return 0;

    }

    private TbLog resultSetToLog(ResultSet rs) throws SQLException {

        TbLog log = new TbLog();
        log.setIdLog(rs.getInt("id_log"));
        log.setIdUser(rs.getInt("id_user"));
        log.setAktivitas(rs.getString("aktivitas"));
        log.setTanggal(rs.getTimestamp("tanggal"));
        log.setDataTerkait(rs.getString("data_terkait"));

        User user = new User();
        user.setId(rs.getInt("id_user"));
        user.setName(rs.getString("user_name"));
        user.setUsername(rs.getString("username"));
        log.setUser(user);

        return log;

    }

}