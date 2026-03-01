package DAO;

import Models.Schedule;
import Utils.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.LocalTime;
import java.math.BigDecimal;

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

    // Generate schedules for about 1 year using a sample-week CSV of 42 prices
    // CSV order: Mon slot1..slot6, Tue slot1..slot6, ..., Sun slot1..slot6
    public void createSchedulesFromSample(UUID fieldId, String csvPrices) throws Exception {
        if (csvPrices == null) return;
        String[] parts = csvPrices.split(",");
        if (parts.length < 42) throw new IllegalArgumentException("sampleWeek must contain 42 values");

        BigDecimal[] prices = new BigDecimal[42];
        for (int i=0;i<42;i++) {
            String p = parts[i].trim();
            if (p.isEmpty()) p = "0";
            prices[i] = new BigDecimal(p);
        }

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusYears(1);

        String sql = "INSERT INTO [Schedule] (schedule_id, field_id, booking_date, price, start_time, end_time, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            con.setAutoCommit(false);

            // base start time 06:00, each slot 90 minutes
            LocalTime base = LocalTime.of(6,0);

            LocalDate cur = start;
            int batch = 0;
            while (!cur.isAfter(end)) {
                int dow = cur.getDayOfWeek().getValue(); // 1=Mon .. 7=Sun
                int dayIndex = dow - 1;
                for (int slot=0; slot<6; slot++) {
                    BigDecimal price = prices[dayIndex*6 + slot];
                    LocalTime sTime = base.plusMinutes(90 * slot);
                    LocalTime eTime = sTime.plusMinutes(90);

                    ps.setString(1, UUID.randomUUID().toString());
                    ps.setString(2, fieldId.toString());
                    ps.setDate(3, Date.valueOf(cur));
                    ps.setBigDecimal(4, price);
                    ps.setTime(5, Time.valueOf(sTime));
                    ps.setTime(6, Time.valueOf(eTime));
                    ps.setNString(7, "available");
                    ps.addBatch();
                    batch++;
                }
                // execute in batches of 500
                if (batch >= 500) {
                    ps.executeBatch();
                    con.commit();
                    batch = 0;
                }
                cur = cur.plusDays(1);
            }
            if (batch > 0) {
                ps.executeBatch();
                con.commit();
            }
            con.setAutoCommit(true);
        }
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

    public Schedule getById(UUID scheduleId) {
        String sql = "SELECT * FROM [Schedule] WHERE schedule_id = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, scheduleId.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public boolean updateSchedule(UUID scheduleId, BigDecimal price, String status) {
        String sql = "UPDATE [Schedule] SET price = ?, status = ? WHERE schedule_id = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, price);
            ps.setNString(2, status);
            ps.setString(3, scheduleId.toString());
            int rows = ps.executeUpdate();
            return rows == 1;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
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
