package Service;

import java.text.NumberFormat;
import java.util.Locale;

public class FormatHelper {

    // Format Rupiah
    public static String rupiah(int amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return format.format(amount);
    }

    // Format Persen
    public static String persen(double value) {
        return String.format("%.0f%%", value * 100);
    }

    // Format Tanggal Indonesia
    public static String formatTanggal(java.util.Date date) {
        if (date == null) return "-";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMMM yyyy");
        return sdf.format(date);
    }

    // Format Tanggal dan Waktu Indonesia
    public static String formatTanggalWaktu(java.util.Date date) {
        if (date == null) return "-";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMMM yyyy HH:mm");
        return sdf.format(date);
    }

    // Format Telepon Indonesia
    public static String formatTelepon(String phone) {
        if (phone == null || phone.isEmpty()) return "-";
        if (phone.startsWith("0")) {
            return "+62 " + phone.substring(1);
        }
        return phone;
    }

    // Limit string length
    public static String limitString(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
}