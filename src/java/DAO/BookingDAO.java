package DAO;

import Models.Booking;
import Models.BookingEquipment;
import Utils.DBConnection;

import java.sql.*;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import Models.BookingViewModel;
import Models.BookingEquipmentViewModel;

public class BookingDAO {

    private String lastInsertError;

    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_PAID = "paid";
    private static final String STATUS_CANCELLED = "cancelled";
    private static final String STATUS_PENDING_REFUND = "pending refund";
    private static final String STATUS_REFUNDED = "refunded";
    private static final String STATUS_CHECKED_IN = "checked in";
    private static final String STATUS_COMPLETED = "completed";

    private static final Set<String> SUPPORTED_STATUSES = new HashSet<>(Arrays.asList(
            STATUS_PENDING,
            STATUS_PAID,
            STATUS_CANCELLED,
            STATUS_PENDING_REFUND,
            STATUS_REFUNDED,
                STATUS_CHECKED_IN,
                STATUS_COMPLETED
    ));

    public boolean insert(Booking booking, List<BookingEquipment> equipmentList) {

    lastInsertError = null;

    try (Connection conn = DBConnection.getConnection()) {

        conn.setAutoCommit(false);

        // 1️⃣ Lock schedule
        String updateSchedule = "UPDATE Schedule SET status = 'unavailable' WHERE schedule_id = ? AND status = 'available'";

        try (PreparedStatement ps = conn.prepareStatement(updateSchedule)) {
            ps.setString(1, booking.getScheduleId().toString());
            int affected = ps.executeUpdate();

            if (affected == 0) {
                conn.rollback();
                lastInsertError = "Selected schedule is no longer available.";
                return false; // already booked
            }
        }

        // 2️⃣ Check & subtract equipment
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
                        lastInsertError = "Equipment stock changed. Please review selected quantities.";
                        return false; // not enough stock
                    }
                }
            }
        }

        // 3️⃣ Insert booking
        if (!insertBookingRow(conn, booking)) {
            conn.rollback();
            return false;
        }

        // 4️⃣ Insert booking_equipment
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
        if (lastInsertError == null || lastInsertError.isBlank()) {
            lastInsertError = "Database error while creating booking: " + e.getMessage();
        }
        return false;
    }
}

    public String getLastInsertError() {
        return lastInsertError;
    }

    private boolean insertBookingRow(Connection conn, Booking booking) throws SQLException {
        String sqlWithDeadline = "INSERT INTO Booking (booking_id, booker_id, field_id, schedule_id, voucher_id, status, total_price, payment_deadline) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlWithoutDeadline = "INSERT INTO Booking (booking_id, booker_id, field_id, schedule_id, voucher_id, status, total_price) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sqlWithDeadline)) {
            bindBookingParams(ps, booking, true);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            String message = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
            boolean deadlineColumnMissing = message.contains("payment_deadline")
                    && (message.contains("invalid column") || message.contains("column name") || message.contains("does not exist"));

            if (!deadlineColumnMissing) {
                lastInsertError = "Cannot insert booking record: " + ex.getMessage();
                return false;
            }

            // Backward compatibility for DBs that have not applied payment migration yet.
            try (PreparedStatement fallback = conn.prepareStatement(sqlWithoutDeadline)) {
                bindBookingParams(fallback, booking, false);
                fallback.executeUpdate();
                return true;
            } catch (SQLException fallbackEx) {
                lastInsertError = "Cannot insert booking record: " + fallbackEx.getMessage();
                return false;
            }
        }
    }

    private void bindBookingParams(PreparedStatement ps, Booking booking, boolean includeDeadline) throws SQLException {
        ps.setString(1, booking.getBookingId().toString());
        ps.setString(2, booking.getBookerId().toString());
        ps.setString(3, booking.getFieldId().toString());
        ps.setString(4, booking.getScheduleId().toString());

        if (booking.getVoucherId() != null) {
            ps.setString(5, booking.getVoucherId().toString());
        } else {
            ps.setNull(5, Types.VARCHAR);
        }

        ps.setString(6, booking.getStatus());
        ps.setBigDecimal(7, booking.getTotalPrice());

        if (includeDeadline) {
            if (booking.getPaymentDeadline() != null) {
                ps.setTimestamp(8, Timestamp.valueOf(booking.getPaymentDeadline()));
            } else {
                ps.setNull(8, Types.TIMESTAMP);
            }
        }
    }

    public List<BookingViewModel> getByBooker(UUID bookerId) {
        synchronizeBookingStates();
        List<BookingViewModel> list = new ArrayList<>();
        String sql = "SELECT b.booking_id, b.booker_id, b.field_id, b.schedule_id, b.status, b.total_price, s.booking_date, s.start_time, s.end_time, f.field_name, u.full_name AS customer_name, u.phone AS customer_phone " +
            "FROM Booking b " +
            "LEFT JOIN Schedule s ON b.schedule_id = s.schedule_id " +
            "LEFT JOIN Field f ON b.field_id = f.field_id " +
            "LEFT JOIN Users u ON b.booker_id = u.user_id " +
            "WHERE b.booker_id = ? ORDER BY s.booking_date DESC, s.start_time";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, bookerId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BookingViewModel vm = new BookingViewModel();
                vm.setBookingId(UUID.fromString(rs.getString("booking_id")));
                vm.setBookerId(UUID.fromString(rs.getString("booker_id")));
                vm.setFieldId(UUID.fromString(rs.getString("field_id")));
                vm.setScheduleId(UUID.fromString(rs.getString("schedule_id")));
                Date bd = rs.getDate("booking_date");
                if (bd != null) vm.setBookingDate(bd.toLocalDate());
                Time st = rs.getTime("start_time");
                if (st != null) vm.setStartTime(st.toLocalTime());
                Time et = rs.getTime("end_time");
                if (et != null) vm.setEndTime(et.toLocalTime());
                vm.setFieldName(rs.getString("field_name"));
                vm.setCustomerName(rs.getString("customer_name"));
                vm.setCustomerPhone(rs.getString("customer_phone"));
                vm.setStatus(rs.getString("status"));
                vm.setTotalPrice(rs.getBigDecimal("total_price"));
                list.add(vm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<BookingViewModel> getByBookerFiltered(UUID bookerId, String bookingDateStr, String startTimeStr, String status) {
        synchronizeBookingStates();
        List<BookingViewModel> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT b.booking_id, b.booker_id, b.field_id, b.schedule_id, b.status, b.total_price, s.booking_date, s.start_time, s.end_time, f.field_name, u.full_name AS customer_name ");
        sql.append("FROM Booking b ");
        sql.append("LEFT JOIN Schedule s ON b.schedule_id = s.schedule_id ");
        sql.append("LEFT JOIN Field f ON b.field_id = f.field_id ");
        sql.append("LEFT JOIN Users u ON b.booker_id = u.user_id ");
        sql.append("WHERE b.booker_id = ? ");

        if (bookingDateStr != null && !bookingDateStr.isBlank()) {
            sql.append(" AND s.booking_date = ? ");
        }
        if (startTimeStr != null && !startTimeStr.isBlank()) {
            sql.append(" AND s.start_time = ? ");
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND LOWER(b.status) = LOWER(?) ");
        }
        // field filter removed per requirements

        sql.append(" ORDER BY s.booking_date DESC, s.start_time");

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setString(idx++, bookerId.toString());
            if (bookingDateStr != null && !bookingDateStr.isBlank()) {
                ps.setDate(idx++, Date.valueOf(bookingDateStr));
            }
            if (startTimeStr != null && !startTimeStr.isBlank()) {
                try {
                    java.time.LocalTime lt = java.time.LocalTime.parse(startTimeStr);
                    ps.setTime(idx++, Time.valueOf(lt));
                } catch (Exception ex) {
                    ps.setTime(idx++, null);
                }
            }
            if (status != null && !status.isBlank()) {
                ps.setString(idx++, status);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BookingViewModel vm = new BookingViewModel();
                vm.setBookingId(UUID.fromString(rs.getString("booking_id")));
                vm.setBookerId(UUID.fromString(rs.getString("booker_id")));
                vm.setFieldId(UUID.fromString(rs.getString("field_id")));
                vm.setScheduleId(UUID.fromString(rs.getString("schedule_id")));
                Date bd = rs.getDate("booking_date");
                if (bd != null) vm.setBookingDate(bd.toLocalDate());
                Time st = rs.getTime("start_time");
                if (st != null) vm.setStartTime(st.toLocalTime());
                Time et = rs.getTime("end_time");
                if (et != null) vm.setEndTime(et.toLocalTime());
                vm.setFieldName(rs.getString("field_name"));
                vm.setCustomerName(rs.getString("customer_name"));
                vm.setStatus(rs.getString("status"));
                vm.setTotalPrice(rs.getBigDecimal("total_price"));
                list.add(vm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public BookingViewModel getById(UUID bookingId) {
        synchronizeBookingStates();
        String sql = "SELECT b.booking_id, b.booker_id, b.field_id, b.schedule_id, b.status, b.total_price, s.booking_date, s.start_time, s.end_time, f.field_name, u.full_name AS customer_name " +
            "FROM Booking b " +
            "LEFT JOIN Schedule s ON b.schedule_id = s.schedule_id " +
            "LEFT JOIN Field f ON b.field_id = f.field_id " +
            "LEFT JOIN Users u ON b.booker_id = u.user_id " +
            "WHERE b.booking_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, bookingId.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                BookingViewModel vm = new BookingViewModel();
                vm.setBookingId(UUID.fromString(rs.getString("booking_id")));
                vm.setBookerId(UUID.fromString(rs.getString("booker_id")));
                vm.setFieldId(UUID.fromString(rs.getString("field_id")));
                vm.setScheduleId(UUID.fromString(rs.getString("schedule_id")));
                Date bd = rs.getDate("booking_date");
                if (bd != null) vm.setBookingDate(bd.toLocalDate());
                Time st = rs.getTime("start_time");
                if (st != null) vm.setStartTime(st.toLocalTime());
                Time et = rs.getTime("end_time");
                if (et != null) vm.setEndTime(et.toLocalTime());
                vm.setFieldName(rs.getString("field_name"));
                vm.setCustomerName(rs.getString("customer_name"));
                vm.setStatus(rs.getString("status"));
                vm.setTotalPrice(rs.getBigDecimal("total_price"));
                return vm;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public BookingViewModel getByScheduleId(UUID scheduleId) {
        synchronizeBookingStates();
        String sql = "SELECT TOP 1 b.booking_id, b.booker_id, b.field_id, b.schedule_id, b.status, b.total_price, s.booking_date, s.start_time, s.end_time, f.field_name, u.full_name AS customer_name, u.phone AS customer_phone " +
            "FROM Booking b " +
            "LEFT JOIN Schedule s ON b.schedule_id = s.schedule_id " +
            "LEFT JOIN Field f ON b.field_id = f.field_id " +
            "LEFT JOIN Users u ON b.booker_id = u.user_id " +
            "WHERE b.schedule_id = ? AND LOWER(ISNULL(b.status, '')) NOT IN ('cancelled', 'refunded') " +
            "ORDER BY b.booking_time DESC, s.booking_date DESC, s.start_time DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, scheduleId.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                BookingViewModel vm = new BookingViewModel();
                vm.setBookingId(UUID.fromString(rs.getString("booking_id")));
                vm.setBookerId(UUID.fromString(rs.getString("booker_id")));
                vm.setFieldId(UUID.fromString(rs.getString("field_id")));
                vm.setScheduleId(UUID.fromString(rs.getString("schedule_id")));
                Date bd = rs.getDate("booking_date");
                if (bd != null) vm.setBookingDate(bd.toLocalDate());
                Time st = rs.getTime("start_time");
                if (st != null) vm.setStartTime(st.toLocalTime());
                Time et = rs.getTime("end_time");
                if (et != null) vm.setEndTime(et.toLocalTime());
                vm.setFieldName(rs.getString("field_name"));
                vm.setCustomerName(rs.getString("customer_name"));
                vm.setCustomerPhone(rs.getString("customer_phone"));
                vm.setStatus(rs.getString("status"));
                vm.setTotalPrice(rs.getBigDecimal("total_price"));
                return vm;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<BookingEquipmentViewModel> getBookingEquipments(UUID bookingId) {
        List<BookingEquipmentViewModel> list = new ArrayList<>();
        String sql = "SELECT be.equipment_id, be.quantity, e.name, e.rental_price " +
                "FROM Booking_Equipment be " +
                "LEFT JOIN Equipment e ON be.equipment_id = e.equipment_id " +
                "WHERE be.booking_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, bookingId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BookingEquipmentViewModel vm = new BookingEquipmentViewModel();
                vm.setEquipmentId(UUID.fromString(rs.getString("equipment_id")));
                vm.setQuantity(rs.getInt("quantity"));
                vm.setName(rs.getString("name"));
                vm.setRentalPrice(rs.getBigDecimal("rental_price"));
                list.add(vm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean cancelBooking(UUID bookingId) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            BookingSnapshot snapshot = getBookingSnapshot(conn, bookingId);
            if (snapshot == null) {
                conn.rollback();
                return false;
            }

            if (STATUS_PENDING.equals(snapshot.status)) {
                conn.rollback();
                return cancelBookingForPayment(bookingId);
            }

            if (!STATUS_PAID.equals(snapshot.status) || snapshot.scheduleStart == null) {
                conn.rollback();
                return false;
            }

            if (!snapshot.scheduleStart.isAfter(LocalDateTime.now().plusDays(2))) {
                conn.rollback();
                return false;
            }

            boolean updated = updateBookingStatus(conn, bookingId, STATUS_PENDING_REFUND, STATUS_PAID);
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

    /**
     * Force-cancel booking for unpaid payment flow and immediately release slot/equipment.
     */
    public boolean cancelBookingForPayment(UUID bookingId) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            BookingSnapshot snapshot = getBookingSnapshot(conn, bookingId);
            if (snapshot == null) {
                conn.rollback();
                return false;
            }

            if (STATUS_CANCELLED.equals(snapshot.status)) {
                conn.rollback();
                return true;
            }

            if (!STATUS_PENDING.equals(snapshot.status)) {
                conn.rollback();
                return false;
            }

            updateBookingStatus(conn, bookingId, STATUS_CANCELLED, STATUS_PENDING);
            updatePaymentStatusByBooking(conn, bookingId, "FAILED", true);
            releaseBookingResources(conn, bookingId, snapshot.scheduleId);
            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean markBookingPaid(UUID bookingId) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            boolean updated = updateBookingStatus(conn, bookingId, STATUS_PAID, STATUS_PENDING);
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

    public boolean updateStatus(UUID bookingId, String newStatus) {
        if (newStatus == null) {
            return false;
        }

        synchronizeBookingStates();
        newStatus = normalizeStatus(newStatus);
        if (!SUPPORTED_STATUSES.contains(newStatus)) {
            return false;
        }

        try {
            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);

                BookingSnapshot snapshot = getBookingSnapshot(conn, bookingId);
                if (snapshot == null) {
                    conn.rollback();
                    return false;
                }

                if (newStatus.equals(snapshot.status)) {
                    conn.rollback();
                    return true;
                }

                boolean updated;
                switch (newStatus) {
                    case STATUS_PAID:
                        updated = updateBookingStatus(conn, bookingId, STATUS_PAID, STATUS_PENDING);
                        break;
                    case STATUS_CHECKED_IN:
                        updated = updateBookingStatus(conn, bookingId, STATUS_CHECKED_IN, STATUS_PAID);
                        break;
                    case STATUS_PENDING_REFUND:
                        if (!STATUS_PAID.equals(snapshot.status)) {
                            updated = false;
                            break;
                        }
                        updated = updateBookingStatus(conn, bookingId, STATUS_PENDING_REFUND, STATUS_PAID);
                        if (updated) {
                            updatePaymentStatusByBooking(conn, bookingId, "REFUND_PENDING", false);
                        }
                        break;
                    case STATUS_REFUNDED:
                        if (!STATUS_PENDING_REFUND.equals(snapshot.status)) {
                            updated = false;
                            break;
                        }
                        updated = updateBookingStatus(conn, bookingId, STATUS_REFUNDED, STATUS_PENDING_REFUND);
                        if (updated) {
                            updatePaymentStatusByBooking(conn, bookingId, "REFUNDED", true);
                            releaseBookingResources(conn, bookingId, snapshot.scheduleId);
                        }
                        break;
                    case STATUS_COMPLETED:
                        updated = updateBookingStatus(conn, bookingId, STATUS_COMPLETED, STATUS_CHECKED_IN);
                        break;
                    case STATUS_CANCELLED:
                        if (!STATUS_PENDING.equals(snapshot.status) && !STATUS_PAID.equals(snapshot.status)) {
                            updated = false;
                            break;
                        }
                        updated = updateBookingStatus(conn, bookingId, STATUS_CANCELLED, snapshot.status);
                        if (updated) {
                            if (STATUS_PENDING.equals(snapshot.status)) {
                                updatePaymentStatusByBooking(conn, bookingId, "FAILED", true);
                            }
                            releaseBookingResources(conn, bookingId, snapshot.scheduleId);
                        }
                        break;
                    default:
                        updated = false;
                        break;
                }

                if (!updated) {
                    conn.rollback();
                    return false;
                }

                conn.commit();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<BookingViewModel> getByLocation(UUID locationId) {
        synchronizeBookingStates();
        List<BookingViewModel> list = new ArrayList<>();
        String sql = "SELECT b.booking_id, b.booker_id, b.field_id, b.schedule_id, b.status, b.total_price, s.booking_date, s.start_time, s.end_time, f.field_name, u.full_name AS customer_name " +
                "FROM Booking b " +
                "LEFT JOIN Schedule s ON b.schedule_id = s.schedule_id " +
                "LEFT JOIN Field f ON b.field_id = f.field_id " +
                "LEFT JOIN Users u ON b.booker_id = u.user_id " +
                "WHERE f.location_id = ? ORDER BY s.booking_date DESC, s.start_time";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, locationId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BookingViewModel vm = new BookingViewModel();
                vm.setBookingId(UUID.fromString(rs.getString("booking_id")));
                vm.setBookerId(UUID.fromString(rs.getString("booker_id")));
                vm.setFieldId(UUID.fromString(rs.getString("field_id")));
                vm.setScheduleId(UUID.fromString(rs.getString("schedule_id")));
                Date bd = rs.getDate("booking_date");
                if (bd != null) vm.setBookingDate(bd.toLocalDate());
                Time st = rs.getTime("start_time");
                if (st != null) vm.setStartTime(st.toLocalTime());
                Time et = rs.getTime("end_time");
                if (et != null) vm.setEndTime(et.toLocalTime());
                vm.setFieldName(rs.getString("field_name"));
                vm.setCustomerName(rs.getString("customer_name"));
                vm.setStatus(rs.getString("status"));
                vm.setTotalPrice(rs.getBigDecimal("total_price"));
                list.add(vm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<BookingViewModel> getByLocationFiltered(UUID locationId, String bookingDateStr, String status, String customerKeyword) {
        synchronizeBookingStates();
        List<BookingViewModel> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT b.booking_id, b.booker_id, b.field_id, b.schedule_id, b.status, b.total_price, s.booking_date, s.start_time, s.end_time, f.field_name, u.full_name AS customer_name, u.phone AS customer_phone ");
        sql.append("FROM Booking b ");
        sql.append("LEFT JOIN Schedule s ON b.schedule_id = s.schedule_id ");
        sql.append("LEFT JOIN Field f ON b.field_id = f.field_id ");
        sql.append("LEFT JOIN Users u ON b.booker_id = u.user_id ");
        sql.append("WHERE f.location_id = ? ");

        if (bookingDateStr != null && !bookingDateStr.isBlank()) {
            sql.append(" AND s.booking_date = ? ");
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND LOWER(b.status) = LOWER(?) ");
        }
        if (customerKeyword != null && !customerKeyword.isBlank()) {
            sql.append(" AND (LOWER(u.full_name) LIKE LOWER(?) OR u.phone LIKE ?) ");
        }

        sql.append(" ORDER BY s.booking_date DESC, s.start_time");

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setString(idx++, locationId.toString());
            if (bookingDateStr != null && !bookingDateStr.isBlank()) {
                ps.setDate(idx++, Date.valueOf(bookingDateStr));
            }
            if (status != null && !status.isBlank()) {
                ps.setString(idx++, status);
            }
            if (customerKeyword != null && !customerKeyword.isBlank()) {
                ps.setString(idx++, "%" + customerKeyword + "%");
                ps.setString(idx++, "%" + customerKeyword + "%");
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BookingViewModel vm = new BookingViewModel();
                vm.setBookingId(UUID.fromString(rs.getString("booking_id")));
                vm.setBookerId(UUID.fromString(rs.getString("booker_id")));
                vm.setFieldId(UUID.fromString(rs.getString("field_id")));
                vm.setScheduleId(UUID.fromString(rs.getString("schedule_id")));
                Date bd = rs.getDate("booking_date");
                if (bd != null) vm.setBookingDate(bd.toLocalDate());
                Time st = rs.getTime("start_time");
                if (st != null) vm.setStartTime(st.toLocalTime());
                Time et = rs.getTime("end_time");
                if (et != null) vm.setEndTime(et.toLocalTime());
                vm.setFieldName(rs.getString("field_name"));
                vm.setCustomerName(rs.getString("customer_name"));
                vm.setCustomerPhone(rs.getString("customer_phone"));
                vm.setStatus(rs.getString("status"));
                vm.setTotalPrice(rs.getBigDecimal("total_price"));
                list.add(vm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Get raw Booking object by ID (including payment_deadline)
     */
    public Booking getBookingById(UUID bookingId) {
        synchronizeBookingStates();
        String sql = "SELECT * FROM Booking WHERE booking_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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
                
                booking.setStatus(rs.getString("status"));
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

    private void synchronizeBookingStates() {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            String sql = "SELECT b.booking_id, LOWER(ISNULL(b.status, '')) AS booking_status "
                    + "FROM Booking b "
                    + "INNER JOIN Schedule s ON s.schedule_id = b.schedule_id "
                    + "WHERE LOWER(ISNULL(b.status, '')) IN ('paid', 'checked in') "
                    + "AND (s.booking_date < CAST(SYSDATETIME() AS DATE) "
                    + "     OR (s.booking_date = CAST(SYSDATETIME() AS DATE) AND s.end_time <= CAST(SYSDATETIME() AS TIME)))";

            List<BookingSnapshot> expiredBookings = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BookingSnapshot snapshot = new BookingSnapshot();
                    snapshot.bookingId = UUID.fromString(rs.getString("booking_id"));
                    snapshot.status = normalizeStatus(rs.getString("booking_status"));
                    expiredBookings.add(snapshot);
                }
            }

            for (BookingSnapshot expiredBooking : expiredBookings) {
                BookingSnapshot snapshot = getBookingSnapshot(conn, expiredBooking.bookingId);
                if (snapshot == null) {
                    continue;
                }
                if (STATUS_PAID.equals(snapshot.status)) {
                    updateBookingStatus(conn, expiredBooking.bookingId, STATUS_CANCELLED, STATUS_PAID);
                    releaseBookingResources(conn, expiredBooking.bookingId, snapshot.scheduleId);
                } else if (STATUS_CHECKED_IN.equals(snapshot.status)) {
                    updateBookingStatus(conn, expiredBooking.bookingId, STATUS_COMPLETED, STATUS_CHECKED_IN);
                }
            }

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BookingSnapshot getBookingSnapshot(Connection conn, UUID bookingId) throws SQLException {
        String sql = "SELECT b.schedule_id, b.status, s.booking_date, s.start_time, s.end_time "
                + "FROM Booking b "
                + "LEFT JOIN Schedule s ON s.schedule_id = b.schedule_id "
                + "WHERE b.booking_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                BookingSnapshot snapshot = new BookingSnapshot();
                String scheduleId = rs.getString("schedule_id");
                if (scheduleId != null) {
                    snapshot.scheduleId = UUID.fromString(scheduleId);
                }
                snapshot.status = normalizeStatus(rs.getString("status"));

                Date bookingDate = rs.getDate("booking_date");
                Time startTime = rs.getTime("start_time");
                Time endTime = rs.getTime("end_time");
                if (bookingDate != null && startTime != null) {
                    snapshot.scheduleStart = LocalDateTime.of(bookingDate.toLocalDate(), startTime.toLocalTime());
                }
                if (bookingDate != null && endTime != null) {
                    snapshot.scheduleEnd = LocalDateTime.of(bookingDate.toLocalDate(), endTime.toLocalTime());
                }
                return snapshot;
            }
        }
    }

    private boolean updateBookingStatus(Connection conn, UUID bookingId, String newStatus, String expectedStatus) throws SQLException {
        String sql = "UPDATE Booking SET status = ? WHERE booking_id = ? AND LOWER(ISNULL(status, '')) = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, bookingId.toString());
            ps.setString(3, normalizeStatus(expectedStatus));
            return ps.executeUpdate() > 0;
        }
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

    private void releaseBookingResources(Connection conn, UUID bookingId, UUID scheduleId) throws SQLException {
        if (scheduleId != null) {
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

    private String normalizeStatus(String status) {
        return status == null ? "" : status.trim().toLowerCase();
    }

    private static class BookingSnapshot {
        private UUID bookingId;
        private UUID scheduleId;
        private String status;
        private LocalDateTime scheduleStart;
        private LocalDateTime scheduleEnd;
    }

}
