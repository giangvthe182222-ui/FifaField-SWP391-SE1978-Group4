package DAO;

import Models.Feedback;
import Models.FieldFeedbackViewModel;
import Utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
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

    public Feedback getFeedbackByBookingAndCustomer(UUID bookingId, UUID customerId) {
        String sql = "SELECT TOP 1 feedback_id, booking_id, customer_id, rating, comment, created_at "
                + "FROM Feedback WHERE booking_id = ? AND customer_id = ? "
                + "ORDER BY created_at DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, bookingId.toString());
            ps.setString(2, customerId.toString());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return null;
            }

            Feedback feedback = new Feedback();
            String feedbackId = rs.getString("feedback_id");
            String foundBookingId = rs.getString("booking_id");
            String foundCustomerId = rs.getString("customer_id");
            if (feedbackId != null) {
                feedback.setFeedbackId(UUID.fromString(feedbackId));
            }
            if (foundBookingId != null) {
                feedback.setBookingId(UUID.fromString(foundBookingId));
            }
            if (foundCustomerId != null) {
                feedback.setCustomerId(UUID.fromString(foundCustomerId));
            }
            feedback.setRating(rs.getInt("rating"));
            feedback.setComment(rs.getString("comment"));
            Timestamp createdAt = rs.getTimestamp("created_at");
            if (createdAt != null) {
                feedback.setCreatedAt(createdAt.toLocalDateTime());
            }
            return feedback;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean canCustomerManageFeedback(UUID bookingId, UUID customerId) {
        String sql = "SELECT b.booking_id FROM Booking b "
                + "INNER JOIN Schedule s ON s.schedule_id = b.schedule_id "
                + "WHERE b.booking_id = ? AND b.booker_id = ? AND LOWER(ISNULL(b.play_status, '')) = 'completed' "
                + "AND (s.booking_date < CAST(SYSDATETIME() AS DATE) "
                + "     OR (s.booking_date = CAST(SYSDATETIME() AS DATE) AND s.end_time <= CAST(SYSDATETIME() AS TIME)))";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, bookingId.toString());
            ps.setString(2, customerId.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean canCustomerFeedback(UUID bookingId, UUID customerId) {
        return canCustomerManageFeedback(bookingId, customerId)
                && getFeedbackByBookingAndCustomer(bookingId, customerId) == null;
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

    public boolean update(UUID bookingId, UUID customerId, int rating, String comment) {
        String sql = "UPDATE Feedback SET rating = ?, comment = ? WHERE booking_id = ? AND customer_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, rating);
            if (comment == null || comment.trim().isEmpty()) {
                ps.setNull(2, Types.NVARCHAR);
            } else {
                ps.setString(2, comment.trim());
            }
            ps.setString(3, bookingId.toString());
            ps.setString(4, customerId.toString());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(UUID bookingId, UUID customerId) {
        String sql = "DELETE FROM Feedback WHERE booking_id = ? AND customer_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, bookingId.toString());
            ps.setString(2, customerId.toString());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Double getAverageRatingByField(UUID fieldId) {
        String sql = "SELECT AVG(CAST(fb.rating AS DECIMAL(10,2))) AS avg_rating "
                + "FROM Feedback fb "
                + "INNER JOIN Booking b ON b.booking_id = fb.booking_id "
                + "WHERE b.field_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fieldId.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double value = rs.getDouble("avg_rating");
                return rs.wasNull() ? null : value;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getFeedbackCountByField(UUID fieldId) {
        String sql = "SELECT COUNT(*) AS feedback_count "
                + "FROM Feedback fb "
                + "INNER JOIN Booking b ON b.booking_id = fb.booking_id "
                + "WHERE b.field_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fieldId.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("feedback_count");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<FieldFeedbackViewModel> getFeedbacksByField(UUID fieldId) {
        List<FieldFeedbackViewModel> feedbacks = new ArrayList<>();
        String sql = "SELECT fb.feedback_id, fb.booking_id, fb.rating, fb.comment, fb.created_at, u.full_name AS customer_name "
                + "FROM Feedback fb "
                + "INNER JOIN Booking b ON b.booking_id = fb.booking_id "
                + "INNER JOIN Users u ON u.user_id = fb.customer_id "
                + "WHERE b.field_id = ? "
                + "ORDER BY fb.created_at DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fieldId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                FieldFeedbackViewModel feedback = new FieldFeedbackViewModel();
                String feedbackId = rs.getString("feedback_id");
                String bookingId = rs.getString("booking_id");
                if (feedbackId != null) {
                    feedback.setFeedbackId(UUID.fromString(feedbackId));
                }
                if (bookingId != null) {
                    feedback.setBookingId(UUID.fromString(bookingId));
                }
                feedback.setRating(rs.getInt("rating"));
                feedback.setComment(rs.getString("comment"));
                feedback.setCustomerName(rs.getString("customer_name"));
                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    feedback.setCreatedAt(createdAt.toLocalDateTime());
                }
                feedbacks.add(feedback);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return feedbacks;
    }
}
