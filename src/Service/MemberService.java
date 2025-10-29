package Service;

import Dao.MemberDao;
import Model.Member;
import javax.swing.*;
import java.util.List;
import java.util.regex.Pattern;

public class MemberService extends BaseService {
    private MemberDao memberDao;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]+$");

    public MemberService() {
        super();
        this.memberDao = new MemberDao();
    }

    // === GET ALL MEMBERS ===
    public List<Member> getAllMembers() {
        try {
            List<Member> members = memberDao.getAllMembers();

            // Log aktivitas menggunakan BaseService
            logView("daftar member", "Total: " + members.size());

            return members;
        } catch (Exception e) {
            System.err.println("Error getting all members: " + e.getMessage());
            return null;
        }
    }

    // === GET MEMBER BY ID ===
    public Member getMemberById(int id) {
        try {
            Member member = memberDao.getMemberById(id);
            if (member != null) {
                logView("detail member", "ID: " + id + ", Nama: " + member.getNama());
            }
            return member;
        } catch (Exception e) {
            System.err.println("Error getting member by id: " + e.getMessage());
            return null;
        }
    }

    // === CREATE MEMBER ===
    public boolean createMember(String nama, String alamat, String jenisKelamin, String tlp, JFrame parentFrame) {
        // Validasi input menggunakan BaseService
        if (!validateMemberInput(nama, alamat, jenisKelamin, tlp, -1, parentFrame)) {
            return false;
        }

        try {
            // Cek duplikasi nomor telepon
            if (tlp != null && !tlp.trim().isEmpty()) {
                if (isPhoneDuplicate(tlp, -1)) {
                    showError(parentFrame, "Nomor telepon sudah digunakan oleh pelanggan lain");
                    return false;
                }
            }

            // Buat member baru
            Member member = new Member();
            member.setNama(nama.trim());
            member.setAlamat(alamat != null ? alamat.trim() : null);
            member.setJenisKelamin(jenisKelamin != null ? jenisKelamin : null);
            member.setTlp(tlp != null ? tlp.trim() : null);

            // Simpan member
            boolean success = memberDao.insertMember(member);
            if (success) {
                // Log aktivitas menggunakan BaseService
                logCreate("member baru", "Nama: " + member.getNama());

                showSuccess(parentFrame, "Registrasi pelanggan berhasil!");
                return true;
            } else {
                showError(parentFrame, "Gagal menambahkan pelanggan");
                return false;
            }

        } catch (Exception e) {
            showError(parentFrame, "Gagal menambahkan pelanggan: " + e.getMessage());
            return false;
        }
    }

    // === UPDATE MEMBER ===
    public boolean updateMember(int id, String nama, String alamat, String jenisKelamin, String tlp, JFrame parentFrame) {
        // Validasi ID
        Member existingMember = memberDao.getMemberById(id);
        if (existingMember == null) {
            showError(parentFrame, "Pelanggan tidak ditemukan");
            return false;
        }

        // Validasi input menggunakan BaseService
        if (!validateMemberInput(nama, alamat, jenisKelamin, tlp, id, parentFrame)) {
            return false;
        }

        try {
            // Simpan data lama untuk logging
            Member oldData = new Member();
            oldData.setNama(existingMember.getNama());
            oldData.setAlamat(existingMember.getAlamat());
            oldData.setJenisKelamin(existingMember.getJenisKelamin());
            oldData.setTlp(existingMember.getTlp());

            // Update member
            existingMember.setNama(nama.trim());
            existingMember.setAlamat(alamat != null ? alamat.trim() : null);
            existingMember.setJenisKelamin(jenisKelamin != null ? jenisKelamin : null);
            existingMember.setTlp(tlp != null ? tlp.trim() : null);

            boolean success = memberDao.updateMember(existingMember);
            if (success) {
                // Log aktivitas menggunakan BaseService
                logUpdate("data member",
                        String.format("Sebelum: %s -> Sesudah: %s", oldData.getNama(), existingMember.getNama()));

                showSuccess(parentFrame, "Data pelanggan berhasil diupdate!");
                return true;
            } else {
                showError(parentFrame, "Gagal mengupdate pelanggan");
                return false;
            }

        } catch (Exception e) {
            showError(parentFrame, "Gagal mengupdate pelanggan: " + e.getMessage());
            return false;
        }
    }

    // === DELETE MEMBER ===
    public boolean deleteMember(int id, JFrame parentFrame) {
        // Validasi ID
        Member member = memberDao.getMemberById(id);
        if (member == null) {
            showError(parentFrame, "Pelanggan tidak ditemukan");
            return false;
        }

        // Cek apakah member memiliki transaksi
        if (member.getTotalTransaksi() > 0) {
            showError(parentFrame, "Tidak dapat menghapus pelanggan karena memiliki transaksi terkait!");
            return false;
        }

        try {
            // Simpan data untuk logging
            String memberName = member.getNama();

            // Hapus member
            boolean success = memberDao.deleteMember(id);
            if (success) {
                // Log aktivitas menggunakan BaseService
                logDelete("member", "Nama: " + memberName);

                showSuccess(parentFrame, "Data pelanggan berhasil dihapus!");
                return true;
            } else {
                showError(parentFrame, "Gagal menghapus pelanggan");
                return false;
            }

        } catch (Exception e) {
            showError(parentFrame, "Gagal menghapus pelanggan: " + e.getMessage());
            return false;
        }
    }

    // === SEARCH MEMBERS ===
    public List<Member> searchMembers(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return memberDao.getAllMembers();
            }
            List<Member> results = memberDao.searchMembers(keyword.trim());
            logActivity("Pencarian member", "Keyword: " + keyword + ", Hasil: " + results.size());
            return results;
        } catch (Exception e) {
            System.err.println("Error searching members: " + e.getMessage());
            return null;
        }
    }

    // === CHECK DUPLICATE PHONE ===
    public boolean checkDuplicatePhone(String tlp, Integer memberId) {
        try {
            if (tlp == null || tlp.trim().isEmpty()) {
                return false;
            }
            return isPhoneDuplicate(tlp.trim(), memberId != null ? memberId : -1);
        } catch (Exception e) {
            System.err.println("Error checking duplicate phone: " + e.getMessage());
            return false;
        }
    }

    public String getDuplicatePhoneMessage(String tlp, Integer memberId) {
        if (checkDuplicatePhone(tlp, memberId)) {
            List<Member> allMembers = memberDao.getAllMembers();
            for (Member member : allMembers) {
                if (member.getTlp() != null && member.getTlp().equals(tlp.trim())) {
                    if (memberId == null || member.getId() != memberId) {
                        return "Nomor telepon sudah digunakan oleh pelanggan: " + member.getNama();
                    }
                }
            }
            return "Nomor telepon sudah digunakan";
        }
        return "Nomor telepon tersedia";
    }

    // === VALIDATION METHODS ===
    private boolean validateMemberInput(String nama, String alamat, String jenisKelamin, String tlp, int memberId, JFrame parentFrame) {
        // Validasi nama menggunakan BaseService
        if (!validateRequired(nama, "Nama pelanggan", parentFrame) ||
                !validateMaxLength(nama, 100, "Nama pelanggan", parentFrame)) {
            return false;
        }

        // Validasi jenis kelamin
        if (jenisKelamin != null && !jenisKelamin.isEmpty()) {
            if (!jenisKelamin.equals("L") && !jenisKelamin.equals("P")) {
                showError(parentFrame, "Jenis kelamin harus L atau P");
                return false;
            }
        }

        // Validasi telepon
        if (tlp != null && !tlp.trim().isEmpty()) {
            if (!validateMaxLength(tlp, 15, "Nomor telepon", parentFrame)) {
                return false;
            }
            if (!PHONE_PATTERN.matcher(tlp.trim()).matches()) {
                showError(parentFrame, "Nomor telepon hanya boleh mengandung angka");
                return false;
            }

            // Cek duplikasi
            if (isPhoneDuplicate(tlp.trim(), memberId)) {
                showError(parentFrame, "Nomor telepon sudah digunakan oleh pelanggan lain");
                return false;
            }
        }

        return true;
    }

    private boolean isPhoneDuplicate(String tlp, int excludeMemberId) {
        List<Member> allMembers = memberDao.getAllMembers();
        for (Member member : allMembers) {
            if (member.getTlp() != null && member.getTlp().equals(tlp.trim())) {
                if (excludeMemberId == -1 || member.getId() != excludeMemberId) {
                    return true;
                }
            }
        }
        return false;
    }

    // === GETTERS FOR STATISTICS ===
    public int getTotalMembers() {
        try {
            return memberDao.getTotalMembers();
        } catch (Exception e) {
            System.err.println("Error getting total members: " + e.getMessage());
            return 0;
        }
    }

    public List<Member> getRecentMembers(int limit) {
        try {
            List<Member> allMembers = memberDao.getAllMembers();
            if (allMembers.size() > limit) {
                return allMembers.subList(0, limit);
            }
            return allMembers;
        } catch (Exception e) {
            System.err.println("Error getting recent members: " + e.getMessage());
            return null;
        }
    }

    // === VALIDATION FOR SWING COMPONENTS ===
    public boolean validateNamaField(JTextField txtNama, JFrame parentFrame) {
        String nama = txtNama.getText().trim();
        if (!validateRequired(nama, "Nama pelanggan", parentFrame) ||
                !validateMaxLength(nama, 100, "Nama pelanggan", parentFrame)) {
            txtNama.requestFocus();
            return false;
        }
        return true;
    }

    public boolean validateTeleponField(JTextField txtTlp, JFrame parentFrame) {
        String tlp = txtTlp.getText().trim();
        if (!tlp.isEmpty()) {
            if (!validateMaxLength(tlp, 15, "Nomor telepon", parentFrame)) {
                txtTlp.requestFocus();
                return false;
            }
            if (!PHONE_PATTERN.matcher(tlp).matches()) {
                showError(parentFrame, "Nomor telepon hanya boleh mengandung angka");
                txtTlp.requestFocus();
                return false;
            }
        }
        return true;
    }

    public boolean validateJenisKelaminField(JComboBox<String> cmbJenisKelamin, JFrame parentFrame) {
        String jenisKelamin = (String) cmbJenisKelamin.getSelectedItem();
        if (jenisKelamin != null && !jenisKelamin.isEmpty()) {
            if (!jenisKelamin.equals("L") && !jenisKelamin.equals("P")) {
                showError(parentFrame, "Jenis kelamin harus L atau P");
                cmbJenisKelamin.requestFocus();
                return false;
            }
        }
        return true;
    }

    // === METHOD FOR COMBOBOX DATA ===
    public String[] getJenisKelaminOptions() {
        return new String[]{"", "L", "P"};
    }

    public String getJenisKelaminDisplay(String kode) {
        if ("L".equals(kode)) {
            return "Laki-laki";
        } else if ("P".equals(kode)) {
            return "Perempuan";
        } else {
            return "Tidak diketahui";
        }
    }
}
