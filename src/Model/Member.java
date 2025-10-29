package Model;

import java.util.List;

public class Member {

    private int id;
    private String nama;
    private String alamat;
    private String jenisKelamin;
    private String tlp;

    private List<Transaksi> transaksi;
    private int totalTransaksi;

    public Member() {

    }

    public Member(int id, String nama, String alamat, String jenisKelamin, String tlp) {

        this.id = id;
        this.nama = nama;
        this.alamat = alamat;
        this.jenisKelamin = jenisKelamin;
        this.tlp = tlp;

    }

    public int getId() {

        return id;

    }

    public void setId(int id) {

        this.id = id;

    }

    public String getNama() {

        return nama;

    }

    public void setNama(String nama) {

        this.nama = nama;

    }

    public String getAlamat() {

        return alamat;

    }

    public void setAlamat(String alamat) {

        this.alamat = alamat;

    }

    public String getJenisKelamin() {

        return jenisKelamin;

    }

    public void setJenisKelamin(String jenisKelamin) {

        this.jenisKelamin = jenisKelamin;

    }

    public String getTlp() {

        return tlp;

    }

    public void setTlp(String tlp) {

        this.tlp = tlp;

    }

    public List<Transaksi> getTransaksi() {

        return transaksi;

    }

    public void setTransaksi(List<Transaksi> transaksi) {

        this.transaksi = transaksi;

    }

    public int getTotalTransaksi() {

        return totalTransaksi;

    }

    public void setTotalTransaksi(int totalTransaksi) {

        this.totalTransaksi = totalTransaksi;

    }

    public String getJenisKelaminFormatted() {

        return "L".equals(jenisKelamin) ? "Laki-laki" : "Perempuan";

    }

    public String getTlpFormatted() {

        return tlp != null && tlp.length() > 1 ? "+62 " + tlp.substring(1) : "-";

    }

    public String getAlamatSingkat() {

        return alamat != null && alamat.length() > 50 ? alamat.substring(0, 50) + "..." : alamat;

    }

}