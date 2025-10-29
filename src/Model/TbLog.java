package Model;

import java.util.Date;

public class TbLog {

    private int idLog;
    private int idUser;
    private String aktivitas;
    private Date tanggal;
    private String dataTerkait;

    private User user;

    public TbLog() {

    }

    public TbLog(int idLog, int idUser, String aktivitas, Date tanggal) {

        this.idLog = idLog;
        this.idUser = idUser;
        this.aktivitas = aktivitas;
        this.tanggal = tanggal;

    }

    public TbLog(int idLog, int idUser, String aktivitas, Date tanggal, String dataTerkait) {

        this.idLog = idLog;
        this.idUser = idUser;
        this.aktivitas = aktivitas;
        this.tanggal = tanggal;
        this.dataTerkait = dataTerkait;

    }

    public int getIdLog() {

        return idLog;

    }

    public void setIdLog(int idLog) {

        this.idLog = idLog;

    }

    public int getIdUser() {

        return idUser;

    }

    public void setIdUser(int idUser) {

        this.idUser = idUser;

    }

    public String getAktivitas() {

        return aktivitas;

    }

    public void setAktivitas(String aktivitas) {

        this.aktivitas = aktivitas;

    }

    public Date getTanggal() {

        return tanggal;

    }

    public void setTanggal(Date tanggal) {

        this.tanggal = tanggal;

    }

    public String getDataTerkait() {

        return dataTerkait;

    }

    public void setDataTerkait(String dataTerkait) {

        this.dataTerkait = dataTerkait;

    }

    public User getUser() {

        return user;

    }

    public void setUser(User user) {

        this.user = user;

    }

    public String getTanggalFormatted() {

        return tanggal != null ? String.format("%1$td %1$tb %1$tY %1$tH:%1$tM", tanggal) : "-";

    }

    public String getNamaUser() {

        return user != null ? user.getName() : "User tidak ditemukan";

    }

    public String getUsername() {

        return user != null ? user.getUsername() : "-";

    }

}