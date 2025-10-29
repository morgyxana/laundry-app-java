package Service;

import Dao.TbLogDao;
import Model.TbLog;
import Model.User;
import javax.swing.*;
import java.util.Date;

/**
 * Base Service class yang menyediakan functionality umum
 * untuk semua service classes (setara dengan Controller di Laravel)
 */
public abstract class BaseService {
    protected TbLogDao tbLogDao;
    protected AuthService authService;

    public BaseService() {
        this.tbLogDao = new TbLogDao();
        this.authService = new AuthService();
    }

    // === AUTHORIZATION & VALIDATION METHODS ===

    /**
     * Memeriksa apakah user sudah login
     */
    protected boolean checkAuthentication(JFrame parentFrame) {
        if (!AuthService.isLoggedIn()) {
            showError(parentFrame, "Anda harus login terlebih dahulu");
            return false;
        }
        return true;
    }

    /**
     * Memeriksa role user (setara dengan middleware di Laravel)
     */
    protected boolean checkRole(String requiredRole, JFrame parentFrame) {
        if (!checkAuthentication(parentFrame)) {
            return false;
        }

        User currentUser = AuthService.getCurrentUser();
        if (!requiredRole.equals(currentUser.getRole())) {
            showError(parentFrame, "Anda tidak memiliki akses untuk fitur ini");
            return false;
        }
        return true;
    }

    /**
     * Memeriksa multiple roles
     */
    protected boolean checkRoles(String[] requiredRoles, JFrame parentFrame) {
        if (!checkAuthentication(parentFrame)) {
            return false;
        }

        User currentUser = AuthService.getCurrentUser();
        for (String role : requiredRoles) {
            if (role.equals(currentUser.getRole())) {
                return true;
            }
        }

        showError(parentFrame, "Anda tidak memiliki akses untuk fitur ini");
        return false;
    }

    // === LOGGING METHODS (setara dengan TbLog di Laravel) ===

    /**
     * Log aktivitas (general purpose)
     */
    protected void logActivity(String aktivitas, String dataTerkait) {
        try {
            if (AuthService.isLoggedIn()) {
                TbLog log = new TbLog();
                log.setIdUser(AuthService.getCurrentUser().getId());
                log.setAktivitas(aktivitas);
                log.setTanggal(new Date());
                log.setDataTerkait(dataTerkait);

                tbLogDao.insertLog(log);
            }
        } catch (Exception e) {
            System.err.println("Gagal mencatat log: " + e.getMessage());
        }
    }

    /**
     * Log untuk view actions
     */
    protected void logView(String entity, String description) {
        logActivity("Melihat " + entity, description);
    }

    /**
     * Log untuk create actions
     */
    protected void logCreate(String entity, String data) {
        logActivity("Menambah " + entity, data);
    }

    /**
     * Log untuk update actions
     */
    protected void logUpdate(String entity, String data) {
        logActivity("Mengubah " + entity, data);
    }

    /**
     * Log untuk delete actions
     */
    protected void logDelete(String entity, String data) {
        logActivity("Menghapus " + entity, data);
    }

    // === VALIDATION METHODS (setara dengan ValidatesRequests di Laravel) ===

    /**
     * Validasi required field
     */
    protected boolean validateRequired(String value, String fieldName, JFrame parentFrame) {
        if (value == null || value.trim().isEmpty()) {
            showError(parentFrame, fieldName + " wajib diisi");
            return false;
        }
        return true;
    }

    /**
     * Validasi max length
     */
    protected boolean validateMaxLength(String value, int maxLength, String fieldName, JFrame parentFrame) {
        if (value != null && value.length() > maxLength) {
            showError(parentFrame, fieldName + " maksimal " + maxLength + " karakter");
            return false;
        }
        return true;
    }

    /**
     * Validasi min length
     */
    protected boolean validateMinLength(String value, int minLength, String fieldName, JFrame parentFrame) {
        if (value != null && value.length() < minLength) {
            showError(parentFrame, fieldName + " minimal " + minLength + " karakter");
            return false;
        }
        return true;
    }

