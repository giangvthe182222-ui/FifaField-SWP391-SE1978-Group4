package DAO;

import Models.Booking;
import Models.BookingEquipment;
import Models.BookingViewModel;
import Utils.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WeeklyBookingDAO {

    private final BookingWriteDAO bookingWriteDAO;
    private final BookingStateDAO bookingStateDAO;

    public WeeklyBookingDAO(BookingWriteDAO bookingWriteDAO, BookingStateDAO bookingStateDAO) {
        this.bookingWriteDAO = bookingWriteDAO;
        this.bookingStateDAO = bookingStateDAO;
    }

    public List<BookingViewModel> getByWeeklyGroupId(UUID weeklyGroupId) {
        bookingStateDAO.synchronizeBookingStates();
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
                java.sql.Date bd = rs.getDate("booking_date");
                if (bd != null) {
                    vm.setBookingDate(bd.toLocalDate());
                }
                java.sql.Time st = rs.getTime("start_time");
                if (st != null) {
                    vm.setStartTime(st.toLocalTime());
                }
                java.sql.Time et = rs.getTime("end_time");
                if (et != null) {
                    vm.setEndTime(et.toLocalTime());
                }
                vm.setFieldName(rs.getString("field_name"));
                vm.setLocationName(rs.getString("location_name"));
                bookingStateDAO.applyStateToBookingViewModel(rs, vm);
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

    public boolean markWeeklyGroupPaid(UUID weeklyGroupId) {
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

        boolean allOk = true;
        for (UUID bookingId : bookingIds) {
            if (!bookingWriteDAO.cancelBookingForPayment(bookingId)) {
                allOk = false;
            }
        }
        return allOk;
    }

    public List<Booking> insertWeekly(UUID bookerId,
                                      UUID fieldId,
                                      List<UUID> scheduleIds,
                                      List<BookingEquipment> equipmentList,
                                      UUID voucherId,
                                      BigDecimal discountPercent,
                                      LocalDateTime paymentDeadline,
                                      UUID weeklyGroupId,
                                      String bookingPhone) throws Exception {
        List<Booking> created = new ArrayList<>();
        int sessionCount = scheduleIds.size();

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                LocalDateTime now = LocalDateTime.now();

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

                String lockSql = "UPDATE [Schedule] SET status = 'unavailable' "
                        + "WHERE schedule_id = ? AND field_id = ? AND status = 'available'";
                String priceSql = "SELECT price FROM [Schedule] WHERE schedule_id = ? AND field_id = ?";

                for (UUID sid : scheduleIds) {
                    BigDecimal rawPrice = BigDecimal.ZERO;
                    try (PreparedStatement ps = conn.prepareStatement(priceSql)) {
                        ps.setString(1, sid.toString());
                        ps.setString(2, fieldId.toString());
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            BigDecimal p = rs.getBigDecimal("price");
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

                    BigDecimal subtotal = rawPrice.add(equipmentTotalPerSession);
                    BigDecimal factor = BigDecimal.ONE.subtract(discountPercent.divide(BigDecimal.valueOf(100)));
                    BigDecimal totalPrice = subtotal.multiply(factor);

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
                    b.setTotalPrice(totalPrice);
                    b.setPaymentDeadline(paymentDeadline);
                    b.setWeeklyGroupId(weeklyGroupId);
                    created.add(b);
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
