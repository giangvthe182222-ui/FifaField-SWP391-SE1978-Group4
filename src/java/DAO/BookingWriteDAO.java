package DAO;

import Models.Booking;
import Models.BookingEquipment;
import Utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class BookingWriteDAO {

    private final BookingDataAccess bookingDataAccess;
    private final BookingStateDAO bookingStateDAO;
    private final BookingResourceDAO bookingResourceDAO;

    public BookingWriteDAO(BookingDataAccess bookingDataAccess,
                           BookingStateDAO bookingStateDAO,
                           BookingResourceDAO bookingResourceDAO) {
        this.bookingDataAccess = bookingDataAccess;
        this.bookingStateDAO = bookingStateDAO;
        this.bookingResourceDAO = bookingResourceDAO;
    }

    public boolean insert(Booking booking, List<BookingEquipment> equipmentList) {
        bookingDataAccess.clearLastInsertError();

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            String updateSchedule = "UPDATE Schedule SET status = 'unavailable' WHERE schedule_id = ? AND status = 'available'";

            try (PreparedStatement ps = conn.prepareStatement(updateSchedule)) {
                ps.setString(1, booking.getScheduleId().toString());
                int affected = ps.executeUpdate();

                if (affected == 0) {
                    conn.rollback();
                    bookingDataAccess.setLastInsertError("Selected schedule is no longer available.");
                    return false;
                }
            }

            if (equipmentList != null && !equipmentList.isEmpty()) {
                String updateEquip = "UPDATE le "
                        + "SET le.quantity = le.quantity - ?, "
                        + "    le.status = CASE WHEN le.quantity - ? <= 0 THEN 'unavailable' ELSE 'available' END "
                        + "FROM Location_Equipment le "
                        + "INNER JOIN Field f ON f.location_id = le.location_id "
                        + "WHERE f.field_id = ? AND le.equipment_id = ? AND le.quantity >= ?";

                try (PreparedStatement ps = conn.prepareStatement(updateEquip)) {
                    for (BookingEquipment be : equipmentList) {
                        ps.setInt(1, be.getQuantity());
                        ps.setInt(2, be.getQuantity());
                        ps.setString(3, booking.getFieldId().toString());
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
            }

            if (!insertBookingRow(conn, booking)) {
                conn.rollback();
                return false;
            }

            if (equipmentList != null && !equipmentList.isEmpty()) {
                String insertEquip = "INSERT INTO Booking_Equipment (booking_id, equipment_id, quantity) VALUES (?, ?, ?)";

                try (PreparedStatement ps = conn.prepareStatement(insertEquip)) {
                    for (BookingEquipment be : equipmentList) {
                        ps.setString(1, booking.getBookingId().toString());
                        ps.setString(2, be.getEquipmentId().toString());
                        ps.setInt(3, be.getQuantity());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            if (bookingDataAccess.getLastInsertError() == null || bookingDataAccess.getLastInsertError().isBlank()) {
                bookingDataAccess.setLastInsertError("Database error while creating booking: " + e.getMessage());
            }
            return false;
        }
    }

    boolean insertBookingRow(Connection conn, Booking booking) throws SQLException {
        String sql = "INSERT INTO Booking (booking_id, booker_id, phone_number, field_id, schedule_id, voucher_id, play_status, payment_status, extra_payment_status, total_price, payment_deadline) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, booking.getBookingId().toString());
            ps.setString(2, booking.getBookerId().toString());

            if (booking.getPhoneNumber() != null && !booking.getPhoneNumber().trim().isEmpty()) {
                ps.setString(3, booking.getPhoneNumber().trim());
            } else {
                ps.setNull(3, Types.NVARCHAR);
            }

            ps.setString(4, booking.getFieldId().toString());
            ps.setString(5, booking.getScheduleId().toString());
            if (booking.getVoucherId() != null) {
                ps.setString(6, booking.getVoucherId().toString());
            } else {
                ps.setNull(6, Types.VARCHAR);
            }

            String playStatus = bookingStateDAO.normalizeStatus(booking.getPlayStatus());
            String paymentStatus = bookingStateDAO.normalizeStatus(booking.getPaymentStatus());
            String extraPaymentStatus = bookingStateDAO.normalizeStatus(booking.getExtraPaymentStatus());
            ps.setString(7, playStatus);
            ps.setString(8, paymentStatus);
            ps.setString(9, extraPaymentStatus);
            ps.setBigDecimal(10, booking.getTotalPrice());

            if (booking.getPaymentDeadline() != null) {
                ps.setTimestamp(11, Timestamp.valueOf(booking.getPaymentDeadline()));
            } else {
                ps.setNull(11, Types.TIMESTAMP);
            }

            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            bookingDataAccess.setLastInsertError("Cannot insert booking record: " + ex.getMessage());
            return false;
        }
    }

    public String getLastInsertError() {
        return bookingDataAccess.getLastInsertError();
    }

    public boolean cancelBooking(UUID bookingId) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            BookingStateDAO.BookingSnapshot snapshot = bookingStateDAO.getBookingSnapshot(conn, bookingId);
            if (snapshot == null) {
                conn.rollback();
                return false;
            }

            if (BookingStateDAO.STATUS_PENDING.equals(snapshot.status)) {
                conn.rollback();
                return cancelBookingForPayment(bookingId);
            }

            if ((!BookingStateDAO.STATUS_PAID.equals(snapshot.status) && !BookingStateDAO.STATUS_DEPOSITED.equals(snapshot.status))
                    || snapshot.scheduleStart == null) {
                conn.rollback();
                return false;
            }

            if (!snapshot.scheduleStart.isAfter(LocalDateTime.now().plusDays(2))) {
                conn.rollback();
                return false;
            }

            boolean updated = bookingStateDAO.updateBookingStatus(conn, bookingId, BookingStateDAO.STATUS_PENDING_REFUND, snapshot.status);
            if (!updated) {
                conn.rollback();
                return false;
            }

            updatePaymentStatusByBooking(conn, bookingId, "REFUND_PENDING", false);
            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean cancelBookingForPayment(UUID bookingId) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            BookingStateDAO.BookingSnapshot snapshot = bookingStateDAO.getBookingSnapshot(conn, bookingId);
            if (snapshot == null) {
                conn.rollback();
                return false;
            }

            if (BookingStateDAO.STATUS_CANCELLED.equals(snapshot.status)) {
                conn.rollback();
                return true;
            }

            if (!BookingStateDAO.STATUS_PENDING.equals(snapshot.status)) {
                conn.rollback();
                return false;
            }

            bookingStateDAO.updateBookingStatus(conn, bookingId, BookingStateDAO.STATUS_CANCELLED, BookingStateDAO.STATUS_PENDING);
            updatePaymentStatusByBooking(conn, bookingId, "FAILED", true);
            bookingResourceDAO.releaseBookingResources(conn, bookingId, snapshot.scheduleId);
            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean resetPaymentDeadline(UUID bookingId, LocalDateTime newDeadline) {
        if (bookingId == null || newDeadline == null) {
            return false;
        }

        String sql = "UPDATE Booking SET payment_deadline = ? WHERE booking_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(newDeadline));
            ps.setString(2, bookingId.toString());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Booking getBookingById(UUID bookingId) {
        bookingStateDAO.synchronizeBookingStates();
        String sql = "SELECT * FROM Booking WHERE booking_id = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingId.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Booking booking = new Booking();
                booking.setBookingId(UUID.fromString(rs.getString("booking_id")));
                booking.setBookerId(UUID.fromString(rs.getString("booker_id")));
                booking.setFieldId(UUID.fromString(rs.getString("field_id")));
                booking.setScheduleId(UUID.fromString(rs.getString("schedule_id")));

                String voucherId = rs.getString("voucher_id");
                if (voucherId != null) {
                    booking.setVoucherId(UUID.fromString(voucherId));
                }

                Timestamp bookingTime = rs.getTimestamp("booking_time");
                if (bookingTime != null) {
                    booking.setBookingTime(bookingTime.toLocalDateTime());
                }

                bookingStateDAO.applyStateToBookingEntity(rs, booking);
                booking.setPhoneNumber(rs.getString("phone_number"));
                booking.setTotalPrice(rs.getBigDecimal("total_price"));

                Timestamp paymentDeadline = rs.getTimestamp("payment_deadline");
                if (paymentDeadline != null) {
                    booking.setPaymentDeadline(paymentDeadline.toLocalDateTime());
                }

                return booking;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updatePaymentStatusByBooking(Connection conn, UUID bookingId, String paymentStatus, boolean touchPaymentTime) throws SQLException {
        String sql = touchPaymentTime
                ? "UPDATE Payment SET payment_status = ?, payment_time = SYSDATETIME() WHERE booking_id = ?"
                : "UPDATE Payment SET payment_status = ? WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paymentStatus);
            ps.setString(2, bookingId.toString());
            ps.executeUpdate();
        }
    }
}
