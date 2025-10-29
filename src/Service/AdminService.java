package Service;

import Dao.*;
import Model.*;
import javax.swing.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class AdminService extends BaseService {
    private OutletDao outletDao;
    private MemberDao memberDao;
    private UserDao userDao;
    private TransaksiDao transaksiDao;
    private TbLogDao tbLogDao;
    private PaketDao paketDao;

    public AdminService() {
        super();
        this.outletDao = new OutletDao();
        this.memberDao = new MemberDao();
        this.userDao = new UserDao();
        this.transaksiDao = new TransaksiDao();
        this.tbLogDao = new TbLogDao();
        this.paketDao = new PaketDao();
    }

    // === ADMIN DASHBOARD ===
    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        try {
            // Basic statistics
            data.put("totalOutlets", outletDao.getTotalOutlets());
            data.put("totalMembers", memberDao.getTotalMembers());
            data.put("totalUsers", userDao.getAllUsers().size());
            data.put("totalPakets", paketDao.getAllPaket().size());
            data.put("totalTransactions", transaksiDao.getTotalTransaksi());

            // Today's transactions
            int todayTransactions = getTodayTransactionsCount();
            data.put("todayTransactions", todayTransactions);

            // Monthly statistics
            Map<String, Object> monthlyStats = getMonthlyStatistics();
            data.putAll(monthlyStats);

            // Status counts
            Map<String, Integer> statusCounts = getTransactionStatusCounts();
            data.put("pendingTransactions", statusCounts.getOrDefault("baru", 0));
            data.put("processingTransactions", statusCounts.getOrDefault("proses", 0));
            data.put("completedTransactions", statusCounts.getOrDefault("diambil", 0));

            // Recent data
            data.put("recentOutlets", getRecentOutlets(5));
            data.put("recentMembers", getRecentMembers(5));
            data.put("recentPakets", getRecentPakets(5));
            data.put("recentLogs", getRecentLogs(5));
            data.put("recentUsers", getRecentUsers(5));
            data.put("recentTransactions", getRecentTransactions(5));

            // Log activity
            logView("dashboard admin", "Admin mengakses dashboard");

        } catch (Exception e) {
            System.err.println("Error getting admin dashboard data: " + e.getMessage());
            // Return empty data on error
            initializeEmptyDashboardData(data);
        }

        return data;
    }

    private int getTodayTransactionsCount() {
        try {
            List<Transaksi> allTransactions = transaksiDao.getAllTransaksi();
            Date today = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String todayStr = sdf.format(today);

            int count = 0;
            for (Transaksi transaksi : allTransactions) {
                if (transaksi.getTgl() != null) {
                    String transaksiDate = sdf.format(transaksi.getTgl());
                    if (transaksiDate.equals(todayStr)) {
                        count++;
                    }
                }
            }
            return count;
        } catch (Exception e) {
            System.err.println("Error counting today transactions: " + e.getMessage());
            return 0;
        }
    }

    private Map<String, Object> getMonthlyStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            List<Transaksi> allTransactions = transaksiDao.getAllTransaksi();
            Calendar cal = Calendar.getInstance();
            int currentMonth = cal.get(Calendar.MONTH) + 1;
            int currentYear = cal.get(Calendar.YEAR);

            int monthlyTransactions = 0;
            double monthlyRevenue = 0;
            int completedTransactions = 0;

            for (Transaksi transaksi : allTransactions) {
                if (transaksi.getTgl() != null) {
                    cal.setTime(transaksi.getTgl());
                    int transaksiMonth = cal.get(Calendar.MONTH) + 1;
                    int transaksiYear = cal.get(Calendar.YEAR);

                    if (transaksiMonth == currentMonth && transaksiYear == currentYear) {
                        monthlyTransactions++;

                        if ("dibayar".equals(transaksi.getDibayar())) {
                            monthlyRevenue += transaksi.getTotal();
                        }

                        if ("diambil".equals(transaksi.getStatus()) && "dibayar".equals(transaksi.getDibayar())) {
                            completedTransactions++;
                        }
                    }
                }
            }

            double averageTransaction = monthlyTransactions > 0 ? monthlyRevenue / monthlyTransactions : 0;

            stats.put("monthlyTransactions", monthlyTransactions);
            stats.put("monthlyRevenue", monthlyRevenue);
            stats.put("completedTransactions", completedTransactions);
            stats.put("averageTransaction", averageTransaction);

        } catch (Exception e) {
            System.err.println("Error calculating monthly statistics: " + e.getMessage());
            stats.put("monthlyTransactions", 0);
            stats.put("monthlyRevenue", 0.0);
            stats.put("completedTransactions", 0);
            stats.put("averageTransaction", 0.0);
        }

        return stats;
    }

    private Map<String, Integer> getTransactionStatusCounts() {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("baru", 0);
        counts.put("proses", 0);
        counts.put("selesai", 0);
        counts.put("diambil", 0);

        try {
            List<Transaksi> allTransactions = transaksiDao.getAllTransaksi();
            for (Transaksi transaksi : allTransactions) {
                String status = transaksi.getStatus();
                counts.put(status, counts.getOrDefault(status, 0) + 1);
            }
        } catch (Exception e) {
            System.err.println("Error counting transaction status: " + e.getMessage());
        }

        return counts;
    }

    private List<Outlet> getRecentOutlets(int limit) {
        try {
            List<Outlet> allOutlets = outletDao.getAllOutlets();
            return allOutlets.size() > limit ? allOutlets.subList(0, limit) : allOutlets;
        } catch (Exception e) {
            System.err.println("Error getting recent outlets: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Member> getRecentMembers(int limit) {
        try {
            List<Member> allMembers = memberDao.getAllMembers();
            return allMembers.size() > limit ? allMembers.subList(0, limit) : allMembers;
        } catch (Exception e) {
            System.err.println("Error getting recent members: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Paket> getRecentPakets(int limit) {
        try {
            List<Paket> allPakets = paketDao.getAllPaket();
            return allPakets.size() > limit ? allPakets.subList(0, limit) : allPakets;
        } catch (Exception e) {
            System.err.println("Error getting recent pakets: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<TbLog> getRecentLogs(int limit) {
        try {
            List<TbLog> allLogs = tbLogDao.getAllLogs();
            return allLogs.size() > limit ? allLogs.subList(0, limit) : allLogs;
        } catch (Exception e) {
            System.err.println("Error getting recent logs: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<User> getRecentUsers(int limit) {
        try {
            List<User> allUsers = userDao.getAllUsers();
            // Sort by creation date (assuming id is auto-increment)
            allUsers.sort((u1, u2) -> Integer.compare(u2.getId(), u1.getId()));
            return allUsers.size() > limit ? allUsers.subList(0, limit) : allUsers;
        } catch (Exception e) {
            System.err.println("Error getting recent users: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Transaksi> getRecentTransactions(int limit) {
        try {
            List<Transaksi> allTransactions = transaksiDao.getAllTransaksi();
            // Sort by date descending
            allTransactions.sort((t1, t2) -> {
                if (t1.getTgl() == null && t2.getTgl() == null) return 0;
                if (t1.getTgl() == null) return 1;
                if (t2.getTgl() == null) return -1;
                return t2.getTgl().compareTo(t1.getTgl());
            });
            return allTransactions.size() > limit ? allTransactions.subList(0, limit) : allTransactions;
        } catch (Exception e) {
            System.err.println("Error getting recent transactions: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void initializeEmptyDashboardData(Map<String, Object> data) {
        data.put("totalOutlets", 0);
        data.put("totalMembers", 0);
        data.put("totalUsers", 0);
        data.put("todayTransactions", 0);
        data.put("totalPakets", 0);
        data.put("totalTransactions", 0);
        data.put("monthlyTransactions", 0);
        data.put("monthlyRevenue", 0.0);
        data.put("completedTransactions", 0);
        data.put("averageTransaction", 0.0);
        data.put("pendingTransactions", 0);
        data.put("processingTransactions", 0);
        data.put("recentOutlets", new ArrayList<>());
        data.put("recentMembers", new ArrayList<>());
        data.put("recentPakets", new ArrayList<>());
        data.put("recentLogs", new ArrayList<>());
        data.put("recentUsers", new ArrayList<>());
        data.put("recentTransactions", new ArrayList<>());
    }

    // === USER MANAGEMENT ===
    public List<User> getAllUsers() {
        try {
            List<User> users = userDao.getAllUsers();
            logView("daftar users", "Total: " + users.size());
            return users;
        } catch (Exception e) {
            System.err.println("Error getting all users: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean updateUserRole(int userId, String newRole, JFrame parentFrame) {
        // Check authorization
        if (!checkRole("admin", parentFrame)) {
            return false;
        }

        try {
            User user = userDao.getUserById(userId);
            if (user == null) {
                showError(parentFrame, "User tidak ditemukan");
                return false;
            }

            // Cannot change own role
            if (user.getId() == AuthService.getCurrentUser().getId()) {
                showError(parentFrame, "Tidak dapat mengubah role sendiri");
                return false;
            }

            String oldRole = user.getRole();
            user.setRole(newRole);
            user.setUpdatedAt(new Date());

            boolean success = userDao.updateUser(user);
            if (success) {
                logUpdate("role user",
                        String.format("User: %s, dari %s menjadi %s", user.getUsername(), oldRole, newRole));
                showSuccess(parentFrame, "Role user berhasil diperbarui");
                return true;
            } else {
                showError(parentFrame, "Gagal memperbarui role user");
                return false;
            }

        } catch (Exception e) {
            showError(parentFrame, "Gagal memperbarui role: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteUser(int userId, JFrame parentFrame) {
        // Check authorization
        if (!checkRole("admin", parentFrame)) {
            return false;
        }

        try {
            User user = userDao.getUserById(userId);
            if (user == null) {
                showError(parentFrame, "User tidak ditemukan");
                return false;
            }

            // Cannot delete own account
            if (user.getId() == AuthService.getCurrentUser().getId()) {
                showError(parentFrame, "Tidak dapat menghapus akun sendiri");
                return false;
            }

            // Confirmation
            if (!showConfirm(parentFrame, "Apakah Anda yakin ingin menghapus user " + user.getUsername() + "?")) {
                return false;
            }

            String userName = user.getUsername();
            boolean success = userDao.deleteUser(userId);
            if (success) {
                logDelete("user", "Username: " + userName);
                showSuccess(parentFrame, "User berhasil dihapus");
                return true;
            } else {
                showError(parentFrame, "Gagal menghapus user");
                return false;
            }

        } catch (Exception e) {
            showError(parentFrame, "Gagal menghapus user: " + e.getMessage());
            return false;
        }
    }

    // === LOG MANAGEMENT ===
    public List<TbLog> getAllLogs() {
        try {
            List<TbLog> logs = tbLogDao.getAllLogs();
            logView("daftar logs", "Total: " + logs.size());
            return logs;
        } catch (Exception e) {
            System.err.println("Error getting all logs: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<TbLog> searchLogs(String keyword) {
        try {
            List<TbLog> allLogs = tbLogDao.getAllLogs();
            List<TbLog> filteredLogs = new ArrayList<>();

            for (TbLog log : allLogs) {
                if (log.getAktivitas().toLowerCase().contains(keyword.toLowerCase()) ||
                        (log.getUser() != null && log.getUser().getName().toLowerCase().contains(keyword.toLowerCase()))) {
                    filteredLogs.add(log);
                }
            }

            logActivity("Pencarian logs", "Keyword: " + keyword + ", Hasil: " + filteredLogs.size());
            return filteredLogs;
        } catch (Exception e) {
            System.err.println("Error searching logs: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean clearLogs(JFrame parentFrame) {
        // Check authorization
        if (!checkRole("admin", parentFrame)) {
            return false;
        }

        try {
            List<TbLog> allLogs = tbLogDao.getAllLogs();
            int deletedCount = allLogs.size();

            // Confirmation
            if (!showConfirm(parentFrame,
                    "Apakah Anda yakin ingin menghapus semua " + deletedCount + " records log?")) {
                return false;
            }

            // Delete all logs (in real implementation, you might want to use a DAO method for this)
            boolean success = true;
            for (TbLog log : allLogs) {
                if (!tbLogDao.deleteLog(log.getIdLog())) {
                    success = false;
                    break;
                }
            }

            if (success) {
                logActivity("Clear semua logs", deletedCount + " records dihapus");
                showSuccess(parentFrame, "Berhasil menghapus " + deletedCount + " records log");
                return true;
            } else {
                showError(parentFrame, "Gagal menghapus beberapa records log");
                return false;
            }

        } catch (Exception e) {
            showError(parentFrame, "Gagal menghapus logs: " + e.getMessage());
            return false;
        }
    }

    // === STATISTICS METHODS ===
    public Map<String, Object> getLogsStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            List<TbLog> allLogs = tbLogDao.getAllLogs();
            int totalLogs = allLogs.size();

            // Today's logs
            int todayLogs = 0;
            Date today = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String todayStr = sdf.format(today);

            for (TbLog log : allLogs) {
                if (log.getTanggal() != null) {
                    String logDate = sdf.format(log.getTanggal());
                    if (logDate.equals(todayStr)) {
                        todayLogs++;
                    }
                }
            }

            // User vs System logs
            int userLogs = 0;
            int systemLogs = 0;
            for (TbLog log : allLogs) {
                if (log.getIdUser() > 0) {
                    userLogs++;
                } else {
                    systemLogs++;
                }
            }

            stats.put("total_logs", totalLogs);
            stats.put("today_logs", todayLogs);
            stats.put("user_logs", userLogs);
            stats.put("system_logs", systemLogs);

        } catch (Exception e) {
            System.err.println("Error getting logs statistics: " + e.getMessage());
            stats.put("total_logs", 0);
            stats.put("today_logs", 0);
            stats.put("user_logs", 0);
            stats.put("system_logs", 0);
        }

        return stats;
    }

    public Map<String, Object> getPaketStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            List<Paket> allPakets = paketDao.getAllPaket();
            int totalPakets = allPakets.size();

            // Group by jenis
            Map<String, Integer> paketsByJenis = new HashMap<>();
            for (Paket paket : allPakets) {
                String jenis = paket.getJenis();
                paketsByJenis.put(jenis, paketsByJenis.getOrDefault(jenis, 0) + 1);
            }

            // Group by outlet
            Map<String, Integer> paketsByOutlet = new HashMap<>();
            for (Paket paket : allPakets) {
                String outletName = paket.getNamaOutlet();
                paketsByOutlet.put(outletName, paketsByOutlet.getOrDefault(outletName, 0) + 1);
            }

            stats.put("total_pakets", totalPakets);
            stats.put("pakets_by_jenis", paketsByJenis);
            stats.put("pakets_by_outlet", paketsByOutlet);

        } catch (Exception e) {
            System.err.println("Error getting paket statistics: " + e.getMessage());
            stats.put("total_pakets", 0);
            stats.put("pakets_by_jenis", new HashMap<>());
            stats.put("pakets_by_outlet", new HashMap<>());
        }

        return stats;
    }

    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            List<User> allUsers = userDao.getAllUsers();
            int totalUsers = allUsers.size();

            // Group by role
            Map<String, Integer> usersByRole = new HashMap<>();
            for (User user : allUsers) {
                String role = user.getRole();
                usersByRole.put(role, usersByRole.getOrDefault(role, 0) + 1);
            }

            // Group by outlet
            Map<String, Integer> usersByOutlet = new HashMap<>();
            for (User user : allUsers) {
                String outletName = user.getNamaOutlet();
                usersByOutlet.put(outletName, usersByOutlet.getOrDefault(outletName, 0) + 1);
            }

            // Today's users
            int todayUsers = 0;
            Date today = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String todayStr = sdf.format(today);

            for (User user : allUsers) {
                if (user.getCreatedAt() != null) {
                    String userDate = sdf.format(user.getCreatedAt());
                    if (userDate.equals(todayStr)) {
                        todayUsers++;
                    }
                }
            }

            stats.put("total_users", totalUsers);
            stats.put("users_by_role", usersByRole);
            stats.put("users_by_outlet", usersByOutlet);
            stats.put("today_users", todayUsers);

        } catch (Exception e) {
            System.err.println("Error getting user statistics: " + e.getMessage());
            stats.put("total_users", 0);
            stats.put("users_by_role", new HashMap<>());
            stats.put("users_by_outlet", new HashMap<>());
            stats.put("today_users", 0);
        }

        return stats;
    }

    public Map<String, Object> getTransactionStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            List<Transaksi> allTransactions = transaksiDao.getAllTransaksi();
            int totalTransactions = allTransactions.size();

            // Today's transactions
            int todayTransactions = getTodayTransactionsCount();

            // Group by status
            Map<String, Integer> transactionsByStatus = new HashMap<>();
            // Group by payment
            Map<String, Integer> transactionsByPayment = new HashMap<>();

            double totalRevenue = 0;

            for (Transaksi transaksi : allTransactions) {
                // Status count
                String status = transaksi.getStatus();
                transactionsByStatus.put(status, transactionsByStatus.getOrDefault(status, 0) + 1);

                // Payment count
                String payment = transaksi.getDibayar();
                transactionsByPayment.put(payment, transactionsByPayment.getOrDefault(payment, 0) + 1);

                // Revenue
                if ("dibayar".equals(transaksi.getDibayar())) {
                    totalRevenue += transaksi.getTotal();
                }
            }

            stats.put("total_transactions", totalTransactions);
            stats.put("today_transactions", todayTransactions);
            stats.put("transactions_by_status", transactionsByStatus);
            stats.put("transactions_by_payment", transactionsByPayment);
            stats.put("total_revenue", totalRevenue);

        } catch (Exception e) {
            System.err.println("Error getting transaction statistics: " + e.getMessage());
            stats.put("total_transactions", 0);
            stats.put("today_transactions", 0);
            stats.put("transactions_by_status", new HashMap<>());
            stats.put("transactions_by_payment", new HashMap<>());
            stats.put("total_revenue", 0.0);
        }

        return stats;
    }
}
