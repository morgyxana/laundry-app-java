package Service;

import Dao.TransaksiDao;
import Dao.DetailTransaksiDao;
import Dao.MemberDao;
import Dao.PaketDao;
import Dao.OutletDao;
import Dao.UserDao;
import Model.Transaksi;
import Model.DetailTransaksi;
import Model.Member;
import Model.Paket;
import Model.Outlet;
import Model.User;
import javax.swing.*;
import java.util.*;
import java.util.regex.Pattern;

public class TransaksiService extends BaseService {
    private TransaksiDao transaksiDao;
    private DetailTransaksiDao detailTransaksiDao;
    private MemberDao memberDao;
    private PaketDao paketDao;
    private OutletDao outletDao;
    private UserDao userDao;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]+$");

    public TransaksiService() {
        super();
        this.transaksiDao = new TransaksiDao();
        this.detailTransaksiDao = new DetailTransaksiDao();
        this.memberDao = new MemberDao();
        this.paketDao = new PaketDao();
        this.outletDao = new OutletDao();
        this.userDao = new UserDao();
    }

    // === GET ALL TRANSACTIONS ===
    public List<Transaksi> getAllTransaksi() {
        try {
            User currentUser = AuthService.getCurrentUser();
            List<Transaksi> transaksi;

            if (currentUser.isAdmin() || currentUser.isKasir()) {
                transaksi = transaksiDao.getAllTransaksi();
            } else {
                transaksi = transaksiDao.getTransaksiByOutlet(currentUser.getIdOutlet());
            }

            logView("daftar transaksi", "Total: " + transaksi.size());
            return transaksi;
        } catch (Exception e) {
            System.err.println("Error getting all transaksi: " + e.getMessage());
            return null;
        }
    }

    // === GET TRANSACTION BY ID ===
    public Transaksi getTransaksiById(int id) {
        try {
            Transaksi transaksi = transaksiDao.getTransaksiById(id);
            if (transaksi != null) {
                logView("detail transaksi", "Kode Invoice: " + transaksi.getKodeInvoice());
            }
            return transaksi;
        } catch (Exception e) {
            System.err.println("Error getting transaksi by id: " + e.getMessage());
            return null;
        }
    }

    // === CREATE TRANSACTION ===
    public boolean createTransaksi(int idOutlet, String tipePelanggan, Integer idMember,
                                   String namaPelanggan, String tlpPelanggan, int durasi,
                                   int biayaTambahan, double diskon, int pajak,
                                   List<Integer> idPaketList, List<Double> qtyList,
                                   List<Double> beratList, List<String> keteranganList,
                                   JFrame parentFrame) {
        // Validasi input menggunakan BaseService
        if (!validateTransaksiInput(idOutlet, tipePelanggan, idMember, namaPelanggan, tlpPelanggan,
                durasi, biayaTambahan, diskon, pajak, idPaketList, qtyList,
                beratList, parentFrame)) {
            return false;
        }

        try {
            // Generate kode invoice
            String kodeInvoice = transaksiDao.generateKodeInvoice();

            // Hitung total dan buat detail transaksi
            double totalBiayaPaket = 0;
            List<DetailTransaksi> detailTransaksiList = new ArrayList<>();

            for (int i = 0; i < idPaketList.size(); i++) {
                int idPaket = idPaketList.get(i);
                Paket paket = paketDao.getPaketById(idPaket);

                if (paket == null) {
                    throw new Exception("Paket dengan ID " + idPaket + " tidak ditemukan");
                }

                double qty, beratValue;
                if ("kiloan".equals(paket.getJenis())) {
                    beratValue = beratList.get(i) != null ? beratList.get(i) : qtyList.get(i);
                    qty = beratValue;
                } else {
                    qty = qtyList.get(i);
                    beratValue = 0;
                }

                double subtotal = paket.getHarga() * qty;
                totalBiayaPaket += subtotal;

                DetailTransaksi detail = new DetailTransaksi();
                detail.setIdPaket(idPaket);
                detail.setQty(qty);
                detail.setBerat(beratValue);
                detail.setKeterangan(keteranganList.get(i));
                detailTransaksiList.add(detail);
            }

            // Hitung total akhir
            double diskonDecimal = diskon / 100.0;
            double totalAkhir = totalBiayaPaket + biayaTambahan - (totalBiayaPaket * diskonDecimal) + pajak;

            // Buat transaksi
            Transaksi transaksi = new Transaksi();
            transaksi.setIdOutlet(idOutlet);
            transaksi.setKodeInvoice(kodeInvoice);
            transaksi.setTipePelanggan(tipePelanggan);
            transaksi.setTgl(getCurrentTimestamp());
            transaksi.setBatasWaktu(addDays(getCurrentTimestamp(), durasi));
            transaksi.setBiayaTambahan(biayaTambahan);
            transaksi.setDiskon(diskonDecimal);
            transaksi.setPajak(pajak);
            transaksi.setStatus("baru");
            transaksi.setDibayar("belum_dibayar");
            transaksi.setIdUser(AuthService.getCurrentUser().getId());

            if ("member".equals(tipePelanggan)) {
                transaksi.setIdMember(idMember);
                transaksi.setNamaPelanggan(null);
                transaksi.setTlpPelanggan(null);
            } else {
                transaksi.setIdMember(null);
                transaksi.setNamaPelanggan(namaPelanggan);
                transaksi.setTlpPelanggan(tlpPelanggan);
            }

            transaksi.setDetailTransaksi(detailTransaksiList);

            // Simpan transaksi
            boolean success = transaksiDao.insertTransaksi(transaksi);
            if (success) {
                // Log aktivitas menggunakan BaseService
                logCreate("transaksi baru",
                        String.format("Kode: %s, Total: %s", kodeInvoice, FormatHelper.rupiah((int) totalAkhir)));

                showSuccess(parentFrame, "Transaksi berhasil dibuat! Kode Invoice: " + kodeInvoice);
                return true;
            } else {
                showError(parentFrame, "Gagal membuat transaksi");
                return false;
            }

        } catch (Exception e) {
            showError(parentFrame, "Terjadi kesalahan: " + e.getMessage());
            return false;
        }
    }

    // === UPDATE STATUS ===
    public boolean updateStatus(int id, String status, String dibayar, JFrame parentFrame) {
        try {
            Transaksi transaksi = transaksiDao.getTransaksiById(id);
            if (transaksi == null) {
                showError(parentFrame, "Transaksi tidak ditemukan");
                return false;
            }

            // Validasi status dan pembayaran
            if (!Arrays.asList("baru", "proses", "selesai", "diambil").contains(status)) {
                showError(parentFrame, "Status tidak valid");
                return false;
            }
            if (!Arrays.asList("dibayar", "belum_dibayar").contains(dibayar)) {
                showError(parentFrame, "Status pembayaran tidak valid");
                return false;
            }

            // Update data
            String oldStatus = transaksi.getStatus();
            String oldDibayar = transaksi.getDibayar();

            transaksi.setStatus(status);
            transaksi.setDibayar(dibayar);

            if ("dibayar".equals(dibayar) && !"dibayar".equals(oldDibayar)) {
                transaksi.setTglBayar(getCurrentTimestamp());
            } else if ("belum_dibayar".equals(dibayar)) {
                transaksi.setTglBayar(null);
            }

            boolean success = transaksiDao.updateTransaksi(transaksi);
            if (success) {
                // Log aktivitas menggunakan BaseService
                logUpdate("status transaksi",
                        String.format("Kode: %s, Status: %s->%s, Bayar: %s->%s",
                                transaksi.getKodeInvoice(), oldStatus, status, oldDibayar, dibayar));

                showSuccess(parentFrame, "Status transaksi berhasil diupdate!");
                return true;
            } else {
                showError(parentFrame, "Gagal mengupdate status transaksi");
                return false;
            }

        } catch (Exception e) {
            showError(parentFrame, "Gagal mengupdate status: " + e.getMessage());
            return false;
        }
    }

    // === QUICK UPDATE STATUS ===
    public boolean updateStatusQuick(int id, String field, String value, JFrame parentFrame) {
        try {
            Transaksi transaksi = transaksiDao.getTransaksiById(id);
            if (transaksi == null) {
                showError(parentFrame, "Transaksi tidak ditemukan");
                return false;
            }

            // Validasi field dan value
            if (!"status".equals(field) && !"dibayar".equals(field)) {
                showError(parentFrame, "Field tidak valid");
                return false;
            }

            if ("status".equals(field) && !Arrays.asList("baru", "proses", "selesai", "diambil").contains(value)) {
                showError(parentFrame, "Status tidak valid");
                return false;
            }

            if ("dibayar".equals(field) && !Arrays.asList("dibayar", "belum_dibayar").contains(value)) {
                showError(parentFrame, "Status pembayaran tidak valid");
                return false;
            }

            // Update data
            String oldValue = "status".equals(field) ? transaksi.getStatus() : transaksi.getDibayar();

            if ("status".equals(field)) {
                transaksi.setStatus(value);
            } else {
                transaksi.setDibayar(value);

                if ("dibayar".equals(value) && !"dibayar".equals(oldValue)) {
                    transaksi.setTglBayar(getCurrentTimestamp());
                } else if ("belum_dibayar".equals(value)) {
                    transaksi.setTglBayar(null);
                }
            }

            boolean success = transaksiDao.updateTransaksi(transaksi);
            if (success) {
                // Log aktivitas menggunakan BaseService
                logUpdate("status transaksi cepat",
                        String.format("Kode: %s, %s: %s->%s",
                                transaksi.getKodeInvoice(), field, oldValue, value));

                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            showError(parentFrame, "Gagal mengupdate status: " + e.getMessage());
            return false;
        }
    }

    // === VALIDATION METHODS ===
    private boolean validateTransaksiInput(int idOutlet, String tipePelanggan, Integer idMember,
                                           String namaPelanggan, String tlpPelanggan, int durasi,
                                           int biayaTambahan, double diskon, int pajak,
                                           List<Integer> idPaketList, List<Double> qtyList,
                                           List<Double> beratList, JFrame parentFrame) {
        // Validasi outlet
        if (idOutlet <= 0) {
            showError(parentFrame, "Outlet wajib dipilih");
            return false;
        }

        // Validasi tipe pelanggan
        if (!Arrays.asList("member", "biasa").contains(tipePelanggan)) {
            showError(parentFrame, "Tipe pelanggan harus member atau biasa");
            return false;
        }

        // Validasi member untuk tipe member
        if ("member".equals(tipePelanggan) && (idMember == null || idMember <= 0)) {
            showError(parentFrame, "Member wajib dipilih untuk tipe pelanggan member");
            return false;
        }

        // Validasi nama pelanggan untuk tipe biasa
        if ("biasa".equals(tipePelanggan)) {
            if (!validateRequired(namaPelanggan, "Nama pelanggan", parentFrame) ||
                    !validateMaxLength(namaPelanggan, 100, "Nama pelanggan", parentFrame)) {
                return false;
            }
        }

        // Validasi telepon
        if (tlpPelanggan != null && !tlpPelanggan.trim().isEmpty()) {
            if (!validateMaxLength(tlpPelanggan, 15, "Nomor telepon", parentFrame)) {
                return false;
            }
            if (!PHONE_PATTERN.matcher(tlpPelanggan.trim()).matches()) {
                showError(parentFrame, "Nomor telepon hanya boleh mengandung angka");
                return false;
            }
        }

        // Validasi durasi menggunakan BaseService
        if (!validateRange(durasi, 1, 30, "Durasi", parentFrame)) {
            return false;
        }

        // Validasi biaya tambahan menggunakan BaseService
        if (!validateRange(biayaTambahan, 0, 1000000, "Biaya tambahan", parentFrame)) {
            return false;
        }

        // Validasi diskon menggunakan BaseService
        if (!validateRange(diskon, 0, 100, "Diskon", parentFrame)) {
            return false;
        }

        // Validasi pajak menggunakan BaseService
        if (!validateRange(pajak, 0, 1000000, "Pajak", parentFrame)) {
            return false;
        }

        // Validasi paket
        if (idPaketList == null || idPaketList.isEmpty()) {
            showError(parentFrame, "Minimal satu paket harus dipilih");
            return false;
        }

        // Validasi quantity
        for (Double qty : qtyList) {
            if (qty == null || !validateRange(qty, 0.1, 100, "Quantity", parentFrame)) {
                return false;
            }
        }

        // Validasi berat
        for (Double berat : beratList) {
            if (berat != null && !validateRange(berat, 0.1, 100, "Berat", parentFrame)) {
                return false;
            }
        }

        return true;
    }

    // === GETTERS FOR COMBOBOX DATA ===
    public String[] getTipePelangganOptions() {
        return new String[]{"member", "biasa"};
    }

    public String[] getStatusOptions() {
        return new String[]{"baru", "proses", "selesai", "diambil"};
    }

    public String[] getDibayarOptions() {
        return new String[]{"belum_dibayar", "dibayar"};
    }

    // === GET PAKET BY OUTLET ===
    public List<Paket> getPaketByOutlet(int outletId) {
        try {
            return paketDao.getPaketByOutlet(outletId);
        } catch (Exception e) {
            System.err.println("Error getting paket by outlet: " + e.getMessage());
            return null;
        }
    }

    // === GET MEMBERS ===
    public List<Member> getAllMembers() {
        try {
            return memberDao.getAllMembers();
        } catch (Exception e) {
            System.err.println("Error getting all members: " + e.getMessage());
            return null;
        }
    }

    // === GET OUTLETS ===
    public List<Outlet> getAvailableOutlets() {
        try {
            User currentUser = AuthService.getCurrentUser();
            if (currentUser.isAdmin() || currentUser.isKasir()) {
                return outletDao.getAllOutlets();
            } else {
                Outlet outlet = outletDao.getOutletById(currentUser.getIdOutlet());
                return outlet != null ? Arrays.asList(outlet) : new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Error getting available outlets: " + e.getMessage());
            return null;
        }
    }

    // === SEARCH TRANSACTIONS ===
    public List<Transaksi> searchTransaksi(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return getAllTransaksi();
            }
            List<Transaksi> results = transaksiDao.searchTransaksi(keyword.trim());
            logActivity("Pencarian transaksi", "Keyword: " + keyword + ", Hasil: " + results.size());
            return results;
        } catch (Exception e) {
            System.err.println("Error searching transaksi: " + e.getMessage());
            return null;
        }
    }
}