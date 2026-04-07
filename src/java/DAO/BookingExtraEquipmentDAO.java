package DAO;

import Models.BookingEquipment;
import Utils.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class BookingExtraEquipmentDAO {

    private final BookingDataAccess bookingDataAccess;
    private final BookingStateDAO bookingStateDAO;

    public BookingExtraEquipmentDAO(BookingDataAccess bookingDataAccess, BookingStateDAO bookingStateDAO) {
        this.bookingDataAccess = bookingDataAccess;
        this.bookingStateDAO = bookingStateDAO;
    }

    public boolean markBookingPendingExtra(UUID bookingId) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            BookingStateDAO.BookingSnapshot snapshot = bookingStateDAO.getBookingSnapshot(conn, bookingId);
            if (snapshot == null || !BookingStateDAO.STATUS_CHECKED_IN.equals(snapshot.status)) {
                conn.rollback();
                return false;
            }

            if (!isBookingActive(snapshot, LocalDateTime.now())) {
                conn.rollback();
                return false;
            }

            boolean updated = bookingStateDAO.updateBookingStatus(conn, bookingId, BookingStateDAO.STATUS_PENDING_EXTRA, BookingStateDAO.STATUS_CHECKED_IN);
            if (!updated) {
                conn.rollback();
                return false;
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean settlePendingExtraStatus(UUID bookingId) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            BookingStateDAO.BookingSnapshot snapshot = bookingStateDAO.getBookingSnapshot(conn, bookingId);
            if (snapshot == null) {
                conn.rollback();
                return false;
            }

            if (!BookingStateDAO.STATUS_PENDING_EXTRA.equals(snapshot.status)) {
                conn.rollback();
                return true;
            }

            if (!isPaymentSuccessful(conn, bookingId)) {
                conn.rollback();
                return false;
            }

            String targetStatus = resolvePostSupplementaryStatus(snapshot, LocalDateTime.now());
            boolean updated = bookingStateDAO.updateBookingStatus(conn, bookingId, targetStatus, BookingStateDAO.STATUS_PENDING_EXTRA);
            if (!updated) {
                conn.rollback();
                return false;
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addEquipmentToBooking(UUID bookingId, List<BookingEquipment> equipmentList, BigDecimal additionalAmount) {
        bookingDataAccess.clearLastInsertError();

        if (equipmentList == null || equipmentList.isEmpty()) {
            bookingDataAccess.setLastInsertError("Please select at least one equipment item.");
            return false;
        }

        if (additionalAmount == null || additionalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            bookingDataAccess.setLastInsertError("Invalid equipment total.");
            return false;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            BookingStateDAO.BookingSnapshot snapshot = bookingStateDAO.getBookingSnapshot(conn, bookingId);
            if (snapshot == null) {
                conn.rollback();
                bookingDataAccess.setLastInsertError("Booking not found.");
                return false;
            }

            if (!BookingStateDAO.STATUS_CHECKED_IN.equals(snapshot.status)) {
                conn.rollback();
                bookingDataAccess.setLastInsertError("Only checked-in bookings can add equipment.");
                return false;
            }

            if (!isBookingActive(snapshot, LocalDateTime.now())) {
                conn.rollback();
                bookingDataAccess.setLastInsertError("Checked-in booking is outside its active time window.");
                return false;
            }

            String updateEquip = "UPDATE le "
                    + "SET le.quantity = le.quantity - ?, "
                    + "    le.status = CASE WHEN le.quantity - ? <= 0 THEN 'unavailable' ELSE 'available' END "
                    + "FROM Location_Equipment le "
                    + "INNER JOIN Field f ON f.location_id = le.location_id "
                    + "INNER JOIN Booking b ON b.field_id = f.field_id "
                    + "WHERE b.booking_id = ? AND le.equipment_id = ? AND le.quantity >= ?";

            try (PreparedStatement ps = conn.prepareStatement(updateEquip)) {
                for (BookingEquipment be : equipmentList) {
                    ps.setInt(1, be.getQuantity());
                    ps.setInt(2, be.getQuantity());
                    ps.setString(3, bookingId.toString());
                    ps.setString(4, be.getEquipmentId().toString());
                    ps.setInt(5, be.getQuantity());

                    int affected = ps.executeUpdate();
                    if (affected == 0) {
                        conn.rollback();
                        bookingDataAccess.setLastInsertError("Equipment stock changed. Please review selected quantities.");
                        return false;
                    }
                }
            }

            String updateExistingEquipment = "UPDATE Booking_Equipment SET quantity = quantity + ? WHERE booking_id = ? AND equipment_id = ?";
            String insertNewEquipment = "INSERT INTO Booking_Equipment (booking_id, equipment_id, quantity) VALUES (?, ?, ?)";

            try (PreparedStatement updatePs = conn.prepareStatement(updateExistingEquipment); PreparedStatement insertPs = conn.prepareStatement(insertNewEquipment)) {
                for (BookingEquipment be : equipmentList) {
                    updatePs.setInt(1, be.getQuantity());
                    updatePs.setString(2, bookingId.toString());
                    updatePs.setString(3, be.getEquipmentId().toString());

                    int updated = updatePs.executeUpdate();
                    if (updated == 0) {
                        insertPs.setString(1, bookingId.toString());
                        insertPs.setString(2, be.getEquipmentId().toString());
                        insertPs.setInt(3, be.getQuantity());
                        insertPs.executeUpdate();
                    }
                }
            }

            try (PreparedStatement bookingPs = conn.prepareStatement(
                    "UPDATE Booking SET total_price = ISNULL(total_price, 0) + ? WHERE booking_id = ?")) {
                bookingPs.setBigDecimal(1, additionalAmount);
                bookingPs.setString(2, bookingId.toString());
                bookingPs.executeUpdate();
            }

            try (PreparedStatement paymentPs = conn.prepareStatement(
                    "UPDATE Payment SET amount = ?, payment_status = 'PENDING', payment_time = SYSDATETIME() WHERE booking_id = ?")) {
                paymentPs.setBigDecimal(1, additionalAmount);
                paymentPs.setString(2, bookingId.toString());
                paymentPs.executeUpdate();
            }

            bookingStateDAO.updateBookingStatus(conn, bookingId, BookingStateDAO.STATUS_PENDING_EXTRA, BookingStateDAO.STATUS_CHECKED_IN);

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (bookingDataAccess.getLastInsertError() == null || bookingDataAccess.getLastInsertError().isBlank()) {
                bookingDataAccess.setLastInsertError("Database error while adding equipment: " + e.getMessage());
            }
            return false;
        }
    }

    public boolean finalizeSupplementaryEquipment(UUID bookingId, List<BookingEquipment> equipmentList, BigDecimal additionalAmount) {
        bookingDataAccess.clearLastInsertError();

        if (bookingId == null) {
            bookingDataAccess.setLastInsertError("Invalid booking.");
            return false;
        }
        if (equipmentList == null || equipmentList.isEmpty()) {
            bookingDataAccess.setLastInsertError("No equipment selected.");
            return false;
        }
        if (additionalAmount == null || additionalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            bookingDataAccess.setLastInsertError("Invalid equipment total.");
            return false;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            BookingStateDAO.BookingSnapshot snapshot = bookingStateDAO.getBookingSnapshot(conn, bookingId);
            if (snapshot == null) {
                conn.rollback();
                bookingDataAccess.setLastInsertError("Booking not found.");
                return false;
            }

            if (!BookingStateDAO.STATUS_PENDING_EXTRA.equals(snapshot.status) && !BookingStateDAO.STATUS_CHECKED_IN.equals(snapshot.status)) {
                conn.rollback();
                bookingDataAccess.setLastInsertError("Only pending-extra bookings can finalize supplementary equipment.");
                return false;
            }

            if (BookingStateDAO.STATUS_CHECKED_IN.equals(snapshot.status) && !isBookingActive(snapshot, LocalDateTime.now())) {
                conn.rollback();
                bookingDataAccess.setLastInsertError("Checked-in booking is outside its active time window.");
                return false;
            }

            String updateEquip = "UPDATE le "
                    + "SET le.quantity = le.quantity - ?, "
                    + "    le.status = CASE WHEN le.quantity - ? <= 0 THEN 'unavailable' ELSE 'available' END "
                    + "FROM Location_Equipment le "
                    + "INNER JOIN Field f ON f.location_id = le.location_id "
                    + "INNER JOIN Booking b ON b.field_id = f.field_id "
                    + "WHERE b.booking_id = ? AND le.equipment_id = ? AND le.quantity >= ?";

            try (PreparedStatement ps = conn.prepareStatement(updateEquip)) {
                for (BookingEquipment be : equipmentList) {
                    ps.setInt(1, be.getQuantity());
                    ps.setInt(2, be.getQuantity());
                    ps.setString(3, bookingId.toString());
                    ps.setString(4, be.getEquipmentId().toString());
                    ps.setInt(5, be.getQuantity());

                    int affected = ps.executeUpdate();
                    if (affected == 0) {
                        conn.rollback();
                        bookingDataAccess.setLastInsertError("Equipment stock changed. Please review selected quantities.");
                        return false;
                    }
                }
            }

            String updateExistingEquipment = "UPDATE Booking_Equipment SET quantity = quantity + ? WHERE booking_id = ? AND equipment_id = ?";
            String insertNewEquipment = "INSERT INTO Booking_Equipment (booking_id, equipment_id, quantity) VALUES (?, ?, ?)";

            try (PreparedStatement updatePs = conn.prepareStatement(updateExistingEquipment); PreparedStatement insertPs = conn.prepareStatement(insertNewEquipment)) {
                for (BookingEquipment be : equipmentList) {
                    updatePs.setInt(1, be.getQuantity());
                    updatePs.setString(2, bookingId.toString());
                    updatePs.setString(3, be.getEquipmentId().toString());

                    int updated = updatePs.executeUpdate();
                    if (updated == 0) {
                        insertPs.setString(1, bookingId.toString());
                        insertPs.setString(2, be.getEquipmentId().toString());
                        insertPs.setInt(3, be.getQuantity());
                        insertPs.executeUpdate();
                    }
                }
            }

            try (PreparedStatement bookingPs = conn.prepareStatement(
                    "UPDATE Booking SET total_price = ISNULL(total_price, 0) + ? WHERE booking_id = ?")) {
                bookingPs.setBigDecimal(1, additionalAmount);
                bookingPs.setString(2, bookingId.toString());
                bookingPs.executeUpdate();
            }

            if (BookingStateDAO.STATUS_CHECKED_IN.equals(snapshot.status)) {
                if (!bookingStateDAO.updateBookingStatus(conn, bookingId, BookingStateDAO.STATUS_PENDING_EXTRA, BookingStateDAO.STATUS_CHECKED_IN)) {
                    conn.rollback();
                    bookingDataAccess.setLastInsertError("Failed to update booking status after supplementary payment.");
                    return false;
                }
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (bookingDataAccess.getLastInsertError() == null || bookingDataAccess.getLastInsertError().isBlank()) {
                bookingDataAccess.setLastInsertError("Database error while finalizing supplementary equipment: " + e.getMessage());
            }
            return false;
        }
    }

    public BigDecimal getSupplementaryAmountByBookingId(UUID bookingId) {
        try (Connection conn = DBConnection.getConnection()) {
            return getSupplementaryAmount(conn, bookingId);
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    BigDecimal getSupplementaryAmount(Connection conn, UUID bookingId) throws SQLException {
        String sql = "SELECT ISNULL(b.total_price, 0) AS total_price, ISNULL(s.price, 0) AS field_price "
                + "FROM Booking b "
                + "LEFT JOIN Schedule s ON b.schedule_id = s.schedule_id "
                + "WHERE b.booking_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal totalPrice = rs.getBigDecimal("total_price");
                    BigDecimal fieldPrice = rs.getBigDecimal("field_price");
                    if (totalPrice == null) {
                        totalPrice = BigDecimal.ZERO;
                    }
                    if (fieldPrice == null) {
                        fieldPrice = BigDecimal.ZERO;
                    }
                    BigDecimal supplementary = totalPrice.subtract(fieldPrice);
                    return supplementary.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : supplementary;
                }
            }
        }

        return BigDecimal.ZERO;
    }

    boolean isBookingActive(BookingStateDAO.BookingSnapshot snapshot, LocalDateTime now) {
        if (snapshot == null || snapshot.scheduleStart == null || snapshot.scheduleEnd == null) {
            return false;
        }
        return !now.isBefore(snapshot.scheduleStart) && now.isBefore(snapshot.scheduleEnd);
    }

    String resolvePostSupplementaryStatus(BookingStateDAO.BookingSnapshot snapshot, LocalDateTime now) {
        if (snapshot != null && snapshot.scheduleEnd != null && !now.isBefore(snapshot.scheduleEnd)) {
            return BookingStateDAO.STATUS_COMPLETED;
        }
        return BookingStateDAO.STATUS_CHECKED_IN;
    }

    boolean isPaymentSuccessful(Connection conn, UUID bookingId) throws SQLException {
        String sql = "SELECT LOWER(ISNULL(payment_status, '')) AS payment_status FROM Payment WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                String paymentStatus = bookingStateDAO.normalizeStatus(rs.getString("payment_status"));
                return "success".equals(paymentStatus) || "paid".equals(paymentStatus);
            }
        }
    }
}
