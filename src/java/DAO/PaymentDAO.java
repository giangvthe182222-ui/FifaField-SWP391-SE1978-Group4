package DAO;

import Models.Payment;
import Models.PaymentLog;
import Utils.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;
import java.math.BigDecimal;

/**
 * PaymentDAO - Handle payment operations for VietQR Banking
 */
public class PaymentDAO {

    private String lastError;

    public String getLastError() {
        return lastError;
    }

    private void setLastError(String message) {
        this.lastError = message;
    }

    /**
     * Create a new payment record for a booking
     */
    public boolean createPayment(Payment payment) {
        setLastError(null);
        String sql = "INSERT INTO Payment (payment_id, booking_id, amount, payment_method, payment_status, "
                + "transaction_code, qr_content, bank_code, account_number) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, payment.getPaymentId().toString());
            ps.setString(2, payment.getBookingId().toString());
            ps.setBigDecimal(3, payment.getAmount());
            ps.setString(4, payment.getPaymentMethod());
            ps.setString(5, payment.getPaymentStatus());
            ps.setString(6, payment.getTransactionCode());
            ps.setString(7, payment.getQrContent());
            ps.setString(8, payment.getBankCode());
            ps.setString(9, payment.getAccountNumber());

            int affected = ps.executeUpdate();
            if (affected <= 0) {
                setLastError("Cannot create payment row in database.");
            }
            return affected > 0;

        } catch (SQLException e) {
            String message = e.getMessage() == null ? "" : e.getMessage();
            if (message.toLowerCase().contains("invalid column name")
                    && (message.toLowerCase().contains("transaction_code")
                    || message.toLowerCase().contains("qr_content")
                    || message.toLowerCase().contains("bank_code")
                    || message.toLowerCase().contains("account_number"))) {
                setLastError("Payment schema is outdated. Please run database/payment_system_update.sql to add required payment columns.");
            } else {
                setLastError("SQL error while creating payment: " + message);
            }
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            setLastError("Error while creating payment: " + (e.getMessage() == null ? "unknown" : e.getMessage()));
            e.printStackTrace();
            return false;
        }
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
     * Get payment by payment ID
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
     * Update payment status to SUCCESS
     */
    public boolean updatePaymentSuccess(UUID paymentId) {
        String sql = "UPDATE Payment SET payment_status = 'SUCCESS', payment_time = SYSDATETIME() "
                + "WHERE payment_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, paymentId.toString());

            int affected = ps.executeUpdate();
            return affected > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update payment status to FAILED
     */
    public boolean updatePaymentFailed(UUID paymentId) {
        String sql = "UPDATE Payment SET payment_status = 'FAILED', payment_time = SYSDATETIME() "
                + "WHERE payment_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, paymentId.toString());

            int affected = ps.executeUpdate();
            return affected > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePaymentRefundPending(UUID bookingId) {
        return updatePaymentStatusByBooking(bookingId, "REFUND_PENDING", false);
    }

    public boolean updatePaymentRefunded(UUID bookingId) {
        return updatePaymentStatusByBooking(bookingId, "REFUNDED", true);
    }

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
     * Log payment check attempt
     */
    public boolean logPaymentCheck(PaymentLog log) {
        String sql = "INSERT INTO Payment_Log (payment_id, check_time, status, response_message) "
                + "VALUES (?, SYSDATETIME(), ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, log.getPaymentId().toString());
            ps.setString(2, log.getStatus());
            ps.setString(3, log.getResponseMessage());

            int affected = ps.executeUpdate();
            return affected > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all pending payments that are expired (past payment_deadline)
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
     * Extract Payment object from ResultSet
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

        payment.setTransactionCode(rs.getString("transaction_code"));
        payment.setQrContent(rs.getString("qr_content"));
        payment.setBankCode(rs.getString("bank_code"));
        payment.setAccountNumber(rs.getString("account_number"));

        return payment;
    }

    /**
     * Check if payment exists for a booking
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
