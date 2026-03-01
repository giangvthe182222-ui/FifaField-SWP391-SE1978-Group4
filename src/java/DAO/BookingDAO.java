package DAO;

import Models.Booking;
import Models.BookingEquipment;
import Utils.DBConnection;

import java.sql.*;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import Models.BookingViewModel;
import Models.BookingEquipmentViewModel;

public class BookingDAO {

    public boolean insert(Booking booking, List<BookingEquipment> equipmentList) {

    try (Connection conn = DBConnection.getConnection()) {

        conn.setAutoCommit(false);

        // 1️⃣ Lock schedule
        String updateSchedule = "UPDATE Schedule SET status = 'unavailable' WHERE schedule_id = ? AND status = 'available'";

        try (PreparedStatement ps = conn.prepareStatement(updateSchedule)) {
            ps.setString(1, booking.getScheduleId().toString());
            int affected = ps.executeUpdate();

            if (affected == 0) {
                conn.rollback();
                return false; // already booked
            }
        }

        // 2️⃣ Check & subtract equipment
        if (equipmentList != null && !equipmentList.isEmpty()) {

            String updateEquip = "UPDATE Location_Equipment SET quantity = quantity - ? WHERE equipment_id = ? AND quantity >= ?";

            try (PreparedStatement ps = conn.prepareStatement(updateEquip)) {

                for (BookingEquipment be : equipmentList) {

                    ps.setInt(1, be.getQuantity());
                    ps.setString(2, be.getEquipmentId().toString());
                    ps.setInt(3, be.getQuantity());

                    int affected = ps.executeUpdate();

                    if (affected == 0) {
                        conn.rollback();
                        return false; // not enough stock
                    }
                }
            }
        }

        // 3️⃣ Insert booking
        String insertBooking = "INSERT INTO Booking (booking_id, booker_id, field_id, schedule_id, voucher_id, status, total_price) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(insertBooking)) {

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

            ps.executeUpdate();
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
        return false;
    }
}

