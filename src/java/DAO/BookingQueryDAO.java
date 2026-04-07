package DAO;

import Models.BookingEquipmentViewModel;
import Models.BookingViewModel;
import Models.Field;
import Models.Location;
import Utils.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BookingQueryDAO {

    private final BookingStateDAO bookingStateDAO;

    public BookingQueryDAO() {
        this(new BookingStateDAO());
    }

    public BookingQueryDAO(BookingStateDAO bookingStateDAO) {
        this.bookingStateDAO = bookingStateDAO;
    }

    public List<BookingViewModel> getByBooker(UUID bookerId) {
        bookingStateDAO.synchronizeBookingStates();
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
                bookingStateDAO.applyStateToBookingViewModel(rs, vm);
                vm.setTotalPrice(rs.getBigDecimal("total_price"));
                list.add(vm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

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
        bookingStateDAO.synchronizeBookingStates();
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
        String lifecycleStatusExpr = bookingStateDAO.lifecycleStatusExpression("b");
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
                sql.append(" AND LOWER(").append(lifecycleStatusExpr).append(") = 'checked out' ");
            } else {
                sql.append(" AND LOWER(").append(lifecycleStatusExpr).append(") = LOWER(?) ");
            }
        }

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
                bookingStateDAO.applyStateToBookingViewModel(rs, vm);
                vm.setTotalPrice(rs.getBigDecimal("total_price"));
                list.add(vm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<BookingViewModel> getByIdWrapper(UUID bookingId) {
        BookingViewModel vm = getById(bookingId);
        List<BookingViewModel> list = new ArrayList<>();
        if (vm != null) {
            list.add(vm);
        }
        return list;
    }

    public BookingViewModel getById(UUID bookingId) {
        bookingStateDAO.synchronizeBookingStates();
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
                bookingStateDAO.applyStateToBookingViewModel(rs, vm);
                vm.setTotalPrice(rs.getBigDecimal("total_price"));
                return vm;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public BookingViewModel getByScheduleId(UUID scheduleId) {
        bookingStateDAO.synchronizeBookingStates();
        String sql = "SELECT TOP 1 b.booking_id, b.booker_id, b.field_id, b.schedule_id, b.play_status, b.payment_status, b.extra_payment_status, b.total_price, s.booking_date, s.start_time, s.end_time, f.field_name, u.full_name AS customer_name, COALESCE(b.phone_number, u.phone) AS customer_phone "
                + "FROM Booking b "
                + "LEFT JOIN Schedule s ON b.schedule_id = s.schedule_id "
                + "LEFT JOIN Field f ON b.field_id = f.field_id "
                + "LEFT JOIN Users u ON b.booker_id = u.user_id "
                + "WHERE b.schedule_id = ? AND LOWER(" + bookingStateDAO.lifecycleStatusExpression("b") + ") NOT IN ('cancelled', 'refunded') "
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
                bookingStateDAO.applyStateToBookingViewModel(rs, vm);
                vm.setTotalPrice(rs.getBigDecimal("total_price"));
                return vm;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public BookingViewModel getByScheduleIdForCalendar(UUID scheduleId) {
        bookingStateDAO.synchronizeBookingStates();
        String sql = "SELECT TOP 1 b.booking_id, b.booker_id, b.field_id, b.schedule_id, b.play_status, b.payment_status, b.extra_payment_status, b.total_price, s.booking_date, s.start_time, s.end_time, f.field_name, u.full_name AS customer_name, COALESCE(b.phone_number, u.phone) AS customer_phone "
                + "FROM Booking b "
                + "LEFT JOIN Schedule s ON b.schedule_id = s.schedule_id "
                + "LEFT JOIN Field f ON b.field_id = f.field_id "
                + "LEFT JOIN Users u ON b.booker_id = u.user_id "
                + "WHERE b.schedule_id = ? "
                + "AND LOWER(" + bookingStateDAO.lifecycleStatusExpression("b") + ") IN ('pending', 'deposited', 'paid', 'checked in', 'checked out', 'completed') "
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
                bookingStateDAO.applyStateToBookingViewModel(rs, vm);
                vm.setTotalPrice(rs.getBigDecimal("total_price"));
                return vm;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<BookingViewModel> getCustomerCalendarBookings(UUID bookerId,
                                                              LocalDate fromDate,
                                                              LocalDate toDate,
                                                              LocalDate selectedDate,
                                                              UUID locationId,
                                                              UUID fieldId) {
        bookingStateDAO.synchronizeBookingStates();
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
                bookingStateDAO.applyStateToBookingViewModel(rs, vm);
                vm.setTotalPrice(rs.getBigDecimal("total_price"));
                list.add(vm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Field> getCustomerCalendarFields(UUID bookerId) {
        return getCustomerCalendarFields(bookerId, null);
    }

    public List<Field> getCustomerCalendarFields(UUID bookerId, UUID locationId) {
        bookingStateDAO.synchronizeBookingStates();
        List<Field> fields = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT f.field_id, f.field_name, f.field_type, f.image_url, f.status, f.[condition], f.location_id ");
        sql.append("FROM Booking b ");
        sql.append("JOIN Field f ON b.field_id = f.field_id ");
        sql.append("WHERE b.booker_id = ? ");
        sql.append("AND LOWER(").append(bookingStateDAO.lifecycleStatusExpression("b")).append(") NOT IN ('cancelled', 'refunded') ");
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

    public List<Location> getCustomerCalendarLocations(UUID bookerId) {
        bookingStateDAO.synchronizeBookingStates();
        List<Location> locations = new ArrayList<>();
        String sql = "SELECT DISTINCT l.location_id, l.location_name "
                + "FROM Booking b "
                + "JOIN Field f ON b.field_id = f.field_id "
                + "JOIN Location l ON f.location_id = l.location_id "
                + "WHERE b.booker_id = ? "
                + "AND LOWER(" + bookingStateDAO.lifecycleStatusExpression("b") + ") NOT IN ('cancelled', 'refunded') "
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

    public List<BookingViewModel> getByLocation(UUID locationId) {
        bookingStateDAO.synchronizeBookingStates();
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
                bookingStateDAO.applyStateToBookingViewModel(rs, vm);
                vm.setTotalPrice(rs.getBigDecimal("total_price"));
                list.add(vm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<BookingViewModel> getByLocationFiltered(UUID locationId, String bookingDateStr, String status, String customerKeyword) {
        bookingStateDAO.synchronizeBookingStates();
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
                sql.append(" AND LOWER(").append(bookingStateDAO.lifecycleStatusExpression("b")).append(") = 'checked out' ");
            } else {
                sql.append(" AND LOWER(").append(bookingStateDAO.lifecycleStatusExpression("b")).append(") = LOWER(?) ");
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
                bookingStateDAO.applyStateToBookingViewModel(rs, vm);
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
        bookingStateDAO.synchronizeBookingStates();
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
                bookingStateDAO.applyStateToBookingViewModel(rs, vm);
                vm.setTotalPrice(rs.getBigDecimal("total_price"));
                list.add(vm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
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
                String paymentMethod = bookingStateDAO.normalizeStatus(rs.getString("payment_method"));
                String paymentStatus = bookingStateDAO.normalizeStatus(rs.getString("payment_status"));
                String bookingPaymentStatus = bookingStateDAO.normalizeStatus(rs.getString("booking_payment_status"));
                String bookingExtraStatus = bookingStateDAO.normalizeStatus(rs.getString("booking_extra_status"));

                if (totalPrice == null || totalPrice.compareTo(BigDecimal.ZERO) <= 0) {
                    return BigDecimal.ZERO;
                }
                if (paidAmount == null) {
                    paidAmount = BigDecimal.ZERO;
                }

                if ("paid".equals(bookingPaymentStatus)
                        && ("none".equals(bookingExtraStatus) || "paid extra".equals(bookingExtraStatus))) {
                    return BigDecimal.ZERO;
                }

                if ("pending refund".equals(bookingPaymentStatus)
                        || "refunded".equals(bookingPaymentStatus)
                        || "failed".equals(bookingPaymentStatus)) {
                    return BigDecimal.ZERO;
                }

                if ("paid".equals(bookingPaymentStatus) && "pending extra".equals(bookingExtraStatus)) {
                    // Pending extra debt is amount-based: total minus already settled payment amount.
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
}
