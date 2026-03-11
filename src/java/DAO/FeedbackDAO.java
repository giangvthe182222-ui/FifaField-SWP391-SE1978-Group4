package DAO;

import Utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FeedbackDAO {

    public Set<UUID> getFeedbackBookingIdsByCustomer(UUID customerId) {
        Set<UUID> ids = new HashSet<>();
        String sql = "SELECT booking_id FROM Feedback WHERE customer_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, customerId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String bookingId = rs.getString("booking_id");
                if (bookingId != null) {
                    ids.add(UUID.fromString(bookingId));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ids;
    }

    public boolean canCustomerFeedback(UUID bookingId, UUID customerId) {
        String sql = "SELECT b.booking_id FROM Booking b "
                + "WHERE b.booking_id = ? AND b.booker_id = ? AND LOWER(b.status) = 'completed' "
                + "AND NOT EXISTS (SELECT 1 FROM Feedback f WHERE f.booking_id = b.booking_id AND f.customer_id = ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, bookingId.toString());
            ps.setString(2, customerId.toString());
            ps.setString(3, customerId.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insert(UUID bookingId, UUID customerId, int rating, String comment) {
        String sql = "INSERT INTO Feedback (feedback_id, booking_id, customer_id, rating, comment) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, bookingId.toString());
            ps.setString(3, customerId.toString());
            ps.setInt(4, rating);

            if (comment == null || comment.trim().isEmpty()) {
                ps.setNull(5, Types.NVARCHAR);
            } else {
                ps.setString(5, comment.trim());
            }

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
