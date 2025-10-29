package Service;

import Dao.UserDao;
import Dao.OutletDao;
import Model.User;
import Model.Outlet;
import javax.swing.*;
import java.util.List;
import java.util.regex.Pattern;

public class UserService extends BaseService {
    private UserDao userDao;
    private OutletDao outletDao;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public UserService() {
        super();
        this.userDao = new UserDao();
        this.outletDao = new OutletDao();
    }

    // === GET ALL USERS ===
    public List<User> getAllUsers() {
        try {
            // Cek authorization - hanya admin yang bisa lihat semua user
            if (!isAdmin()) {
                return null;
            }

            List<User> users = userDao.getAllUsers();

            // Log aktivitas menggunakan BaseService
            logView("daftar pengguna", "Total: " + users.size());

            return users;
        } catch (Exception e) {
            System.err.println("Error getting all users: " + e.getMessage());
            return null;
        }
    }

    // === GET USER BY ID ===
    public User getUserById(int id) {
        try {
            User user = userDao.getUserById(id);
            if (user != null) {
                logView("detail pengguna", "ID: " + id + ", Username: " + user.getUsername());
            }
            return user;
        } catch (Exception e) {
            System.err.println("Error getting user by id: " + e.getMessage());
            return null;
        }
    }

    // === CREATE USER ===
    public boolean createUser(String name, String username, String email, String phone,
                              String address, String password, String confirmPassword,
                              String role, Integer idOutlet, JFrame parentFrame) {
        // Cek authorization - hanya admin yang bisa buat user
        if (!checkRole("admin", parentFrame)) {
            return false;
        }

        // Validasi input menggunakan BaseService
        if (!validateUserInput(name, username, email, phone, address, password, confirmPassword,
                role, idOutlet, -1, parentFrame)) {
            return false;
        }

        try {
            // Buat user baru
            User user = new User();
            user.setName(name.trim());
            user.setUsername(username.trim());
            user.setEmail(email.trim());
            user.setPhone(phone != null ? phone.trim() : null);
            user.setAddress(address != null ? address.trim() : null);
            user.setPassword(password);
            user.setRole(role);
            user.setIdOutlet(idOutlet != null ? idOutlet : 0);
            user.setCreatedAt(getCurrentTimestamp());
            user.setUpdatedAt(getCurrentTimestamp());

            // Simpan user
            boolean success = userDao.insertUser(user);
            if (success) {
                // Log aktivitas menggunakan BaseService
                logCreate("pengguna baru", "Username: " + user.getUsername() + ", Role: " + user.getRole());

                showSuccess(parentFrame, "Pengguna berhasil ditambahkan!");
                return true;
            } else {
                showError(parentFrame, "Gagal menambahkan pengguna");
                return false;
            }

        } catch (Exception e) {
            showError(parentFrame, "Gagal menambahkan pengguna: " + e.getMessage());
            return false;
        }
    }

    // === UPDATE USER ===
    public boolean updateUser(int id, String name, String username, String email, String phone,
                              String address, String password, String confirmPassword,
                              String role, Integer idOutlet, JFrame parentFrame) {
        // Cek authorization - hanya admin yang bisa update user
        if (!checkRole("admin", parentFrame)) {
            return false;
        }

        // Validasi ID
        User existingUser = userDao.getUserById(id);
        if (existingUser == null) {
            showError(parentFrame, "Pengguna tidak ditemukan");
            return false;
        }

        // Validasi input menggunakan BaseService
        if (!validateUserInput(name, username, email, phone, address, password, confirmPassword,
                role, idOutlet, id, parentFrame)) {
            return false;
        }

        try {
            // Update user
            existingUser.setName(name.trim());
            existingUser.setUsername(username.trim());
            existingUser.setEmail(email.trim());
            existingUser.setPhone(phone != null ? phone.trim() : null);
            existingUser.setAddress(address != null ? address.trim() : null);
            existingUser.setRole(role);
            existingUser.setIdOutlet(idOutlet != null ? idOutlet : 0);
            existingUser.setUpdatedAt(getCurrentTimestamp());

            // Update password jika diisi
            if (password != null && !password.trim().isEmpty()) {
                existingUser.setPassword(password);
            }

            boolean success = userDao.updateUser(existingUser);
            if (success) {
                // Log aktivitas menggunakan BaseService
                logUpdate("data pengguna", "Username: " + existingUser.getUsername() + ", Role: " + existingUser.getRole());

                showSuccess(parentFrame, "Pengguna berhasil diupdate!");
                return true;
            } else {
                showError(parentFrame, "Gagal mengupdate pengguna");
                return false;
            }

        } catch (Exception e) {
            showError(parentFrame, "Gagal mengupdate pengguna: " + e.getMessage());
            return false;
        }
    }

    // === DELETE USER ===
    public boolean deleteUser(int id, JFrame parentFrame) {
        // Cek authorization - hanya admin yang bisa hapus user
        if (!checkRole("admin", parentFrame)) {
            return false;
        }

        // Validasi ID
        User user = userDao.getUserById(id);
        if (user == null) {
            showError(parentFrame, "Pengguna tidak ditemukan");
            return false;
        }

        // Tidak boleh hapus diri sendiri
        if (user.getId() == AuthService.getCurrentUser().getId()) {
            showError(parentFrame, "Tidak dapat menghapus akun sendiri");
            return false;
        }

        try {
            // Konfirmasi penghapusan
            if (!showConfirm(parentFrame, "Apakah Anda yakin ingin menghapus pengguna " + user.getUsername() + "?")) {
                return false;
            }

            // Simpan data untuk logging
            String userName = user.getUsername();

            // Hapus user
            boolean success = userDao.deleteUser(id);
            if (success) {
                // Log aktivitas menggunakan BaseService
                logDelete("pengguna", "Username: " + userName);

                showSuccess(parentFrame, "Pengguna berhasil dihapus!");
                return true;
            } else {
                showError(parentFrame, "Gagal menghapus pengguna");
                return false;
            }

        } catch (Exception e) {
            showError(parentFrame, "Gagal menghapus pengguna: " + e.getMessage());
            return false;
        }
    }

