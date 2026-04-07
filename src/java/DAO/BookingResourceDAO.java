package DAO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class BookingResourceDAO {

    void releaseBookingResources(Connection conn, UUID bookingId, UUID scheduleId) throws SQLException {
        releaseBookingResources(conn, bookingId, scheduleId, true);
    }

    void releaseBookingResources(Connection conn, UUID bookingId, UUID scheduleId, boolean releaseSchedule) throws SQLException {
        if (releaseSchedule && scheduleId != null) {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE Schedule SET status = 'available' WHERE schedule_id = ?")) {
                ps.setString(1, scheduleId.toString());
                ps.executeUpdate();
            }
        }

        String sql = "UPDATE le "
                + "SET le.quantity = le.quantity + be.quantity, "
                + "    le.status = CASE WHEN le.quantity + be.quantity > 0 THEN 'available' ELSE le.status END "
                + "FROM Booking_Equipment be "
                + "INNER JOIN Booking b ON b.booking_id = be.booking_id "
                + "INNER JOIN Field f ON f.field_id = b.field_id "
                + "INNER JOIN Location_Equipment le ON le.location_id = f.location_id AND le.equipment_id = be.equipment_id "
                + "WHERE be.booking_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingId.toString());
            ps.executeUpdate();
        }
    }

    boolean shouldReleaseSchedule(String lifecycleStatus) {
        String normalized = normalizeStatus(lifecycleStatus);
        return BookingStateDAO.STATUS_CANCELLED.equals(normalized) || BookingStateDAO.STATUS_REFUNDED.equals(normalized);
    }

    boolean shouldReleaseEquipment(String lifecycleStatus) {
        String normalized = normalizeStatus(lifecycleStatus);
        return BookingStateDAO.STATUS_CANCELLED.equals(normalized)
                || BookingStateDAO.STATUS_REFUNDED.equals(normalized)
                || BookingStateDAO.STATUS_COMPLETED.equals(normalized);
    }

    boolean hasOutstandingRemainingAmount(Connection conn, UUID bookingId) throws SQLException {
        String sql = "SELECT ISNULL(b.total_price, 0) AS total_price, "
                + "LOWER(ISNULL(b.payment_status, '')) AS booking_payment_status, "
                + "LOWER(ISNULL(b.extra_payment_status, '')) AS booking_extra_status, "
                + "ISNULL(p.amount, 0) AS paid_amount, "
                + "LOWER(ISNULL(p.payment_method, '')) AS payment_method, "
                + "LOWER(ISNULL(p.payment_status, '')) AS payment_status "
                + "FROM Booking b "
                + "OUTER APPLY ("
                + "    SELECT TOP 1 amount, payment_method, payment_status "
                + "    FROM Payment p "
                + "    WHERE p.booking_id = b.booking_id "
                + "    ORDER BY p.payment_time DESC"
                + ") p "
                + "WHERE b.booking_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }

                BigDecimal totalPrice = rs.getBigDecimal("total_price");
                BigDecimal paidAmount = rs.getBigDecimal("paid_amount");
                String paymentMethod = rs.getString("payment_method");
                String paymentStatus = rs.getString("payment_status");
                String bookingPaymentStatus = normalizeStatus(rs.getString("booking_payment_status"));
                String bookingExtraStatus = normalizeStatus(rs.getString("booking_extra_status"));

                if (totalPrice == null) {
                    totalPrice = BigDecimal.ZERO;
                }
                if (paidAmount == null) {
                    paidAmount = BigDecimal.ZERO;
                }

                if (BookingStateDAO.STATUS_PAID.equals(bookingPaymentStatus)
                        && (BookingStateDAO.EXTRA_PAYMENT_STATUS_NONE.equals(bookingExtraStatus)
                        || BookingStateDAO.EXTRA_PAYMENT_STATUS_PAID.equals(bookingExtraStatus))) {
                    return false;
                }

                if (BookingStateDAO.STATUS_PAID.equals(bookingPaymentStatus)
                        && BookingStateDAO.STATUS_PENDING_EXTRA.equals(bookingExtraStatus)) {
                    BigDecimal supplementaryOutstanding = totalPrice.subtract(paidAmount);
                    return supplementaryOutstanding.compareTo(BigDecimal.ZERO) > 0;
                }

                if (BookingStateDAO.STATUS_DEPOSITED.equals(bookingPaymentStatus)) {
                    return true;
                }

                if (BookingStateDAO.STATUS_PENDING_REFUND.equals(bookingPaymentStatus)
                        || BookingStateDAO.STATUS_REFUNDED.equals(bookingPaymentStatus)
                        || BookingStateDAO.PAYMENT_STATUS_FAILED.equals(bookingPaymentStatus)) {
                    return false;
                }

                if (paymentMethod != null && paymentMethod.contains("|remaining")) {
                    return !"success".equals(paymentStatus) && !"paid".equals(paymentStatus);
                }

                if ("success".equals(paymentStatus) || "paid".equals(paymentStatus)) {
                    return totalPrice.compareTo(paidAmount) > 0;
                }
                return false;
            }
        }
    }

    boolean applyCashSettlementForDepositedUpgrade(Connection conn,
                                                   UUID bookingId,
                                                   String previousPaymentStatus,
                                                   String nextPaymentStatus,
                                                   String nextExtraPaymentStatus) throws SQLException {
        if (!BookingStateDAO.STATUS_DEPOSITED.equals(normalizeStatus(previousPaymentStatus))
                || !BookingStateDAO.STATUS_PAID.equals(normalizeStatus(nextPaymentStatus))) {
            return true;
        }

        BookingStateDAO.LatestPaymentInfo latestPaymentInfo = getLatestPaymentInfo(conn, bookingId);
        if (latestPaymentInfo == null) {
            return false;
        }

        BigDecimal currentPaidAmount = latestPaymentInfo.amount == null
                ? BigDecimal.ZERO
                : latestPaymentInfo.amount;
        BigDecimal settledAmount = resolveCashSettledAmount(
                conn,
                bookingId,
                nextExtraPaymentStatus,
                currentPaidAmount,
                latestPaymentInfo.paymentMethod);
        String sql = "UPDATE Payment SET amount = ?, payment_status = 'SUCCESS', payment_time = SYSDATETIME() WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, settledAmount);
            ps.setString(2, bookingId.toString());
            return ps.executeUpdate() > 0;
        }
    }

    BigDecimal resolveCashSettledAmount(Connection conn,
                                        UUID bookingId,
                                        String nextExtraPaymentStatus,
                                        BigDecimal currentPaidAmount,
                                        String paymentMethod) throws SQLException {
        BigDecimal totalPrice = getBookingTotalPrice(conn, bookingId);
        if (!BookingStateDAO.STATUS_PENDING_EXTRA.equals(normalizeStatus(nextExtraPaymentStatus))) {
            return totalPrice.max(BigDecimal.ZERO);
        }

        BigDecimal supplementaryOutstanding = estimatePendingExtraOutstanding(
            totalPrice,
                currentPaidAmount == null ? BigDecimal.ZERO : currentPaidAmount,
                paymentMethod);

        BigDecimal settledAmount = totalPrice.subtract(supplementaryOutstanding);
        if (currentPaidAmount != null && settledAmount.compareTo(currentPaidAmount) < 0) {
            settledAmount = currentPaidAmount;
        }
        if (settledAmount.compareTo(totalPrice) > 0) {
            settledAmount = totalPrice;
        }
        return settledAmount.max(BigDecimal.ZERO);
    }

    BigDecimal estimatePendingExtraOutstanding(BigDecimal totalPrice,
                                               BigDecimal currentPaidAmount,
                                               String paymentMethod) {
        if (totalPrice == null || totalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal paidAmount = currentPaidAmount == null ? BigDecimal.ZERO : currentPaidAmount;
        String normalizedPaymentMethod = normalizeStatus(paymentMethod);

        if (normalizedPaymentMethod.contains("|deposit")
                && paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal estimatedBookedTotal = paidAmount.divide(BookingStateDAO.DEPOSIT_RATE, 0, RoundingMode.HALF_UP);
            BigDecimal supplementaryOutstanding = totalPrice.subtract(estimatedBookedTotal);
            if (supplementaryOutstanding.compareTo(BigDecimal.ZERO) < 0) {
                return BigDecimal.ZERO;
            }
            if (supplementaryOutstanding.compareTo(totalPrice) > 0) {
                return totalPrice;
            }
            return supplementaryOutstanding;
        }

        BigDecimal outstandingByPaidAmount = totalPrice.subtract(paidAmount);
        if (outstandingByPaidAmount.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        if (outstandingByPaidAmount.compareTo(totalPrice) > 0) {
            return totalPrice;
        }
        return outstandingByPaidAmount;
    }

    BookingStateDAO.LatestPaymentInfo getLatestPaymentInfo(Connection conn, UUID bookingId) throws SQLException {
        String sql = "SELECT TOP 1 amount, payment_method FROM Payment WHERE booking_id = ? ORDER BY payment_time DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new BookingStateDAO.LatestPaymentInfo(rs.getBigDecimal("amount"), rs.getString("payment_method"));
            }
        }
    }

    BigDecimal getBookingTotalPrice(Connection conn, UUID bookingId) throws SQLException {
        String sql = "SELECT ISNULL(total_price, 0) AS total_price FROM Booking WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal totalPrice = rs.getBigDecimal("total_price");
                    return totalPrice == null ? BigDecimal.ZERO : totalPrice;
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private String normalizeStatus(String status) {
        return status == null ? "" : status.trim().toLowerCase();
    }
}
