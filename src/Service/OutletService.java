package Service;

import Dao.OutletDao;
import Dao.UserDao;
import Dao.TransaksiDao;
import Model.Outlet;
import Model.User;
import javax.swing.*;
import java.util.List;
import java.util.regex.Pattern;

public class OutletService extends BaseService {
    private OutletDao outletDao;
    private UserDao userDao;
    private TransaksiDao transaksiDao;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]+$");

    public OutletService() {
        super();
        this.outletDao = new OutletDao();
        this.userDao = new UserDao();
        this.transaksiDao = new TransaksiDao();
    }

    // === GET ALL OUTLETS ===
    public List<Outlet> getAllOutlets() {
        try {
            List<Outlet> outlets = outletDao.getAllOutlets();

            // Log aktivitas menggunakan BaseService
            logView("daftar outlet", "Total: " + outlets.size());

            return outlets;
        } catch (Exception e) {
            System.err.println("Error getting all outlets: " + e.getMessage());
            return null;
        }
    }

    // === GET OUTLET BY ID ===
    public Outlet getOutletById(int id) {
        try {
            Outlet outlet = outletDao.getOutletById(id);
            if (outlet != null) {
                // Log aktivitas menggunakan BaseService
                logView("detail outlet", "ID: " + id + ", Nama: " + outlet.getNama());
            }
            return outlet;
        } catch (Exception e) {
            System.err.println("Error getting outlet by id: " + e.getMessage());
            return null;
        }
    }

    // === CREATE OUTLET ===
    public boolean createOutlet(String nama, String alamat, String tlp, JFrame parentFrame) {
        // Cek authorization - hanya admin yang bisa buat outlet
        if (!checkRole("admin", parentFrame)) {
            return false;
        }

        // Validasi input menggunakan BaseService
        if (!validateOutletInput(nama, alamat, tlp, -1, parentFrame)) {
            return false;
        }

        try {
            // Cek duplikasi nama
            if (isOutletNameDuplicate(nama, -1)) {
                showError(parentFrame, "Nama outlet sudah digunakan");
                return false;
            }

            // Cek duplikasi telepon
            if (isOutletPhoneDuplicate(tlp, -1)) {
                showError(parentFrame, "Nomor telepon sudah digunakan");
                return false;
            }

            // Buat outlet baru
            Outlet outlet = new Outlet();
            outlet.setNama(nama.trim());
            outlet.setAlamat(alamat.trim());
            outlet.setTlp(tlp.trim());
            outlet.setCreatedAt(getCurrentTimestamp());
            outlet.setUpdatedAt(getCurrentTimestamp());

            // Simpan outlet
            boolean success = outletDao.insertOutlet(outlet);
            if (success) {
                // Log aktivitas menggunakan BaseService
                logCreate("outlet baru", "Nama: " + outlet.getNama());

                showSuccess(parentFrame, "Outlet berhasil ditambahkan!");
                return true;
            } else {
                showError(parentFrame, "Gagal menambahkan outlet");
                return false;
            }

        } catch (Exception e) {
            showError(parentFrame, "Gagal menambahkan outlet: " + e.getMessage());
            return false;
        }
    }

    // === UPDATE OUTLET ===
    public boolean updateOutlet(int id, String nama, String alamat, String tlp, JFrame parentFrame) {
        // Cek authorization - hanya admin yang bisa update outlet
        if (!checkRole("admin", parentFrame)) {
            return false;
        }

        // Validasi ID
        Outlet existingOutlet = outletDao.getOutletById(id);
        if (existingOutlet == null) {
            showError(parentFrame, "Outlet tidak ditemukan");
            return false;
        }

        // Validasi input menggunakan BaseService
        if (!validateOutletInput(nama, alamat, tlp, id, parentFrame)) {
            return false;
        }

        try {
            // Simpan data lama untuk logging
            Outlet oldData = new Outlet();
            oldData.setNama(existingOutlet.getNama());
            oldData.setAlamat(existingOutlet.getAlamat());
            oldData.setTlp(existingOutlet.getTlp());

            // Update outlet
            existingOutlet.setNama(nama.trim());
            existingOutlet.setAlamat(alamat.trim());
            existingOutlet.setTlp(tlp.trim());
            existingOutlet.setUpdatedAt(getCurrentTimestamp());

            boolean success = outletDao.updateOutlet(existingOutlet);
            if (success) {
                // Log aktivitas menggunakan BaseService
                logUpdate("outlet",
                        String.format("ID: %d, Sebelum: %s -> Sesudah: %s", id, oldData.getNama(), existingOutlet.getNama()));

                showSuccess(parentFrame, "Outlet berhasil diperbarui!");
                return true;
            } else {
                showError(parentFrame, "Gagal memperbarui outlet");
                return false;
            }

        } catch (Exception e) {
            showError(parentFrame, "Gagal memperbarui outlet: " + e.getMessage());
            return false;
        }
    }

    // === DELETE OUTLET ===
    public boolean deleteOutlet(int id, JFrame parentFrame) {
        // Cek authorization - hanya admin yang bisa hapus outlet
        if (!checkRole("admin", parentFrame)) {
            return false;
        }

        // Validasi ID
        Outlet outlet = outletDao.getOutletById(id);
        if (outlet == null) {
            showError(parentFrame, "Outlet tidak ditemukan");
            return false;
        }

        try {
            // Cek apakah outlet memiliki users
            int userCount = getUsersCountByOutlet(id);
            if (userCount > 0) {
                showError(parentFrame,
                        String.format("Tidak dapat menghapus outlet karena masih memiliki %d user terkait.", userCount));
                return false;
            }

            // Cek apakah outlet memiliki transaksi
            int transaksiCount = getTransactionsCountByOutlet(id);
            if (transaksiCount > 0) {
                showError(parentFrame,
                        String.format("Tidak dapat menghapus outlet karena masih memiliki %d transaksi terkait.", transaksiCount));
                return false;
            }

            // Konfirmasi penghapusan
            if (!showConfirm(parentFrame, "Apakah Anda yakin ingin menghapus outlet " + outlet.getNama() + "?")) {
                return false;
            }

            // Simpan data untuk logging
            String outletName = outlet.getNama();
            int outletId = outlet.getId();

            // Hapus outlet
            boolean success = outletDao.deleteOutlet(id);
            if (success) {
                // Log aktivitas menggunakan BaseService
                logDelete("outlet", "Nama: " + outletName);

                showSuccess(parentFrame, "Outlet berhasil dihapus!");
                return true;
            } else {
                showError(parentFrame, "Gagal menghapus outlet");
                return false;
            }

        } catch (Exception e) {
            showError(parentFrame, "Gagal menghapus outlet: " + e.getMessage());
            return false;
        }
    }

    // === SEARCH OUTLETS ===
    public List<Outlet> searchOutlets(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return outletDao.getAllOutlets();
            }
            List<Outlet> results = outletDao.searchOutlets(keyword.trim());
            logActivity("Pencarian outlet", "Keyword: " + keyword + ", Hasil: " + results.size());
            return results;
        } catch (Exception e) {
            System.err.println("Error searching outlets: " + e.getMessage());
            return null;
        }
    }

    // === VALIDATION METHODS ===
    private boolean validateOutletInput(String nama, String alamat, String tlp, int outletId, JFrame parentFrame) {
        // Validasi nama menggunakan BaseService
        if (!validateRequired(nama, "Nama outlet", parentFrame) ||
                !validateMaxLength(nama, 100, "Nama outlet", parentFrame)) {
            return false;
        }
        if (isOutletNameDuplicate(nama.trim(), outletId)) {
            showError(parentFrame, "Nama outlet sudah digunakan");
            return false;
        }

        // Validasi alamat menggunakan BaseService
        if (!validateRequired(alamat, "Alamat outlet", parentFrame) ||
                !validateMinLength(alamat, 10, "Alamat outlet", parentFrame)) {
            return false;
        }

        // Validasi telepon menggunakan BaseService
        if (!validateRequired(tlp, "Nomor telepon", parentFrame) ||
                !validateMaxLength(tlp, 15, "Nomor telepon", parentFrame)) {
            return false;
        }
        if (!PHONE_PATTERN.matcher(tlp.trim()).matches()) {
            showError(parentFrame, "Nomor telepon hanya boleh mengandung angka");
            return false;
        }
        if (isOutletPhoneDuplicate(tlp.trim(), outletId)) {
            showError(parentFrame, "Nomor telepon sudah digunakan");
            return false;
        }

        return true;
    }

    private boolean isOutletNameDuplicate(String nama, int excludeOutletId) {
        List<Outlet> allOutlets = outletDao.getAllOutlets();
        for (Outlet outlet : allOutlets) {
            if (outlet.getNama().equalsIgnoreCase(nama)) {
                if (excludeOutletId == -1 || outlet.getId() != excludeOutletId) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isOutletPhoneDuplicate(String tlp, int excludeOutletId) {
        List<Outlet> allOutlets = outletDao.getAllOutlets();
        for (Outlet outlet : allOutlets) {
            if (outlet.getTlp().equals(tlp)) {
                if (excludeOutletId == -1 || outlet.getId() != excludeOutletId) {
                    return true;
                }
            }
        }
        return false;
    }

    // === COUNT METHODS ===
    private int getUsersCountByOutlet(int outletId) {
        try {
            List<User> users = userDao.getUsersByOutlet(outletId);
            return users != null ? users.size() : 0;
        } catch (Exception e) {
            System.err.println("Error getting users count by outlet: " + e.getMessage());
            return 0;
        }
    }

    private int getTransactionsCountByOutlet(int outletId) {
        try {
            return transaksiDao.getTotalTransaksiByOutlet(outletId);
        } catch (Exception e) {
            System.err.println("Error getting transactions count by outlet: " + e.getMessage());
            return 0;
        }
    }

    // === GETTERS FOR STATISTICS ===
    public int getTotalOutlets() {
        try {
            return outletDao.getTotalOutlets();
        } catch (Exception e) {
            System.err.println("Error getting total outlets: " + e.getMessage());
            return 0;
        }
    }

    public List<Outlet> getRecentOutlets(int limit) {
        try {
            List<Outlet> allOutlets = outletDao.getAllOutlets();
            if (allOutlets.size() > limit) {
                return allOutlets.subList(0, limit);
            }
            return allOutlets;
        } catch (Exception e) {
            System.err.println("Error getting recent outlets: " + e.getMessage());
            return null;
        }
    }

    // === VALIDATION FOR SWING COMPONENTS ===
    public boolean validateNamaField(JTextField txtNama, JFrame parentFrame) {
        String nama = txtNama.getText().trim();
        if (!validateRequired(nama, "Nama outlet", parentFrame) ||
                !validateMaxLength(nama, 100, "Nama outlet", parentFrame)) {
            txtNama.requestFocus();
            return false;
        }
        return true;
    }

    public boolean validateAlamatField(JTextArea txtAlamat, JFrame parentFrame) {
        String alamat = txtAlamat.getText().trim();
        if (!validateRequired(alamat, "Alamat outlet", parentFrame) ||
                !validateMinLength(alamat, 10, "Alamat outlet", parentFrame)) {
            txtAlamat.requestFocus();
            return false;
        }
        return true;
    }

    public boolean validateTeleponField(JTextField txtTlp, JFrame parentFrame) {
        String tlp = txtTlp.getText().trim();
        if (!validateRequired(tlp, "Nomor telepon", parentFrame) ||
                !validateMaxLength(tlp, 15, "Nomor telepon", parentFrame)) {
            txtTlp.requestFocus();
            return false;
        }
        if (!PHONE_PATTERN.matcher(tlp).matches()) {
            showError(parentFrame, "Nomor telepon hanya boleh mengandung angka");
            txtTlp.requestFocus();
            return false;
        }
        return true;
    }

    // === CHECK DUPLICATE METHODS FOR REAL-TIME VALIDATION ===
    public boolean checkDuplicateNama(String nama, Integer outletId) {
        try {
            if (nama == null || nama.trim().isEmpty()) {
                return false;
            }
            return isOutletNameDuplicate(nama.trim(), outletId != null ? outletId : -1);
        } catch (Exception e) {
            System.err.println("Error checking duplicate nama: " + e.getMessage());
            return false;
        }
    }

    public boolean checkDuplicateTelepon(String tlp, Integer outletId) {
        try {
            if (tlp == null || tlp.trim().isEmpty()) {
                return false;
            }
            return isOutletPhoneDuplicate(tlp.trim(), outletId != null ? outletId : -1);
        } catch (Exception e) {
            System.err.println("Error checking duplicate telepon: " + e.getMessage());
            return false;
        }
    }

    public String getDuplicateNamaMessage(String nama, Integer outletId) {
        if (checkDuplicateNama(nama, outletId)) {
            return "Nama outlet sudah digunakan";
        }
        return "Nama outlet tersedia";
    }

    public String getDuplicateTeleponMessage(String tlp, Integer outletId) {
        if (checkDuplicateTelepon(tlp, outletId)) {
            return "Nomor telepon sudah digunakan";
        }
        return "Nomor telepon tersedia";
    }

    // === GET OUTLET DETAILS WITH STATISTICS ===
    public Outlet getOutletDetailWithStats(int outletId) {
        try {
            Outlet outlet = outletDao.getOutletById(outletId);
            if (outlet != null) {
                // Hitung jumlah user
                int userCount = getUsersCountByOutlet(outletId);

                // Hitung jumlah transaksi
                int transaksiCount = getTransactionsCountByOutlet(outletId);

                // Set statistics (bisa ditambahkan field di model Outlet jika diperlukan)
                return outlet;
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error getting outlet detail with stats: " + e.getMessage());
            return null;
        }
    }

    public int getOutletUserCount(int outletId) {
        return getUsersCountByOutlet(outletId);
    }

    public int getOutletTransactionCount(int outletId) {
        return getTransactionsCountByOutlet(outletId);
    }
}