    public List<BookingViewModel> getByBooker(UUID bookerId) {
        List<BookingViewModel> list = new ArrayList<>();
        String sql = "SELECT b.booking_id, b.booker_id, b.field_id, b.schedule_id, b.status, b.total_price, s.booking_date, s.start_time, s.end_time, f.field_name, u.full_name AS customer_name " +
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

            // get schedule info
            String q = "SELECT b.schedule_id, s.booking_date, s.start_time FROM Booking b LEFT JOIN Schedule s ON b.schedule_id = s.schedule_id WHERE b.booking_id = ?";
            UUID scheduleId = null;
            java.time.LocalDate bookingDate = null;
            java.time.LocalTime startTime = null;

            try (PreparedStatement ps = conn.prepareStatement(q)) {
                ps.setString(1, bookingId.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String sid = rs.getString("schedule_id");
                    if (sid != null) scheduleId = UUID.fromString(sid);
                    Date bd = rs.getDate("booking_date");
                    if (bd != null) bookingDate = bd.toLocalDate();
                    Time st = rs.getTime("start_time");
                    if (st != null) startTime = st.toLocalTime();
                } else {
                    conn.rollback();
                    return false;
                }
            }

            if (bookingDate == null || startTime == null) {
                conn.rollback();
                return false;
            }

            java.time.LocalDateTime scheduleDateTime = java.time.LocalDateTime.of(bookingDate, startTime);
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            if (!scheduleDateTime.isAfter(now.plusDays(2))) {
                conn.rollback();
                return false; // cannot cancel within 2 days
            }

            // set booking status
            String updBooking = "UPDATE Booking SET status = 'cancelled' WHERE booking_id = ? AND status <> 'cancelled'";
            try (PreparedStatement ps = conn.prepareStatement(updBooking)) {
                ps.setString(1, bookingId.toString());
                int affected = ps.executeUpdate();
                if (affected == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // set schedule available
            if (scheduleId != null) {
                String updSchedule = "UPDATE Schedule SET status = 'available' WHERE schedule_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updSchedule)) {
                    ps.setString(1, scheduleId.toString());
                    ps.executeUpdate();
                }
            }

            // restore equipment quantities (if any)
            String qEquip = "SELECT equipment_id, quantity FROM Booking_Equipment WHERE booking_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(qEquip)) {
                ps.setString(1, bookingId.toString());
                ResultSet rs = ps.executeQuery();
                String updEquip = "UPDATE Location_Equipment SET quantity = quantity + ? WHERE equipment_id = ?";
                try (PreparedStatement ps2 = conn.prepareStatement(updEquip)) {
                    while (rs.next()) {
                        int qty = rs.getInt("quantity");
                        String eid = rs.getString("equipment_id");
                        ps2.setInt(1, qty);
                        ps2.setString(2, eid);
                        ps2.addBatch();
                    }
                    ps2.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean completeBooking(UUID bookingId) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            // set booking status to completed if not already
            String updBooking = "UPDATE Booking SET status = 'completed' WHERE booking_id = ? AND status <> 'completed'";
            try (PreparedStatement ps = conn.prepareStatement(updBooking)) {
                ps.setString(1, bookingId.toString());
                int affected = ps.executeUpdate();
                if (affected == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // restore equipment quantities (booking finished -> return equipments)
            String qEquip = "SELECT equipment_id, quantity FROM Booking_Equipment WHERE booking_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(qEquip)) {
                ps.setString(1, bookingId.toString());
                ResultSet rs = ps.executeQuery();
                String updEquip = "UPDATE Location_Equipment SET quantity = quantity + ? WHERE equipment_id = ?";
                try (PreparedStatement ps2 = conn.prepareStatement(updEquip)) {
                    while (rs.next()) {
                        int qty = rs.getInt("quantity");
                        String eid = rs.getString("equipment_id");
                        ps2.setInt(1, qty);
                        ps2.setString(2, eid);
                        ps2.addBatch();
                    }
                    ps2.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStatus(UUID bookingId, String newStatus) {
        if (newStatus == null) return false;
        newStatus = newStatus.trim().toLowerCase();
        try {
            if ("cancelled".equals(newStatus)) {
                return cancelBooking(bookingId);
            } else if ("completed".equals(newStatus)) {
                return completeBooking(bookingId);
            } else {
                String sql = "UPDATE Booking SET status = ? WHERE booking_id = ?";
                try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, newStatus);
                    ps.setString(2, bookingId.toString());
                    int affected = ps.executeUpdate();
                    return affected > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<BookingViewModel> getByLocation(UUID locationId) {
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

    public List<BookingViewModel> getByLocationFiltered(UUID locationId, String bookingDateStr, String startTimeStr, String status, String customerName) {
        List<BookingViewModel> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT b.booking_id, b.booker_id, b.field_id, b.schedule_id, b.status, b.total_price, s.booking_date, s.start_time, s.end_time, f.field_name, u.full_name AS customer_name ");
        sql.append("FROM Booking b ");
        sql.append("LEFT JOIN Schedule s ON b.schedule_id = s.schedule_id ");
        sql.append("LEFT JOIN Field f ON b.field_id = f.field_id ");
        sql.append("LEFT JOIN Users u ON b.booker_id = u.user_id ");
        sql.append("WHERE f.location_id = ? ");

        if (bookingDateStr != null && !bookingDateStr.isBlank()) {
            sql.append(" AND s.booking_date = ? ");
        }
        if (startTimeStr != null && !startTimeStr.isBlank()) {
            sql.append(" AND s.start_time = ? ");
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND LOWER(b.status) = LOWER(?) ");
        }
        if (customerName != null && !customerName.isBlank()) {
            sql.append(" AND LOWER(u.full_name) LIKE LOWER(?) ");
        }

        sql.append(" ORDER BY s.booking_date DESC, s.start_time");

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setString(idx++, locationId.toString());
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
            if (customerName != null && !customerName.isBlank()) {
                ps.setString(idx++, "%" + customerName + "%");
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

}
