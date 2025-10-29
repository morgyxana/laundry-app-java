package Model;

import java.util.Date;
import java.util.List;

public class Transaksi {

    private int id;
    private int idOutlet;
    private String kodeInvoice;
    private Integer idMember;
    private String tipePelanggan;
    private String namaPelanggan;
    private String tlpPelanggan;
    private Date tgl;
    private Date batasWaktu;
    private Date tglBayar;
    private int biayaTambahan;
    private double diskon;
    private int pajak;
    private String status;
    private String dibayar;
    private int idUser;

    private List<DetailTransaksi> detailTransaksi;
    private Outlet outlet;
    private Member member;
    private User user;

    public Transaksi() {

    }

    public Transaksi(int id, int idOutlet, String kodeInvoice, String tipePelanggan, Date tgl) {

        this.id = id;
        this.idOutlet = idOutlet;
        this.kodeInvoice = kodeInvoice;
        this.tipePelanggan = tipePelanggan;
        this.tgl = tgl;

    }

    public int getId() {

        return id;
    }

    public void setId(int id) {

        this.id = id;

    }

    public int getIdOutlet() {

        return idOutlet;

    }

    public void setIdOutlet(int idOutlet) {

        this.idOutlet = idOutlet;

    }

    public String getKodeInvoice() {

        return kodeInvoice;

    }

    public void setKodeInvoice(String kodeInvoice) {

        this.kodeInvoice = kodeInvoice;

    }

    public Integer getIdMember() {

        return idMember;

    }

    public void setIdMember(Integer idMember) {

        this.idMember = idMember;

    }

    public String getTipePelanggan() {

        return tipePelanggan;

    }

    public void setTipePelanggan(String tipePelanggan) {

        this.tipePelanggan = tipePelanggan;

    }

    public String getNamaPelanggan() {

        return namaPelanggan;

    }

    public void setNamaPelanggan(String namaPelanggan) {

        this.namaPelanggan = namaPelanggan;

    }

    public String getTlpPelanggan() {

        return tlpPelanggan;

    }

    public void setTlpPelanggan(String tlpPelanggan) {

        this.tlpPelanggan = tlpPelanggan;

    }

    public Date getTgl() {

        return tgl;

    }

    public void setTgl(Date tgl) {

        this.tgl = tgl;

    }

    public Date getBatasWaktu() {

        return batasWaktu;

    }

    public void setBatasWaktu(Date batasWaktu) {

        this.batasWaktu = batasWaktu;

    }

    public Date getTglBayar() {

        return tglBayar;

    }

    public void setTglBayar(Date tglBayar) {

        this.tglBayar = tglBayar;

    }

    public int getBiayaTambahan() {

        return biayaTambahan;

    }

    public void setBiayaTambahan(int biayaTambahan) {

        this.biayaTambahan = biayaTambahan;

    }

    public double getDiskon() {

        return diskon;

    }

    public void setDiskon(double diskon) {

        this.diskon = diskon;

    }

    public int getPajak() {

        return pajak;

    }

    public void setPajak(int pajak) {

        this.pajak = pajak;

    }

    public String getStatus() {

        return status;

    }

    public void setStatus(String status) {

        this.status = status;

    }

    public String getDibayar() {

        return dibayar;

    }

    public void setDibayar(String dibayar) {

        this.dibayar = dibayar;

    }

    public int getIdUser() {

        return idUser;

    }

    public void setIdUser(int idUser) {

        this.idUser = idUser;

    }

    public List<DetailTransaksi> getDetailTransaksi() {

        return detailTransaksi;

    }

    public void setDetailTransaksi(List<DetailTransaksi> detailTransaksi) {

        this.detailTransaksi = detailTransaksi;

    }

    public Outlet getOutlet() {

        return outlet;

    }

    public void setOutlet(Outlet outlet) {

        this.outlet = outlet;

    }

    public Member getMember() {

        return member;

    }

    public void setMember(Member member) {

        this.member = member;

    }

    public User getUser() {

        return user;

    }

    public void setUser(User user) {

        this.user = user;

    }

    public double getDiskonPersen() {

        return diskon * 100;

    }

    public String getBiayaTambahanRupiah() {

        return "Rp " + String.format("%,d", biayaTambahan).replace(",", ".");

    }

    public String getPajakRupiah() {

        return "Rp " + String.format("%,d", pajak).replace(",", ".");

    }

    public String getDiskonDisplay() {

        return String.format("%.0f%%", getDiskonPersen());

    }

    public String getStatusFormatted() {

        switch (status) {

            case "baru": return "Baru";
            case "proses": return "Diproses";
            case "selesai": return "Selesai";
            case "diambil": return "Diambil";
            default: return status;

        }

    }

    public String getDibayarFormatted() {

        return "dibayar".equals(dibayar) ? "Sudah Dibayar" : "Belum Dibayar";

    }

    public String getTipePelangganFormatted() {

        if ("member".equals(tipePelanggan)) {

            return "Member";

        } else if ("biasa".equals(tipePelanggan)) {

            return "Pelanggan Biasa";

        }

        return tipePelanggan;

    }

    public boolean isTerlambat() {

        if (batasWaktu == null || "diambil".equals(status)) {

            return false;

        }

        return new Date().after(batasWaktu);

    }

    public double getSubtotal() {

        double subtotal = 0;

        if (detailTransaksi != null) {

            for (DetailTransaksi detail : detailTransaksi) {

                subtotal += detail.getSubtotal();

            }

        }

        return subtotal;

    }

    public double getTotal() {

        double subtotal = getSubtotal();
        double total = subtotal + biayaTambahan - (subtotal * diskon) + pajak;
        return Math.max(0, total);

    }

    public String getTotalFormatted() {

        return "Rp " + String.format("%,d", (int) getTotal()).replace(",", ".");

    }

    public String getSubtotalFormatted() {

        return "Rp " + String.format("%,d", (int) getSubtotal()).replace(",", ".");

    }

    public String getNamaPelangganDisplay() {

        if ("member".equals(tipePelanggan) && member != null) {

            return member.getNama();

        } else if ("biasa".equals(tipePelanggan)) {

            return namaPelanggan != null ? namaPelanggan : "Pelanggan Biasa";

        }

        return "Tidak Diketahui";

    }

    public String getTlpPelangganDisplay() {

        if ("member".equals(tipePelanggan) && member != null) {

            return member.getTlp();

        } else if ("biasa".equals(tipePelanggan)) {

            return tlpPelanggan != null ? tlpPelanggan : "-";

        }

        return "-";

    }

    public String getTglFormatted() {

        return tgl != null ? String.format("%1$td %1$tb %1$tY", tgl) : "-";

    }

    public String getBatasWaktuFormatted() {

        return batasWaktu != null ? String.format("%1$td %1$tb %1$tY", batasWaktu) : "-";

    }

    public String getTglBayarFormatted() {

        return tglBayar != null ? String.format("%1$td %1$tb %1$tY", tglBayar) : "-";

    }

    public String getNamaOutlet() {

        return outlet != null ? outlet.getNama() : "Outlet tidak ditemukan";

    }

    public String getNamaUser() {

        return user != null ? user.getName() : "User tidak ditemukan";

    }

}