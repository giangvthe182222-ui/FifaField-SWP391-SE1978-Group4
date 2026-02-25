package DAO;

import Models.Booking;
import Models.BookingEquipment;
import Utils.DBConnection;

import java.sql.*;
import java.util.List;
import java.util.UUID;

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

}
