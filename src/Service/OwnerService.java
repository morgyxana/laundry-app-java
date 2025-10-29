package Service;

import Dao.*;
import Model.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class OwnerService extends BaseService {
    private TransaksiDao transaksiDao;
    private MemberDao memberDao;
    private OutletDao outletDao;
    private PaketDao paketDao;

    public OwnerService() {
        super();
        this.transaksiDao = new TransaksiDao();
        this.memberDao = new MemberDao();
        this.outletDao = new OutletDao();
        this.paketDao = new PaketDao();
    }

    // === OWNER DASHBOARD ===
    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        try {
            User currentUser = AuthService.getCurrentUser();

            // Statistics
            Map<String, Object> stats = getDashboardStatistics();
            data.putAll(stats);

            // Recent transactions
            List<Transaksi> recentTransactions = getRecentTransactions(5);
            data.put("recentTransactions", recentTransactions);

            // Weekly revenue
            List<Map<String, Object>> weeklyRevenue = getWeeklyRevenue();
            data.put("weeklyRevenue", weeklyRevenue);

            // Top outlets
            List<Outlet> topOutlets = getTopOutlets(5);
            data.put("topOutlets", topOutlets);

            // User info
            data.put("user", currentUser);

            // Log activity
            logView("dashboard owner", "Owner mengakses dashboard");

        } catch (Exception e) {
            System.err.println("Error getting owner dashboard data: " + e.getMessage());
            initializeEmptyOwnerDashboardData(data);
        }

        return data;
    }

    private Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            stats.put("totalRevenue", calculateTotalRevenue());
            stats.put("monthlyRevenue", calculateMonthlyRevenue());
            stats.put("totalMembers", memberDao.getTotalMembers());
            stats.put("totalTransactions", transaksiDao.getTotalTransaksi());
            stats.put("todayTransactions", getTodayTransactionsCount());
            stats.put("completedTransactions", getCompletedTransactionsCount());
            stats.put("pendingPayments", getPendingPaymentsCount());
            stats.put("pendingTransactions", getTransactionCountByStatus("baru"));
            stats.put("processingTransactions", getTransactionCountByStatus("proses"));

        } catch (Exception e) {
            System.err.println("Error calculating dashboard statistics: " + e.getMessage());
            initializeEmptyStatistics(stats);
        }

        return stats;
    }

    private double calculateTotalRevenue() {
        try {
            List<Transaksi> allTransactions = transaksiDao.getAllTransaksi();
            double totalRevenue = 0;

            for (Transaksi transaksi : allTransactions) {
                if ("dibayar".equals(transaksi.getDibayar())) {
                    totalRevenue += transaksi.getTotal();
                }
            }

            return totalRevenue;
        } catch (Exception e) {
            System.err.println("Error calculating total revenue: " + e.getMessage());
            return 0;
        }
    }

    private double calculateMonthlyRevenue() {
        try {
            List<Transaksi> allTransactions = transaksiDao.getAllTransaksi();
            Calendar cal = Calendar.getInstance();
            int currentMonth = cal.get(Calendar.MONTH) + 1;
            int currentYear = cal.get(Calendar.YEAR);

            double monthlyRevenue = 0;

            for (Transaksi transaksi : allTransactions) {
                if (transaksi.getTgl() != null && "dibayar".equals(transaksi.getDibayar())) {
                    cal.setTime(transaksi.getTgl());
                    int transaksiMonth = cal.get(Calendar.MONTH) + 1;
                    int transaksiYear = cal.get(Calendar.YEAR);

                    if (transaksiMonth == currentMonth && transaksiYear == currentYear) {
                        monthlyRevenue += transaksi.getTotal();
                    }
                }
            }

            return monthlyRevenue;
        } catch (Exception e) {
            System.err.println("Error calculating monthly revenue: " + e.getMessage());
            return 0;
        }
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

    private int getCompletedTransactionsCount() {
        try {
            List<Transaksi> allTransactions = transaksiDao.getAllTransaksi();
            int count = 0;
            for (Transaksi transaksi : allTransactions) {
                if ("diambil".equals(transaksi.getStatus())) {
                    count++;
                }
            }
            return count;
        } catch (Exception e) {
            System.err.println("Error counting completed transactions: " + e.getMessage());
            return 0;
        }
    }

    private int getPendingPaymentsCount() {
        try {
            List<Transaksi> allTransactions = transaksiDao.getAllTransaksi();
            int count = 0;
            for (Transaksi transaksi : allTransactions) {
                if ("belum_dibayar".equals(transaksi.getDibayar())) {
                    count++;
                }
            }
            return count;
        } catch (Exception e) {
            System.err.println("Error counting pending payments: " + e.getMessage());
            return 0;
        }
    }

    private int getTransactionCountByStatus(String status) {
        try {
            List<Transaksi> allTransactions = transaksiDao.getAllTransaksi();
            int count = 0;
            for (Transaksi transaksi : allTransactions) {
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

    private List<Map<String, Object>> getWeeklyRevenue() {
        List<Map<String, Object>> weeklyRevenue = new ArrayList<>();

        try {
            List<Transaksi> allTransactions = transaksiDao.getAllTransaksi();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

            // Group by date for last 7 days
            Map<String, Double> revenueByDate = new HashMap<>();

            for (int i = 6; i >= 0; i--) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, -i);
                String dateStr = sdf.format(cal.getTime());
                revenueByDate.put(dateStr, 0.0);
            }

            // Calculate revenue for each date
            for (Transaksi transaksi : allTransactions) {
                if (transaksi.getTgl() != null && "dibayar".equals(transaksi.getDibayar())) {
                    String dateStr = sdf.format(transaksi.getTgl());
                    if (revenueByDate.containsKey(dateStr)) {
                        double currentRevenue = revenueByDate.get(dateStr);
                        revenueByDate.put(dateStr, currentRevenue + transaksi.getTotal());
                    }
                }
            }

            // Convert to list of maps
            for (int i = 6; i >= 0; i--) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, -i);
                String dateStr = sdf.format(cal.getTime());

                Map<String, Object> dayRevenue = new HashMap<>();
                dayRevenue.put("date", dateStr);
                dayRevenue.put("revenue", revenueByDate.get(dateStr));
                weeklyRevenue.add(dayRevenue);
            }

        } catch (Exception e) {
            System.err.println("Error calculating weekly revenue: " + e.getMessage());
            // Return empty data for last 7 days
            for (int i = 6; i >= 0; i--) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, -i);
                String dateStr = new SimpleDateFormat("dd-MM-yyyy").format(cal.getTime());

                Map<String, Object> dayRevenue = new HashMap<>();
                dayRevenue.put("date", dateStr);
                dayRevenue.put("revenue", 0.0);
                weeklyRevenue.add(dayRevenue);
            }
        }

        return weeklyRevenue;
    }

    private List<Outlet> getTopOutlets(int limit) {
        try {
            List<Outlet> allOutlets = outletDao.getAllOutlets();
            List<OutletWithRevenue> outletsWithRevenue = new ArrayList<>();

            // Calculate revenue for each outlet
            for (Outlet outlet : allOutlets) {
                double revenue = calculateOutletRevenue(outlet.getId());
                outletsWithRevenue.add(new OutletWithRevenue(outlet, revenue));
            }

            // Sort by revenue descending
            outletsWithRevenue.sort((o1, o2) -> Double.compare(o2.revenue, o1.revenue));

            // Return top outlets
            List<Outlet> topOutlets = new ArrayList<>();
            for (int i = 0; i < Math.min(limit, outletsWithRevenue.size()); i++) {
                topOutlets.add(outletsWithRevenue.get(i).outlet);
            }

            return topOutlets;
        } catch (Exception e) {
            System.err.println("Error getting top outlets: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private double calculateOutletRevenue(int outletId) {
        try {
            List<Transaksi> outletTransactions = transaksiDao.getTransaksiByOutlet(outletId);
            double revenue = 0;

            for (Transaksi transaksi : outletTransactions) {
                if ("dibayar".equals(transaksi.getDibayar())) {
                    revenue += transaksi.getTotal();
                }
            }

            return revenue;
        } catch (Exception e) {
            System.err.println("Error calculating outlet revenue: " + e.getMessage());
            return 0;
        }
    }

    private void initializeEmptyOwnerDashboardData(Map<String, Object> data) {
        initializeEmptyStatistics(data);
        data.put("recentTransactions", new ArrayList<>());
        data.put("weeklyRevenue", new ArrayList<>());
        data.put("topOutlets", new ArrayList<>());
        data.put("user", AuthService.getCurrentUser());
    }

    private void initializeEmptyStatistics(Map<String, Object> stats) {
        stats.put("totalRevenue", 0.0);
        stats.put("monthlyRevenue", 0.0);
        stats.put("totalMembers", 0);
        stats.put("totalTransactions", 0);
        stats.put("todayTransactions", 0);
        stats.put("completedTransactions", 0);
        stats.put("pendingPayments", 0);
        stats.put("pendingTransactions", 0);
        stats.put("processingTransactions", 0);
    }

    // Helper class for sorting outlets by revenue
    private class OutletWithRevenue {
        Outlet outlet;
        double revenue;

        OutletWithRevenue(Outlet outlet, double revenue) {
            this.outlet = outlet;
            this.revenue = revenue;
        }
    }
}
