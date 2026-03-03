package DAO;

import Models.StaffShift;

import Models.StaffShiftViewModel;
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
        // legacy method still available
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

    public List<StaffShiftViewModel> getShiftsAssignedByWithNames(UUID managerId) throws SQLException {
        List<StaffShiftViewModel> list = new ArrayList<>();
        String sql = "SELECT ss.staff_id, u.full_name AS staff_name, ss.field_id, f.field_name, "
                   + "ss.shift_id, sh.shift_name, ss.working_date, ss.assigned_by, ss.status "
                   + "FROM Staff_Shift ss "
                   + "JOIN Users u ON ss.staff_id = u.user_id "
                   + "JOIN Field f ON ss.field_id = f.field_id "
                   + "JOIN Shift sh ON ss.shift_id = sh.shift_id "
                   + "WHERE ss.assigned_by = ? "
                   + "ORDER BY ss.working_date DESC";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, managerId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffShiftViewModel vm = new StaffShiftViewModel();
                    vm.setStaffId(UUID.fromString(rs.getString("staff_id")));
                    vm.setStaffName(rs.getString("staff_name"));
                    vm.setFieldId(UUID.fromString(rs.getString("field_id")));
                    vm.setFieldName(rs.getString("field_name"));
                    vm.setShiftId(UUID.fromString(rs.getString("shift_id")));
                    vm.setShiftName(rs.getString("shift_name"));
                    vm.setWorkingDate(rs.getDate("working_date").toLocalDate());
                    vm.setAssignedBy(UUID.fromString(rs.getString("assigned_by")));
                    vm.setStatus(rs.getString("status"));
                    list.add(vm);
                }
            }
        }
        return list;
    }

    public boolean updateStaffShift(UUID origStaff, UUID origField, UUID origShift, LocalDate origDate, StaffShift newData) throws SQLException {
        String sql = "UPDATE Staff_Shift SET staff_id=?, field_id=?, shift_id=?, working_date=?, status=? "
                   + "WHERE staff_id=? AND field_id=? AND shift_id=? AND working_date=?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newData.getStaffId().toString());
            ps.setString(2, newData.getFieldId().toString());
            ps.setString(3, newData.getShiftId().toString());
            ps.setDate(4, Date.valueOf(newData.getWorkingDate()));
            ps.setString(5, newData.getStatus());
            ps.setString(6, origStaff.toString());
            ps.setString(7, origField.toString());
            ps.setString(8, origShift.toString());
            ps.setDate(9, Date.valueOf(origDate));
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteStaffShift(UUID staffId, UUID fieldId, UUID shiftId, LocalDate workingDate) throws SQLException {
        String sql = "DELETE FROM Staff_Shift WHERE staff_id=? AND field_id=? AND shift_id=? AND working_date=?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, staffId.toString());
            ps.setString(2, fieldId.toString());
            ps.setString(3, shiftId.toString());
            ps.setDate(4, Date.valueOf(workingDate));
            return ps.executeUpdate() > 0;
        }
    }

    // dashboard helpers
    public int countAssignedBy(UUID managerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Staff_Shift WHERE assigned_by = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, managerId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public int countUpcoming(UUID managerId, LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Staff_Shift WHERE assigned_by = ? AND working_date >= ? AND working_date < ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, managerId.toString());
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public int countAssignedOnDate(UUID managerId, LocalDate date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Staff_Shift WHERE assigned_by = ? AND working_date = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, managerId.toString());
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}

