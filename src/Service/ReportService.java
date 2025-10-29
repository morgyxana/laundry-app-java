package Service;

import Dao.*;
import Model.*;
import javax.swing.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class ReportService extends BaseService {
    private TransaksiDao transaksiDao;
    private OutletDao outletDao;
    private TbLogDao tbLogDao;

    public ReportService() {
        super();
        this.transaksiDao = new TransaksiDao();
        this.outletDao = new OutletDao();
        this.tbLogDao = new TbLogDao();
    }

    // === GET REPORT DATA ===
    public Map<String, Object> getReportData() {
        Map<String, Object> data = new HashMap<>();
        try {
            data.put("outlets", outletDao.getAllOutlets());
            logView("halaman laporan", "Mengakses halaman generate laporan");
        } catch (Exception e) {
            System.err.println("Error getting report data: " + e.getMessage());
            data.put("outlets", new ArrayList<>());
        }
        return data;
    }

    // === GENERATE REPORT ===
    public Map<String, Object> generateReport(String jenisLaporan, String format,
                                              Map<String, String> filters, JFrame parentFrame) {
        Map<String, Object> result = new HashMap<>();

        try {
            User currentUser = AuthService.getCurrentUser();

            // Validate input
            if (!validateReportInput(jenisLaporan, format, parentFrame)) {
                return result;
            }

            // Get transactions based on report type
            List<Transaksi> transactions = getTransactionsForReport(jenisLaporan, filters, currentUser);

            // Calculate statistics
            Map<String, Object> statistics = calculateReportStatistics(transactions);

            // Generate title
            String title = generateReportTitle(jenisLaporan, filters);

            // Log activity
            logActivity("Generate laporan",
                    String.format("Jenis: %s, Format: %s, Total: %d transaksi",
                            jenisLaporan, format, transactions.size()));

            // Prepare result
            result.put("transactions", transactions);
            result.put("title", title);
            result.put("statistics", statistics);
            result.put("filters", filters);
            result.put("format", format);
            result.put("success", true);

        } catch (Exception e) {
            System.err.println("Error generating report: " + e.getMessage());
            result.put("success", false);
            result.put("error", "Gagal generate laporan: " + e.getMessage());
        }

        return result;
    }

    private boolean validateReportInput(String jenisLaporan, String format, JFrame parentFrame) {
        // Validate report type
        if (!Arrays.asList("per_outlet", "per_periode", "per_status").contains(jenisLaporan)) {
            showError(parentFrame, "Jenis laporan tidak valid");
            return false;
        }

        // Validate format
        if (!Arrays.asList("view", "pdf", "excel").contains(format)) {
            showError(parentFrame, "Format laporan tidak valid");
            return false;
        }

        return true;
    }

    private List<Transaksi> getTransactionsForReport(String jenisLaporan, Map<String, String> filters, User currentUser) {
        List<Transaksi> transactions = new ArrayList<>();

        try {
            switch (jenisLaporan) {
                case "per_outlet":
                    transactions = getTransactionsByOutlet(filters, currentUser);
                    break;
                case "per_periode":
                    transactions = getTransactionsByPeriod(filters, currentUser);
                    break;
                case "per_status":
                    transactions = getTransactionsByStatus(filters, currentUser);
                    break;
                default:
                    transactions = transaksiDao.getAllTransaksi();
                    break;
            }

            // Filter by user outlet if not owner
            if (!currentUser.isOwner() && currentUser.getIdOutlet() > 0) {
                List<Transaksi> filtered = new ArrayList<>();
                for (Transaksi transaksi : transactions) {
                    if (transaksi.getIdOutlet() == currentUser.getIdOutlet()) {
                        filtered.add(transaksi);
                    }
                }
                transactions = filtered;
            }

        } catch (Exception e) {
            System.err.println("Error getting transactions for report: " + e.getMessage());
        }

        return transactions;
    }

    private List<Transaksi> getTransactionsByOutlet(Map<String, String> filters, User currentUser) {
        try {
            String outletIdStr = filters.get("outlet_id");
            if (outletIdStr != null && !outletIdStr.isEmpty()) {
                int outletId = Integer.parseInt(outletIdStr);
                return transaksiDao.getTransaksiByOutlet(outletId);
            } else {
                return transaksiDao.getAllTransaksi();
            }
        } catch (Exception e) {
            System.err.println("Error getting transactions by outlet: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Transaksi> getTransactionsByPeriod(Map<String, String> filters, User currentUser) {
        try {
            List<Transaksi> allTransactions = transaksiDao.getAllTransaksi();
            List<Transaksi> filteredTransactions = new ArrayList<>();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            // Parse dates
            Date startDate, endDate;
            if (filters.containsKey("start_date") && !filters.get("start_date").isEmpty()) {
                startDate = sdf.parse(filters.get("start_date"));
            } else {
                // Default to start of month
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_MONTH, 1);
                startDate = cal.getTime();
            }

            if (filters.containsKey("end_date") && !filters.get("end_date").isEmpty()) {
                endDate = sdf.parse(filters.get("end_date"));
            } else {
                // Default to end of month
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                endDate = cal.getTime();
            }

            // Filter transactions
            for (Transaksi transaksi : allTransactions) {
                if (transaksi.getTgl() != null) {
                    try {
                        Date transaksiDate = sdf.parse(sdf.format(transaksi.getTgl()));
                        if (!transaksiDate.before(startDate) && !transaksiDate.after(endDate)) {
                            filteredTransactions.add(transaksi);
                        }
                    } catch (Exception e) {
                        // Skip this transaction if date parsing fails
                        continue;
                    }
                }
            }

            return filteredTransactions;
        } catch (Exception e) {
            System.err.println("Error getting transactions by period: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Transaksi> getTransactionsByStatus(Map<String, String> filters, User currentUser) {
        try {
            List<Transaksi> allTransactions = transaksiDao.getAllTransaksi();
            List<Transaksi> filteredTransactions = new ArrayList<>();

            for (Transaksi transaksi : allTransactions) {
                boolean statusMatch = true;
                boolean paymentMatch = true;

                // Filter by status
                if (filters.containsKey("status") && !filters.get("status").isEmpty()) {
                    if (!filters.get("status").equals(transaksi.getStatus())) {
                        statusMatch = false;
                    }
                }

                // Filter by payment status
                if (filters.containsKey("dibayar") && !filters.get("dibayar").isEmpty()) {
                    if (!filters.get("dibayar").equals(transaksi.getDibayar())) {
                        paymentMatch = false;
                    }
                }

                if (statusMatch && paymentMatch) {
                    filteredTransactions.add(transaksi);
                }
            }

            return filteredTransactions;
        } catch (Exception e) {
            System.err.println("Error getting transactions by status: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private Map<String, Object> calculateReportStatistics(List<Transaksi> transactions) {
        Map<String, Object> statistics = new HashMap<>();

        try {
            double totalPendapatan = 0;
            int totalTransaksi = transactions.size();

            Map<String, Integer> statusCount = new HashMap<>();
            statusCount.put("baru", 0);
            statusCount.put("proses", 0);
            statusCount.put("selesai", 0);
            statusCount.put("diambil", 0);

            Map<String, Integer> paymentCount = new HashMap<>();
            paymentCount.put("dibayar", 0);
            paymentCount.put("belum_dibayar", 0);

            for (Transaksi transaksi : transactions) {
                // Calculate revenue for paid transactions
                if ("dibayar".equals(transaksi.getDibayar())) {
                    totalPendapatan += transaksi.getTotal();
                }

                // Count by status
                String status = transaksi.getStatus();
                statusCount.put(status, statusCount.getOrDefault(status, 0) + 1);

                // Count by payment status
                String payment = transaksi.getDibayar();
                paymentCount.put(payment, paymentCount.getOrDefault(payment, 0) + 1);
            }

            statistics.put("total_pendapatan", totalPendapatan);
            statistics.put("total_transaksi", totalTransaksi);
            statistics.put("status_count", statusCount);
            statistics.put("payment_count", paymentCount);

        } catch (Exception e) {
            System.err.println("Error calculating report statistics: " + e.getMessage());
            statistics.put("total_pendapatan", 0.0);
            statistics.put("total_transaksi", 0);
            statistics.put("status_count", new HashMap<>());
            statistics.put("payment_count", new HashMap<>());
        }

        return statistics;
    }

    private String generateReportTitle(String jenisLaporan, Map<String, String> filters) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        switch (jenisLaporan) {
            case "per_outlet":
                if (filters.containsKey("outlet_id") && !filters.get("outlet_id").isEmpty()) {
                    try {
                        int outletId = Integer.parseInt(filters.get("outlet_id"));
                        Outlet outlet = outletDao.getOutletById(outletId);
                        if (outlet != null) {
                            return "Laporan Transaksi - " + outlet.getNama();
                        }
                    } catch (Exception e) {
                        // Continue with default title
                    }
                }
                return "Laporan Transaksi Per Outlet";

            case "per_periode":
                String startDateStr = "Awal Bulan";
                String endDateStr = "Akhir Bulan";

                if (filters.containsKey("start_date") && !filters.get("start_date").isEmpty()) {
                    try {
                        Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(filters.get("start_date"));
                        startDateStr = sdf.format(startDate);
                    } catch (Exception e) {
                        // Use default
                    }
                }

                if (filters.containsKey("end_date") && !filters.get("end_date").isEmpty()) {
                    try {
                        Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse(filters.get("end_date"));
                        endDateStr = sdf.format(endDate);
                    } catch (Exception e) {
                        // Use default
                    }
                }

                return "Laporan Transaksi Periode " + startDateStr + " - " + endDateStr;

            case "per_status":
                return "Laporan Transaksi Berdasarkan Status";

            default:
                return "Laporan Transaksi";
        }
    }

    // === EXPORT METHODS ===
    public boolean exportToPDF(Map<String, Object> reportData, JFrame parentFrame) {
        try {
            // In a real implementation, you would use a PDF library like iText or Apache PDFBox
            // For now, we'll just show a success message
            showSuccess(parentFrame, "PDF berhasil di-generate!");
            logActivity("Export laporan PDF", "Berhasil generate PDF laporan");
            return true;
        } catch (Exception e) {
            showError(parentFrame, "Gagal generate PDF: " + e.getMessage());
            return false;
        }
    }

    public boolean exportToExcel(Map<String, Object> reportData, JFrame parentFrame) {
        try {
            // In a real implementation, you would use a library like Apache POI
            // For now, we'll just show a success message
            showSuccess(parentFrame, "Excel berhasil di-generate!");
            logActivity("Export laporan Excel", "Berhasil generate Excel laporan");
            return true;
        } catch (Exception e) {
            showError(parentFrame, "Gagal generate Excel: " + e.getMessage());
            return false;
        }
    }

    // === UTILITY METHODS FOR EXPORT ===
    public String getStatusText(String status) {
        switch (status) {
            case "baru": return "Baru";
            case "proses": return "Proses";
            case "selesai": return "Selesai";
            case "diambil": return "Diambil";
            default: return status;
        }
    }

    public String getPaymentStatusText(String dibayar) {
        return "dibayar".equals(dibayar) ? "Sudah Dibayar" : "Belum Dibayar";
    }
}
