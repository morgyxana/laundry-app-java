package Model;

import java.util.List;

public class Paket {

    private int id;
    private int idOutlet;
    private String jenis;
    private String namaPaket;
    private int harga;

    private Outlet outlet;
    private List<DetailTransaksi> detailTransaksi;

    public Paket() {

    }

    public Paket(int id, int idOutlet, String jenis, String namaPaket, int harga) {

        this.id = id;
        this.idOutlet = idOutlet;
        this.jenis = jenis;
        this.namaPaket = namaPaket;
        this.harga = harga;

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

    public String getJenis() {

        return jenis;

    }

    public void setJenis(String jenis) {

        this.jenis = jenis;

    }

    public String getNamaPaket() {

        return namaPaket;

    }

    public void setNamaPaket(String namaPaket) {

        this.namaPaket = namaPaket;

    }

    public int getHarga() {

        return harga;

    }

    public void setHarga(int harga) {

        this.harga = harga;

    }

    public Outlet getOutlet() {

        return outlet;

    }

    public void setOutlet(Outlet outlet) {

        this.outlet = outlet;

    }

    public List<DetailTransaksi> getDetailTransaksi() {

        return detailTransaksi;

    }

    public void setDetailTransaksi(List<DetailTransaksi> detailTransaksi) {

        this.detailTransaksi = detailTransaksi;

    }

    public String getJenisFormatted() {

        switch (jenis) {

            case "kiloan": return "Kiloan";
            case "selimut": return "Selimut";
            case "bed_cover": return "Bed Cover";
            case "kaos": return "Kaos";
            case "lain": return "Lainnya";
            default: return jenis;

        }

    }

    public String getHargaFormatted() {

        return "Rp " + String.format("%,d", harga).replace(",", ".");

    }

    public int getTotalTransaksi() {

        return detailTransaksi != null ? detailTransaksi.size() : 0;

    }

    public String getNamaOutlet() {

        return outlet != null ? outlet.getNama() : "Outlet tidak ditemukan";

    }

}