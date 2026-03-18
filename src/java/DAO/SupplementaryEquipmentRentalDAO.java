package DAO;

import Models.SupplementaryEquipmentRental;
import Models.SupplementaryEquipment;
import Utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

public class SupplementaryEquipmentRentalDAO {
    private String lastInsertError;

    public SupplementaryEquipmentRentalDAO() {
    }

    public String getLastInsertError() {
        return lastInsertError;
    }

    public boolean createSupplementaryRental(SupplementaryEquipmentRental rental, List<SupplementaryEquipment> equipmentList) {
        lastInsertError = null;

        if (equipmentList == null || equipmentList.isEmpty()) {
            lastInsertError = "Please select at least one equipment item.";
            return false;
        }

        BigDecimal totalPrice = BigDecimal.ZERO;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            String updateEquip = "UPDATE le "
                    + "SET le.quantity = le.quantity - ?, "
                    + "    le.status = CASE WHEN le.quantity - ? <= 0 THEN 'unavailable' ELSE 'available' END "
                    + "FROM Location_Equipment le "
                    + "WHERE le.location_id = ? AND le.equipment_id = ? AND le.quantity >= ?";

            for (SupplementaryEquipment se : equipmentList) {
                try (PreparedStatement ps = conn.prepareStatement(updateEquip)) {
                    ps.setInt(1, se.getQuantity());
                    ps.setInt(2, se.getQuantity());
                    ps.setString(3, rental.getLocationId().toString());
                    ps.setString(4, se.getEquipmentId().toString());
                    ps.setInt(5, se.getQuantity());

                    int affected = ps.executeUpdate();
                    if (affected == 0) {
                        conn.rollback();
                        lastInsertError = "Equipment stock changed for " + se.getEquipmentId() + ". Please review quantities.";
                        return false;
                    }
                }
            }

            String insertRental = "INSERT INTO Supplementary_Equipment_Rental (rental_id, original_booking_id, customer_id, field_id, location_id, created_time, status, total_price) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertRental)) {
                ps.setString(1, rental.getRentalId().toString());
                ps.setString(2, rental.getOriginalBookingId().toString());
                ps.setString(3, rental.getCustomerId().toString());
                ps.setString(4, rental.getFieldId().toString());
                ps.setString(5, rental.getLocationId().toString());
                ps.setTimestamp(6, Timestamp.valueOf(rental.getCreatedTime()));
                ps.setString(7, rental.getStatus());

                if (rental.getTotalPrice() != null) {
                    ps.setBigDecimal(8, rental.getTotalPrice());
                } else {
                    ps.setNull(8, Types.DECIMAL);
                }

                ps.executeUpdate();
            }

            String insertEquip = "INSERT INTO Supplementary_Equipment (rental_id, equipment_id, quantity) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertEquip)) {
                for (SupplementaryEquipment se : equipmentList) {
                    ps.setString(1, rental.getRentalId().toString());
                    ps.setString(2, se.getEquipmentId().toString());
                    ps.setInt(3, se.getQuantity());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            if (lastInsertError == null || lastInsertError.isBlank()) {
                lastInsertError = "Database error while creating rental: " + e.getMessage();
            }
            return false;
        }
    }

    public SupplementaryEquipmentRental getRentalById(UUID rentalId) {
        String sql = "SELECT * FROM Supplementary_Equipment_Rental WHERE rental_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, rentalId.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                SupplementaryEquipmentRental rental = new SupplementaryEquipmentRental();
                rental.setRentalId(UUID.fromString(rs.getString("rental_id")));
                rental.setOriginalBookingId(UUID.fromString(rs.getString("original_booking_id")));
                rental.setCustomerId(UUID.fromString(rs.getString("customer_id")));
                rental.setFieldId(UUID.fromString(rs.getString("field_id")));
                rental.setLocationId(UUID.fromString(rs.getString("location_id")));

                Timestamp createdTime = rs.getTimestamp("created_time");
                if (createdTime != null) {
                    rental.setCreatedTime(createdTime.toLocalDateTime());
                }

                rental.setStatus(rs.getString("status"));
                rental.setTotalPrice(rs.getBigDecimal("total_price"));

                return rental;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<SupplementaryEquipment> getRentalEquipments(UUID rentalId) {
        List<SupplementaryEquipment> list = new ArrayList<>();
        String sql = "SELECT rental_id, equipment_id, quantity FROM Supplementary_Equipment WHERE rental_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, rentalId.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                SupplementaryEquipment se = new SupplementaryEquipment();
                se.setRentalId(UUID.fromString(rs.getString("rental_id")));
                se.setEquipmentId(UUID.fromString(rs.getString("equipment_id")));
                se.setQuantity(rs.getInt("quantity"));
                list.add(se);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<SupplementaryEquipmentRental> getRentalsByOriginalBooking(UUID originalBookingId) {
        List<SupplementaryEquipmentRental> list = new ArrayList<>();
        String sql = "SELECT * FROM Supplementary_Equipment_Rental WHERE original_booking_id = ? ORDER BY created_time DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, originalBookingId.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                SupplementaryEquipmentRental rental = new SupplementaryEquipmentRental();
                rental.setRentalId(UUID.fromString(rs.getString("rental_id")));
                rental.setOriginalBookingId(UUID.fromString(rs.getString("original_booking_id")));
                rental.setCustomerId(UUID.fromString(rs.getString("customer_id")));
                rental.setFieldId(UUID.fromString(rs.getString("field_id")));
                rental.setLocationId(UUID.fromString(rs.getString("location_id")));

                Timestamp createdTime = rs.getTimestamp("created_time");
                if (createdTime != null) {
                    rental.setCreatedTime(createdTime.toLocalDateTime());
                }

                rental.setStatus(rs.getString("status"));
                rental.setTotalPrice(rs.getBigDecimal("total_price"));

                list.add(rental);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateStatus(UUID rentalId, String newStatus) {
        String sql = "UPDATE Supplementary_Equipment_Rental SET status = ? WHERE rental_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setString(2, rentalId.toString());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
