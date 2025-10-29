package Model;

import java.util.Date;
import java.util.List;

public class Outlet {

    private int id;
    private String nama;
    private String alamat;
    private String tlp;
    private Date createdAt;
    private Date updatedAt;

    private List<User> users;
    private List<Transaksi> transaksi;
    private List<Paket> paket;

    public Outlet() {

    }

    public Outlet(int id, String nama, String alamat, String tlp) {

        this.id = id;
        this.nama = nama;
        this.alamat = alamat;
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

    public String getTlp() {

        return tlp;

    }

    public void setTlp(String tlp) {

        this.tlp = tlp;

    }

    public Date getCreatedAt() {

        return createdAt;

    }

    public void setCreatedAt(Date createdAt) {

        this.createdAt = createdAt;

    }

    public Date getUpdatedAt() {

        return updatedAt;

    }

    public void setUpdatedAt(Date updatedAt) {

        this.updatedAt = updatedAt;

    }

    public List<User> getUsers() {

        return users;

    }

    public void setUsers(List<User> users) {

        this.users = users;

    }

    public List<Transaksi> getTransaksi() {

        return transaksi;

    }

    public void setTransaksi(List<Transaksi> transaksi) {

        this.transaksi = transaksi;

    }

    public List<Paket> getPaket() {

        return paket;

    }

    public void setPaket(List<Paket> paket) {

        this.paket = paket;

    }

    public String getTlpFormatted() {

        return tlp != null && !tlp.isEmpty() && tlp.length() > 1 ? "+62 " + tlp.substring(1) : "-";

    }

    public String getAlamatSingkat() {

        return alamat != null && alamat.length() > 50 ? alamat.substring(0, 50) + "..." : alamat;

    }

    public int getTotalUsers() {

        return users != null ? users.size() : 0;

    }

    public int getTotalTransactions() {

        return transaksi != null ? transaksi.size() : 0;

    }

    public int getTotalPaket() {

        return paket != null ? paket.size() : 0;

    }

    public boolean getCanDelete() {

        return getTotalUsers() == 0 && getTotalTransactions() == 0;

    }

    public String getCreatedAtFormatted() {

        return createdAt != null ? String.format("%1$td %1$tb %1$tY %1$tH:%1$tM", createdAt) : "-";

    }

    public String getUpdatedAtFormatted() {

        return updatedAt != null ? String.format("%1$td %1$tb %1$tY %1$tH:%1$tM", updatedAt) : "-";

    }

}