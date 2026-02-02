package DAO;

import Models.Schedule;
import Utils.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScheduleDAO {

    private Schedule map(ResultSet rs) throws SQLException {
        Schedule s = new Schedule();
        s.setScheduleId(UUID.fromString(rs.getString("schedule_id")));
        s.setFieldId(UUID.fromString(rs.getString("field_id")));
        s.setBookingDate(rs.getDate("booking_date").toLocalDate());
        s.setPrice(rs.getBigDecimal("price"));
        s.setStartTime(rs.getTime("start_time").toLocalTime());
        s.setEndTime(rs.getTime("end_time").toLocalTime());
        s.setStatus(rs.getString("status"));
        return s;
    }

    public List<Schedule> getScheduleByField(UUID fieldId) {
        List<Schedule> list = new ArrayList<>();
        String sql = "SELECT * FROM [Schedule] WHERE field_id = ? ORDER BY booking_date, start_time";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, fieldId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 🔥 MỚI – LỌC THEO NGÀY
    public List<Schedule> getScheduleByFieldAndDate(UUID fieldId, LocalDate date) {
        List<Schedule> list = new ArrayList<>();
        String sql = "SELECT * FROM [Schedule] WHERE field_id = ? AND booking_date = ? ORDER BY start_time";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, fieldId);
            ps.setDate(2, Date.valueOf(date));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
