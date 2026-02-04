package DAO;

import Utils.DBConnection;
import Models.StaffViewModel;
import Models.Staff;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {

    public List<StaffViewModel> getAllStaff() throws SQLException {
        List<StaffViewModel> list = new ArrayList<>();
        String sql = "SELECT s.user_id, s.employee_code, s.hire_date, s.status, s.location_id, u.full_name, u.phone, l.location_name "
                   + "FROM Staff s JOIN Users u ON s.user_id = u.user_id JOIN Location l ON s.location_id = l.location_id";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffViewModel vm = new StaffViewModel();
                    vm.setUserId(rs.getString("user_id"));
                    vm.setEmployeeCode(rs.getString("employee_code"));
                    Date d = rs.getDate("hire_date");
                    if (d != null) vm.setHireDate(d.toLocalDate());
                    vm.setStatus(rs.getString("status"));
                    vm.setLocationId(rs.getString("location_id"));
                    vm.setFullName(rs.getString("full_name"));
                    vm.setPhone(rs.getString("phone"));
                    vm.setLocationName(rs.getString("location_name"));
                    list.add(vm);
                }
            }
        }
        return list;
    }

    public StaffViewModel getStaffById(String userId) throws SQLException {
        String sql = "SELECT s.user_id, s.employee_code, s.hire_date, s.status, s.location_id, u.full_name, u.phone, u.address, u.gender, l.location_name "
                   + "FROM Staff s JOIN Users u ON s.user_id = u.user_id JOIN Location l ON s.location_id = l.location_id WHERE s.user_id = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    StaffViewModel vm = new StaffViewModel();
                    vm.setUserId(rs.getString("user_id"));
                    vm.setEmployeeCode(rs.getString("employee_code"));
                    Date d = rs.getDate("hire_date");
                    if (d != null) vm.setHireDate(d.toLocalDate());
                    vm.setStatus(rs.getString("status"));
                    vm.setLocationId(rs.getString("location_id"));
                    vm.setFullName(rs.getString("full_name"));
                    vm.setPhone(rs.getString("phone"));
                    vm.setLocationName(rs.getString("location_name"));
                    vm.setAddress(rs.getString("address"));
                    vm.setGender(rs.getString("gender"));
                    return vm;
                }
            }
        }
        return null;
    }

    public boolean updateStaff(String userId, String fullName, String phone, String address, String gender,
                               String employeeCode, Date hireDate, String status, String locationId) throws SQLException {
        String updateUser = "UPDATE Users SET full_name = ?, phone = ?, address = ?, gender = ? WHERE user_id = ?";
        String updateStaff = "UPDATE Staff SET employee_code = ?, hire_date = ?, status = ?, location_id = ? WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps1 = con.prepareStatement(updateUser);
                 PreparedStatement ps2 = con.prepareStatement(updateStaff)) {
                ps1.setString(1, fullName);
                ps1.setString(2, phone);
                ps1.setString(3, address);
                ps1.setString(4, gender);
                ps1.setString(5, userId);
                ps1.executeUpdate();

                ps2.setString(1, employeeCode);
                ps2.setDate(2, hireDate);
                ps2.setString(3, status);
                ps2.setString(4, locationId);
                ps2.setString(5, userId);
                ps2.executeUpdate();

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
}
