package Service;

import Dao.PaketDao;
import Dao.OutletDao;
import Model.Paket;
import Model.Outlet;
import javax.swing.*;
import java.util.Arrays;
import java.util.List;

public class PaketService extends BaseService {
    private PaketDao paketDao;
    private OutletDao outletDao;

    public PaketService() {
        super();
        this.paketDao = new PaketDao();
        this.outletDao = new OutletDao();
    }

    // === GET ALL PAKET ===
    public List<Paket> getAllPaket() {
        try {
            List<Paket> pakets = paketDao.getAllPaket();
            logView("daftar paket", "Total: " + pakets.size());
            return pakets;
        } catch (Exception e) {
            System.err.println("Error getting all paket: " + e.getMessage());
            return null;
        }
    }

    // === GET PAKET BY ID ===
    public Paket getPaketById(int id) {
        try {
            Paket paket = paketDao.getPaketById(id);
            if (paket != null) {
                logView("detail paket", "ID: " + id + ", Nama: " + paket.getNamaPaket());
            }
            return paket;
        } catch (Exception e) {
            System.err.println("Error getting paket by id: " + e.getMessage());
            return null;
        }
    }

    // === GET PAKET BY OUTLET ===
    public List<Paket> getPaketByOutlet(int outletId) {
        try {
            List<Paket> pakets = paketDao.getPaketByOutlet(outletId);
            logActivity("Melihat paket by outlet", "Outlet ID: " + outletId + ", Total: " + pakets.size());
            return pakets;
        } catch (Exception e) {
            System.err.println("Error getting paket by outlet: " + e.getMessage());
            return null;
        }
    }

    // === GET PAKET BY JENIS ===
    public List<Paket> getPaketByJenis(int outletId, String jenis) {
        try {
            List<Paket> pakets = paketDao.getPaketByJenis(outletId, jenis);
            logActivity("Melihat paket by jenis", "Outlet: " + outletId + ", Jenis: " + jenis + ", Total: " + pakets.size());
            return pakets;
        } catch (Exception e) {
            System.err.println("Error getting paket by jenis: " + e.getMessage());
            return null;
        }
    }

    // === CREATE PAKET ===
    public boolean createPaket(int idOutlet, String jenis, String namaPaket, int harga, JFrame parentFrame) {
        // Validasi input menggunakan BaseService
        if (!validatePaketInput(idOutlet, jenis, namaPaket, harga, -1, parentFrame)) {
            return false;
        }

        try {
            // Cek duplikasi nama paket di outlet yang sama
            if (isPaketDuplicate(idOutlet, namaPaket, -1)) {
                Outlet outlet = outletDao.getOutletById(idOutlet);
                String outletName = outlet != null ? outlet.getNama() : "Outlet";
                showError(parentFrame,
                        String.format("Nama paket '%s' sudah digunakan di outlet %s!", namaPaket, outletName));
                return false;
            }

            // Buat paket baru
            Paket paket = new Paket();
            paket.setIdOutlet(idOutlet);
            paket.setJenis(jenis);
            paket.setNamaPaket(namaPaket.trim());
            paket.setHarga(harga);

            // Simpan paket
            boolean success = paketDao.insertPaket(paket);
            if (success) {
                // Log aktivitas menggunakan BaseService
                logCreate("paket baru", "Nama: " + paket.getNamaPaket() + ", Harga: " + FormatHelper.rupiah(paket.getHarga()));

                showSuccess(parentFrame, "Paket berhasil ditambahkan.");
                return true;
            } else {
                showError(parentFrame, "Gagal menambahkan paket");
                return false;
            }

        } catch (Exception e) {
            showError(parentFrame, "Gagal menambahkan paket: " + e.getMessage());
            return false;
        }
    }

    // === UPDATE PAKET ===
    public boolean updatePaket(int id, int idOutlet, String jenis, String namaPaket, int harga, JFrame parentFrame) {
        // Validasi ID
        Paket existingPaket = paketDao.getPaketById(id);
        if (existingPaket == null) {
            showError(parentFrame, "Paket tidak ditemukan");
            return false;
        }

        // Validasi input menggunakan BaseService
        if (!validatePaketInput(idOutlet, jenis, namaPaket, harga, id, parentFrame)) {
            return false;
        }

        try {
            // Cek duplikasi nama paket di outlet yang sama
            if (isPaketDuplicate(idOutlet, namaPaket, id)) {
                Outlet outlet = outletDao.getOutletById(idOutlet);
                String outletName = outlet != null ? outlet.getNama() : "Outlet";
                showError(parentFrame,
                        String.format("Nama paket '%s' sudah digunakan di outlet %s!", namaPaket, outletName));
                return false;
            }

            // Simpan data lama untuk logging
            Paket oldData = new Paket();
            oldData.setIdOutlet(existingPaket.getIdOutlet());
            oldData.setJenis(existingPaket.getJenis());
            oldData.setNamaPaket(existingPaket.getNamaPaket());
            oldData.setHarga(existingPaket.getHarga());

            // Update paket
            existingPaket.setIdOutlet(idOutlet);
            existingPaket.setJenis(jenis);
            existingPaket.setNamaPaket(namaPaket.trim());
            existingPaket.setHarga(harga);

            boolean success = paketDao.updatePaket(existingPaket);
            if (success) {
                // Log aktivitas menggunakan BaseService
                logUpdate("paket",
                        String.format("ID: %d, Sebelum: %s -> Sesudah: %s", id, oldData.getNamaPaket(), existingPaket.getNamaPaket()));

                showSuccess(parentFrame, "Paket berhasil diperbarui.");
                return true;
            } else {
                showError(parentFrame, "Gagal memperbarui paket");
                return false;
            }

        } catch (Exception e) {
            showError(parentFrame, "Gagal memperbarui paket: " + e.getMessage());
            return false;
        }
    }

