package Service;

import Dao.UserDao;
import Dao.OutletDao;
import Model.User;
import Model.Outlet;
import javax.swing.*;
import java.util.Date;
import java.util.regex.Pattern;

public class AuthService extends BaseService {
    private UserDao userDao;
    private OutletDao outletDao;
    private static User currentUser;

    
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public AuthService() {
        super();
        this.userDao = new UserDao();
        this.outletDao = new OutletDao();
    }

   
    public boolean login(String emailOrUsername, String password, JFrame parentFrame) {
        
        if (!validateRequired(emailOrUsername, "Email/username", parentFrame) ||
                !validateRequired(password, "Password", parentFrame)) {
            return false;
        }

        try {
           
            User user;
            if (isEmail(emailOrUsername)) {
                user = userDao.getUserByUsername(emailOrUsername);
                if (user == null) {
                    user = userDao.getUserByUsername(emailOrUsername);
                }
            } else {
                user = userDao.getUserByUsername(emailOrUsername);
            }

            if (user == null) {
                showError(parentFrame, "Email/username atau password salah");
                return false;
            }

            
            if (!user.getPassword().equals(password)) {
                showError(parentFrame, "Email/username atau password salah");
                return false;
            }

            
            currentUser = user;

            
            logActivity("Login", "User login ke sistem: " + user.getUsername());

            showSuccess(parentFrame, "Login berhasil! Selamat datang " + user.getName());
            return true;

        } catch (Exception e) {
            showError(parentFrame, "Terjadi kesalahan saat login: " + e.getMessage());
            return false;
        }
    }

    
    public boolean register(String name, String username, String email, String password,
                            String confirmPassword, String phone, String address,
                            String role, JFrame parentFrame) {

        
        if (!validateRegistration(name, username, email, password, confirmPassword, phone, address, role, parentFrame)) {
            return false;
        }

        try {
            
            User existingUsername = userDao.getUserByUsername(username);
            if (existingUsername != null) {
                showError(parentFrame, "Username sudah terdaftar");
                return false;
            }

            
            User existingEmail = userDao.getUserByUsername(email);
            if (existingEmail != null) {
                showError(parentFrame, "Email sudah terdaftar");
                return false;
            }

            
            User newUser = new User();
            newUser.setName(name.trim());
            newUser.setUsername(username.trim());
            newUser.setEmail(email.trim());
            newUser.setPassword(password);
            newUser.setPhone(phone);
            newUser.setAddress(address);
            newUser.setRole(role);
            newUser.setCreatedAt(new Date());
            newUser.setUpdatedAt(new Date());

            
            if (role.equals("admin") || role.equals("owner") || role.equals("kasir")) {
                newUser.setIdOutlet(1);
            }

           
            boolean success = userDao.insertUser(newUser);
            if (success) {
                
                logCreate("user baru", "Username: " + newUser.getUsername());

                showSuccess(parentFrame, "Registrasi berhasil! Silakan login.");
                return true;
            } else {
                showError(parentFrame, "Gagal melakukan registrasi");
                return false;
            }

        } catch (Exception e) {
            showError(parentFrame, "Terjadi kesalahan saat registrasi: " + e.getMessage());
            return false;
        }
    }

    
    private boolean validateRegistration(String name, String username, String email, String password,
                                         String confirmPassword, String phone, String address,
                                         String role, JFrame parentFrame) {

        // Validasi name menggunakan BaseService
        if (!validateRequired(name, "Nama", parentFrame) ||
                !validateMaxLength(name, 255, "Nama", parentFrame)) {
            return false;
        }

        // Validasi username
        if (!validateRequired(username, "Username", parentFrame) ||
                !validateMinLength(username, 3, "Username", parentFrame) ||
                !validateMaxLength(username, 8, "Username", parentFrame)) {
            return false;
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            showError(parentFrame, "Username hanya boleh mengandung huruf, angka, dan underscore");
            return false;
        }

        // Validasi email
        if (!validateRequired(email, "Email", parentFrame)) {
            return false;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError(parentFrame, "Format email tidak valid");
            return false;
        }

        // Validasi password
        if (!validateRequired(password, "Password", parentFrame) ||
                !validateMinLength(password, 8, "Password", parentFrame) ||
                !validateMaxLength(password, 20, "Password", parentFrame)) {
            return false;
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            showError(parentFrame, "Password harus mengandung huruf besar, huruf kecil, dan angka");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            showError(parentFrame, "Konfirmasi password tidak sesuai");
            return false;
        }

        // Validasi phone
        if (!validateRequired(phone, "Nomor telepon", parentFrame) ||
                !validateMaxLength(phone, 15, "Nomor telepon", parentFrame)) {
            return false;
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            showError(parentFrame, "Nomor telepon hanya boleh mengandung angka");
            return false;
        }

        // Validasi address
        if (!validateRequired(address, "Alamat", parentFrame) ||
                !validateMaxLength(address, 500, "Alamat", parentFrame)) {
            return false;
        }

        // Validasi role
        if (role == null || (!role.equals("admin") && !role.equals("owner") && !role.equals("kasir"))) {
            showError(parentFrame, "Role tidak valid");
            return false;
        }

        return true;
    }

