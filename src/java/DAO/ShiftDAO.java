package DAO;

import Models.Shift;
import Utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShiftDAO {

    public List<Shift> getAllShifts() throws SQLException {
        List<Shift> list = new ArrayList<>();
        String sql = "SELECT shift_id, shift_name, start_time, end_time FROM Shift ORDER BY shift_name";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Shift s = new Shift();
                s.setShiftId(UUID.fromString(rs.getString("shift_id")));
                s.setShiftName(rs.getString("shift_name"));
                s.setStartTime(rs.getTime("start_time").toLocalTime());
                s.setEndTime(rs.getTime("end_time").toLocalTime());
                list.add(s);
            }
        }
        return list;
    }

    public Shift getShiftById(UUID id) throws SQLException {
        String sql = "SELECT shift_id, shift_name, start_time, end_time FROM Shift WHERE shift_id = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Shift s = new Shift();
                    s.setShiftId(UUID.fromString(rs.getString("shift_id")));
                    s.setShiftName(rs.getString("shift_name"));
                    s.setStartTime(rs.getTime("start_time").toLocalTime());
                    s.setEndTime(rs.getTime("end_time").toLocalTime());
                    return s;
                }
            }
        }
        return null;
    }

    public boolean addShift(Shift s) throws SQLException {
        String sql = "INSERT INTO Shift(shift_id, shift_name, start_time, end_time) VALUES(?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getShiftId().toString());
            ps.setString(2, s.getShiftName());
            ps.setTime(3, Time.valueOf(s.getStartTime()));
            ps.setTime(4, Time.valueOf(s.getEndTime()));
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateShift(Shift s) throws SQLException {
        String sql = "UPDATE Shift SET shift_name = ?, start_time = ?, end_time = ? WHERE shift_id = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getShiftName());
            ps.setTime(2, Time.valueOf(s.getStartTime()));
            ps.setTime(3, Time.valueOf(s.getEndTime()));
            ps.setString(4, s.getShiftId().toString());
            return ps.executeUpdate() > 0;
        }
    }
}