    // === DELETE PAKET ===
    public boolean deletePaket(int id, JFrame parentFrame) {
        // Validasi ID
        Paket paket = paketDao.getPaketById(id);
        if (paket == null) {
            showError(parentFrame, "Paket tidak ditemukan");
            return false;
        }

        try {
            // Cek apakah paket memiliki transaksi
            if (paket.getTotalTransaksi() > 0) {
                showError(parentFrame, "Tidak dapat menghapus paket karena sudah digunakan dalam transaksi!");
                return false;
            }

            // Konfirmasi penghapusan
            if (!showConfirm(parentFrame, "Apakah Anda yakin ingin menghapus paket " + paket.getNamaPaket() + "?")) {
                return false;
            }

            // Simpan data untuk logging
            String namaPaket = paket.getNamaPaket();
            String outletName = paket.getNamaOutlet();

            // Hapus paket
            boolean success = paketDao.deletePaket(id);
            if (success) {
                // Log aktivitas menggunakan BaseService
                logDelete("paket", "Nama: " + namaPaket + ", Outlet: " + outletName);

                showSuccess(parentFrame, "Paket berhasil dihapus.");
                return true;
            } else {
                showError(parentFrame, "Gagal menghapus paket");
                return false;
            }

        } catch (Exception e) {
            showError(parentFrame, "Gagal menghapus paket: " + e.getMessage());
            return false;
        }
    }

    // === VALIDATION METHODS ===
    private boolean validatePaketInput(int idOutlet, String jenis, String namaPaket, int harga, int paketId, JFrame parentFrame) {
        // Validasi outlet
        if (idOutlet <= 0) {
            showError(parentFrame, "Outlet wajib dipilih");
            return false;
        }

        // Validasi jenis
        if (!validateRequired(jenis, "Jenis paket", parentFrame)) {
            return false;
        }
        if (!Arrays.asList("kiloan", "selimut", "bed_cover", "kaos", "lain").contains(jenis)) {
            showError(parentFrame, "Jenis paket tidak valid");
            return false;
        }

        // Validasi nama paket menggunakan BaseService
        if (!validateRequired(namaPaket, "Nama paket", parentFrame) ||
                !validateMaxLength(namaPaket, 100, "Nama paket", parentFrame)) {
            return false;
        }

        // Validasi harga menggunakan BaseService
        if (!validateRange(harga, 1000, 10000000, "Harga paket", parentFrame)) {
            return false;
        }

        return true;
    }

    private boolean isPaketDuplicate(int outletId, String namaPaket, int excludePaketId) {
        List<Paket> allPakets = paketDao.getAllPaket();
        for (Paket paket : allPakets) {
            if (paket.getIdOutlet() == outletId &&
                    paket.getNamaPaket().equalsIgnoreCase(namaPaket.trim())) {
                if (excludePaketId == -1 || paket.getId() != excludePaketId) {
                    return true;
                }
            }
        }
        return false;
    }

    // === CHECK DUPLICATE ===
    public boolean checkDuplicate(int outletId, String namaPaket, Integer paketId) {
        try {
            if (namaPaket == null || namaPaket.trim().isEmpty()) {
                return false;
            }
            return isPaketDuplicate(outletId, namaPaket.trim(), paketId != null ? paketId : -1);
        } catch (Exception e) {
            System.err.println("Error checking duplicate paket: " + e.getMessage());
            return false;
        }
    }

    public String getDuplicateMessage(int outletId, String namaPaket, Integer paketId) {
        if (checkDuplicate(outletId, namaPaket, paketId)) {
            Outlet outlet = outletDao.getOutletById(outletId);
            String outletName = outlet != null ? outlet.getNama() : "Outlet";
            return String.format("Nama paket '%s' sudah digunakan di outlet %s", namaPaket, outletName);
        }
        return "Nama paket tersedia";
    }

    // === GETTERS FOR COMBOBOX DATA ===
    public String[] getJenisOptions() {
        return new String[]{"kiloan", "selimut", "bed_cover", "kaos", "lain"};
    }

    public String getJenisDisplay(String jenis) {
        switch (jenis) {
            case "kiloan": return "Kiloan";
            case "selimut": return "Selimut";
            case "bed_cover": return "Bed Cover";
            case "kaos": return "Kaos";
            case "lain": return "Lainnya";
            default: return jenis;
        }
    }

    // === VALIDATION FOR SWING COMPONENTS ===
    public boolean validateNamaPaketField(JTextField txtNamaPaket, JFrame parentFrame) {
        String namaPaket = txtNamaPaket.getText().trim();
        if (!validateRequired(namaPaket, "Nama paket", parentFrame) ||
                !validateMaxLength(namaPaket, 100, "Nama paket", parentFrame)) {
            txtNamaPaket.requestFocus();
            return false;
        }
        return true;
    }

    public boolean validateHargaField(JTextField txtHarga, JFrame parentFrame) {
        try {
            String hargaText = txtHarga.getText().trim();
            if (!validateRequired(hargaText, "Harga paket", parentFrame)) {
                txtHarga.requestFocus();
                return false;
            }

            int harga = Integer.parseInt(hargaText.replaceAll("[^0-9]", ""));

            if (!validateRange(harga, 1000, 10000000, "Harga paket", parentFrame)) {
                txtHarga.requestFocus();
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            showError(parentFrame, "Harga harus berupa angka");
            txtHarga.requestFocus();
            return false;
        }
    }

    public int parseHarga(String hargaText) {
        try {
            return Integer.parseInt(hargaText.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
