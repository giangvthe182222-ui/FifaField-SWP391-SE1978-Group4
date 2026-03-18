package DAO;

import Models.WeeklyBookingGroup;
import Utils.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.UUID;

public class WeeklyBookingGroupDAO {

    public boolean create(WeeklyBookingGroup group) {
        String sql = "INSERT INTO Weekly_Booking_Group (weekly_group_id, booker_id, total_amount, status, payment_deadline) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, group.getWeeklyGroupId().toString());
            ps.setString(2, group.getBookerId().toString());
            ps.setBigDecimal(3, group.getTotalAmount() == null ? BigDecimal.ZERO : group.getTotalAmount());
            ps.setString(4, group.getStatus());
            if (group.getPaymentDeadline() != null) {
                ps.setTimestamp(5, Timestamp.valueOf(group.getPaymentDeadline()));
            } else {
                ps.setNull(5, Types.TIMESTAMP);
            }
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public WeeklyBookingGroup getById(UUID weeklyGroupId) {
        String sql = "SELECT weekly_group_id, booker_id, total_amount, status, payment_deadline, created_at FROM Weekly_Booking_Group WHERE weekly_group_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, weeklyGroupId.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                WeeklyBookingGroup g = new WeeklyBookingGroup();
                g.setWeeklyGroupId(UUID.fromString(rs.getString("weekly_group_id")));
                g.setBookerId(UUID.fromString(rs.getString("booker_id")));
                g.setTotalAmount(rs.getBigDecimal("total_amount"));
                g.setStatus(rs.getString("status"));
                Timestamp pd = rs.getTimestamp("payment_deadline");
                if (pd != null) {
                    g.setPaymentDeadline(pd.toLocalDateTime());
                }
                Timestamp ca = rs.getTimestamp("created_at");
                if (ca != null) {
                    g.setCreatedAt(ca.toLocalDateTime());
                }
                return g;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateTotalAmount(UUID weeklyGroupId, BigDecimal totalAmount) {
        String sql = "UPDATE Weekly_Booking_Group SET total_amount = ? WHERE weekly_group_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, totalAmount == null ? BigDecimal.ZERO : totalAmount);
            ps.setString(2, weeklyGroupId.toString());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStatus(UUID weeklyGroupId, String status) {
        String sql = "UPDATE Weekly_Booking_Group SET status = ? WHERE weekly_group_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, weeklyGroupId.toString());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(UUID weeklyGroupId) {
        String sql = "DELETE FROM Weekly_Booking_Group WHERE weekly_group_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, weeklyGroupId.toString());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