    // === PROFILE UPDATE ===
    public boolean updateProfile(String name, String username, String phone, String address, JFrame parentFrame) {
        if (currentUser == null) {
            showError(parentFrame, "User tidak login");
            return false;
        }

        // Validasi input menggunakan BaseService
        if (!validateProfileUpdate(name, username, phone, address, parentFrame)) {
            return false;
        }

        try {
            // Cek apakah username sudah digunakan oleh user lain
            User existingUser = userDao.getUserByUsername(username);
            if (existingUser != null && existingUser.getId() != currentUser.getId()) {
                showError(parentFrame, "Username sudah digunakan");
                return false;
            }

            // Update user
            currentUser.setName(name);
            currentUser.setUsername(username);
            currentUser.setPhone(phone);
            currentUser.setAddress(address);
            currentUser.setUpdatedAt(new Date());

            boolean success = userDao.updateUser(currentUser);
            if (success) {
                // Log aktivitas menggunakan BaseService
                logUpdate("profile user", "Username: " + currentUser.getUsername());

                showSuccess(parentFrame, "Profile berhasil diperbarui!");
                return true;
            } else {
                showError(parentFrame, "Gagal memperbarui profile");
                return false;
            }

        } catch (Exception e) {
            showError(parentFrame, "Terjadi kesalahan saat memperbarui profile: " + e.getMessage());
            return false;
        }
    }

    private boolean validateProfileUpdate(String name, String username, String phone, String address, JFrame parentFrame) {
        // Validasi menggunakan BaseService
        if (!validateRequired(name, "Nama", parentFrame) ||
                !validateMaxLength(name, 255, "Nama", parentFrame) ||
                !validateRequired(username, "Username", parentFrame) ||
                !validateMinLength(username, 3, "Username", parentFrame) ||
                !validateMaxLength(username, 8, "Username", parentFrame) ||
                !validateRequired(phone, "Nomor telepon", parentFrame) ||
                !validateMaxLength(phone, 15, "Nomor telepon", parentFrame) ||
                !validateRequired(address, "Alamat", parentFrame) ||
                !validateMaxLength(address, 500, "Alamat", parentFrame)) {
            return false;
        }

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            showError(parentFrame, "Username hanya boleh mengandung huruf, angka, dan underscore");
            return false;
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            showError(parentFrame, "Nomor telepon hanya boleh mengandung angka");
            return false;
        }

        return true;
    }

    // === CHANGE PASSWORD ===
    public boolean changePassword(String currentPassword, String newPassword, String confirmPassword, JFrame parentFrame) {
        if (currentUser == null) {
            showError(parentFrame, "User tidak login");
            return false;
        }

        // Validasi input menggunakan BaseService
        if (!validatePasswordChange(currentPassword, newPassword, confirmPassword, parentFrame)) {
            return false;
        }

        try {
            // Verifikasi password saat ini
            if (!currentUser.getPassword().equals(currentPassword)) {
                showError(parentFrame, "Password saat ini salah");
                return false;
            }

            // Update password
            currentUser.setPassword(newPassword);
            currentUser.setUpdatedAt(new Date());

            boolean success = userDao.updateUser(currentUser);
            if (success) {
                // Log aktivitas menggunakan BaseService
                logActivity("Change Password", "User mengubah password");

                showSuccess(parentFrame, "Password berhasil diubah!");
                return true;
            } else {
                showError(parentFrame, "Gagal mengubah password");
                return false;
            }

        } catch (Exception e) {
            showError(parentFrame, "Terjadi kesalahan saat mengubah password: " + e.getMessage());
            return false;
        }
    }

    private boolean validatePasswordChange(String currentPassword, String newPassword, String confirmPassword, JFrame parentFrame) {
        // Validasi menggunakan BaseService
        if (!validateRequired(currentPassword, "Password saat ini", parentFrame) ||
                !validateRequired(newPassword, "Password baru", parentFrame) ||
                !validateMinLength(newPassword, 8, "Password baru", parentFrame) ||
                !validateMaxLength(newPassword, 20, "Password baru", parentFrame)) {
            return false;
        }

        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            showError(parentFrame, "Password harus mengandung huruf besar, huruf kecil, dan angka");
            return false;
        }
        if (!newPassword.equals(confirmPassword)) {
            showError(parentFrame, "Konfirmasi password tidak sesuai");
            return false;
        }

        return true;
    }

    // === LOGOUT ===
    public boolean logout(JFrame parentFrame) {
        if (currentUser == null) {
            showError(parentFrame, "Tidak ada user yang login");
            return false;
        }

        try {
            // Log aktivitas menggunakan BaseService
            logActivity("Logout", "User logout dari sistem: " + currentUser.getUsername());

            // Clear current user
            currentUser = null;

            showSuccess(parentFrame, "Logout berhasil!");
            return true;

        } catch (Exception e) {
            showError(parentFrame, "Terjadi kesalahan saat logout: " + e.getMessage());
            return false;
        }
    }

    // === UTILITY METHODS ===
    private boolean isEmail(String input) {
        return EMAIL_PATTERN.matcher(input).matches();
    }

    // === GETTERS ===
    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static String getDashboardRoute() {
        if (currentUser == null) return "login";

        switch (currentUser.getRole()) {
            case "admin":
                return "admin.dashboard";
            case "kasir":
                return "kasir.dashboard";
            case "owner":
                return "owner.dashboard";
            default:
                return "dashboard";
        }
    }

    public static String getCurrentUserRoleFormatted() {
        if (currentUser == null) return "Tidak Login";

        switch (currentUser.getRole()) {
            case "admin": return "Admin";
            case "kasir": return "Kasir";
            case "owner": return "Owner";
            default: return currentUser.getRole();
        }
    }

    public static String getCurrentUserOutletName() {
        if (currentUser == null) return "Tidak ada outlet";
        return currentUser.getNamaOutlet();
    }
}
