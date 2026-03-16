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

    /**
     * Insert a shift for each date in [start,end] inclusive. Performs overlap check externally.
     * Returns true if all rows inserted successfully.
     */
    public boolean assignShiftRange(StaffShift template, LocalDate start, LocalDate end) throws SQLException {
        String sql = "INSERT INTO Staff_Shift(staff_id, field_id, shift_id, working_date, assigned_by, status) VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                LocalDate d = start;
                while (!d.isAfter(end)) {
                    ps.setString(1, template.getStaffId().toString());
                    ps.setString(2, template.getFieldId().toString());
                    ps.setString(3, template.getShiftId().toString());
                    ps.setDate(4, Date.valueOf(d));
                    ps.setString(5, template.getAssignedBy().toString());
                    ps.setString(6, template.getStatus());
                    ps.addBatch();
                    d = d.plusDays(1);
                }
                int[] results = ps.executeBatch();
                for (int r : results) {
                    if (r == Statement.EXECUTE_FAILED) {
                        con.rollback();
                        return false;
                    }
                }
                con.commit();
                return true;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    /**
     * Checks if staff has any assignment overlapping the provided date range.
     */
    public boolean hasOverlap(UUID staffId, LocalDate start, LocalDate end) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Staff_Shift WHERE staff_id = ? AND working_date BETWEEN ? AND ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, staffId.toString());
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
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

    /**
     * Return all shifts assigned by a manager, without filtering.
     *
     * Kept for backward compatibility; callers should prefer {@link #searchShiftsAssignedBy(UUID,String,LocalDate,LocalDate)}
     * if any filter criteria are needed.
     */
    public List<StaffShiftViewModel> getShiftsAssignedByWithNames(UUID managerId) throws SQLException {
        List<StaffShiftViewModel> list = new ArrayList<>();
        String sql = "SELECT ss.staff_id, u.full_name AS staff_name, ss.field_id, f.field_name, "
                   + "ss.shift_id, sh.shift_name, MIN(ss.working_date) AS start_date, MAX(ss.working_date) AS end_date, ss.assigned_by, ss.status "
                   + "FROM Staff_Shift ss "
                   + "JOIN Users u ON ss.staff_id = u.user_id "
                   + "JOIN Field f ON ss.field_id = f.field_id "
                   + "JOIN Shift sh ON ss.shift_id = sh.shift_id "
                   + "WHERE ss.assigned_by = ? "
                   + "GROUP BY ss.staff_id, u.full_name, ss.field_id, f.field_name, ss.shift_id, sh.shift_name, ss.assigned_by, ss.status "
                   + "ORDER BY start_date DESC";
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
                    java.sql.Date sd = rs.getDate("start_date");
                    if (sd != null) vm.setStartDate(sd.toLocalDate());
                    java.sql.Date ed = rs.getDate("end_date");
                    if (ed != null) vm.setEndDate(ed.toLocalDate());
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

    /**
     * Delete all shifts for a given staff-field-shift combination (all working dates).
     * Used when deleting a grouped assignment.
     */
    public boolean deleteStaffShiftGroup(UUID staffId, UUID fieldId, UUID shiftId) throws SQLException {
        String sql = "DELETE FROM Staff_Shift WHERE staff_id=? AND field_id=? AND shift_id=?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, staffId.toString());
            ps.setString(2, fieldId.toString());
            ps.setString(3, shiftId.toString());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Update a staff shift group by deleting old assignments and creating new ones with new field/shift/dates.
     * Used when editing an assignment.
     */
    public boolean updateStaffShiftGroup(UUID origStaffId, UUID origFieldId, UUID origShiftId,
                                          UUID newFieldId, UUID newShiftId, 
                                          LocalDate newStartDate, LocalDate newEndDate,
                                          UUID assignedBy) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // 1. Delete old group
                String deleteSql = "DELETE FROM Staff_Shift WHERE staff_id=? AND field_id=? AND shift_id=?";
                try (PreparedStatement ps = con.prepareStatement(deleteSql)) {
                    ps.setString(1, origStaffId.toString());
                    ps.setString(2, origFieldId.toString());
                    ps.setString(3, origShiftId.toString());
                    ps.executeUpdate();
                }
                
                // 2. Insert new range
                String insertSql = "INSERT INTO Staff_Shift(staff_id, field_id, shift_id, working_date, assigned_by, status) VALUES(?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                    LocalDate d = newStartDate;
                    while (!d.isAfter(newEndDate)) {
                        ps.setString(1, origStaffId.toString());
                        ps.setString(2, newFieldId.toString());
                        ps.setString(3, newShiftId.toString());
                        ps.setDate(4, Date.valueOf(d));
                        ps.setString(5, assignedBy.toString());
                        ps.setString(6, "assigned");
                        ps.addBatch();
                        d = d.plusDays(1);
                    }
                    ps.executeBatch();
                }
                
                con.commit();
                return true;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
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

    /**
     * Search for shifts assigned by a manager using optional criteria.
     *
     * @param managerId ID of manager who assigned the shifts
     * @param staffName optional substring of staff full name (case-insensitive)
     * @param startDate optional start working date (inclusive)
     * @param endDate optional end working date (inclusive)
     * @return matching records sorted by working_date desc
     * @throws SQLException
     */
    public List<StaffShiftViewModel> searchShiftsAssignedBy(UUID managerId, String staffName, LocalDate startDate, LocalDate endDate) throws SQLException {
        List<StaffShiftViewModel> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT ss.staff_id, u.full_name AS staff_name, ss.field_id, f.field_name, "
                + "ss.shift_id, sh.shift_name, MIN(ss.working_date) AS start_date, MAX(ss.working_date) AS end_date, ss.assigned_by, ss.status "
                + "FROM Staff_Shift ss "
                + "JOIN Users u ON ss.staff_id = u.user_id "
                + "JOIN Field f ON ss.field_id = f.field_id "
                + "JOIN Shift sh ON ss.shift_id = sh.shift_id "
                + "WHERE ss.assigned_by = ?");
        if (staffName != null && !staffName.trim().isEmpty()) {
            sql.append(" AND LOWER(u.full_name) LIKE ?");
        }
        if (startDate != null) {
            sql.append(" AND ss.working_date >= ?");
        }
        if (endDate != null) {
            sql.append(" AND ss.working_date <= ?");
        }
        sql.append(" GROUP BY ss.staff_id, u.full_name, ss.field_id, f.field_name, ss.shift_id, sh.shift_name, ss.assigned_by, ss.status");
        sql.append(" ORDER BY start_date DESC");

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setString(idx++, managerId.toString());
            if (staffName != null && !staffName.trim().isEmpty()) {
                ps.setString(idx++, "%" + staffName.toLowerCase() + "%");
            }
            if (startDate != null) {
                ps.setDate(idx++, Date.valueOf(startDate));
            }
            if (endDate != null) {
                ps.setDate(idx++, Date.valueOf(endDate));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffShiftViewModel vm = new StaffShiftViewModel();
                    vm.setStaffId(UUID.fromString(rs.getString("staff_id")));
                    vm.setStaffName(rs.getString("staff_name"));
                    vm.setFieldId(UUID.fromString(rs.getString("field_id")));
                    vm.setFieldName(rs.getString("field_name"));
                    vm.setShiftId(UUID.fromString(rs.getString("shift_id")));
                    vm.setShiftName(rs.getString("shift_name"));
                    java.sql.Date sd = rs.getDate("start_date");
                    if (sd != null) vm.setStartDate(sd.toLocalDate());
                    java.sql.Date ed = rs.getDate("end_date");
                    if (ed != null) vm.setEndDate(ed.toLocalDate());
                    vm.setAssignedBy(UUID.fromString(rs.getString("assigned_by")));
                    vm.setStatus(rs.getString("status"));
                    list.add(vm);
                }
            }
        }
        return list;
    }

    public List<StaffShiftViewModel> getShiftsForStaff(UUID staffId) throws SQLException {
        List<StaffShiftViewModel> list = new ArrayList<>();
        String sql = "SELECT ss.staff_id, u.full_name AS staff_name, ss.field_id, f.field_name, "
                + "ss.shift_id, sh.shift_name, ss.working_date, ss.assigned_by, ss.status "
                + "FROM Staff_Shift ss "
                + "JOIN Users u ON ss.staff_id = u.user_id "
                + "JOIN Field f ON ss.field_id = f.field_id "
                + "JOIN Shift sh ON ss.shift_id = sh.shift_id "
                + "WHERE ss.staff_id = ? "
                + "ORDER BY ss.working_date ASC";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, staffId.toString());
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
}

