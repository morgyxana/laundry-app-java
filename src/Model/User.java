package Model;

import java.util.Date;
import java.util.List;

public class User {

    private int id;
    private String name;
    private String username;
    private String email;
    private String password;
    private String role;
    private String phone;
    private String address;
    private int idOutlet;
    private Date emailVerifiedAt;
    private Date createdAt;
    private Date updatedAt;

    private Outlet outlet;
    private List<Transaksi> transaksi;
    private List<TbLog> logs;

    public User() {

    }

    public User(int id, String name, String username, String email, String password, String role, int idOutlet) {

        this.id = id;
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.idOutlet = idOutlet;

    }

    public int getId() {

        return id;

    }

    public void setId(int id) {

        this.id = id;

    }

    public String getName() {

        return name;

    }

    public void setName(String name) {

        this.name = name;

    }

    public String getUsername() {

        return username;

    }

    public void setUsername(String username) {

        this.username = username;

    }

    public String getEmail() {

        return email;

    }

    public void setEmail(String email) {

        this.email = email;

    }

    public String getPassword() {

        return password;

    }

    public void setPassword(String password) {

        this.password = password;

    }

    public String getRole() {

        return role;

    }

    public void setRole(String role) {

        this.role = role;

    }

    public String getPhone() {

        return phone;

    }

    public void setPhone(String phone) {

        this.phone = phone;

    }

    public String getAddress() {

        return address;

    }

    public void setAddress(String address) {

        this.address = address;

    }

    public int getIdOutlet() {

        return idOutlet;

    }

    public void setIdOutlet(int idOutlet) {

        this.idOutlet = idOutlet;

    }

    public Date getEmailVerifiedAt() {

        return emailVerifiedAt;

    }

    public void setEmailVerifiedAt(Date emailVerifiedAt) {

        this.emailVerifiedAt = emailVerifiedAt;

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

    public Outlet getOutlet() {

        return outlet;

    }

    public void setOutlet(Outlet outlet) {

        this.outlet = outlet;

    }

    public List<Transaksi> getTransaksi() {

        return transaksi;

    }

    public void setTransaksi(List<Transaksi> transaksi) {

        this.transaksi = transaksi;

    }

    public List<TbLog> getLogs() {

        return logs;

    }

    public void setLogs(List<TbLog> logs) {

        this.logs = logs;

    }

    public String getRoleFormatted() {

        switch (role) {

            case "admin": return "Admin";
            case "kasir": return "Kasir";
            case "owner": return "Owner";
            default: return role;

        }

    }

    public boolean isAdmin() {

        return "admin".equals(role);

    }

    public boolean isKasir() {

        return "kasir".equals(role);

    }

    public boolean isOwner() {

        return "owner".equals(role);

    }

    public String getNamaOutlet() {

        return outlet != null ? outlet.getNama() : "Tidak ada outlet";

    }

    public String getPhoneFormatted() {

        return phone != null && phone.length() > 1 ? "+62 " + phone.substring(1) : "-";

    }

    public String getAddressSingkat() {

        return address != null && address.length() > 50 ? address.substring(0, 50) + "..." : address;

    }

    public int getTotalTransaksi() {

        return transaksi != null ? transaksi.size() : 0;

    }

    public int getTotalLogs() {

        return logs != null ? logs.size() : 0;

    }

    public String getCreatedAtFormatted() {

        return createdAt != null ? String.format("%1$td %1$tb %1$tY %1$tH:%1$tM", createdAt) : "-";

    }

    public String getUpdatedAtFormatted() {

        return updatedAt != null ? String.format("%1$td %1$tb %1$tY %1$tH:%1$tM", updatedAt) : "-";

    }

    public String getEmailVerifiedAtFormatted() {

        return emailVerifiedAt != null ? String.format("%1$td %1$tb %1$tY %1$tH:%1$tM", emailVerifiedAt) : "Belum diverifikasi";

    }

    public boolean isEmailVerified() {

        return emailVerifiedAt != null;

    }

}