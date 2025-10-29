package Service;

import Dao.*;
import Model.*;
import javax.swing.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class KasirService extends BaseService {
    private MemberDao memberDao;
    private TransaksiDao transaksiDao;
    private PaketDao paketDao;
    private OutletDao outletDao;

    public KasirService() {
        super();
        this.memberDao = new MemberDao();
        this.transaksiDao = new TransaksiDao();
        this.paketDao = new PaketDao();
        this.outletDao = new OutletDao();
    }

    // === KASIR DASHBOARD ===
    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        try {
            User currentUser = AuthService.getCurrentUser();
            int userOutletId = currentUser.getIdOutlet();

            // Basic statistics
            data.put("totalMembers", memberDao.getTotalMembers());
            data.put("totalTransactions", transaksiDao.getTotalTransaksi());
            data.put("totalPakets", paketDao.getAllPaket().size());
            data.put("todayTransactions", getTodayTransactionsCount());

            // Outlet-specific statistics
            if (userOutletId > 0) {
                data.put("myOutletTransactions", getOutletTransactionCount(userOutletId));
                data.put("myOutletTodayTransactions", getOutletTodayTransactionCount(userOutletId));
            } else {
                data.put("myOutletTransactions", 0);
                data.put("myOutletTodayTransactions", 0);
            }

            // Recent data
            data.put("recentMembers", getRecentMembers(5));
            data.put("recentTransactions", getRecentTransactions(5, userOutletId));

            // Status counts
            data.put("pendingTransactions", getTransactionCountByStatus("baru", userOutletId));
            data.put("processingTransactions", getTransactionCountByStatus("proses", userOutletId));
            data.put("completedTransactions", getTransactionCountByStatus("selesai", userOutletId));

            // Log activity
            logView("dashboard kasir", "Kasir mengakses dashboard");

        } catch (Exception e) {
            System.err.println("Error getting kasir dashboard data: " + e.getMessage());
            initializeEmptyKasirDashboardData(data);
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

    private int getOutletTransactionCount(int outletId) {
        try {
            List<Transaksi> outletTransactions = transaksiDao.getTransaksiByOutlet(outletId);
            return outletTransactions.size();
        } catch (Exception e) {
            System.err.println("Error counting outlet transactions: " + e.getMessage());
            return 0;
        }
    }

    private int getOutletTodayTransactionCount(int outletId) {
        try {
            List<Transaksi> outletTransactions = transaksiDao.getTransaksiByOutlet(outletId);
            Date today = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String todayStr = sdf.format(today);

            int count = 0;
            for (Transaksi transaksi : outletTransactions) {
                if (transaksi.getTgl() != null) {
                    String transaksiDate = sdf.format(transaksi.getTgl());
                    if (transaksiDate.equals(todayStr)) {
                        count++;
                    }
                }
            }
            return count;
        } catch (Exception e) {
            System.err.println("Error counting outlet today transactions: " + e.getMessage());
            return 0;
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

    private List<Transaksi> getRecentTransactions(int limit, int outletId) {
        try {
            List<Transaksi> transactions;
            if (outletId > 0) {
                transactions = transaksiDao.getTransaksiByOutlet(outletId);
            } else {
                transactions = transaksiDao.getAllTransaksi();
            }

            // Sort by date descending
            transactions.sort((t1, t2) -> {
                if (t1.getTgl() == null && t2.getTgl() == null) return 0;
                if (t1.getTgl() == null) return 1;
                if (t2.getTgl() == null) return -1;
                return t2.getTgl().compareTo(t1.getTgl());
            });

            return transactions.size() > limit ? transactions.subList(0, limit) : transactions;
        } catch (Exception e) {
            System.err.println("Error getting recent transactions: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private int getTransactionCountByStatus(String status, int outletId) {
        try {
            List<Transaksi> transactions;
            if (outletId > 0) {
                transactions = transaksiDao.getTransaksiByOutlet(outletId);
            } else {
                transactions = transaksiDao.getAllTransaksi();
            }

            int count = 0;
            for (Transaksi transaksi : transactions) {
                if (status.equals(transaksi.getStatus())) {
                    count++;
                }
            }
            return count;
        } catch (Exception e) {
            System.err.println("Error counting transactions by status: " + e.getMessage());
            return 0;
        }
    }

    private void initializeEmptyKasirDashboardData(Map<String, Object> data) {
        data.put("totalMembers", 0);
        data.put("totalTransactions", 0);
        data.put("totalPakets", 0);
        data.put("todayTransactions", 0);
        data.put("myOutletTransactions", 0);
        data.put("myOutletTodayTransactions", 0);
        data.put("recentMembers", new ArrayList<>());
        data.put("recentTransactions", new ArrayList<>());
        data.put("pendingTransactions", 0);
        data.put("processingTransactions", 0);
        data.put("completedTransactions", 0);
    }

    // === LAPORAN KASIR ===
    public Map<String, Object> generateLaporan(Date startDate, Date endDate) {
        Map<String, Object> result = new HashMap<>();

        try {
            User currentUser = AuthService.getCurrentUser();
            int userOutletId = currentUser.getIdOutlet();

            // Get transactions
            List<Transaksi> transactions = getTransactionsByDateRange(startDate, endDate, userOutletId);

            // Calculate statistics
            Map<String, Object> statistics = calculateLaporanStatistics(transactions);

            // Prepare result
            result.put("transactions", transactions);
            result.put("totalPendapatan", statistics.get("totalPendapatan"));
            result.put("startDate", startDate);
            result.put("endDate", endDate);
            result.put("userRole", "kasir");
            result.put("reportTitle", "Laporan Kasir - " + currentUser.getName());
            result.put("filterByOutlet", userOutletId > 0);
            result.put("outlets", outletDao.getAllOutlets());

            // Log activity
            logActivity("Generate laporan kasir",
                    String.format("Periode: %s - %s, Total: %d transaksi",
                            formatDate(startDate), formatDate(endDate), transactions.size()));

        } catch (Exception e) {
            System.err.println("Error generating kasir laporan: " + e.getMessage());
            initializeEmptyLaporanData(result);
        }

        return result;
    }

    public Map<String, Object> generateQuickReport() {
        try {
            Date today = new Date();
            return generateLaporan(today, today);
        } catch (Exception e) {
            System.err.println("Error generating quick report: " + e.getMessage());
            return new HashMap<>();
        }
    }

    private List<Transaksi> getTransactionsByDateRange(Date startDate, Date endDate, int outletId) {
        try {
            List<Transaksi> allTransactions;
            if (outletId > 0) {
                allTransactions = transaksiDao.getTransaksiByOutlet(outletId);
            } else {
                allTransactions = transaksiDao.getAllTransaksi();
            }

            List<Transaksi> filteredTransactions = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            for (Transaksi transaksi : allTransactions) {
                if (transaksi.getTgl() != null) {
                    try {
                        Date transaksiDate = sdf.parse(sdf.format(transaksi.getTgl()));
                        Date start = sdf.parse(sdf.format(startDate));
                        Date end = sdf.parse(sdf.format(endDate));

                        if (!transaksiDate.before(start) && !transaksiDate.after(end)) {
                            filteredTransactions.add(transaksi);
                        }
                    } catch (Exception e) {
                        // Skip this transaction if date parsing fails
                        continue;
                    }
                }
            }

            // Sort by date descending
            filteredTransactions.sort((t1, t2) -> {
                if (t1.getTgl() == null && t2.getTgl() == null) return 0;
                if (t1.getTgl() == null) return 1;
                if (t2.getTgl() == null) return -1;
                return t2.getTgl().compareTo(t1.getTgl());
            });

            return filteredTransactions;
        } catch (Exception e) {
            System.err.println("Error filtering transactions by date range: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private Map<String, Object> calculateLaporanStatistics(List<Transaksi> transactions) {
        Map<String, Object> stats = new HashMap<>();
        double totalPendapatan = 0;

        for (Transaksi transaksi : transactions) {
            if ("dibayar".equals(transaksi.getDibayar())) {
                totalPendapatan += transaksi.getTotal();
            }
        }

        stats.put("totalPendapatan", totalPendapatan);
        return stats;
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        return sdf.format(date);
    }

    private void initializeEmptyLaporanData(Map<String, Object> data) {
        data.put("transactions", new ArrayList<>());
        data.put("totalPendapatan", 0.0);
        data.put("startDate", new Date());
        data.put("endDate", new Date());
        data.put("userRole", "kasir");
        data.put("reportTitle", "Laporan Kasir");
        data.put("filterByOutlet", false);
        data.put("outlets", new ArrayList<>());
    }
}