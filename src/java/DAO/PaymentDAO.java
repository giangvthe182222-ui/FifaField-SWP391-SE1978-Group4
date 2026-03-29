package DAO;

import Models.Payment;
import Models.PaymentLog;
import Utils.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;
import java.math.BigDecimal;

/**
 * PaymentDAO
 * This class handles all DB operations related to Payment.
 * Basically, it's the bridge between your system and the Payment table.
 */
public class PaymentDAO {

    private String lastError;

    // Get the last error message (useful for debugging or returning to service layer)
    public String getLastError() {
        return lastError;
    }

    // Set internal error (only used inside DAO)
    private void setLastError(String message) {
        this.lastError = message;
    }

    /**
     * Create a new payment record
     * Each payment = one payment attempt (important: we don't reuse old ones)
     */
    public boolean createPayment(Payment payment) {
        setLastError(null); 

        String sql = "INSERT INTO Payment (payment_id, booking_id, weekly_group_id, amount, payment_method, payment_status, "
            + "transaction_code, qr_content, bank_code, account_number) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, payment.getPaymentId().toString());
            ps.setString(2, payment.getBookingId().toString());

            // Weekly booking group id (can be null as there are independent bookings)
            if (payment.getWeeklyGroupId() != null) {
                ps.setString(3, payment.getWeeklyGroupId().toString());
            } else {
                ps.setNull(3, Types.VARCHAR);
            }

            ps.setBigDecimal(4, payment.getAmount());
            ps.setString(5, payment.getPaymentMethod());
            ps.setString(6, payment.getPaymentStatus());
            ps.setString(7, payment.getTransactionCode()); 
            ps.setString(8, payment.getQrContent());
            ps.setString(9, payment.getBankCode());
            ps.setString(10, payment.getAccountNumber());

            int affected = ps.executeUpdate();

            if (affected <= 0) {
                setLastError("Cannot create payment row in database.");
            }

            return affected > 0;

        } catch (SQLException e) {
            String message = e.getMessage() == null ? "" : e.getMessage();

            // DB not updated 
            if (message.toLowerCase().contains("invalid column name")
                    && (message.toLowerCase().contains("transaction_code")
                    || message.toLowerCase().contains("qr_content")
                    || message.toLowerCase().contains("bank_code")
                    || message.toLowerCase().contains("account_number"))) {
                setLastError("database thieu roi.");
            } else {
                setLastError("SQL error while creating payment: " + message);
            }

            e.printStackTrace();
            return false;

        } catch (Exception e) {
            setLastError("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get the latest payment of a weekly group
     */
    public Payment getPaymentByWeeklyGroupId(UUID weeklyGroupId) {
        String sql = "SELECT * FROM Payment WHERE weekly_group_id = ? ORDER BY payment_time DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, weeklyGroupId.toString());
            ResultSet rs = ps.executeQuery();

            // only take the newest one (current active payment)
            if (rs.next()) {
                return extractPaymentFromResultSet(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get payment by booking ID
     */
    public Payment getPaymentByBookingId(UUID bookingId) {
        String sql = "SELECT * FROM Payment WHERE booking_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, bookingId.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return extractPaymentFromResultSet(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get payment by its primary ID
     */
    public Payment getPaymentById(UUID paymentId) {
        String sql = "SELECT * FROM Payment WHERE payment_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, paymentId.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return extractPaymentFromResultSet(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Mark payment as SUCCESS
     * Called when bank confirms the transaction
     */
    public boolean updatePaymentSuccess(UUID paymentId) {
        String sql = "UPDATE Payment SET payment_status = 'SUCCESS', payment_time = SYSDATETIME() "
                + "WHERE payment_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, paymentId.toString());
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Mark payment as FAILED
     * Used when payment fails or expires
     */
    public boolean updatePaymentFailed(UUID paymentId) {
        String sql = "UPDATE Payment SET payment_status = 'FAILED', payment_time = SYSDATETIME() "
                + "WHERE payment_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, paymentId.toString());
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Set payment to REFUND_PENDING
     * Means refund process has started but not done yet
     */
    public boolean updatePaymentRefundPending(UUID bookingId) {
        return updatePaymentStatusByBooking(bookingId, "REFUND_PENDING", false);
    }

    /**
     * Set payment to REFUNDED
     * Final state after money is returned to user
     */
    public boolean updatePaymentRefunded(UUID bookingId) {
        return updatePaymentStatusByBooking(bookingId, "REFUNDED", true);
    }

    /**
     * Find payment using transaction code from bank
     * Match incoming bank transactions to the system
     */
    public Payment getPaymentByTransactionCode(String transactionCode) {
        String sql = "SELECT * FROM Payment WHERE transaction_code = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, transactionCode);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return extractPaymentFromResultSet(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Log every time we check payment status with bank
     * Useful for debugging and tracking payment attempts
     */
    public boolean logPaymentCheck(PaymentLog log) {
        String sql = "INSERT INTO Payment_Log (payment_id, check_time, status, response_message) "
                + "VALUES (?, SYSDATETIME(), ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, log.getPaymentId().toString());
            ps.setString(2, log.getStatus());
            ps.setString(3, log.getResponseMessage());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all PENDING payments that are already expired
     * Usually used by background job to auto-cancel bookings
     */
    public java.util.List<Payment> getExpiredPendingPayments() {
        java.util.List<Payment> list = new java.util.ArrayList<>();

        String sql = "SELECT p.* FROM Payment p "
                + "INNER JOIN Booking b ON p.booking_id = b.booking_id "
                + "WHERE p.payment_status = 'PENDING' "
                + "AND b.payment_deadline < SYSDATETIME()";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(extractPaymentFromResultSet(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Convert DB row → Payment object
     * Central place for mapping to avoid duplicate code
     */
    private Payment extractPaymentFromResultSet(ResultSet rs) throws SQLException {
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

        // bank info
        payment.setTransactionCode(rs.getString("transaction_code"));
        payment.setQrContent(rs.getString("qr_content"));
        payment.setBankCode(rs.getString("bank_code"));
        payment.setAccountNumber(rs.getString("account_number"));

        // optional field (for backward compatibility)
        try {
            String wg = rs.getString("weekly_group_id");
            if (wg != null && !wg.isBlank()) {
                payment.setWeeklyGroupId(UUID.fromString(wg));
            }
        } catch (SQLException ignored) {}

        return payment;
    }

    /**
     * Check if a booking already has a payment
     * Used to prevent duplicate payment creation
     */
    public boolean paymentExistsForBooking(UUID bookingId) {
        String sql = "SELECT COUNT(*) FROM Payment WHERE booking_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, bookingId.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Update payment when extra cost is added (e.g., user adds services)
     * Reset status to PENDING so user needs to pay again
     */
    public boolean markSupplementaryPending(UUID bookingId, BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        String sql = "UPDATE Payment SET amount = ?, payment_status = 'PENDING', payment_time = SYSDATETIME() WHERE booking_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBigDecimal(1, amount);
            ps.setString(2, bookingId.toString());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Generic method to update payment status by booking
     * Helps avoid duplicate code for refund / other states
     */
    private boolean updatePaymentStatusByBooking(UUID bookingId, String paymentStatus, boolean touchPaymentTime) {

        String sql = touchPaymentTime
                ? "UPDATE Payment SET payment_status = ?, payment_time = SYSDATETIME() WHERE booking_id = ?"
                : "UPDATE Payment SET payment_status = ? WHERE booking_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, paymentStatus);
            ps.setString(2, bookingId.toString());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
