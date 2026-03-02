package DAO;

import Models.StaffShift;
import Utils.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StaffShiftDAO {

    public boolean assignShift(StaffShift ss) throws SQLException {
        String sql = "INSERT INTO Staff_Shift(staff_id, field_id, shift_id, working_date, assigned_by, status) VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ss.getStaffId().toString());
            ps.setString(2, ss.getFieldId().toString());
            ps.setString(3, ss.getShiftId().toString());
            ps.setDate(4, Date.valueOf(ss.getWorkingDate()));
            ps.setString(5, ss.getAssignedBy().toString());
            ps.setString(6, ss.getStatus());
            return ps.executeUpdate() > 0;
        }
    }

    public List<StaffShift> getShiftsAssignedBy(UUID managerId) throws SQLException {
        List<StaffShift> list = new ArrayList<>();
        String sql = "SELECT staff_id, field_id, shift_id, working_date, assigned_by, status FROM Staff_Shift WHERE assigned_by = ? ORDER BY working_date DESC";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, managerId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffShift ss = new StaffShift();
                    ss.setStaffId(UUID.fromString(rs.getString("staff_id")));
                    ss.setFieldId(UUID.fromString(rs.getString("field_id")));
                    ss.setShiftId(UUID.fromString(rs.getString("shift_id")));
                    ss.setWorkingDate(rs.getDate("working_date").toLocalDate());
                    ss.setAssignedBy(UUID.fromString(rs.getString("assigned_by")));
                    ss.setStatus(rs.getString("status"));
                    list.add(ss);
                }
            }
        }
        return list;
    }
}
