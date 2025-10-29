package Model;

public class DetailTransaksi {

    private int id;
    private int idTransaksi;
    private int idPaket;
    private double qty;
    private double berat;
    private String keterangan;

    private Paket paket;
    private Transaksi transaksi;

    public DetailTransaksi() {

    }

    public DetailTransaksi(int id, int idTransaksi, int idPaket, double qty, double berat, String keterangan) {

        this.id = id;
        this.idTransaksi = idTransaksi;
        this.idPaket = idPaket;
        this.qty = qty;
        this.berat = berat;
        this.keterangan = keterangan;

    }

    public int getId() {

        return id;

    }

    public void setId(int id) {

        this.id = id;

    }

    public int getIdTransaksi() {

        return idTransaksi;

    }

    public void setIdTransaksi(int idTransaksi) {

        this.idTransaksi = idTransaksi;

    }

    public int getIdPaket() {

        return idPaket;

    }

    public void setIdPaket(int idPaket) {

        this.idPaket = idPaket;

    }

    public double getQty() {

        return qty;

    }

    public void setQty(double qty) {

        this.qty = qty;

    }

    public double getBerat() {

        return berat;

    }

    public void setBerat(double berat) {

        this.berat = berat;

    }

    public String getKeterangan() {

        return keterangan;

    }

    public void setKeterangan(String keterangan) {

        this.keterangan = keterangan;

    }

    public Paket getPaket() {

        return paket;

    }

    public void setPaket(Paket paket) {

        this.paket = paket;

    }

    public Transaksi getTransaksi() {

        return transaksi;

    }

    public void setTransaksi(Transaksi transaksi) {

        this.transaksi = transaksi;

    }

    public double getQtyDigunakan() {

        if (paket != null && "kiloan".equals(paket.getJenis()) && berat > 0) {

            return berat;

        }

        return qty;

    }

    public double getSubtotal() {

        if (paket != null) {

            if ("kiloan".equals(paket.getJenis()) && berat > 0) {

                return paket.getHarga() * berat;

            }

            return paket.getHarga() * qty;

        }

        return 0;

    }

    public String getNamaPaket() {

        return paket != null ? paket.getNamaPaket() : "Paket tidak ditemukan";

    }

    public double getHargaPaket() {

        return paket != null ? paket.getHarga() : 0;

    }

    public String getHargaPaketFormatted() {

        return "Rp " + String.format("%,d", (int) getHargaPaket()).replace(",", ".");

    }

    public String getSubtotalFormatted() {

        return "Rp " + String.format("%,d", (int) getSubtotal()).replace(",", ".");

    }

    public String getDisplayQty() {

        if (paket != null && "kiloan".equals(paket.getJenis())) {

            return String.format("%.1f kg", berat > 0 ? berat : 0);

        }

        return String.format("%.1f pcs", qty);

    }

    public String getJenisPaket() {

        return paket != null ? paket.getJenis() : null;

    }

}