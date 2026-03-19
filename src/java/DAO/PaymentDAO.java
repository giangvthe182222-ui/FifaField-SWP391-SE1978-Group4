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

    //Creator: GitHub Copilot
    //Description: Retrieves and prepares data for getLastError by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public String getLastError() {
        // Internal Flow: query data source, map database rows to model objects, and return null/empty values safely on edge cases.
        return lastError;
    }

    //Creator: GitHub Copilot
    //Description: Implements the setLastError business routine with validation, database interaction, exception handling, and predictable outputs for upstream controllers/services.
    private void setLastError(String message) {
        // Internal Flow: apply guard checks, execute core logic, and keep exception handling localized to DAO responsibilities.
        this.lastError = message;
    }

    /**
     * Create a new payment record for a booking
     */
    //Creator: GitHub Copilot
    //Description: Executes the createPayment write workflow, including input normalization, transactional SQL updates/inserts, consistency checks, and explicit success/failure signaling for calling services.
    public boolean createPayment(Payment payment) {
        // Internal Flow: validate inputs, run transactional SQL mutations, and propagate a clear commit/rollback result.
        setLastError(null);
        String sql = "INSERT INTO Payment (payment_id, booking_id, weekly_group_id, amount, payment_method, payment_status, "
            + "transaction_code, qr_content, bank_code, account_number) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, payment.getPaymentId().toString());
            ps.setString(2, payment.getBookingId().toString());
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

    //Creator: GitHub Copilot
    //Description: Retrieves and prepares data for getPaymentByWeeklyGroupId by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public Payment getPaymentByWeeklyGroupId(UUID weeklyGroupId) {
        // Internal Flow: query data source, map database rows to model objects, and return null/empty values safely on edge cases.
        String sql = "SELECT * FROM Payment WHERE weekly_group_id = ? ORDER BY payment_time DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, weeklyGroupId.toString());
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
     * Get payment by booking ID
     */
    //Creator: GitHub Copilot
    //Description: Retrieves and prepares data for getPaymentByBookingId by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public Payment getPaymentByBookingId(UUID bookingId) {
        // Internal Flow: query data source, map database rows to model objects, and return null/empty values safely on edge cases.
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
    //Creator: GitHub Copilot
    //Description: Retrieves and prepares data for getPaymentById by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public Payment getPaymentById(UUID paymentId) {
        // Internal Flow: query data source, map database rows to model objects, and return null/empty values safely on edge cases.
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
    //Creator: GitHub Copilot
    //Description: Executes the updatePaymentSuccess write workflow, including input normalization, transactional SQL updates/inserts, consistency checks, and explicit success/failure signaling for calling services.
    public boolean updatePaymentSuccess(UUID paymentId) {
        // Internal Flow: validate inputs, run transactional SQL mutations, and propagate a clear commit/rollback result.
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
    //Creator: GitHub Copilot
    //Description: Executes the updatePaymentFailed write workflow, including input normalization, transactional SQL updates/inserts, consistency checks, and explicit success/failure signaling for calling services.
    public boolean updatePaymentFailed(UUID paymentId) {
        // Internal Flow: validate inputs, run transactional SQL mutations, and propagate a clear commit/rollback result.
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

    //Creator: GitHub Copilot
    //Description: Executes the updatePaymentRefundPending write workflow, including input normalization, transactional SQL updates/inserts, consistency checks, and explicit success/failure signaling for calling services.
    public boolean updatePaymentRefundPending(UUID bookingId) {
        // Internal Flow: validate inputs, run transactional SQL mutations, and propagate a clear commit/rollback result.
        return updatePaymentStatusByBooking(bookingId, "REFUND_PENDING", false);
    }

    //Creator: GitHub Copilot
    //Description: Executes the updatePaymentRefunded write workflow, including input normalization, transactional SQL updates/inserts, consistency checks, and explicit success/failure signaling for calling services.
    public boolean updatePaymentRefunded(UUID bookingId) {
        // Internal Flow: validate inputs, run transactional SQL mutations, and propagate a clear commit/rollback result.
        return updatePaymentStatusByBooking(bookingId, "REFUNDED", true);
    }

    //Creator: GitHub Copilot
    //Description: Retrieves and prepares data for getPaymentByTransactionCode by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public Payment getPaymentByTransactionCode(String transactionCode) {
        // Internal Flow: query data source, map database rows to model objects, and return null/empty values safely on edge cases.
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
    //Creator: GitHub Copilot
    //Description: Implements the logPaymentCheck business routine with validation, database interaction, exception handling, and predictable outputs for upstream controllers/services.
    public boolean logPaymentCheck(PaymentLog log) {
        // Internal Flow: apply guard checks, execute core logic, and keep exception handling localized to DAO responsibilities.
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
    //Creator: GitHub Copilot
    //Description: Retrieves and prepares data for getExpiredPendingPayments by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public java.util.List<Payment> getExpiredPendingPayments() {
        // Internal Flow: query data source, map database rows to model objects, and return null/empty values safely on edge cases.
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
    //Creator: GitHub Copilot
    //Description: Applies normalization/mapping logic in extractPaymentFromResultSet to keep data format stable across DAO boundaries and reduce duplicate parsing logic in higher layers.
    private Payment extractPaymentFromResultSet(ResultSet rs) throws SQLException {
        // Internal Flow: query data source, map database rows to model objects, and return null/empty values safely on edge cases.
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
        try {
            String wg = rs.getString("weekly_group_id");
            if (wg != null && !wg.isBlank()) {
                payment.setWeeklyGroupId(UUID.fromString(wg));
            }
        } catch (SQLException ignored) {
            // Backward compatibility when schema has not been migrated yet.
        }

        return payment;
    }

    /**
     * Check if payment exists for a booking
     */
    //Creator: GitHub Copilot
    //Description: Implements the paymentExistsForBooking business routine with validation, database interaction, exception handling, and predictable outputs for upstream controllers/services.
    public boolean paymentExistsForBooking(UUID bookingId) {
        // Internal Flow: apply guard checks, execute core logic, and keep exception handling localized to DAO responsibilities.
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

    //Creator: GitHub Copilot
    //Description: Executes the markSupplementaryPending write workflow, including input normalization, transactional SQL updates/inserts, consistency checks, and explicit success/failure signaling for calling services.
    public boolean markSupplementaryPending(UUID bookingId, BigDecimal amount) {
        // Internal Flow: validate inputs, run transactional SQL mutations, and propagate a clear commit/rollback result.
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

    //Creator: GitHub Copilot
    //Description: Executes the updatePaymentStatusByBooking write workflow, including input normalization, transactional SQL updates/inserts, consistency checks, and explicit success/failure signaling for calling services.
    private boolean updatePaymentStatusByBooking(UUID bookingId, String paymentStatus, boolean touchPaymentTime) {
        // Internal Flow: validate inputs, run transactional SQL mutations, and propagate a clear commit/rollback result.
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
