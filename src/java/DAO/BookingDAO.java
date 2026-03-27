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
import java.time.LocalDateTime;
import Models.BookingViewModel;
import Models.BookingEquipmentViewModel;
import Models.Field;
import Models.Location;
import java.math.BigDecimal;

public class BookingDAO {

    private String lastInsertError;

    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_PAID = "paid";
    private static final String STATUS_CANCELLED = "cancelled";
    private static final String STATUS_PENDING_REFUND = "pending refund";
    private static final String STATUS_REFUNDED = "refunded";
    private static final String STATUS_CHECKED_IN = "checked in";
    private static final String STATUS_PENDING_EXTRA = "pending extra";
    private static final String STATUS_COMPLETED = "completed";

    private static final Set<String> SUPPORTED_STATUSES = new HashSet<>(Arrays.asList(
            STATUS_PENDING,
            STATUS_PAID,
            STATUS_CANCELLED,
            STATUS_PENDING_REFUND,
            STATUS_REFUNDED,
            STATUS_CHECKED_IN,
            STATUS_PENDING_EXTRA,
            STATUS_COMPLETED
    ));

    /**
     * Creates a new booking in a single transaction:
     * locks schedule, reserves equipment stock, inserts booking and booking-equipment rows.
     */
    
    //Description: Executes the insert write workflow, including input normalization, transactional SQL updates/inserts, consistency checks, and explicit success/failure signaling for calling services.
    public boolean insert(Booking booking, List<BookingEquipment> equipmentList) {
        // Internal Flow: validate inputs, run transactional SQL mutations, and propagate a clear commit/rollback result.

    lastInsertError = null;

    try (Connection conn = DBConnection.getConnection()) {

        conn.setAutoCommit(false);

        // 1Ã¯Â¸ÂÃ¢Æ’Â£ Lock schedule
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

        // 2Ã¯Â¸ÂÃ¢Æ’Â£ Check & subtract equipment
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

        // 3Ã¯Â¸ÂÃ¢Æ’Â£ Insert booking
        if (!insertBookingRow(conn, booking)) {
            conn.rollback();
            return false;
        }

        // 4Ã¯Â¸ÂÃ¢Æ’Â£ Insert booking_equipment
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

    /**
     * Returns the last business/database error captured by write operations in this DAO.
     */
    
    //Description: Retrieves and prepares data for getLastInsertError by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public String getLastInsertError() {
        // Internal Flow: query data source, map database rows to model objects, and return null/empty values safely on edge cases.
        return lastInsertError;
    }

    /**
     * Inserts the booking row and automatically falls back when legacy databases
     * do not yet contain payment_deadline.
     */
    
    //Description: Executes the insertBookingRow write workflow, including input normalization, transactional SQL updates/inserts, consistency checks, and explicit success/failure signaling for calling services.
    private boolean insertBookingRow(Connection conn, Booking booking) throws SQLException {
        // Internal Flow: validate inputs, run transactional SQL mutations, and propagate a clear commit/rollback result.
        String sqlWithDeadlineAndPhone = "INSERT INTO Booking (booking_id, booker_id, phone_number, field_id, schedule_id, voucher_id, status, total_price, payment_deadline) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlWithDeadlineNoPhone = "INSERT INTO Booking (booking_id, booker_id, field_id, schedule_id, voucher_id, status, total_price, payment_deadline) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlWithoutDeadlineAndPhone = "INSERT INTO Booking (booking_id, booker_id, phone_number, field_id, schedule_id, voucher_id, status, total_price) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlWithoutDeadlineNoPhone = "INSERT INTO Booking (booking_id, booker_id, field_id, schedule_id, voucher_id, status, total_price) VALUES (?, ?, ?, ?, ?, ?, ?)";

        String[] sqlAttempts = new String[]{
            sqlWithDeadlineAndPhone,
            sqlWithDeadlineNoPhone,
            sqlWithoutDeadlineAndPhone,
            sqlWithoutDeadlineNoPhone
        };

        boolean[] includeDeadlineAttempts = new boolean[]{true, true, false, false};
        boolean[] includePhoneAttempts = new boolean[]{true, false, true, false};

        SQLException lastException = null;
        for (int i = 0; i < sqlAttempts.length; i++) {
            try (PreparedStatement ps = conn.prepareStatement(sqlAttempts[i])) {
                bindBookingParams(ps, booking, includeDeadlineAttempts[i], includePhoneAttempts[i]);
                ps.executeUpdate();
                return true;
            } catch (SQLException ex) {
                lastException = ex;
                if (!isMissingColumnError(ex)) {
                    lastInsertError = "Cannot insert booking record: " + ex.getMessage();
                    return false;
                }
            }
        }

        lastInsertError = "Cannot insert booking record: "
                + (lastException != null ? lastException.getMessage() : "unsupported database schema");
        return false;
    }

    
    //Description: Evaluates business conditions in isMissingColumnError and returns a deterministic boolean result used to protect state transitions and payment/booking integrity rules.
    private boolean isMissingColumnError(SQLException ex) {
        // Internal Flow: compute condition from normalized state and return a strict boolean outcome.
        String message = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
        return message.contains("invalid column") || message.contains("column name") || message.contains("does not exist");
    }

    /**
     * Binds common booking parameters for insert statements.
     */
    
    //Description: Applies normalization/mapping logic in bindBookingParams to keep data format stable across DAO boundaries and reduce duplicate parsing logic in higher layers.
    private void bindBookingParams(PreparedStatement ps, Booking booking, boolean includeDeadline, boolean includePhone) throws SQLException {
        // Internal Flow: apply guard checks, execute core logic, and keep exception handling localized to DAO responsibilities.
        ps.setString(1, booking.getBookingId().toString());
        ps.setString(2, booking.getBookerId().toString());

        int idx = 3;
        if (includePhone) {
            if (booking.getPhoneNumber() != null && !booking.getPhoneNumber().trim().isEmpty()) {
                ps.setString(idx++, booking.getPhoneNumber().trim());
            } else {
                ps.setNull(idx++, Types.NVARCHAR);
            }
        }

        ps.setString(idx++, booking.getFieldId().toString());
        ps.setString(idx++, booking.getScheduleId().toString());

        if (booking.getVoucherId() != null) {
            ps.setString(idx++, booking.getVoucherId().toString());
        } else {
            ps.setNull(idx++, Types.VARCHAR);
        }

        ps.setString(idx++, booking.getStatus());
        ps.setBigDecimal(idx++, booking.getTotalPrice());

        if (includeDeadline) {
            if (booking.getPaymentDeadline() != null) {
                ps.setTimestamp(idx, Timestamp.valueOf(booking.getPaymentDeadline()));
            } else {
                ps.setNull(idx, Types.TIMESTAMP);
            }
        }
    }

    /**
     * Gets booking history for one customer.
     */
    
    //Description: Retrieves and prepares data for getByBooker by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public List<BookingViewModel> getByBooker(UUID bookerId) {
        // Internal Flow: query data source, map database rows to model objects, and return null/empty values safely on edge cases.
        synchronizeBookingStates();
        List<BookingViewModel> list = new ArrayList<>();
        String sql = "SELECT b.booking_id, b.booker_id, b.field_id, b.schedule_id, b.status, b.total_price, s.booking_date, s.start_time, s.end_time, f.field_name, u.full_name AS customer_name, COALESCE(b.phone_number, u.phone) AS customer_phone " +
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

    /**
     * Gets customer bookings with optional filters by date, start time and status.
     */
    
    //Description: Retrieves and prepares data for getByBookerFiltered by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public List<BookingViewModel> getByBookerFiltered(UUID bookerId, String bookingDateStr, String startTimeStr, String status) {
        // Internal Flow: query data source, map database rows to model objects, and return null/empty values safely on edge cases.
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

    
    //Description: Retrieves and prepares data for getByWeeklyGroupId by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public List<BookingViewModel> getByWeeklyGroupId(UUID weeklyGroupId) {
        // Internal Flow: query data source, map database rows to model objects, and return null/empty values safely on edge cases.
        synchronizeBookingStates();
        List<BookingViewModel> list = new ArrayList<>();
        String sql = "SELECT b.booking_id, b.booker_id, b.field_id, b.schedule_id, b.status, b.total_price, "
            + "ISNULL(s.price, 0) AS field_price, "
            + "ISNULL((SELECT SUM(ISNULL(be.quantity, 0) * ISNULL(e.rental_price, 0)) "
            + "       FROM Booking_Equipment be "
            + "       LEFT JOIN Equipment e ON be.equipment_id = e.equipment_id "
            + "       WHERE be.booking_id = b.booking_id), 0) AS equipment_price, "
                + "s.booking_date, s.start_time, s.end_time, f.field_name, l.location_name "
                + "FROM Booking b "
                + "LEFT JOIN Schedule s ON b.schedule_id = s.schedule_id "
                + "LEFT JOIN Field f ON b.field_id = f.field_id "
                + "LEFT JOIN Location l ON f.location_id = l.location_id "
                + "WHERE b.weekly_group_id = ? "
                + "ORDER BY s.booking_date, s.start_time";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, weeklyGroupId.toString());
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
                vm.setLocationName(rs.getString("location_name"));
                vm.setStatus(rs.getString("status"));
                vm.setFieldPrice(rs.getBigDecimal("field_price"));
                vm.setEquipmentPrice(rs.getBigDecimal("equipment_price"));
                vm.setTotalPrice(rs.getBigDecimal("total_price"));
                list.add(vm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Gets customer bookings for calendar rendering in a given date range and optional location/field filters.
     */
    
    //Description: Retrieves and prepares data for getCustomerCalendarBookings by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public List<BookingViewModel> getCustomerCalendarBookings(UUID bookerId, LocalDate fromDate, LocalDate toDate, LocalDate selectedDate, UUID locationId, UUID fieldId) {
        // Internal Flow: query data source, map database rows to model objects, and return null/empty values safely on edge cases.
        synchronizeBookingStates();
        List<BookingViewModel> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT b.booking_id, b.booker_id, b.field_id, b.schedule_id, b.status, b.total_price, s.booking_date, s.start_time, s.end_time, f.field_name, l.location_name, u.full_name AS customer_name ");
        sql.append("FROM Booking b ");
        sql.append("LEFT JOIN Schedule s ON b.schedule_id = s.schedule_id ");
        sql.append("LEFT JOIN Field f ON b.field_id = f.field_id ");
        sql.append("LEFT JOIN Location l ON f.location_id = l.location_id ");
        sql.append("LEFT JOIN Users u ON b.booker_id = u.user_id ");
        sql.append("WHERE b.booker_id = ? ");
        sql.append("AND LOWER(ISNULL(b.status, '')) NOT IN ('cancelled', 'refunded') ");
        sql.append("AND s.booking_date >= ? AND s.booking_date <= ? ");

        if (selectedDate != null) {
            sql.append("AND s.booking_date = ? ");
        }
        if (locationId != null) {
            sql.append("AND f.location_id = ? ");
        }
        if (fieldId != null) {
            sql.append("AND b.field_id = ? ");
        }

        sql.append("ORDER BY s.booking_date, s.start_time");

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setString(idx++, bookerId.toString());
            ps.setDate(idx++, Date.valueOf(fromDate));
            ps.setDate(idx++, Date.valueOf(toDate));

            if (selectedDate != null) {
                ps.setDate(idx++, Date.valueOf(selectedDate));
            }
            if (locationId != null) {
                ps.setString(idx++, locationId.toString());
            }
            if (fieldId != null) {
                ps.setString(idx++, fieldId.toString());
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
                vm.setLocationName(rs.getString("location_name"));
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

    /**
     * Gets distinct fields appearing in the customer's booking calendar dataset.
     */
    
    //Description: Retrieves and prepares data for getCustomerCalendarFields by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public List<Field> getCustomerCalendarFields(UUID bookerId) {
        // Internal Flow: query data source, map database rows to model objects, and return null/empty values safely on edge cases.
        return getCustomerCalendarFields(bookerId, null);
    }

    
    //Description: Retrieves and prepares data for getCustomerCalendarFields by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public List<Field> getCustomerCalendarFields(UUID bookerId, UUID locationId) {
        // Internal Flow: query data source, map database rows to model objects, and return null/empty values safely on edge cases.
        synchronizeBookingStates();
        List<Field> fields = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT f.field_id, f.field_name, f.field_type, f.image_url, f.status, f.[condition], f.location_id ");
        sql.append("FROM Booking b ");
        sql.append("JOIN Field f ON b.field_id = f.field_id ");
        sql.append("WHERE b.booker_id = ? ");
        sql.append("AND LOWER(ISNULL(b.status, '')) NOT IN ('cancelled', 'refunded') ");
        if (locationId != null) {
            sql.append("AND f.location_id = ? ");
        }
        sql.append("ORDER BY f.field_name");

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setString(idx++, bookerId.toString());
            if (locationId != null) {
                ps.setString(idx++, locationId.toString());
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Field f = new Field();
                f.setFieldId(UUID.fromString(rs.getString("field_id")));
                f.setFieldName(rs.getString("field_name"));
                f.setFieldType(rs.getString("field_type"));
                f.setImageUrl(rs.getString("image_url"));
                f.setStatus(rs.getString("status"));
                f.setFieldCondition(rs.getString("condition"));
                f.setLocationId(UUID.fromString(rs.getString("location_id")));
                fields.add(f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fields;
    }

    /**
     * Gets distinct locations appearing in the customer's booking calendar dataset.
     */
    
    //Description: Retrieves and prepares data for getCustomerCalendarLocations by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public List<Location> getCustomerCalendarLocations(UUID bookerId) {
        // Internal Flow: query data source, map database rows to model objects, and return null/empty values safely on edge cases.
        synchronizeBookingStates();
        List<Location> locations = new ArrayList<>();
        String sql = "SELECT DISTINCT l.location_id, l.location_name " +
                "FROM Booking b " +
                "JOIN Field f ON b.field_id = f.field_id " +
                "JOIN Location l ON f.location_id = l.location_id " +
                "WHERE b.booker_id = ? " +
                "AND LOWER(ISNULL(b.status, '')) NOT IN ('cancelled', 'refunded') " +
                "ORDER BY l.location_name";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, bookerId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Location l = new Location();
                l.setLocationId(UUID.fromString(rs.getString("location_id")));
                l.setLocationName(rs.getString("location_name"));
                locations.add(l);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return locations;
    }

    /**
     * Gets one booking view model by booking ID.
     */
    
    //Description: Retrieves and prepares data for getById by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public BookingViewModel getById(UUID bookingId) {
        // Internal Flow: query data source, map database rows to model objects, and return null/empty values safely on edge cases.
        synchronizeBookingStates();
        String sql = "SELECT b.booking_id, b.booker_id, b.field_id, b.schedule_id, b.status, b.total_price, s.booking_date, s.start_time, s.end_time, f.field_name, f.location_id, u.full_name AS customer_name " +
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
                String locationId = rs.getString("location_id");
                if (locationId != null) vm.setLocationId(UUID.fromString(locationId));
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

    /**
     * Gets latest non-cancelled/non-refunded booking by schedule ID.
     */
    
    //Description: Retrieves and prepares data for getByScheduleId by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public BookingViewModel getByScheduleId(UUID scheduleId) {
        // Internal Flow: query data source, map database rows to model objects, and return null/empty values safely on edge cases.
        synchronizeBookingStates();
        String sql = "SELECT TOP 1 b.booking_id, b.booker_id, b.field_id, b.schedule_id, b.status, b.total_price, s.booking_date, s.start_time, s.end_time, f.field_name, u.full_name AS customer_name, COALESCE(b.phone_number, u.phone) AS customer_phone " +
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

    /**
     * Gets latest booking by schedule ID for calendar context.
     */
    
    //Description: Retrieves and prepares data for getByScheduleIdForCalendar by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public BookingViewModel getByScheduleIdForCalendar(UUID scheduleId) {
        // Internal Flow: query data source, map database rows to model objects, and return null/empty values safely on edge cases.
        synchronizeBookingStates();
        String sql = "SELECT TOP 1 b.booking_id, b.booker_id, b.field_id, b.schedule_id, b.status, b.total_price, s.booking_date, s.start_time, s.end_time, f.field_name, u.full_name AS customer_name, COALESCE(b.phone_number, u.phone) AS customer_phone " +
            "FROM Booking b " +
            "LEFT JOIN Schedule s ON b.schedule_id = s.schedule_id " +
            "LEFT JOIN Field f ON b.field_id = f.field_id " +
            "LEFT JOIN Users u ON b.booker_id = u.user_id " +
            "WHERE b.schedule_id = ? " +
            "AND LOWER(ISNULL(b.status, '')) IN ('pending', 'paid', 'checked in', 'pending extra', 'completed') " +
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

    /**
     * Gets equipment lines attached to a booking.
     */
    
    //Description: Retrieves and prepares data for getBookingEquipments by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public List<BookingEquipmentViewModel> getBookingEquipments(UUID bookingId) {
        // Internal Flow: query data source, map database rows to model objects, and return null/empty values safely on edge cases.
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

    /**
     * Cancels booking by policy:
     * pending bookings use payment-cancel flow; paid bookings may move to pending refund when eligible.
     */
    
    //Description: Executes the cancelBooking write workflow, including input normalization, transactional SQL updates/inserts, consistency checks, and explicit success/failure signaling for calling services.
    public boolean cancelBooking(UUID bookingId) {
        // Internal Flow: validate inputs, run transactional SQL mutations, and propagate a clear commit/rollback result.
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
    
    //Description: Executes the cancelBookingForPayment write workflow, including input normalization, transactional SQL updates/inserts, consistency checks, and explicit success/failure signaling for calling services.
    public boolean cancelBookingForPayment(UUID bookingId) {
        // Internal Flow: validate inputs, run transactional SQL mutations, and propagate a clear commit/rollback result.
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

    /**
     * Marks booking as paid (pending -> paid).
     */
    
    //Description: Executes the markBookingPaid write workflow, including input normalization, transactional SQL updates/inserts, consistency checks, and explicit success/failure signaling for calling services.
    public boolean markBookingPaid(UUID bookingId) {
        // Internal Flow: validate inputs, run transactional SQL mutations, and propagate a clear commit/rollback result.
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

    
    //Description: Executes the markWeeklyGroupPaid write workflow, including input normalization, transactional SQL updates/inserts, consistency checks, and explicit success/failure signaling for calling services.
    public boolean markWeeklyGroupPaid(UUID weeklyGroupId) {
        // Internal Flow: validate inputs, run transactional SQL mutations, and propagate a clear commit/rollback result.
        String sql = "UPDATE Booking SET status = 'paid' WHERE weekly_group_id = ? AND status = 'pending'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, weeklyGroupId.toString());
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Marks booking as pending extra (checked in -> pending extra) when staff creates extra-equipment payment.
     */
    
    public boolean markBookingPendingExtra(UUID bookingId) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            BookingSnapshot snapshot = getBookingSnapshot(conn, bookingId);
            if (snapshot == null || !STATUS_CHECKED_IN.equals(snapshot.status)) {
                conn.rollback();
                return false;
            }

            if (!isBookingActive(snapshot, LocalDateTime.now())) {
                conn.rollback();
                return false;
            }

            boolean updated = updateBookingStatus(conn, bookingId, STATUS_PENDING_EXTRA, STATUS_CHECKED_IN);
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

    /**
     * Resolves booking status after supplementary payment succeeds:
     * - pending extra -> completed if schedule already ended
     * - pending extra -> checked in if schedule has not ended yet
     */
    
    public boolean settlePendingExtraStatus(UUID bookingId) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            BookingSnapshot snapshot = getBookingSnapshot(conn, bookingId);
            if (snapshot == null) {
                conn.rollback();
                return false;
            }

            if (!STATUS_PENDING_EXTRA.equals(snapshot.status)) {
                conn.rollback();
                return true;
            }

            if (!isPaymentSuccessful(conn, bookingId)) {
                conn.rollback();
                return false;
            }

            String targetStatus = resolvePostSupplementaryStatus(snapshot, LocalDateTime.now());
            boolean updated = updateBookingStatus(conn, bookingId, targetStatus, STATUS_PENDING_EXTRA);
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

    
    //Description: Executes the cancelWeeklyGroupForPayment write workflow, including input normalization, transactional SQL updates/inserts, consistency checks, and explicit success/failure signaling for calling services.
    public boolean cancelWeeklyGroupForPayment(UUID weeklyGroupId) {
        // Internal Flow: validate inputs, run transactional SQL mutations, and propagate a clear commit/rollback result.
        List<UUID> bookingIds = new ArrayList<>();
        String sql = "SELECT booking_id FROM Booking WHERE weekly_group_id = ? AND status = 'pending'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, weeklyGroupId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                bookingIds.add(UUID.fromString(rs.getString("booking_id")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        boolean allOk = true;
        for (UUID bookingId : bookingIds) {
            if (!cancelBookingForPayment(bookingId)) {
                allOk = false;
            }
        }
        return allOk;
    }

    /**
     * Central status transition API with guarded business rules.
     */
    
    //Description: Executes the updateStatus write workflow, including input normalization, transactional SQL updates/inserts, consistency checks, and explicit success/failure signaling for calling services.
    public boolean updateStatus(UUID bookingId, String newStatus) {
        // Internal Flow: validate inputs, run transactional SQL mutations, and propagate a clear commit/rollback result.
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
                        if (STATUS_PAID.equals(snapshot.status)) {
                            updated = updateBookingStatus(conn, bookingId, STATUS_CHECKED_IN, STATUS_PAID);
                        } else if (STATUS_PENDING_EXTRA.equals(snapshot.status) && isPaymentSuccessful(conn, bookingId)) {
                            updated = updateBookingStatus(conn, bookingId, STATUS_CHECKED_IN, STATUS_PENDING_EXTRA);
                        } else {
                            updated = false;
                        }
                        break;
                    case STATUS_PENDING_EXTRA:
                        updated = updateBookingStatus(conn, bookingId, STATUS_PENDING_EXTRA, STATUS_CHECKED_IN);
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
                        if (!(STATUS_CHECKED_IN.equals(snapshot.status) || STATUS_PENDING_EXTRA.equals(snapshot.status))) {
                            updated = false;
                            break;
                        }

                        if (snapshot.scheduleEnd == null || LocalDateTime.now().isBefore(snapshot.scheduleEnd)) {
                            updated = false;
                            break;
                        }

                        if (STATUS_PENDING_EXTRA.equals(snapshot.status) && !isPaymentSuccessful(conn, bookingId)) {
                            updated = false;
                            break;
                        }

                        updated = updateBookingStatus(conn, bookingId, STATUS_COMPLETED, snapshot.status);
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

    /**
     * Legacy direct supplementary insertion flow.
     * Kept for compatibility; current payment-first flow should use finalizeSupplementaryEquipment after payment success.
     */
    
    public boolean addEquipmentToBooking(UUID bookingId, List<BookingEquipment> equipmentList, BigDecimal additionalAmount) {
        lastInsertError = null;

        if (equipmentList == null || equipmentList.isEmpty()) {
            lastInsertError = "Please select at least one equipment item.";
            return false;
        }

        if (additionalAmount == null || additionalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            lastInsertError = "Invalid equipment total.";
            return false;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            BookingSnapshot snapshot = getBookingSnapshot(conn, bookingId);
            if (snapshot == null) {
                conn.rollback();
                lastInsertError = "Booking not found.";
                return false;
            }

            if (!STATUS_CHECKED_IN.equals(snapshot.status)) {
                conn.rollback();
                lastInsertError = "Only checked-in bookings can add equipment.";
                return false;
            }

            if (!isBookingActive(snapshot, LocalDateTime.now())) {
                conn.rollback();
                lastInsertError = "Checked-in booking is outside its active time window.";
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
                        lastInsertError = "Equipment stock changed. Please review selected quantities.";
                        return false;
                    }
                }
            }

            String updateExistingEquipment = "UPDATE Booking_Equipment SET quantity = quantity + ? WHERE booking_id = ? AND equipment_id = ?";
            String insertNewEquipment = "INSERT INTO Booking_Equipment (booking_id, equipment_id, quantity) VALUES (?, ?, ?)";

            try (PreparedStatement updatePs = conn.prepareStatement(updateExistingEquipment);
                 PreparedStatement insertPs = conn.prepareStatement(insertNewEquipment)) {
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

            updateBookingStatus(conn, bookingId, STATUS_PENDING_EXTRA, STATUS_CHECKED_IN);

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (lastInsertError == null || lastInsertError.isBlank()) {
                lastInsertError = "Database error while adding equipment: " + e.getMessage();
            }
            return false;
        }
    }

    /**
     * Finalizes supplementary equipment only after payment success:
     * reserves stock, merges booking equipment, updates booking total,
     * then resolves status from pending extra to checked in/completed.
     */
    
    public boolean finalizeSupplementaryEquipment(UUID bookingId, List<BookingEquipment> equipmentList, BigDecimal additionalAmount) {
        lastInsertError = null;

        if (bookingId == null) {
            lastInsertError = "Invalid booking.";
            return false;
        }
        if (equipmentList == null || equipmentList.isEmpty()) {
            lastInsertError = "No equipment selected.";
            return false;
        }
        if (additionalAmount == null || additionalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            lastInsertError = "Invalid equipment total.";
            return false;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            BookingSnapshot snapshot = getBookingSnapshot(conn, bookingId);
            if (snapshot == null) {
                conn.rollback();
                lastInsertError = "Booking not found.";
                return false;
            }

            if (!STATUS_PENDING_EXTRA.equals(snapshot.status) && !STATUS_CHECKED_IN.equals(snapshot.status)) {
                conn.rollback();
                lastInsertError = "Only pending-extra bookings can finalize supplementary equipment.";
                return false;
            }

            if (STATUS_CHECKED_IN.equals(snapshot.status) && !isBookingActive(snapshot, LocalDateTime.now())) {
                conn.rollback();
                lastInsertError = "Checked-in booking is outside its active time window.";
                return false;
            }

            String updateEquip = "UPDATE le "
                    + "SET le.quantity = le.quantity - ?, "
                    + "    le.status = CASE WHEN le.quantity - ? <= 0 THEN 'unavailable' ELSE 'available' END "
                    + "FROM Location_Equipment le "
                    + "INNER JOIN Field f ON f.location_id = le.location_id "
                    + "INNER JOIN Booking b ON b.field_id = f.field_id "
                    + "WHERE b.booking_id = ? AND le.equipment_id = ? AND le.quantity >= ?";

            // Reserve stock first. If any row fails, rollback whole supplementary finalize.
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
                        lastInsertError = "Equipment stock changed. Please review selected quantities.";
                        return false;
                    }
                }
            }

            String updateExistingEquipment = "UPDATE Booking_Equipment SET quantity = quantity + ? WHERE booking_id = ? AND equipment_id = ?";
            String insertNewEquipment = "INSERT INTO Booking_Equipment (booking_id, equipment_id, quantity) VALUES (?, ?, ?)";

            // Merge supplementary items into Booking_Equipment: update existing rows, insert missing rows.
            try (PreparedStatement updatePs = conn.prepareStatement(updateExistingEquipment);
                 PreparedStatement insertPs = conn.prepareStatement(insertNewEquipment)) {
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

            // After payment success, move status from pending extra to checked in/completed by slot time.
            String postPaymentStatus = resolvePostSupplementaryStatus(snapshot, LocalDateTime.now());
            if (!postPaymentStatus.equals(snapshot.status)) {
                if (!updateBookingStatus(conn, bookingId, postPaymentStatus, snapshot.status)) {
                    conn.rollback();
                    lastInsertError = "Failed to update booking status after supplementary payment.";
                    return false;
                }
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (lastInsertError == null || lastInsertError.isBlank()) {
                lastInsertError = "Database error while finalizing supplementary equipment: " + e.getMessage();
            }
            return false;
        }
    }

    /**
     * Returns supplementary amount currently represented by booking total - schedule field price.
     */
    
    public BigDecimal getSupplementaryAmountByBookingId(UUID bookingId) {
        try (Connection conn = DBConnection.getConnection()) {
            return getSupplementaryAmount(conn, bookingId);
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    /**
     * Calculates supplementary amount using one open connection.
     */
    
    private BigDecimal getSupplementaryAmount(Connection conn, UUID bookingId) throws SQLException {
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

    /**
     * Gets bookings for one location.
     */
    
    //Description: Retrieves and prepares data for getByLocation by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public List<BookingViewModel> getByLocation(UUID locationId) {
        // Internal Flow: query data source, map database rows to model objects, and return null/empty values safely on edge cases.
        synchronizeBookingStates();
        List<BookingViewModel> list = new ArrayList<>();
    String sql = "SELECT b.booking_id, b.booker_id, b.field_id, b.schedule_id, b.status, b.total_price, s.booking_date, s.start_time, s.end_time, f.field_name, f.location_id, u.full_name AS customer_name, COALESCE(b.phone_number, u.phone) AS customer_phone " +
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
                String bookingLocationId = rs.getString("location_id");
                if (bookingLocationId != null) vm.setLocationId(UUID.fromString(bookingLocationId));
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
     * Gets location bookings with optional filters by date, status and customer keyword.
     */
    
    //Description: Retrieves and prepares data for getByLocationFiltered by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public List<BookingViewModel> getByLocationFiltered(UUID locationId, String bookingDateStr, String status, String customerKeyword) {
        // Internal Flow: query data source, map database rows to model objects, and return null/empty values safely on edge cases.
        synchronizeBookingStates();
        List<BookingViewModel> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
    sql.append("SELECT b.booking_id, b.booker_id, b.field_id, b.schedule_id, b.status, b.total_price, s.booking_date, s.start_time, s.end_time, f.field_name, f.location_id, u.full_name AS customer_name, COALESCE(b.phone_number, u.phone) AS customer_phone ");
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
            sql.append(" AND (LOWER(u.full_name) LIKE LOWER(?) OR b.phone_number LIKE ?) ");
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
                String bookingLocationId = rs.getString("location_id");
                if (bookingLocationId != null) vm.setLocationId(UUID.fromString(bookingLocationId));
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
    
    //Description: Retrieves and prepares data for getBookingById by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public Booking getBookingById(UUID bookingId) {
        // Internal Flow: query data source, map database rows to model objects, and return null/empty values safely on edge cases.
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

    /**
     * Resets payment deadline for a booking (typically used by staff/admin support flow).
     */
    
    //Description: Executes the resetPaymentDeadline write workflow, including input normalization, transactional SQL updates/inserts, consistency checks, and explicit success/failure signaling for calling services.
    public boolean resetPaymentDeadline(UUID bookingId, LocalDateTime newDeadline) {
        // Internal Flow: apply guard checks, execute core logic, and keep exception handling localized to DAO responsibilities.
        if (bookingId == null || newDeadline == null) {
            return false;
        }

        String sql = "UPDATE Booking SET payment_deadline = ? WHERE booking_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(newDeadline));
            ps.setString(2, bookingId.toString());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Auto-synchronizes lifecycle states:
     * - paid bookings past slot end => cancelled and resources released
     * - checked-in bookings past slot end => completed
     * pending extra is intentionally excluded to allow payment at any time.
     */
    
    //Description: Implements the synchronizeBookingStates business routine with validation, database interaction, exception handling, and predictable outputs for upstream controllers/services.
    private void synchronizeBookingStates() {
        // Internal Flow: apply guard checks, execute core logic, and keep exception handling localized to DAO responsibilities.
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

    /**
     * Loads a compact booking snapshot used by status-transition logic.
     */
    
    //Description: Retrieves and prepares data for getBookingSnapshot by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    private BookingSnapshot getBookingSnapshot(Connection conn, UUID bookingId) throws SQLException {
        // Internal Flow: query data source, map database rows to model objects, and return null/empty values safely on edge cases.
        String sql = "SELECT b.schedule_id, b.field_id, b.status, s.booking_date, s.start_time, s.end_time "
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
                String fieldId = rs.getString("field_id");
                if (fieldId != null) {
                    snapshot.fieldId = UUID.fromString(fieldId);
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

    /**
     * Checks whether current time lies inside booking schedule window [start, end).
     */
    
    //Description: Evaluates business conditions in isBookingActive and returns a deterministic boolean result used to protect state transitions and payment/booking integrity rules.
    private boolean isBookingActive(BookingSnapshot snapshot, LocalDateTime now) {
        // Internal Flow: compute condition from normalized state and return a strict boolean outcome.
        if (snapshot == null || snapshot.scheduleStart == null || snapshot.scheduleEnd == null) {
            return false;
        }
        return !now.isBefore(snapshot.scheduleStart) && now.isBefore(snapshot.scheduleEnd);
    }

    /**
     * Resolves status after supplementary payment based on schedule end time.
     */
    
    //Description: Retrieves and prepares data for resolvePostSupplementaryStatus by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    private String resolvePostSupplementaryStatus(BookingSnapshot snapshot, LocalDateTime now) {
        // Internal Flow: apply guard checks, execute core logic, and keep exception handling localized to DAO responsibilities.
        if (snapshot != null && snapshot.scheduleEnd != null && !now.isBefore(snapshot.scheduleEnd)) {
            return STATUS_COMPLETED;
        }
        return STATUS_CHECKED_IN;
    }

    /**
     * Checks whether payment linked to booking is successful.
     */
    
    //Description: Evaluates business conditions in isPaymentSuccessful and returns a deterministic boolean result used to protect state transitions and payment/booking integrity rules.
    private boolean isPaymentSuccessful(Connection conn, UUID bookingId) throws SQLException {
        // Internal Flow: compute condition from normalized state and return a strict boolean outcome.
        String sql = "SELECT LOWER(ISNULL(payment_status, '')) AS payment_status FROM Payment WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                String paymentStatus = normalizeStatus(rs.getString("payment_status"));
                return "success".equals(paymentStatus) || "paid".equals(paymentStatus);
            }
        }
    }

    /**
     * Updates booking status only when current status matches expectedStatus.
     */
    
    //Description: Executes the updateBookingStatus write workflow, including input normalization, transactional SQL updates/inserts, consistency checks, and explicit success/failure signaling for calling services.
    private boolean updateBookingStatus(Connection conn, UUID bookingId, String newStatus, String expectedStatus) throws SQLException {
        // Internal Flow: validate inputs, run transactional SQL mutations, and propagate a clear commit/rollback result.
        String sql = "UPDATE Booking SET status = ? WHERE booking_id = ? AND LOWER(ISNULL(status, '')) = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, bookingId.toString());
            ps.setString(3, normalizeStatus(expectedStatus));
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Updates payment status by booking ID and optionally touches payment_time.
     */
    
    //Description: Executes the updatePaymentStatusByBooking write workflow, including input normalization, transactional SQL updates/inserts, consistency checks, and explicit success/failure signaling for calling services.
    private void updatePaymentStatusByBooking(Connection conn, UUID bookingId, String paymentStatus, boolean touchPaymentTime) throws SQLException {
        // Internal Flow: validate inputs, run transactional SQL mutations, and propagate a clear commit/rollback result.
        String sql = touchPaymentTime
                ? "UPDATE Payment SET payment_status = ?, payment_time = SYSDATETIME() WHERE booking_id = ?"
                : "UPDATE Payment SET payment_status = ? WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paymentStatus);
            ps.setString(2, bookingId.toString());
            ps.executeUpdate();
        }
    }

    /**
     * Releases schedule slot and returns all booked equipment quantity to location stock.
     */
    
    //Description: Implements the releaseBookingResources business routine with validation, database interaction, exception handling, and predictable outputs for upstream controllers/services.
    private void releaseBookingResources(Connection conn, UUID bookingId, UUID scheduleId) throws SQLException {
        // Internal Flow: apply guard checks, execute core logic, and keep exception handling localized to DAO responsibilities.
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

    /**
     * Normalizes status string for stable comparisons.
     */
    
    //Description: Applies normalization/mapping logic in normalizeStatus to keep data format stable across DAO boundaries and reduce duplicate parsing logic in higher layers.
    private String normalizeStatus(String status) {
        // Internal Flow: apply guard checks, execute core logic, and keep exception handling localized to DAO responsibilities.
        return status == null ? "" : status.trim().toLowerCase();
    }

    private static class BookingSnapshot {
        private UUID bookingId;
        private UUID scheduleId;
        private UUID fieldId;
        private String status;
        private LocalDateTime scheduleStart;
        private LocalDateTime scheduleEnd;
    }

    /**
     * Weekly booking: lock multiple schedule slots and create one Booking per slot in a single
     * atomic transaction.  If ANY slot is already 'unavailable' the whole transaction rolls back.
     * Equipment (same list) is attached to every booking and stock is decremented per session.
     *
     * @param equipmentList equipment to attach to each session (may be null/empty)
     * @return list of created Booking objects (one per selected schedule)
     */
    
    //Description: Executes the insertWeekly write workflow, including input normalization, transactional SQL updates/inserts, consistency checks, and explicit success/failure signaling for calling services.
    public List<Booking> insertWeekly(UUID bookerId, UUID fieldId, List<UUID> scheduleIds,
                                      List<BookingEquipment> equipmentList,
                                      UUID voucherId, java.math.BigDecimal discountPercent,
                                      java.time.LocalDateTime paymentDeadline,
                                      UUID weeklyGroupId,
                                      String bookingPhone) throws Exception {
        // Internal Flow: validate inputs, run transactional SQL mutations, and propagate a clear commit/rollback result.

        List<Booking> created = new ArrayList<>();
        int sessionCount = scheduleIds.size();

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                java.time.LocalDateTime now = java.time.LocalDateTime.now();

                // Ã¢â€â‚¬Ã¢â€â‚¬ Pre-check equipment stock across ALL sessions Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
                if (equipmentList != null && !equipmentList.isEmpty()) {
                    String stockSql = "SELECT le.quantity FROM Location_Equipment le "
                            + "INNER JOIN Field f ON f.location_id = le.location_id "
                            + "WHERE f.field_id = ? AND le.equipment_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(stockSql)) {
                        for (BookingEquipment be : equipmentList) {
                            ps.setString(1, fieldId.toString());
                            ps.setString(2, be.getEquipmentId().toString());
                            ResultSet rs = ps.executeQuery();
                            int stock = rs.next() ? rs.getInt(1) : 0;
                            int needed = be.getQuantity() * sessionCount;
                            if (stock < needed) {
                                conn.rollback();
                                throw new Exception("Khong du so luong dung cu cho tat ca "
                                        + sessionCount + " phien trong tuan. "
                                        + "Ton kho hien tai: " + stock + ", can: " + needed + ".");
                            }
                        }
                    }
                }

                BigDecimal equipmentTotalPerSession = BigDecimal.ZERO;
                if (equipmentList != null && !equipmentList.isEmpty()) {
                    String equipPriceSql = "SELECT rental_price FROM Equipment WHERE equipment_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(equipPriceSql)) {
                        for (BookingEquipment be : equipmentList) {
                            ps.setString(1, be.getEquipmentId().toString());
                            ResultSet rs = ps.executeQuery();
                            if (!rs.next()) {
                                conn.rollback();
                                throw new Exception("Equipment not found: " + be.getEquipmentId());
                            }
                            BigDecimal rentalPrice = rs.getBigDecimal(1);
                            if (rentalPrice != null && be.getQuantity() > 0) {
                                equipmentTotalPerSession = equipmentTotalPerSession.add(
                                        rentalPrice.multiply(BigDecimal.valueOf(be.getQuantity()))
                                );
                            }
                        }
                    }
                }

                String lockSql  = "UPDATE [Schedule] SET status = 'unavailable' "
                        + "WHERE schedule_id = ? AND field_id = ? AND status = 'available'";
                String priceSql = "SELECT price FROM [Schedule] WHERE schedule_id = ? AND field_id = ?";

                // Ã¢â€â‚¬Ã¢â€â‚¬ Phase 1: lock every slot + compute per-booking price Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
                for (UUID sid : scheduleIds) {
                    java.math.BigDecimal rawPrice = java.math.BigDecimal.ZERO;
                    try (PreparedStatement ps = conn.prepareStatement(priceSql)) {
                        ps.setString(1, sid.toString());
                        ps.setString(2, fieldId.toString());
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            java.math.BigDecimal p = rs.getBigDecimal("price");
                            if (p != null) rawPrice = p;
                        } else {
                            conn.rollback();
                            throw new Exception("Khung gio khong hop le hoac khong thuoc san da chon.");
                        }
                    }

                    try (PreparedStatement ps = conn.prepareStatement(lockSql)) {
                        ps.setString(1, sid.toString());
                        ps.setString(2, fieldId.toString());
                        int affected = ps.executeUpdate();
                        if (affected == 0) {
                            conn.rollback();
                            throw new Exception("Mot hoac nhieu khung gio da duoc dat boi nguoi khac. "
                                    + "Vui long kiem tra lai lich trong va thu chon lai.");
                        }
                    }

                    java.math.BigDecimal subtotal = rawPrice.add(equipmentTotalPerSession);
                    java.math.BigDecimal factor = java.math.BigDecimal.ONE.subtract(
                            discountPercent.divide(java.math.BigDecimal.valueOf(100)));
                    java.math.BigDecimal totalPrice = subtotal.multiply(factor);

                    Booking b = new Booking();
                    b.setBookingId(UUID.randomUUID());
                    b.setBookerId(bookerId);
                    b.setPhoneNumber(bookingPhone);
                    b.setFieldId(fieldId);
                    b.setScheduleId(sid);
                    b.setVoucherId(voucherId);
                    b.setBookingTime(now);
                    b.setStatus("pending");
                    b.setTotalPrice(totalPrice);
                    b.setPaymentDeadline(paymentDeadline);
                    b.setWeeklyGroupId(weeklyGroupId);
                    created.add(b);
                }

                // Ã¢â€â‚¬Ã¢â€â‚¬ Phase 2: deduct equipment stock once per session Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
                if (equipmentList != null && !equipmentList.isEmpty()) {
                    String updateEquip = "UPDATE le "
                            + "SET le.quantity = le.quantity - ?, "
                            + "    le.status = CASE WHEN le.quantity - ? <= 0 THEN 'unavailable' ELSE 'available' END "
                            + "FROM Location_Equipment le "
                            + "INNER JOIN Field f ON f.location_id = le.location_id "
                            + "WHERE f.field_id = ? AND le.equipment_id = ? AND le.quantity >= ?";
                    try (PreparedStatement ps = conn.prepareStatement(updateEquip)) {
                        for (BookingEquipment be : equipmentList) {
                            int totalQty = be.getQuantity() * sessionCount;
                            ps.setInt(1, totalQty);
                            ps.setInt(2, totalQty);
                            ps.setString(3, fieldId.toString());
                            ps.setString(4, be.getEquipmentId().toString());
                            ps.setInt(5, totalQty);
                            int affected = ps.executeUpdate();
                            if (affected == 0) {
                                conn.rollback();
                                throw new Exception("Dung cu khong du so luong. Vui long giam so luong hoac bo chon.");
                            }
                        }
                    }
                }

                // Ã¢â€â‚¬Ã¢â€â‚¬ Phase 3: insert all booking rows Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
                String insertSql = "INSERT INTO Booking "
                        + "(booking_id, booker_id, phone_number, field_id, schedule_id, voucher_id, weekly_group_id, "
                        + " status, total_price, payment_deadline) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, 'pending', ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    for (Booking b : created) {
                        ps.setString(1, b.getBookingId().toString());
                        ps.setString(2, b.getBookerId().toString());
                        if (b.getPhoneNumber() != null && !b.getPhoneNumber().trim().isEmpty()) {
                            ps.setString(3, b.getPhoneNumber().trim());
                        } else {
                            ps.setNull(3, Types.NVARCHAR);
                        }
                        ps.setString(4, b.getFieldId().toString());
                        ps.setString(5, b.getScheduleId().toString());
                        if (b.getVoucherId() != null) {
                            ps.setString(6, b.getVoucherId().toString());
                        } else {
                            ps.setNull(6, Types.VARCHAR);
                        }
                        if (b.getWeeklyGroupId() != null) {
                            ps.setString(7, b.getWeeklyGroupId().toString());
                        } else {
                            ps.setNull(7, Types.VARCHAR);
                        }
                        ps.setBigDecimal(8, b.getTotalPrice());
                        if (b.getPaymentDeadline() != null) {
                            ps.setTimestamp(9, Timestamp.valueOf(b.getPaymentDeadline()));
                        } else {
                            ps.setNull(9, Types.TIMESTAMP);
                        }
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                // Ã¢â€â‚¬Ã¢â€â‚¬ Phase 4: insert Booking_Equipment rows for each session Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
                if (equipmentList != null && !equipmentList.isEmpty()) {
                    String insertEquip = "INSERT INTO Booking_Equipment (booking_id, equipment_id, quantity) VALUES (?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(insertEquip)) {
                        for (Booking b : created) {
                            for (BookingEquipment be : equipmentList) {
                                ps.setString(1, b.getBookingId().toString());
                                ps.setString(2, be.getEquipmentId().toString());
                                ps.setInt(3, be.getQuantity());
                                ps.addBatch();
                            }
                        }
                        ps.executeBatch();
                    }
                }

                conn.commit();
                return created;

            } catch (Exception e) {
                try { conn.rollback(); } catch (SQLException ignored) {}
                throw e;
            }
        }
    }

}
