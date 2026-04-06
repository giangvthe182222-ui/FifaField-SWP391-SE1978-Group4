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

    // last insert error: to inform user to view error of the last booking 
    private String lastInsertError;

    // All booking statuses
    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_PAID = "paid";
    private static final String STATUS_CANCELLED = "cancelled";
    private static final String STATUS_PENDING_REFUND = "pending refund";
    private static final String STATUS_PENDING_REFUND_CONFIRM = "pending refund confirm";
    private static final String STATUS_REFUNDED = "refunded";
    private static final String STATUS_CHECKED_IN = "checked in";
    private static final String STATUS_CHECKED_OUT = "checked out";
    private static final String STATUS_PENDING_EXTRA = "pending extra";
    private static final String STATUS_FINISHED = "finished";
    private static final String STATUS_COMPLETED = "completed";
    private static final String STATUS_DEPOSITED = "deposited";

    private static final String PLAY_STATUS_BOOKED = "booked";
    private static final String PLAY_STATUS_CANCELLED = "cancelled";
    private static final String PAYMENT_STATUS_FAILED = "failed";
    private static final String EXTRA_PAYMENT_STATUS_NONE = "none";
    private static final String EXTRA_PAYMENT_STATUS_PAID = "paid extra";

        private static final Set<String> SUPPORTED_PLAY_STATUSES = new HashSet<>(Arrays.asList(
            PLAY_STATUS_BOOKED,
            STATUS_CHECKED_IN,
            STATUS_CHECKED_OUT,
            STATUS_COMPLETED,
            PLAY_STATUS_CANCELLED
        ));

        private static final Set<String> SUPPORTED_PAYMENT_STATUSES = new HashSet<>(Arrays.asList(
            STATUS_PENDING,
            STATUS_DEPOSITED,
            STATUS_PAID,
            STATUS_PENDING_REFUND,
            STATUS_REFUNDED,
            PAYMENT_STATUS_FAILED
        ));

        private static final Set<String> SUPPORTED_EXTRA_PAYMENT_STATUSES = new HashSet<>(Arrays.asList(
            EXTRA_PAYMENT_STATUS_NONE,
            STATUS_PENDING_EXTRA,
            EXTRA_PAYMENT_STATUS_PAID
        ));

    private static final Set<String> SUPPORTED_STATUSES = new HashSet<>(Arrays.asList(
            STATUS_PENDING,
            STATUS_PAID,
            STATUS_CANCELLED,
            STATUS_PENDING_REFUND,
            STATUS_REFUNDED,
            STATUS_CHECKED_IN,
            STATUS_CHECKED_OUT,
            STATUS_PENDING_EXTRA,
            STATUS_FINISHED,
            STATUS_COMPLETED,
            STATUS_DEPOSITED
    ));

    /**
     * giangvthe182222 Creates a new booking in a single transaction: locks
     * schedule, reserves equipment stock, inserts booking and booking-equipment
     * rows.
     */
    public boolean insert(Booking booking, List<BookingEquipment> equipmentList) {

        lastInsertError = null;

        try (Connection conn = DBConnection.getConnection()) {
            //set auto commit to false to heandle transaction properly
            conn.setAutoCommit(false);

            // Locking the slot so that double booking cannot happen
            String updateSchedule = "UPDATE Schedule SET status = 'unavailable' WHERE schedule_id = ? AND status = 'available'";

            try (PreparedStatement ps = conn.prepareStatement(updateSchedule)) {
                ps.setString(1, booking.getScheduleId().toString());
                int affected = ps.executeUpdate();

                if (affected == 0) {
                    conn.rollback();
                    lastInsertError = "Selected schedule is no longer available.";
                    return false; // slot haven taken
                }
            }

            // Minus the equipment amount chosed in the bookings
            if (equipmentList != null && !equipmentList.isEmpty()) {

                String updateEquip = "UPDATE le "
                        + "SET le.quantity = le.quantity - ?, "
                        + "    le.status = CASE WHEN le.quantity - ? <= 0 THEN 'unavailable' ELSE 'available' END "
                        + "FROM Location_Equipment le "
                        + "INNER JOIN Field f ON f.location_id = le.location_id "
                        + "WHERE f.field_id = ? AND le.equipment_id = ? AND le.quantity >= ?";

                try (PreparedStatement ps = conn.prepareStatement(updateEquip)) {

                    for (BookingEquipment be : equipmentList) {

                        // Decrease equipment quantity
                        // If quantity <= 0, set status to 'unavailable'
                        ps.setInt(1, be.getQuantity());
                        ps.setInt(2, be.getQuantity());
                        ps.setString(3, booking.getFieldId().toString());
                        ps.setString(4, be.getEquipmentId().toString());
                        ps.setInt(5, be.getQuantity());

                        int affected = ps.executeUpdate();

                        if (affected == 0) {
                            conn.rollback();
                            lastInsertError = "Equipment stock changed. Please review selected quantities.";
                            return false; // equipment amount not enough in db when trying to insert
                        }
                    }
                }
            }

            // insert the booking (migration handled)
            if (!insertBookingRow(conn, booking)) {
                conn.rollback();
                return false;
            }

            // insert the info of equipments chosen for a booking
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
            //final commit
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
     * Returns the last error captured by write operations in this DAO.
     */
    //Retrieves and prepares data for getLastInsertError by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public String getLastInsertError() {
        return lastInsertError;
    }

    /**
     * Inserts booking row using the current schema contract.
     */
    private boolean insertBookingRow(Connection conn, Booking booking) throws SQLException {
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

            String playStatus = normalizeStatus(booking.getPlayStatus());
            String paymentStatus = normalizeStatus(booking.getPaymentStatus());
            String extraPaymentStatus = normalizeStatus(booking.getExtraPaymentStatus());
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
            lastInsertError = "Cannot insert booking record: " + ex.getMessage();
            return false;
        }
    }

    //=====================================================================GET BOOKINGs INFO================================================================//
    /**
     * Gets booking history for one customer.
     */
    //etrieves and prepares data for getByBooker by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public List<BookingViewModel> getByBooker(UUID bookerId) {
        // Internal Flow: query data source, map database rows to model objects, and return null/empty values safely on edge cases.
        synchronizeBookingStates();
        List<BookingViewModel> list = new ArrayList<>();
        String sql = "SELECT b.booking_id, b.booker_id, b.field_id, b.schedule_id, b.play_status, b.payment_status, b.extra_payment_status, b.total_price, s.booking_date, s.start_time, s.end_time, f.field_name, u.full_name AS customer_name, COALESCE(b.phone_number, u.phone) AS customer_phone "
                + "FROM Booking b "
                + "LEFT JOIN Schedule s ON b.schedule_id = s.schedule_id "
                + "LEFT JOIN Field f ON b.field_id = f.field_id "
                + "LEFT JOIN Users u ON b.booker_id = u.user_id "
            + "WHERE b.booker_id = ? ORDER BY b.booking_time DESC, s.booking_date DESC, s.start_time DESC";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, bookerId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BookingViewModel vm = new BookingViewModel();
                vm.setBookingId(UUID.fromString(rs.getString("booking_id")));
                vm.setBookerId(UUID.fromString(rs.getString("booker_id")));
                vm.setFieldId(UUID.fromString(rs.getString("field_id")));
                vm.setScheduleId(UUID.fromString(rs.getString("schedule_id")));
                Date bd = rs.getDate("booking_date");
                if (bd != null) {
                    vm.setBookingDate(bd.toLocalDate());
                }
                Time st = rs.getTime("start_time");
                if (st != null) {
                    vm.setStartTime(st.toLocalTime());
                }
                Time et = rs.getTime("end_time");
                if (et != null) {
                    vm.setEndTime(et.toLocalTime());
                }
                vm.setFieldName(rs.getString("field_name"));
                vm.setCustomerName(rs.getString("customer_name"));
                vm.setCustomerPhone(rs.getString("customer_phone"));
                applyStateToBookingViewModel(rs, vm);
                vm.setTotalPrice(rs.getBigDecimal("total_price"));
                list.add(vm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Gets customer bookings with optional filters by date, start time and
     * status.
     */
    //Retrieves and prepares data for getByBookerFiltered by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public List<BookingViewModel> getByBookerFiltered(UUID bookerId, String bookingDateStr, String startTimeStr, String status) {
        return getByBookerFiltered(bookerId, bookingDateStr, startTimeStr, null, null, null, status);
    }

    public List<BookingViewModel> getByBookerFiltered(UUID bookerId,
                                                       String bookingDateStr,
                                                       String startTimeStr,
                                                       String playStatus,
                                                       String paymentStatus,
                                                       String extraPaymentStatus,
                                                       String status) {
        // Internal Flow: query data source, map database rows to model objects, and return null/empty values safely on edge cases.
        synchronizeBookingStates();
        List<BookingViewModel> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT b.booking_id, b.booker_id, b.field_id, b.schedule_id, b.play_status, b.payment_status, b.extra_payment_status, b.total_price, s.booking_date, s.start_time, s.end_time, f.field_name, u.full_name AS customer_name ");
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
        String legacyStatusExpr = legacyStatusExpression("b");
        boolean filterCheckedOut = "checked out".equalsIgnoreCase(status == null ? "" : status.trim());
        if (playStatus != null && !playStatus.isBlank()) {
            boolean playCheckedOut = "checked out".equalsIgnoreCase(playStatus.trim());
            if (playCheckedOut) {
                sql.append(" AND LOWER(ISNULL(b.play_status, '')) = 'checked out' ");
            } else {
                sql.append(" AND LOWER(ISNULL(b.play_status, '')) = LOWER(?) ");
            }
        }
        if (paymentStatus != null && !paymentStatus.isBlank()) {
            sql.append(" AND LOWER(ISNULL(b.payment_status, '')) = LOWER(?) ");
        }
        if (extraPaymentStatus != null && !extraPaymentStatus.isBlank()) {
            sql.append(" AND LOWER(ISNULL(b.extra_payment_status, '')) = LOWER(?) ");
        }
        if (status != null && !status.isBlank()) {
            if (filterCheckedOut) {
                sql.append(" AND LOWER(").append(legacyStatusExpr).append(") = 'checked out' ");
            } else {
                sql.append(" AND LOWER(").append(legacyStatusExpr).append(") = LOWER(?) ");
            }
        }
        // field filter removed per requirements

        sql.append(" ORDER BY b.booking_time DESC, s.booking_date DESC, s.start_time DESC");

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
            if (playStatus != null && !playStatus.isBlank() && !"checked out".equalsIgnoreCase(playStatus.trim())) {
                ps.setString(idx++, playStatus);
            }
            if (paymentStatus != null && !paymentStatus.isBlank()) {
                ps.setString(idx++, paymentStatus);
            }
            if (extraPaymentStatus != null && !extraPaymentStatus.isBlank()) {
                ps.setString(idx++, extraPaymentStatus);
            }
            if (status != null && !status.isBlank() && !filterCheckedOut) {
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
                if (bd != null) {
                    vm.setBookingDate(bd.toLocalDate());
                }
                Time st = rs.getTime("start_time");
                if (st != null) {
                    vm.setStartTime(st.toLocalTime());
                }
                Time et = rs.getTime("end_time");
                if (et != null) {
                    vm.setEndTime(et.toLocalTime());
                }
                vm.setFieldName(rs.getString("field_name"));
                vm.setCustomerName(rs.getString("customer_name"));
                applyStateToBookingViewModel(rs, vm);
                vm.setTotalPrice(rs.getBigDecimal("total_price"));
                list.add(vm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    //Retrieves and prepares data for getByWeeklyGroupId by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public List<BookingViewModel> getByWeeklyGroupId(UUID weeklyGroupId) {
        synchronizeBookingStates();
        List<BookingViewModel> list = new ArrayList<>();
        String sql = "SELECT b.booking_id, b.booker_id, b.field_id, b.schedule_id, b.play_status, b.payment_status, b.extra_payment_status, b.total_price, "
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

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, weeklyGroupId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BookingViewModel vm = new BookingViewModel();
                vm.setBookingId(UUID.fromString(rs.getString("booking_id")));
                vm.setBookerId(UUID.fromString(rs.getString("booker_id")));
                vm.setFieldId(UUID.fromString(rs.getString("field_id")));
                vm.setScheduleId(UUID.fromString(rs.getString("schedule_id")));
                Date bd = rs.getDate("booking_date");
                if (bd != null) {
                    vm.setBookingDate(bd.toLocalDate());
                }
                Time st = rs.getTime("start_time");
                if (st != null) {
                    vm.setStartTime(st.toLocalTime());
                }
                Time et = rs.getTime("end_time");
                if (et != null) {
                    vm.setEndTime(et.toLocalTime());
                }
                vm.setFieldName(rs.getString("field_name"));
                vm.setLocationName(rs.getString("location_name"));
                applyStateToBookingViewModel(rs, vm);
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
     * Gets customer bookings for calendar rendering in a given date range and
     * optional location/field filters.
     */
    //Retrieves and prepares data for getCustomerCalendarBookings by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public List<BookingViewModel> getCustomerCalendarBookings(UUID bookerId, LocalDate fromDate, LocalDate toDate, LocalDate selectedDate, UUID locationId, UUID fieldId) {
        synchronizeBookingStates();
        List<BookingViewModel> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT b.booking_id, b.booker_id, b.field_id, b.schedule_id, b.play_status, b.payment_status, b.extra_payment_status, b.total_price, s.booking_date, s.start_time, s.end_time, f.field_name, l.location_name, u.full_name AS customer_name ");
        sql.append("FROM Booking b ");
        sql.append("LEFT JOIN Schedule s ON b.schedule_id = s.schedule_id ");
        sql.append("LEFT JOIN Field f ON b.field_id = f.field_id ");
        sql.append("LEFT JOIN Location l ON f.location_id = l.location_id ");
        sql.append("LEFT JOIN Users u ON b.booker_id = u.user_id ");
        sql.append("WHERE b.booker_id = ? ");
        sql.append("AND LOWER(ISNULL(b.play_status, 'booked')) IN ('booked', 'checked in', 'checked out') ");
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
                if (bd != null) {
                    vm.setBookingDate(bd.toLocalDate());
                }
                Time st = rs.getTime("start_time");
                if (st != null) {
                    vm.setStartTime(st.toLocalTime());
                }
                Time et = rs.getTime("end_time");
                if (et != null) {
                    vm.setEndTime(et.toLocalTime());
                }

                vm.setFieldName(rs.getString("field_name"));
                vm.setLocationName(rs.getString("location_name"));
                vm.setCustomerName(rs.getString("customer_name"));
                applyStateToBookingViewModel(rs, vm);
                vm.setTotalPrice(rs.getBigDecimal("total_price"));
                list.add(vm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Gets distinct fields appearing in the customer's booking calendar type
     * dataset.
     */
    //Retrieves and prepares data for getCustomerCalendarFields by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public List<Field> getCustomerCalendarFields(UUID bookerId) {
        return getCustomerCalendarFields(bookerId, null);
    }

    //Retrieves and prepares data for getCustomerCalendarFields by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public List<Field> getCustomerCalendarFields(UUID bookerId, UUID locationId) {
        synchronizeBookingStates();
        List<Field> fields = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT f.field_id, f.field_name, f.field_type, f.image_url, f.status, f.[condition], f.location_id ");
        sql.append("FROM Booking b ");
        sql.append("JOIN Field f ON b.field_id = f.field_id ");
        sql.append("WHERE b.booker_id = ? ");
        sql.append("AND LOWER(").append(legacyStatusExpression("b")).append(") NOT IN ('cancelled', 'refunded') ");
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
     * Gets distinct locations appearing in the customer's booking calendar
     * dataset.
     */
    //Description: Retrieves and prepares data for getCustomerCalendarLocations by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public List<Location> getCustomerCalendarLocations(UUID bookerId) {
        // Internal Flow: query data source, map database rows to model objects, and return null/empty values safely on edge cases.
        synchronizeBookingStates();
        List<Location> locations = new ArrayList<>();
        String sql = "SELECT DISTINCT l.location_id, l.location_name "
                + "FROM Booking b "
                + "JOIN Field f ON b.field_id = f.field_id "
                + "JOIN Location l ON f.location_id = l.location_id "
                + "WHERE b.booker_id = ? "
                + "AND LOWER(" + legacyStatusExpression("b") + ") NOT IN ('cancelled', 'refunded') "
                + "ORDER BY l.location_name";

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
    //Retrieves and prepares data for getById by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public BookingViewModel getById(UUID bookingId) {
        // Internal Flow: query data source, map database rows to model objects, and return null/empty values safely on edge cases.
        synchronizeBookingStates();
        String sql = "SELECT b.booking_id, b.booker_id, b.field_id, b.schedule_id, b.play_status, b.payment_status, b.extra_payment_status, b.total_price, s.booking_date, s.start_time, s.end_time, f.field_name, f.location_id, u.full_name AS customer_name "
                + "FROM Booking b "
                + "LEFT JOIN Schedule s ON b.schedule_id = s.schedule_id "
                + "LEFT JOIN Field f ON b.field_id = f.field_id "
                + "LEFT JOIN Users u ON b.booker_id = u.user_id "
                + "WHERE b.booking_id = ?";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, bookingId.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                BookingViewModel vm = new BookingViewModel();
                vm.setBookingId(UUID.fromString(rs.getString("booking_id")));
                vm.setBookerId(UUID.fromString(rs.getString("booker_id")));
                vm.setFieldId(UUID.fromString(rs.getString("field_id")));
                String locationId = rs.getString("location_id");
                if (locationId != null) {
                    vm.setLocationId(UUID.fromString(locationId));
                }
                vm.setScheduleId(UUID.fromString(rs.getString("schedule_id")));
                Date bd = rs.getDate("booking_date");
                if (bd != null) {
                    vm.setBookingDate(bd.toLocalDate());
                }
                Time st = rs.getTime("start_time");
                if (st != null) {
                    vm.setStartTime(st.toLocalTime());
                }
                Time et = rs.getTime("end_time");
                if (et != null) {
                    vm.setEndTime(et.toLocalTime());
                }
                vm.setFieldName(rs.getString("field_name"));
                vm.setCustomerName(rs.getString("customer_name"));
                applyStateToBookingViewModel(rs, vm);
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
    //Retrieves and prepares data for getByScheduleId by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public BookingViewModel getByScheduleId(UUID scheduleId) {
        synchronizeBookingStates();
        String sql = "SELECT TOP 1 b.booking_id, b.booker_id, b.field_id, b.schedule_id, b.play_status, b.payment_status, b.extra_payment_status, b.total_price, s.booking_date, s.start_time, s.end_time, f.field_name, u.full_name AS customer_name, COALESCE(b.phone_number, u.phone) AS customer_phone "
                + "FROM Booking b "
                + "LEFT JOIN Schedule s ON b.schedule_id = s.schedule_id "
                + "LEFT JOIN Field f ON b.field_id = f.field_id "
                + "LEFT JOIN Users u ON b.booker_id = u.user_id "
            + "WHERE b.schedule_id = ? AND LOWER(" + legacyStatusExpression("b") + ") NOT IN ('cancelled', 'refunded') "
                + "ORDER BY b.booking_time DESC, s.booking_date DESC, s.start_time DESC";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, scheduleId.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                BookingViewModel vm = new BookingViewModel();
                vm.setBookingId(UUID.fromString(rs.getString("booking_id")));
                vm.setBookerId(UUID.fromString(rs.getString("booker_id")));
                vm.setFieldId(UUID.fromString(rs.getString("field_id")));
                vm.setScheduleId(UUID.fromString(rs.getString("schedule_id")));
                Date bd = rs.getDate("booking_date");
                if (bd != null) {
                    vm.setBookingDate(bd.toLocalDate());
                }
                Time st = rs.getTime("start_time");
                if (st != null) {
                    vm.setStartTime(st.toLocalTime());
                }
                Time et = rs.getTime("end_time");
                if (et != null) {
                    vm.setEndTime(et.toLocalTime());
                }
                vm.setFieldName(rs.getString("field_name"));
                vm.setCustomerName(rs.getString("customer_name"));
                vm.setCustomerPhone(rs.getString("customer_phone"));
                applyStateToBookingViewModel(rs, vm);
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
    //Retrieves and prepares data for getByScheduleIdForCalendar by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public BookingViewModel getByScheduleIdForCalendar(UUID scheduleId) {
        synchronizeBookingStates();
        String sql = "SELECT TOP 1 b.booking_id, b.booker_id, b.field_id, b.schedule_id, b.play_status, b.payment_status, b.extra_payment_status, b.total_price, s.booking_date, s.start_time, s.end_time, f.field_name, u.full_name AS customer_name, COALESCE(b.phone_number, u.phone) AS customer_phone "
                + "FROM Booking b "
                + "LEFT JOIN Schedule s ON b.schedule_id = s.schedule_id "
                + "LEFT JOIN Field f ON b.field_id = f.field_id "
                + "LEFT JOIN Users u ON b.booker_id = u.user_id "
                + "WHERE b.schedule_id = ? "
            + "AND LOWER(" + legacyStatusExpression("b") + ") IN ('pending', 'deposited', 'paid', 'checked in', 'checked out', 'completed') "
                + "ORDER BY b.booking_time DESC, s.booking_date DESC, s.start_time DESC";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, scheduleId.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                BookingViewModel vm = new BookingViewModel();
                vm.setBookingId(UUID.fromString(rs.getString("booking_id")));
                vm.setBookerId(UUID.fromString(rs.getString("booker_id")));
                vm.setFieldId(UUID.fromString(rs.getString("field_id")));
                vm.setScheduleId(UUID.fromString(rs.getString("schedule_id")));
                Date bd = rs.getDate("booking_date");
                if (bd != null) {
                    vm.setBookingDate(bd.toLocalDate());
                }
                Time st = rs.getTime("start_time");
                if (st != null) {
                    vm.setStartTime(st.toLocalTime());
                }
                Time et = rs.getTime("end_time");
                if (et != null) {
                    vm.setEndTime(et.toLocalTime());
                }
                vm.setFieldName(rs.getString("field_name"));
                vm.setCustomerName(rs.getString("customer_name"));
                vm.setCustomerPhone(rs.getString("customer_phone"));
                applyStateToBookingViewModel(rs, vm);
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
    //etrieves and prepares data for getBookingEquipments by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public List<BookingEquipmentViewModel> getBookingEquipments(UUID bookingId) {
        List<BookingEquipmentViewModel> list = new ArrayList<>();
        String sql = "SELECT be.equipment_id, be.quantity, e.name, e.rental_price "
                + "FROM Booking_Equipment be "
                + "LEFT JOIN Equipment e ON be.equipment_id = e.equipment_id "
                + "WHERE be.booking_id = ?";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

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
     * Cancels booking by policy: pending bookings use payment-cancel flow; deposited
     * bookings may move to pending refund when eligible.
     */
    //Description: Executes the cancelBooking write workflow, including input normalization, transactional SQL updates/inserts, consistency checks, and explicit success/failure signaling for calling services.
    public boolean cancelBooking(UUID bookingId) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            // snapshot of booking to get booking's slot time and current status
            BookingSnapshot snapshot = getBookingSnapshot(conn, bookingId);
            if (snapshot == null) {
                conn.rollback();
                return false;
            }

            // to retrieve slots, equipments data if booking is cancelled
            if (STATUS_PENDING.equals(snapshot.status)) {
                conn.rollback();
                return cancelBookingForPayment(bookingId);
            }

            // refund request is only possible for paid/deposited bookings
            if ((!STATUS_PAID.equals(snapshot.status) && !STATUS_DEPOSITED.equals(snapshot.status)) || snapshot.scheduleStart == null) {
                conn.rollback();
                return false;
            }

            // refund is only allowed within maximum more than 2 days before schedule
            if (!snapshot.scheduleStart.isAfter(LocalDateTime.now().plusDays(2))) {
                conn.rollback();
                return false;
            }
            
            boolean updated = updateBookingStatus(conn, bookingId, STATUS_PENDING_REFUND, snapshot.status);
            if (!updated) {
                conn.rollback();
                return false;
            }

            // update peyment to refund pending status 
            updatePaymentStatusByBooking(conn, bookingId, "REFUND_PENDING", false);
            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Force-cancel booking for unpaid payment flow and immediately release
     * slot/equipment.
     */
    //Description: Executes the cancelBookingForPayment write workflow, including input normalization, transactional SQL updates/inserts, consistency checks, and explicit success/failure signaling for calling services.
    public boolean cancelBookingForPayment(UUID bookingId) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            BookingSnapshot snapshot = getBookingSnapshot(conn, bookingId);
            if (snapshot == null) {
                conn.rollback();
                return false;
            }

            // ennsure idempotent .
            if (STATUS_CANCELLED.equals(snapshot.status)) {
                conn.rollback();
                return true;
            }

            // to represent that this function applies only to pending status bookings
            if (!STATUS_PENDING.equals(snapshot.status)) {
                conn.rollback();
                return false;
            }

            // automatic cancellation based on payment timeout
            updateBookingStatus(conn, bookingId, STATUS_CANCELLED, STATUS_PENDING);
            // payment to failed status
            updatePaymentStatusByBooking(conn, bookingId, "FAILED", true);
            // Releases the slot and equipments
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
    //Executes the markBookingPaid write workflow, including input normalization, transactional SQL updates/inserts, consistency checks, and explicit success/failure signaling for calling services.
    public boolean markBookingPaid(UUID bookingId) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            boolean updated = updateBookingStatus(conn, bookingId, STATUS_PAID, STATUS_PENDING);
            if (!updated) {
                updated = updateBookingStatus(conn, bookingId, STATUS_PAID, STATUS_DEPOSITED);
            }
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
    
    public boolean markBookingDeposited(UUID bookingId) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            // only allows pending -> deposited status
            boolean updated = updateBookingStatus(conn, bookingId, STATUS_DEPOSITED, STATUS_PENDING);
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

    //Executes the markWeeklyGroupPaid write workflow, including input normalization, transactional SQL updates/inserts, consistency checks, and explicit success/failure signaling for calling services.
    public boolean markWeeklyGroupPaid(UUID weeklyGroupId) {
        // To set all the bookings from weekly booking group from pending to paid
        String sql = "UPDATE Booking SET payment_status = 'paid' WHERE weekly_group_id = ? AND LOWER(ISNULL(play_status, '')) = 'booked' AND LOWER(ISNULL(payment_status, '')) = 'pending'";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, weeklyGroupId.toString());
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Marks booking as pending extra (checked in -> pending extra) when staff
     * creates extra-equipment payment.
     */
    public boolean markBookingPendingExtra(UUID bookingId) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            BookingSnapshot snapshot = getBookingSnapshot(conn, bookingId);
            // only bookings that are checked in are allowed to have extra equipment booking
            if (snapshot == null || !STATUS_CHECKED_IN.equals(snapshot.status)) {
                conn.rollback();
                return false;
            }

            // Bookings have to be happening so that extra equipment booking is possible
            if (!isBookingActive(snapshot, LocalDateTime.now())) {
                conn.rollback();
                return false;
            }

            // to set status for unpaid extra equipment booking
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
     * Resolves booking status after supplementary payment succeeds: - pending
     * extra -> completed if schedule already ended - pending extra -> checked
     * in if schedule has not ended yet
     */
    public boolean settlePendingExtraStatus(UUID bookingId) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            BookingSnapshot snapshot = getBookingSnapshot(conn, bookingId);
            if (snapshot == null) {
                conn.rollback();
                return false;
            }

            // if booking is not pending extra then states in this function is needn't to happen
            if (!STATUS_PENDING_EXTRA.equals(snapshot.status)) {
                conn.rollback();
                return true;
            }

            // only allows function to work if payment is succesful
            if (!isPaymentSuccessful(conn, bookingId)) {
                conn.rollback();
                return false;
            }

            // if playtime is over then completed, if not booking keeps on at checked in status
            String targetStatus = resolvePostSupplementaryStatus(snapshot, LocalDateTime.now());
            // Move booking from 'pending extra' to the next state (checked in or completed)
            // Only update if current status is still 'pending extra' to avoid race condition issues
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

    //Executes the cancelWeeklyGroupForPayment write workflow, including input normalization, transactional SQL updates/inserts, consistency checks, and explicit success/failure signaling for calling services.
    public boolean cancelWeeklyGroupForPayment(UUID weeklyGroupId) {
        List<UUID> bookingIds = new ArrayList<>();
        String sql = "SELECT booking_id FROM Booking WHERE weekly_group_id = ? AND LOWER(ISNULL(play_status, '')) = 'booked' AND LOWER(ISNULL(payment_status, '')) = 'pending'";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, weeklyGroupId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                bookingIds.add(UUID.fromString(rs.getString("booking_id")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        //
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
    //Executes the updateStatus write workflow, including input normalization, transactional SQL updates/inserts, consistency checks, and explicit success/failure signaling for calling services.
public boolean updateStatus(UUID bookingId, String newStatus) {

    if (newStatus == null) return false;

    synchronizeBookingStates();
    newStatus = normalizeStatus(newStatus);
    if (STATUS_FINISHED.equals(newStatus)) {
        newStatus = STATUS_CHECKED_OUT;
    }

    if (!SUPPORTED_STATUSES.contains(newStatus)) {
        return false;
    }

    try (Connection conn = DBConnection.getConnection()) {
        conn.setAutoCommit(false);

        BookingSnapshot snapshot = getBookingSnapshot(conn, bookingId);
        if (snapshot == null) {
            conn.rollback();
            return false;
        }

        // Idempotent
        if (newStatus.equals(snapshot.status)) {
            conn.rollback();
            return true;
        }

        boolean success = false;

        switch (newStatus) {

            case STATUS_PAID:
                // pending -> paid (full online) OR deposited -> paid (cash settlement at venue)
                if (STATUS_PENDING.equals(snapshot.status) || STATUS_DEPOSITED.equals(snapshot.status)) {
                    success = updateBookingStatus(conn, bookingId, STATUS_PAID, STATUS_PENDING);
                    if (!success && STATUS_DEPOSITED.equals(snapshot.status)) {
                        success = updateBookingStatus(conn, bookingId, STATUS_PAID, STATUS_DEPOSITED);
                    }
                }
                break;

            case STATUS_DEPOSITED:
                // pending -> deposited
                if (STATUS_PENDING.equals(snapshot.status)) {
                    success = updateBookingStatus(conn, bookingId, STATUS_DEPOSITED, STATUS_PENDING);
                }
                break;

            case STATUS_CHECKED_IN:
                if (STATUS_PAID.equals(snapshot.status) || STATUS_DEPOSITED.equals(snapshot.status)) {
                    success = updateBookingStatus(conn, bookingId, STATUS_CHECKED_IN, STATUS_PAID);
                    if (!success && STATUS_DEPOSITED.equals(snapshot.status)) {
                        success = updateBookingStatus(conn, bookingId, STATUS_CHECKED_IN, STATUS_DEPOSITED);
                    }
                } else if (STATUS_PENDING_EXTRA.equals(snapshot.status)
                        && isPaymentSuccessful(conn, bookingId)) {
                    success = updateBookingStatus(conn, bookingId, STATUS_CHECKED_IN, STATUS_PENDING_EXTRA);
                }
                break;

            case STATUS_CHECKED_OUT:
                if (STATUS_CHECKED_IN.equals(snapshot.status)) {
                    success = updateBookingStatus(conn, bookingId, STATUS_CHECKED_OUT, STATUS_CHECKED_IN);
                }
                break;

            case STATUS_PENDING_EXTRA:
                // checked_in -> pending_extra
                if (STATUS_CHECKED_IN.equals(snapshot.status)) {
                    success = updateBookingStatus(conn, bookingId, STATUS_PENDING_EXTRA, STATUS_CHECKED_IN);
                }
                break;

            case STATUS_PENDING_REFUND:
                // paid/deposited -> pending_refund
                if (STATUS_PAID.equals(snapshot.status) || STATUS_DEPOSITED.equals(snapshot.status)) {
                    success = updateBookingStatus(conn, bookingId, STATUS_PENDING_REFUND, snapshot.status);
                    if (success) {
                        updatePaymentStatusByBooking(conn, bookingId, "REFUND_PENDING", false);
                    }
                }
                break;

            case STATUS_REFUNDED:
                // legacy flow: pending_refund -> refunded
                if (STATUS_PENDING_REFUND.equals(snapshot.status)
                        || STATUS_PENDING_REFUND_CONFIRM.equals(snapshot.status)) {
                    success = updateBookingStatus(conn, bookingId, STATUS_REFUNDED, snapshot.status);
                    if (success) {
                        updatePaymentStatusByBooking(conn, bookingId, "REFUNDED", true);
                        releaseBookingResources(conn, bookingId, snapshot.scheduleId);
                    }
                }
                break;

            case STATUS_COMPLETED:
                // checked_in OR pending_extra OR checked_out
                if ((STATUS_CHECKED_IN.equals(snapshot.status)
                    || STATUS_PENDING_EXTRA.equals(snapshot.status)
                    || STATUS_CHECKED_OUT.equals(snapshot.status)
                    || STATUS_FINISHED.equals(snapshot.status))
                        && !hasOutstandingRemainingAmount(conn, bookingId)
                        && ((STATUS_CHECKED_IN.equals(snapshot.status) || STATUS_PENDING_EXTRA.equals(snapshot.status))
                                ? (snapshot.scheduleEnd != null && !LocalDateTime.now().isBefore(snapshot.scheduleEnd))
                                : true)) {

                    // nếu pending_extra phải thanh toán xong
                    if (STATUS_PENDING_EXTRA.equals(snapshot.status)
                            && !isPaymentSuccessful(conn, bookingId)) {
                        break;
                    }

                    success = updateBookingStatus(conn, bookingId, STATUS_COMPLETED, snapshot.status);
                }
                break;

            case STATUS_CANCELLED:
                // pending / paid / deposited
                if (STATUS_PENDING.equals(snapshot.status)
                        || STATUS_PAID.equals(snapshot.status)
                    || STATUS_DEPOSITED.equals(snapshot.status)
                    || STATUS_CHECKED_OUT.equals(snapshot.status)
                    || STATUS_FINISHED.equals(snapshot.status)) {

                    success = updateBookingStatus(conn, bookingId, STATUS_CANCELLED, snapshot.status);

                    if (success) {
                        if (STATUS_PENDING.equals(snapshot.status)) {
                            updatePaymentStatusByBooking(conn, bookingId, "FAILED", true);
                        }
                        releaseBookingResources(conn, bookingId, snapshot.scheduleId);
                    }
                }
                break;

            default:
                success = false;
        }

        if (!success) {
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
     * Legacy direct supplementary insertion flow. Kept for compatibility;
     * current payment-first flow should use finalizeSupplementaryEquipment
     * after payment success.
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
     * Finalizes supplementary equipment only after payment success: reserves
     * stock, merges booking equipment, updates booking total, then resolves
     * status from pending extra to checked in/completed.
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

            // Add supplementary debt marker so staff/customer can settle later in remaining-payment flow.
            if (STATUS_CHECKED_IN.equals(snapshot.status)) {
                if (!updateBookingStatus(conn, bookingId, STATUS_PENDING_EXTRA, STATUS_CHECKED_IN)) {
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
     * Returns supplementary amount currently represented by booking total -
     * schedule field price.
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
        String sql = "SELECT b.booking_id, b.booker_id, b.field_id, b.schedule_id, b.play_status, b.payment_status, b.extra_payment_status, b.total_price, s.booking_date, s.start_time, s.end_time, f.field_name, f.location_id, u.full_name AS customer_name, COALESCE(b.phone_number, u.phone) AS customer_phone "
                + "FROM Booking b "
                + "LEFT JOIN Schedule s ON b.schedule_id = s.schedule_id "
                + "LEFT JOIN Field f ON b.field_id = f.field_id "
                + "LEFT JOIN Users u ON b.booker_id = u.user_id "
            + "WHERE f.location_id = ? ORDER BY b.booking_time DESC, s.booking_date DESC, s.start_time DESC";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, locationId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BookingViewModel vm = new BookingViewModel();
                vm.setBookingId(UUID.fromString(rs.getString("booking_id")));
                vm.setBookerId(UUID.fromString(rs.getString("booker_id")));
                vm.setFieldId(UUID.fromString(rs.getString("field_id")));
                String bookingLocationId = rs.getString("location_id");
                if (bookingLocationId != null) {
                    vm.setLocationId(UUID.fromString(bookingLocationId));
                }
                vm.setScheduleId(UUID.fromString(rs.getString("schedule_id")));
                Date bd = rs.getDate("booking_date");
                if (bd != null) {
                    vm.setBookingDate(bd.toLocalDate());
                }
                Time st = rs.getTime("start_time");
                if (st != null) {
                    vm.setStartTime(st.toLocalTime());
                }
                Time et = rs.getTime("end_time");
                if (et != null) {
                    vm.setEndTime(et.toLocalTime());
                }
                vm.setFieldName(rs.getString("field_name"));
                vm.setCustomerName(rs.getString("customer_name"));
                vm.setCustomerPhone(rs.getString("customer_phone"));
                applyStateToBookingViewModel(rs, vm);
                vm.setTotalPrice(rs.getBigDecimal("total_price"));
                list.add(vm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Gets location bookings with optional filters by date, status and customer
     * keyword.
     */
    //Description: Retrieves and prepares data for getByLocationFiltered by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    public List<BookingViewModel> getByLocationFiltered(UUID locationId, String bookingDateStr, String status, String customerKeyword) {
        // Internal Flow: query data source, map database rows to model objects, and return null/empty values safely on edge cases.
        synchronizeBookingStates();
        List<BookingViewModel> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT b.booking_id, b.booker_id, b.field_id, b.schedule_id, b.play_status, b.payment_status, b.extra_payment_status, b.total_price, s.booking_date, s.start_time, s.end_time, f.field_name, f.location_id, u.full_name AS customer_name, COALESCE(b.phone_number, u.phone) AS customer_phone ");
        sql.append("FROM Booking b ");
        sql.append("LEFT JOIN Schedule s ON b.schedule_id = s.schedule_id ");
        sql.append("LEFT JOIN Field f ON b.field_id = f.field_id ");
        sql.append("LEFT JOIN Users u ON b.booker_id = u.user_id ");
        sql.append("WHERE f.location_id = ? ");

        if (bookingDateStr != null && !bookingDateStr.isBlank()) {
            sql.append(" AND s.booking_date = ? ");
        }
        boolean filterCheckedOut = "checked out".equalsIgnoreCase(status == null ? "" : status.trim());
        if (status != null && !status.isBlank()) {
            if (filterCheckedOut) {
                sql.append(" AND LOWER(").append(legacyStatusExpression("b")).append(") = 'checked out' ");
            } else {
                sql.append(" AND LOWER(").append(legacyStatusExpression("b")).append(") = LOWER(?) ");
            }
        }
        if (customerKeyword != null && !customerKeyword.isBlank()) {
            sql.append(" AND (LOWER(u.full_name) LIKE LOWER(?) OR b.phone_number LIKE ?) ");
        }

        sql.append(" ORDER BY b.booking_time DESC, s.booking_date DESC, s.start_time DESC");

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setString(idx++, locationId.toString());
            if (bookingDateStr != null && !bookingDateStr.isBlank()) {
                ps.setDate(idx++, Date.valueOf(bookingDateStr));
            }
            if (status != null && !status.isBlank() && !filterCheckedOut) {
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
                if (bookingLocationId != null) {
                    vm.setLocationId(UUID.fromString(bookingLocationId));
                }
                vm.setScheduleId(UUID.fromString(rs.getString("schedule_id")));
                Date bd = rs.getDate("booking_date");
                if (bd != null) {
                    vm.setBookingDate(bd.toLocalDate());
                }
                Time st = rs.getTime("start_time");
                if (st != null) {
                    vm.setStartTime(st.toLocalTime());
                }
                Time et = rs.getTime("end_time");
                if (et != null) {
                    vm.setEndTime(et.toLocalTime());
                }
                vm.setFieldName(rs.getString("field_name"));
                vm.setCustomerName(rs.getString("customer_name"));
                vm.setCustomerPhone(rs.getString("customer_phone"));
                applyStateToBookingViewModel(rs, vm);
                vm.setTotalPrice(rs.getBigDecimal("total_price"));
                list.add(vm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<BookingViewModel> getByLocationFilteredByState(UUID locationId,
                                                                String bookingDateStr,
                                                                String playStatus,
                                                                String paymentStatus,
                                                                String extraPaymentStatus,
                                                                String customerKeyword) {
        synchronizeBookingStates();
        List<BookingViewModel> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT b.booking_id, b.booker_id, b.field_id, b.schedule_id, b.play_status, b.payment_status, b.extra_payment_status, b.total_price, s.booking_date, s.start_time, s.end_time, f.field_name, f.location_id, u.full_name AS customer_name, COALESCE(b.phone_number, u.phone) AS customer_phone ");
        sql.append("FROM Booking b ");
        sql.append("LEFT JOIN Schedule s ON b.schedule_id = s.schedule_id ");
        sql.append("LEFT JOIN Field f ON b.field_id = f.field_id ");
        sql.append("LEFT JOIN Users u ON b.booker_id = u.user_id ");
        sql.append("WHERE f.location_id = ? ");

        if (bookingDateStr != null && !bookingDateStr.isBlank()) {
            sql.append(" AND s.booking_date = ? ");
        }
        if (playStatus != null && !playStatus.isBlank()) {
            sql.append(" AND LOWER(ISNULL(b.play_status, '')) = LOWER(?) ");
        }
        if (paymentStatus != null && !paymentStatus.isBlank()) {
            sql.append(" AND LOWER(ISNULL(b.payment_status, '')) = LOWER(?) ");
        }
        if (extraPaymentStatus != null && !extraPaymentStatus.isBlank()) {
            sql.append(" AND LOWER(ISNULL(b.extra_payment_status, '')) = LOWER(?) ");
        }
        if (customerKeyword != null && !customerKeyword.isBlank()) {
            sql.append(" AND (LOWER(u.full_name) LIKE LOWER(?) OR b.phone_number LIKE ?) ");
        }

        sql.append(" ORDER BY b.booking_time DESC, s.booking_date DESC, s.start_time DESC");

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setString(idx++, locationId.toString());
            if (bookingDateStr != null && !bookingDateStr.isBlank()) {
                ps.setDate(idx++, Date.valueOf(bookingDateStr));
            }
            if (playStatus != null && !playStatus.isBlank()) {
                ps.setString(idx++, playStatus);
            }
            if (paymentStatus != null && !paymentStatus.isBlank()) {
                ps.setString(idx++, paymentStatus);
            }
            if (extraPaymentStatus != null && !extraPaymentStatus.isBlank()) {
                ps.setString(idx++, extraPaymentStatus);
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
                if (bookingLocationId != null) {
                    vm.setLocationId(UUID.fromString(bookingLocationId));
                }
                vm.setScheduleId(UUID.fromString(rs.getString("schedule_id")));
                Date bd = rs.getDate("booking_date");
                if (bd != null) {
                    vm.setBookingDate(bd.toLocalDate());
                }
                Time st = rs.getTime("start_time");
                if (st != null) {
                    vm.setStartTime(st.toLocalTime());
                }
                Time et = rs.getTime("end_time");
                if (et != null) {
                    vm.setEndTime(et.toLocalTime());
                }
                vm.setFieldName(rs.getString("field_name"));
                vm.setCustomerName(rs.getString("customer_name"));
                vm.setCustomerPhone(rs.getString("customer_phone"));
                applyStateToBookingViewModel(rs, vm);
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

                applyStateToBookingEntity(rs, booking);
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
     * Resets payment deadline for a booking (typically used by staff/admin
     * support flow).
     */
    //Description: Executes the resetPaymentDeadline write workflow, including input normalization, transactional SQL updates/inserts, consistency checks, and explicit success/failure signaling for calling services.
    public boolean resetPaymentDeadline(UUID bookingId, LocalDateTime newDeadline) {
        // Internal Flow: apply guard checks, execute core logic, and keep exception handling localized to DAO responsibilities.
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

    /**
     * Auto-synchronizes lifecycle states: - paid bookings past slot end =>
     * cancelled and resources released - checked-in bookings past slot end =>
     * completed pending extra is intentionally excluded to allow payment at any
     * time.
     */
    //Description: Implements the synchronizeBookingStates business routine with validation, database interaction, exception handling, and predictable outputs for upstream controllers/services.
    private void synchronizeBookingStates() {
        // Internal Flow: apply guard checks, execute core logic, and keep exception handling localized to DAO responsibilities.
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            // Cron-on-read: he thong tu dong chot cac booking da qua gio cho 2 trang thai paid/checked in.
                String sql = "SELECT b.booking_id, LOWER(" + legacyStatusExpression("b") + ") AS booking_status "
                    + "FROM Booking b "
                    + "INNER JOIN Schedule s ON s.schedule_id = b.schedule_id "
                    + "WHERE LOWER(" + legacyStatusExpression("b") + ") IN ('paid', 'deposited', 'checked in', 'checked out') "
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
                if (STATUS_PAID.equals(snapshot.status) || STATUS_DEPOSITED.equals(snapshot.status)) {
                    // paid/deposited ma qua gio nhung chua check-in -> coi nhu khong su dung, chuyen cancelled.
                    updateBookingStatus(conn, expiredBooking.bookingId, STATUS_CANCELLED, snapshot.status);
                    releaseBookingResources(conn, expiredBooking.bookingId, snapshot.scheduleId);
                } else if (STATUS_CHECKED_IN.equals(snapshot.status)) {
                    // checked-in qua gio: doi sang checked out. Thanh toan hoan tat se dua den completed.
                    updateBookingStatus(conn, expiredBooking.bookingId, STATUS_CHECKED_OUT, STATUS_CHECKED_IN);
                } else if (STATUS_CHECKED_OUT.equals(snapshot.status) || STATUS_FINISHED.equals(snapshot.status)) {
                    // Tuong thich du lieu cu finished va cho phep tu dong complete khi da het cong no.
                    if (STATUS_FINISHED.equals(snapshot.status)) {
                        updateBookingStatus(conn, expiredBooking.bookingId, STATUS_CHECKED_OUT, STATUS_FINISHED);
                    }
                    if (!hasOutstandingRemainingAmount(conn, expiredBooking.bookingId)) {
                        boolean updated = updateBookingStatus(conn, expiredBooking.bookingId, STATUS_COMPLETED, STATUS_CHECKED_OUT);
                        if (!updated) {
                            updateBookingStatus(conn, expiredBooking.bookingId, STATUS_COMPLETED, STATUS_FINISHED);
                        }
                    }
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
        // Snapshot la du lieu toi thieu de quyet dinh transition: khong load du model de giam chi phi.
        String sql = "SELECT b.schedule_id, b.field_id, b.play_status, b.payment_status, b.extra_payment_status, "
            + "s.booking_date, s.start_time, s.end_time "
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
                snapshot.playStatus = normalizeStatus(rs.getString("play_status"));
                snapshot.paymentStatus = normalizeStatus(rs.getString("payment_status"));
                snapshot.extraPaymentStatus = normalizeStatus(rs.getString("extra_payment_status"));
                snapshot.status = resolveLegacyStatus(snapshot.playStatus, snapshot.paymentStatus, snapshot.extraPaymentStatus);

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
     * Checks whether current time lies inside booking schedule window [start,
     * end).
     */
    //Description: Evaluates business conditions in isBookingActive and returns a deterministic boolean result used to protect state transitions and payment/booking integrity rules.
    private boolean isBookingActive(BookingSnapshot snapshot, LocalDateTime now) {
        // Internal Flow: compute condition from normalized state and return a strict boolean outcome.
        if (snapshot == null || snapshot.scheduleStart == null || snapshot.scheduleEnd == null) {
            return false;
        }
        // [start, end): cho phep dung san tu luc bat dau den truoc luc ket thuc.
        return !now.isBefore(snapshot.scheduleStart) && now.isBefore(snapshot.scheduleEnd);
    }

    /**
     * Resolves status after supplementary payment based on schedule end time.
     */
    //Description: Retrieves and prepares data for resolvePostSupplementaryStatus by applying guard checks, querying mapped tables, transforming result sets into domain objects, and returning a safe fallback when no record is found.
    private String resolvePostSupplementaryStatus(BookingSnapshot snapshot, LocalDateTime now) {
        // Internal Flow: apply guard checks, execute core logic, and keep exception handling localized to DAO responsibilities.
        // Neu khach thanh toan bo sung khi tran da ket thuc -> complete luon.
        if (snapshot != null && snapshot.scheduleEnd != null && !now.isBefore(snapshot.scheduleEnd)) {
            return STATUS_COMPLETED;
        }
        // Nguoc lai tra ve checked in de tiep tuc tran dang dien ra.
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
                // Ho tro du lieu lich su co the ghi 'paid' thay vi 'success'.
                return "success".equals(paymentStatus) || "paid".equals(paymentStatus);
            }
        }
    }

    private boolean hasOutstandingRemainingAmount(Connection conn, UUID bookingId) throws SQLException {
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

                // Business-first settlement rules from split states.
                if (STATUS_PAID.equals(bookingPaymentStatus)
                        && (EXTRA_PAYMENT_STATUS_NONE.equals(bookingExtraStatus)
                        || EXTRA_PAYMENT_STATUS_PAID.equals(bookingExtraStatus))) {
                    return false;
                }

                if (STATUS_PAID.equals(bookingPaymentStatus)
                        && STATUS_PENDING_EXTRA.equals(bookingExtraStatus)) {
                    return true;
                }

                if (STATUS_DEPOSITED.equals(bookingPaymentStatus)) {
                    return true;
                }

                if (STATUS_PENDING_REFUND.equals(bookingPaymentStatus)
                    || STATUS_PENDING_REFUND_CONFIRM.equals(bookingPaymentStatus)
                        || STATUS_REFUNDED.equals(bookingPaymentStatus)
                        || PAYMENT_STATUS_FAILED.equals(bookingPaymentStatus)) {
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

    public BigDecimal getOutstandingAmount(UUID bookingId) {
        if (bookingId == null) {
            return BigDecimal.ZERO;
        }

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

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return BigDecimal.ZERO;
                }

                BigDecimal totalPrice = rs.getBigDecimal("total_price");
                BigDecimal paidAmount = rs.getBigDecimal("paid_amount");
                String paymentMethod = normalizeStatus(rs.getString("payment_method"));
                String paymentStatus = normalizeStatus(rs.getString("payment_status"));
                String bookingPaymentStatus = normalizeStatus(rs.getString("booking_payment_status"));
                String bookingExtraStatus = normalizeStatus(rs.getString("booking_extra_status"));

                if (totalPrice == null || totalPrice.compareTo(BigDecimal.ZERO) <= 0) {
                    return BigDecimal.ZERO;
                }
                if (paidAmount == null) {
                    paidAmount = BigDecimal.ZERO;
                }

                // Business-first settlement rules from split states.
                if ("paid".equals(bookingPaymentStatus)
                        && ("none".equals(bookingExtraStatus) || "paid extra".equals(bookingExtraStatus))) {
                    return BigDecimal.ZERO;
                }

                if ("pending refund".equals(bookingPaymentStatus)
                    || "pending refund confirm".equals(bookingPaymentStatus)
                        || "refunded".equals(bookingPaymentStatus)
                        || "failed".equals(bookingPaymentStatus)) {
                    return BigDecimal.ZERO;
                }

                if ("paid".equals(bookingPaymentStatus) && "pending extra".equals(bookingExtraStatus)) {
                    BigDecimal remaining = totalPrice.subtract(paidAmount);
                    return remaining.max(BigDecimal.ZERO);
                }

                if ("deposited".equals(bookingPaymentStatus)) {
                    BigDecimal remaining = totalPrice.subtract(paidAmount);
                    return remaining.max(BigDecimal.ZERO);
                }

                if (paymentMethod.contains("|remaining")) {
                    if ("success".equals(paymentStatus) || "paid".equals(paymentStatus)) {
                        return BigDecimal.ZERO;
                    }
                    return paidAmount.max(BigDecimal.ZERO);
                }

                if ("success".equals(paymentStatus) || "paid".equals(paymentStatus)) {
                    BigDecimal remaining = totalPrice.subtract(paidAmount);
                    return remaining.max(BigDecimal.ZERO);
                }

                return totalPrice.max(BigDecimal.ZERO);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

//=============================================================================================STATUS CONTROL===============================================================================================//
    /**
     * Updates booking status only when current status matches expectedStatus.
     */
    //Description: Executes the updateBookingStatus write workflow, including input normalization, transactional SQL updates/inserts, consistency checks, and explicit success/failure signaling for calling services.
    private boolean updateBookingStatus(Connection conn, UUID bookingId, String newStatus, String expectedStatus) throws SQLException {
        String normalizedExpectedStatus = normalizeStatus(expectedStatus);
        BookingSnapshot current = getBookingSnapshot(conn, bookingId);
        if (current == null || !normalizedExpectedStatus.equals(normalizeStatus(current.status))) {
            return false;
        }

        BookingSplitState nextState = resolveNextSplitState(current, normalizeStatus(newStatus));
        String sql = "UPDATE Booking "
                + "SET play_status = ?, payment_status = ?, extra_payment_status = ? "
                + "WHERE booking_id = ? "
                + "AND LOWER(ISNULL(play_status, '')) = ? "
                + "AND LOWER(ISNULL(payment_status, '')) = ? "
                + "AND LOWER(ISNULL(extra_payment_status, '')) = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nextState.playStatus);
            ps.setString(2, nextState.paymentStatus);
            ps.setString(3, nextState.extraPaymentStatus);
            ps.setString(4, bookingId.toString());
            ps.setString(5, current.playStatus);
            ps.setString(6, current.paymentStatus);
            ps.setString(7, current.extraPaymentStatus);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Updates payment status by booking ID and optionally touches payment_time.
     */
    //=============================================================================================PAYMENT STATUS CONTROLE===============================================================================================//
    //Description: Executes the updatePaymentStatusByBooking write workflow, including input normalization, transactional SQL updates/inserts, consistency checks, and explicit success/failure signaling for calling services.
    private void updatePaymentStatusByBooking(Connection conn, UUID bookingId, String paymentStatus, boolean touchPaymentTime) throws SQLException {
        // Internal Flow: validate inputs, run transactional SQL mutations, and propagate a clear commit/rollback result.
        // touchPaymentTime=true khi can danh dau moc thay doi payment (failed/refunded...).
        String sql = touchPaymentTime
                ? "UPDATE Payment SET payment_status = ?, payment_time = SYSDATETIME() WHERE booking_id = ?"
                : "UPDATE Payment SET payment_status = ? WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paymentStatus);
            ps.setString(2, bookingId.toString());
            ps.executeUpdate();
        }
    }

//=============================================================================================GIVE BACK EQUIPMENTS===============================================================================================//
    /**
     * Releases schedule slot and returns all booked equipment quantity to
     * location stock.
     */
    //Description: Implements the releaseBookingResources business routine with validation, database interaction, exception handling, and predictable outputs for upstream controllers/services.
    private void releaseBookingResources(Connection conn, UUID bookingId, UUID scheduleId) throws SQLException {
        // Internal Flow: apply guard checks, execute core logic, and keep exception handling localized to DAO responsibilities.
        if (scheduleId != null) {
            // Tra slot lich ve available de cho phep dat lai.
            try (PreparedStatement ps = conn.prepareStatement("UPDATE Schedule SET status = 'available' WHERE schedule_id = ?")) {
                ps.setString(1, scheduleId.toString());
                ps.executeUpdate();
            }
        }

        // Tra lai dung cu da gan voi booking vao ton kho cua location tuong ung.
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

    public boolean updateSplitStates(UUID bookingId, String playStatus, String paymentStatus, String extraPaymentStatus) {
        String normalizedPlayStatus = normalizeStatus(playStatus);
        String normalizedPaymentStatus = normalizeStatus(paymentStatus);
        String normalizedExtraPaymentStatus = normalizeStatus(extraPaymentStatus);

        if (!SUPPORTED_PLAY_STATUSES.contains(normalizedPlayStatus)
                || !SUPPORTED_PAYMENT_STATUSES.contains(normalizedPaymentStatus)
                || !SUPPORTED_EXTRA_PAYMENT_STATUSES.contains(normalizedExtraPaymentStatus)) {
            return false;
        }

        String sql = "UPDATE Booking "
                + "SET play_status = ?, payment_status = ?, extra_payment_status = ? "
                + "WHERE booking_id = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizedPlayStatus);
            ps.setString(2, normalizedPaymentStatus);
            ps.setString(3, normalizedExtraPaymentStatus);
            ps.setString(4, bookingId.toString());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isBookedByStaff(UUID bookingId) {
        if (bookingId == null) {
            return false;
        }

        String sql = "SELECT LOWER(ISNULL(r.role_name, '')) AS booker_role_name "
                + "FROM Booking b "
                + "LEFT JOIN Users u ON u.user_id = b.booker_id "
                + "LEFT JOIN Role r ON r.role_id = u.role_id "
                + "WHERE b.booking_id = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                return "staff".equals(normalizeStatus(rs.getString("booker_role_name")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String legacyStatusExpression(String alias) {
        String p = alias + ".payment_status";
        String pl = alias + ".play_status";
        String ex = alias + ".extra_payment_status";
        return "CASE "
                + "WHEN LOWER(ISNULL(" + p + ", '')) = 'pending refund' THEN 'pending refund' "
            + "WHEN LOWER(ISNULL(" + p + ", '')) = 'pending refund confirm' THEN 'pending refund' "
                + "WHEN LOWER(ISNULL(" + p + ", '')) = 'refunded' THEN 'refunded' "
                + "WHEN LOWER(ISNULL(" + p + ", '')) = 'failed' OR LOWER(ISNULL(" + pl + ", '')) = 'cancelled' THEN 'cancelled' "
                + "WHEN LOWER(ISNULL(" + pl + ", '')) = 'completed' THEN 'completed' "
                + "WHEN LOWER(ISNULL(" + pl + ", '')) = 'checked out' THEN 'checked out' "
                + "WHEN LOWER(ISNULL(" + pl + ", '')) = 'checked in' AND LOWER(ISNULL(" + ex + ", 'none')) = 'pending extra' THEN 'pending extra' "
                + "WHEN LOWER(ISNULL(" + pl + ", '')) = 'checked in' THEN 'checked in' "
                + "WHEN LOWER(ISNULL(" + p + ", '')) = 'deposited' THEN 'deposited' "
                + "WHEN LOWER(ISNULL(" + p + ", '')) = 'paid' THEN 'paid' "
                + "ELSE 'pending' END";
    }

    private BookingSplitState resolveNextSplitState(BookingSnapshot current, String newStatus) {
        String currentPlay = normalizeStatus(current.playStatus);
        String currentPayment = normalizeStatus(current.paymentStatus);
        String currentExtra = normalizeStatus(current.extraPaymentStatus);

        if (currentPlay.isEmpty()) {
            currentPlay = PLAY_STATUS_BOOKED;
        }
        if (currentPayment.isEmpty()) {
            currentPayment = STATUS_PENDING;
        }
        if (currentExtra.isEmpty()) {
            currentExtra = EXTRA_PAYMENT_STATUS_NONE;
        }

        switch (newStatus) {
            case STATUS_PENDING:
                return new BookingSplitState(PLAY_STATUS_BOOKED, STATUS_PENDING, EXTRA_PAYMENT_STATUS_NONE);
            case STATUS_DEPOSITED:
                return new BookingSplitState(PLAY_STATUS_BOOKED, STATUS_DEPOSITED, currentExtra.isEmpty() ? EXTRA_PAYMENT_STATUS_NONE : currentExtra);
            case STATUS_PAID:
                return new BookingSplitState(currentPlay.isEmpty() ? PLAY_STATUS_BOOKED : currentPlay, STATUS_PAID, currentExtra.isEmpty() ? EXTRA_PAYMENT_STATUS_NONE : currentExtra);
            case STATUS_CHECKED_IN:
                return new BookingSplitState(
                        STATUS_CHECKED_IN,
                        currentPayment.isEmpty() ? STATUS_PAID : currentPayment,
                        STATUS_PENDING_EXTRA.equals(currentExtra) ? EXTRA_PAYMENT_STATUS_PAID : (currentExtra.isEmpty() ? EXTRA_PAYMENT_STATUS_NONE : currentExtra)
                );
            case STATUS_PENDING_EXTRA:
                return new BookingSplitState(
                        STATUS_CHECKED_IN,
                        currentPayment.isEmpty() ? STATUS_PAID : currentPayment,
                        STATUS_PENDING_EXTRA
                );
            case STATUS_CHECKED_OUT:
                return new BookingSplitState(
                        STATUS_CHECKED_OUT,
                        currentPayment.isEmpty() ? STATUS_PAID : currentPayment,
                        currentExtra.isEmpty() ? EXTRA_PAYMENT_STATUS_NONE : currentExtra
                );
            case STATUS_COMPLETED:
                return new BookingSplitState(
                        STATUS_COMPLETED,
                        STATUS_PAID,
                        STATUS_PENDING_EXTRA.equals(currentExtra) ? EXTRA_PAYMENT_STATUS_PAID : (currentExtra.isEmpty() ? EXTRA_PAYMENT_STATUS_NONE : currentExtra)
                );
            case STATUS_PENDING_REFUND:
                return new BookingSplitState(
                        PLAY_STATUS_CANCELLED,
                        STATUS_PENDING_REFUND,
                        currentExtra.isEmpty() ? EXTRA_PAYMENT_STATUS_NONE : currentExtra
                );
            case STATUS_REFUNDED:
                return new BookingSplitState(
                        PLAY_STATUS_CANCELLED,
                        STATUS_REFUNDED,
                        currentExtra.isEmpty() ? EXTRA_PAYMENT_STATUS_NONE : currentExtra
                );
            case STATUS_CANCELLED:
                return new BookingSplitState(
                        PLAY_STATUS_CANCELLED,
                        PAYMENT_STATUS_FAILED,
                        currentExtra.isEmpty() ? EXTRA_PAYMENT_STATUS_NONE : currentExtra
                );
            default:
                return new BookingSplitState(
                        resolvePlayStatus(newStatus),
                        resolvePaymentStatus(newStatus),
                        resolveExtraPaymentStatus(newStatus)
                );
        }
    }

    private String resolvePlayStatus(String legacyStatus) {
        String normalized = normalizeStatus(legacyStatus);
        if (STATUS_CHECKED_IN.equals(normalized) || STATUS_PENDING_EXTRA.equals(normalized)) {
            return STATUS_CHECKED_IN;
        }
        if (STATUS_FINISHED.equals(normalized) || STATUS_CHECKED_OUT.equals(normalized)) {
            return STATUS_CHECKED_OUT;
        }
        if (STATUS_COMPLETED.equals(normalized)) {
            return STATUS_COMPLETED;
        }
        if (STATUS_CANCELLED.equals(normalized)
                || STATUS_REFUNDED.equals(normalized)
                || STATUS_PENDING_REFUND.equals(normalized)) {
            return PLAY_STATUS_CANCELLED;
        }
        return PLAY_STATUS_BOOKED;
    }

    private String resolvePaymentStatus(String legacyStatus) {
        String normalized = normalizeStatus(legacyStatus);
        if (STATUS_PENDING.equals(normalized)) {
            return STATUS_PENDING;
        }
        if (STATUS_DEPOSITED.equals(normalized)) {
            return STATUS_DEPOSITED;
        }
        if (STATUS_PENDING_REFUND.equals(normalized)) {
            return STATUS_PENDING_REFUND;
        }
        if (STATUS_PENDING_REFUND_CONFIRM.equals(normalized)) {
            return STATUS_PENDING_REFUND;
        }
        if (STATUS_REFUNDED.equals(normalized)) {
            return STATUS_REFUNDED;
        }
        if (STATUS_CANCELLED.equals(normalized)) {
            return PAYMENT_STATUS_FAILED;
        }
        return STATUS_PAID;
    }

    private String resolveExtraPaymentStatus(String legacyStatus) {
        return STATUS_PENDING_EXTRA.equals(normalizeStatus(legacyStatus)) ? STATUS_PENDING_EXTRA : EXTRA_PAYMENT_STATUS_NONE;
    }

    private String resolveLegacyStatus(String playStatus, String paymentStatus, String extraPaymentStatus) {
        String normalizedPlay = normalizeStatus(playStatus);
        String normalizedPayment = normalizeStatus(paymentStatus);
        String normalizedExtra = normalizeStatus(extraPaymentStatus);

        if (STATUS_PENDING_REFUND.equals(normalizedPayment)) {
            return STATUS_PENDING_REFUND;
        }
        if (STATUS_PENDING_REFUND_CONFIRM.equals(normalizedPayment)) {
            return STATUS_PENDING_REFUND;
        }
        if (STATUS_REFUNDED.equals(normalizedPayment)) {
            return STATUS_REFUNDED;
        }
        if (PAYMENT_STATUS_FAILED.equals(normalizedPayment) || STATUS_CANCELLED.equals(normalizedPlay)) {
            return STATUS_CANCELLED;
        }
        if (STATUS_COMPLETED.equals(normalizedPlay)) {
            return STATUS_COMPLETED;
        }
        if (STATUS_CHECKED_OUT.equals(normalizedPlay)) {
            return STATUS_CHECKED_OUT;
        }
        if (STATUS_CHECKED_IN.equals(normalizedPlay)) {
            return STATUS_PENDING_EXTRA.equals(normalizedExtra) ? STATUS_PENDING_EXTRA : STATUS_CHECKED_IN;
        }
        if (STATUS_DEPOSITED.equals(normalizedPayment)) {
            return STATUS_DEPOSITED;
        }
        if (STATUS_PAID.equals(normalizedPayment)) {
            return STATUS_PAID;
        }
        return STATUS_PENDING;
    }

    private void applyStateToBookingViewModel(ResultSet rs, BookingViewModel vm) throws SQLException {
        String playStatus = normalizeStatus(rs.getString("play_status"));
        String paymentStatus = normalizeStatus(rs.getString("payment_status"));
        String extraPaymentStatus = normalizeStatus(rs.getString("extra_payment_status"));
        String legacyStatus = resolveLegacyStatus(playStatus, paymentStatus, extraPaymentStatus);

        vm.setStatus(legacyStatus);
        vm.setPlayStatus(playStatus);
        vm.setPaymentStatus(paymentStatus);
        vm.setExtraPaymentStatus(extraPaymentStatus);
    }

    private void applyStateToBookingEntity(ResultSet rs, Booking booking) throws SQLException {
        String playStatus = normalizeStatus(rs.getString("play_status"));
        String paymentStatus = normalizeStatus(rs.getString("payment_status"));
        String extraPaymentStatus = normalizeStatus(rs.getString("extra_payment_status"));
        String legacyStatus = resolveLegacyStatus(playStatus, paymentStatus, extraPaymentStatus);

        booking.setStatus(legacyStatus);
        booking.setPlayStatus(playStatus);
        booking.setPaymentStatus(paymentStatus);
        booking.setExtraPaymentStatus(extraPaymentStatus);
    }
//=============================================================================================QUICK MAPPING===============================================================================================//

    private static class BookingSnapshot {

        private UUID bookingId;
        private UUID scheduleId;
        private String status;
        private String playStatus;
        private String paymentStatus;
        private String extraPaymentStatus;
        private LocalDateTime scheduleStart;
        private LocalDateTime scheduleEnd;
    }

    private static class BookingSplitState {

        private final String playStatus;
        private final String paymentStatus;
        private final String extraPaymentStatus;

        private BookingSplitState(String playStatus, String paymentStatus, String extraPaymentStatus) {
            this.playStatus = playStatus;
            this.paymentStatus = paymentStatus;
            this.extraPaymentStatus = extraPaymentStatus;
        }
    }

    /**
     * Weekly booking: lock multiple schedule slots and create one Booking per
     * slot in a single atomic transaction. If ANY slot is already 'unavailable'
     * the whole transaction rolls back. Equipment (same list) is attached to
     * every booking and stock is decremented per session.
     *
     * @param equipmentList equipment to attach to each session (may be
     * null/empty)
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
        // sessionCount = so buoi dat trong tuan. Dung de tinh tong ton dung cu can tru.
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
                            // needed = so luong moi session * tong so session.
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
                                // Tong gia dung cu cho 1 session de cong vao gia san tung booking.
                                equipmentTotalPerSession = equipmentTotalPerSession.add(
                                        rentalPrice.multiply(BigDecimal.valueOf(be.getQuantity()))
                                );
                            }
                        }
                    }
                }

                String lockSql = "UPDATE [Schedule] SET status = 'unavailable' "
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
                            if (p != null) {
                                rawPrice = p;
                            }
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
                    // totalPrice sau voucher = (gia san + gia dung cu/session) * (1 - discount%).
                    java.math.BigDecimal totalPrice = subtotal.multiply(factor);

                    Booking b = new Booking();
                    b.setBookingId(UUID.randomUUID());
                    b.setBookerId(bookerId);
                    b.setPhoneNumber(bookingPhone);
                    b.setFieldId(fieldId);
                    b.setScheduleId(sid);
                    b.setVoucherId(voucherId);
                    b.setBookingTime(now);
                    b.setPlayStatus("booked");
                    b.setPaymentStatus("pending");
                    b.setExtraPaymentStatus("none");
                    // pending: cho khach thanh toan trong payment_deadline.
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
                            // totalQty la tong dung cu can tru cho tat ca session trong tuan.
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
                    + " play_status, payment_status, extra_payment_status, total_price, payment_deadline) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, 'booked', 'pending', 'none', ?, ?)";
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
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
                throw e;
            }
        }
    }

}