    /**
     * Validasi numeric
     */
    protected boolean validateNumeric(String value, String fieldName, JFrame parentFrame) {
        if (value != null && !value.trim().isEmpty()) {
            try {
                Double.parseDouble(value);
            } catch (NumberFormatException e) {
                showError(parentFrame, fieldName + " harus berupa angka");
                return false;
            }
        }
        return true;
    }

    /**
     * Validasi integer
     */
    protected boolean validateInteger(String value, String fieldName, JFrame parentFrame) {
        if (value != null && !value.trim().isEmpty()) {
            try {
                Integer.parseInt(value);
            } catch (NumberFormatException e) {
                showError(parentFrame, fieldName + " harus berupa bilangan bulat");
                return false;
            }
        }
        return true;
    }

    /**
     * Validasi range numeric
     */
    protected boolean validateRange(double value, double min, double max, String fieldName, JFrame parentFrame) {
        if (value < min || value > max) {
            showError(parentFrame, fieldName + " harus antara " + min + " dan " + max);
            return false;
        }
        return true;
    }

    // === UTILITY METHODS ===

    /**
     * Show error message (reusable)
     */
    protected void showError(JFrame parentFrame, String message) {
        JOptionPane.showMessageDialog(parentFrame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Show success message (reusable)
     */
    protected void showSuccess(JFrame parentFrame, String message) {
        JOptionPane.showMessageDialog(parentFrame, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Show warning message
     */
    protected void showWarning(JFrame parentFrame, String message) {
        JOptionPane.showMessageDialog(parentFrame, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Show confirmation dialog
     */
    protected boolean showConfirm(JFrame parentFrame, String message) {
        int result = JOptionPane.showConfirmDialog(parentFrame, message, "Konfirmasi",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    /**
     * Format JSON string untuk logging (sederhana)
     */
    protected String formatJson(String key, Object value) {
        return String.format("{\"%s\": \"%s\"}", key, value != null ? value.toString() : "");
    }

    /**
     * Format JSON multiple key-value
     */
    protected String formatJson(String[] keys, Object[] values) {
        if (keys.length != values.length) {
            return "{}";
        }

        StringBuilder json = new StringBuilder("{");
        for (int i = 0; i < keys.length; i++) {
            json.append(String.format("\"%s\": \"%s\"", keys[i], values[i] != null ? values[i].toString() : ""));
            if (i < keys.length - 1) {
                json.append(", ");
            }
        }
        json.append("}");
        return json.toString();
    }

    // === DATE/TIME UTILITIES ===

    /**
     * Get current timestamp
     */
    protected Date getCurrentTimestamp() {
        return new Date();
    }

    /**
     * Add days to date
     */
    protected Date addDays(Date date, int days) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(java.util.Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }

    // === ROLE-BASED ACCESS CONTROL ===

    /**
     * Check if current user is admin
     */
    protected boolean isAdmin() {
        return AuthService.isLoggedIn() && "admin".equals(AuthService.getCurrentUser().getRole());
    }

    /**
     * Check if current user is kasir
     */
    protected boolean isKasir() {
        return AuthService.isLoggedIn() && "kasir".equals(AuthService.getCurrentUser().getRole());
    }

    /**
     * Check if current user is owner
     */
    protected boolean isOwner() {
        return AuthService.isLoggedIn() && "owner".equals(AuthService.getCurrentUser().getRole());
    }

    /**
     * Get current user's outlet ID
     */
    protected int getCurrentUserOutletId() {
        return AuthService.isLoggedIn() ? AuthService.getCurrentUser().getIdOutlet() : 0;
    }

    /**
     * Redirect based on role (setara dengan redirectToDashboard di AuthController)
     */
    protected String getDashboardRoute() {
        return AuthService.getDashboardRoute();
    }
}
