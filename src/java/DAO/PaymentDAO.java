package DAO;

import Models.Payment;
import Utils.DBConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;
import java.math.BigDecimal;

public class PaymentDAO {

    // Constants for VietQR
    public static final String BANK_CODE = "VCB";
    public static final String ACCOUNT_NUMBER = "123456789";
    public static final String ACCOUNT_NAME = "FIFA FIELD";
    public static final long PAYMENT_TIMEOUT_MINUTES = 1;

    /**
     * Create payment record for booking
     */
    public static boolean createPayment(UUID bookingId, BigDecimal amount, String qrContent) {
        String sql = "INSERT INTO Payment (payment_id, booking_id, amount, payment_method, "
                + "payment_status, payment_time, qr_content, bank_code, account_number) "
                + "VALUES (?, ?, ?, 'VietQR', 'PENDING', SYSDATETIME(), ?, 'VCB', '123456789')";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            UUID paymentId = UUID.randomUUID();
            pstmt.setString(1, paymentId.toString());
            pstmt.setString(2, bookingId.toString());
            pstmt.setBigDecimal(3, amount);
            pstmt.setString(4, qrContent);

            int result = pstmt.executeUpdate();

            // Set payment_deadline on Booking
            if (result > 0) {
                updateBookingPaymentDeadline(bookingId);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Update payment deadline on booking
     */
    private static void updateBookingPaymentDeadline(UUID bookingId) {
        String sql = "UPDATE Booking SET payment_deadline = DATEADD(MINUTE, 1, SYSDATETIME()) "
                + "WHERE booking_id = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, bookingId.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get payment by booking ID
     */
    public static Payment getPaymentByBookingId(UUID bookingId) {
        String sql = "SELECT * FROM Payment WHERE booking_id = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, bookingId.toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Payment payment = new Payment();
                payment.setPaymentId(UUID.fromString(rs.getString("payment_id")));
                payment.setBookingId(UUID.fromString(rs.getString("booking_id")));
                payment.setAmount(rs.getBigDecimal("amount"));
                payment.setPaymentMethod(rs.getString("payment_method"));
                payment.setPaymentStatus(rs.getString("payment_status"));

                Timestamp paymentTime = rs.getTimestamp("payment_time");
                if (paymentTime != null) {
                    payment.setPaymentTime(paymentTime.toLocalDateTime());
                }

                payment.setTransactionCode(rs.getString("transaction_code"));
                payment.setQrContent(rs.getString("qr_content"));
                payment.setBankCode(rs.getString("bank_code"));
                payment.setAccountNumber(rs.getString("account_number"));

                return payment;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get payment by payment ID
     */
    public static Payment getPaymentById(UUID paymentId) {
        String sql = "SELECT * FROM Payment WHERE payment_id = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, paymentId.toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Payment payment = new Payment();
                payment.setPaymentId(UUID.fromString(rs.getString("payment_id")));
                payment.setBookingId(UUID.fromString(rs.getString("booking_id")));
                payment.setAmount(rs.getBigDecimal("amount"));
                payment.setPaymentMethod(rs.getString("payment_method"));
                payment.setPaymentStatus(rs.getString("payment_status"));

                Timestamp paymentTime = rs.getTimestamp("payment_time");
                if (paymentTime != null) {
                    payment.setPaymentTime(paymentTime.toLocalDateTime());
                }

                payment.setTransactionCode(rs.getString("transaction_code"));
                payment.setQrContent(rs.getString("qr_content"));
                payment.setBankCode(rs.getString("bank_code"));
                payment.setAccountNumber(rs.getString("account_number"));

                return payment;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Update payment status
     */
    public static boolean updatePaymentStatus(UUID paymentId, String status, String transactionCode) {
        String sql = "UPDATE Payment SET payment_status = ?, transaction_code = ?, payment_time = SYSDATETIME() "
                + "WHERE payment_id = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setString(2, transactionCode);
            pstmt.setString(3, paymentId.toString());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Log payment check attempt
     */
    public static void logPaymentCheck(UUID paymentId, String status, String message) {
        String sql = "INSERT INTO Payment_Log (payment_id, status, response_message) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, paymentId.toString());
            pstmt.setString(2, status);
            pstmt.setString(3, message);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if booking payment is overdue and cancel if needed
     * Also restore schedule and equipment stock
     */
    public static boolean checkAndCancelOverdueBookings() {
        String sql = "SELECT b.booking_id, p.payment_id FROM Booking b "
            + "LEFT JOIN Payment p ON p.booking_id = b.booking_id "
            + "WHERE UPPER(b.status) = 'PENDING' AND payment_deadline < SYSDATETIME()";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();
            boolean anyCancelled = false;
            
            while (rs.next()) {
                String bookingId = rs.getString("booking_id");
                String paymentId = rs.getString("payment_id");
                // Call BookingDAO to properly cancel booking with schedule/equipment restore
                BookingDAO dao = new BookingDAO();
                if (dao.cancelBookingByTimeout(java.util.UUID.fromString(bookingId))) {
                    if (paymentId != null) {
                        updatePaymentStatus(java.util.UUID.fromString(paymentId), "CANCELLED", "TIMEOUT");
                    }
                    anyCancelled = true;
                }
            }
            return anyCancelled;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get time remaining for payment (in seconds)
     */
    public static long getTimeRemainingForPayment(UUID bookingId) {
        String sql = "SELECT DATEDIFF(SECOND, SYSDATETIME(), payment_deadline) as seconds_remaining "
                + "FROM Booking WHERE booking_id = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, bookingId.toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                long seconds = rs.getLong("seconds_remaining");
                return Math.max(0, seconds); // Return 0 if already expired
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Verify payment by checking if transaction matches booking amount In real
     * scenario, you would call bank API to verify transaction
     */
    public static boolean verifyPaymentFromBank(UUID paymentId, String transactionCode) {
        // TODO: Implement actual bank API verification
        // This is a placeholder for real payment verification logic

        Payment payment = getPaymentById(paymentId);
        if (payment == null) {
            return false;
        }

        // Log the check attempt
        logPaymentCheck(paymentId, "CHECKING", "Verifying transaction: " + transactionCode);

        // In production, you would call bank API here
        // For now, we'll just mark as verified
        return true;
    }
}