    // === VALIDATION METHODS ===
    private boolean validateUserInput(String name, String username, String email, String phone,
                                      String address, String password, String confirmPassword,
                                      String role, Integer idOutlet, int userId, JFrame parentFrame) {
        // Validasi name menggunakan BaseService
        if (!validateRequired(name, "Nama", parentFrame) ||
                !validateMaxLength(name, 255, "Nama", parentFrame)) {
            return false;
        }

        // Validasi username
        if (!validateRequired(username, "Username", parentFrame)) {
            return false;
        }
        if (isUsernameDuplicate(username.trim(), userId)) {
            showError(parentFrame, "Username sudah digunakan");
            return false;
        }

        // Validasi email
        if (!validateRequired(email, "Email", parentFrame)) {
            return false;
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            showError(parentFrame, "Format email tidak valid");
            return false;
        }
        if (isEmailDuplicate(email.trim(), userId)) {
            showError(parentFrame, "Email sudah digunakan");
            return false;
        }

        // Validasi password (hanya untuk create atau update dengan password baru)
        if (userId == -1 || (password != null && !password.trim().isEmpty())) {
            if (!validateRequired(password, "Password", parentFrame) ||
                    !validateMinLength(password, 8, "Password", parentFrame)) {
                return false;
            }
            if (!password.equals(confirmPassword)) {
                showError(parentFrame, "Konfirmasi password tidak sesuai");
                return false;
            }
        }

        // Validasi role
        if (role == null || (!role.equals("admin") && !role.equals("kasir") && !role.equals("owner"))) {
            showError(parentFrame, "Role tidak valid");
            return false;
        }

        // Validasi outlet
        if (idOutlet != null && idOutlet > 0) {
            Outlet outlet = outletDao.getOutletById(idOutlet);
            if (outlet == null) {
                showError(parentFrame, "Outlet yang dipilih tidak valid");
                return false;
            }
        }

        return true;
    }

    private boolean isUsernameDuplicate(String username, int excludeUserId) {
        List<User> allUsers = userDao.getAllUsers();
        for (User user : allUsers) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                if (excludeUserId == -1 || user.getId() != excludeUserId) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isEmailDuplicate(String email, int excludeUserId) {
        List<User> allUsers = userDao.getAllUsers();
        for (User user : allUsers) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                if (excludeUserId == -1 || user.getId() != excludeUserId) {
                    return true;
                }
            }
        }
        return false;
    }

    // === GETTERS FOR COMBOBOX DATA ===
    public String[] getRoleOptions() {
        return new String[]{"admin", "kasir", "owner"};
    }

    public List<Outlet> getAllOutlets() {
        try {
            return outletDao.getAllOutlets();
        } catch (Exception e) {
            System.err.println("Error getting all outlets: " + e.getMessage());
            return null;
        }
    }

    // === VALIDATION FOR SWING COMPONENTS ===
    public boolean validateNameField(JTextField txtName, JFrame parentFrame) {
        String name = txtName.getText().trim();
        if (!validateRequired(name, "Nama", parentFrame) ||
                !validateMaxLength(name, 255, "Nama", parentFrame)) {
            txtName.requestFocus();
            return false;
        }
        return true;
    }

    public boolean validateUsernameField(JTextField txtUsername, JFrame parentFrame, Integer userId) {
        String username = txtUsername.getText().trim();
        if (!validateRequired(username, "Username", parentFrame)) {
            txtUsername.requestFocus();
            return false;
        }
        if (isUsernameDuplicate(username, userId != null ? userId : -1)) {
            showError(parentFrame, "Username sudah digunakan");
            txtUsername.requestFocus();
            return false;
        }
        return true;
    }

    public boolean validateEmailField(JTextField txtEmail, JFrame parentFrame, Integer userId) {
        String email = txtEmail.getText().trim();
        if (!validateRequired(email, "Email", parentFrame)) {
            txtEmail.requestFocus();
            return false;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError(parentFrame, "Format email tidak valid");
            txtEmail.requestFocus();
            return false;
        }
        if (isEmailDuplicate(email, userId != null ? userId : -1)) {
            showError(parentFrame, "Email sudah digunakan");
            txtEmail.requestFocus();
            return false;
        }
        return true;
    }

    public boolean validatePasswordField(JPasswordField txtPassword, JPasswordField txtConfirmPassword, JFrame parentFrame, boolean isUpdate) {
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());

        if (!isUpdate || !password.isEmpty()) {
            if (!validateRequired(password, "Password", parentFrame) ||
                    !validateMinLength(password, 8, "Password", parentFrame)) {
                txtPassword.requestFocus();
                return false;
            }
            if (!password.equals(confirmPassword)) {
                showError(parentFrame, "Konfirmasi password tidak sesuai");
                txtConfirmPassword.requestFocus();
                return false;
            }
        }
        return true;
    }

    // === SEARCH USERS ===
    public List<User> searchUsers(String keyword) {
        try {
            if (!isAdmin()) {
                return null;
            }

            if (keyword == null || keyword.trim().isEmpty()) {
                return userDao.getAllUsers();
            }
            List<User> results = userDao.searchUsers(keyword.trim());
            logActivity("Pencarian pengguna", "Keyword: " + keyword + ", Hasil: " + results.size());
            return results;
        } catch (Exception e) {
            System.err.println("Error searching users: " + e.getMessage());
            return null;
        }
    }
}