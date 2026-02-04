package DAO;

import Models.Manager;
import Models.User;
import Utils.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ManagerDAO {

    // ================= HELPER: Check if Email Exists =================
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) as cnt FROM Gmail_Account WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt") > 0;
                }
            }
        }
        return false;
    }

    // ================= HELPER: Check if Phone Exists =================
    public boolean phoneExists(String phone) throws SQLException {
        String sql = "SELECT COUNT(*) as cnt FROM Users WHERE phone = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt") > 0;
                }
            }
        }
        return false;
    }

    // ================= HELPER: Get Role ID by Name =================
    private String getRoleIdByName(Connection con, String roleName) throws SQLException {
        String sql = "SELECT role_id FROM Role WHERE role_name = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, roleName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    // ================= HELPER: Generate GUID =================
    private String newGuid(Connection con) throws SQLException {
        String sql = "SELECT CONVERT(VARCHAR(36), NEWID())";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getString(1);
        }
    }

    // ================= ADD MANAGER =================
    public boolean addManager(
            String fullName,
            String email,
            String password,
            String phone,
            String address,
            String gender,
            LocalDate startDate,
            UUID locationId
    ) throws SQLException {
        if (password.length() > 20) {
            throw new SQLException("Password tối đa 20 ký tự (do DB đang NVARCHAR(20)).");
        }

        Connection con = DBConnection.getConnection();
        if (con == null) {
            throw new SQLException("Không thể kết nối đến database");
        }

        try {
            con.setAutoCommit(false);

            String roleId = getRoleIdByName(con, "manager");
            if (roleId == null) {
                con.rollback();
                throw new SQLException("Role 'manager' chưa tồn tại. Hãy insert role trước.");
            }

            // Ensure location exists and is not already assigned to another manager
            String checkLocation = "SELECT manager_id FROM Location WHERE location_id = ?";
            try (PreparedStatement ps = con.prepareStatement(checkLocation)) {
                ps.setString(1, locationId.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        throw new SQLException("Vị trí không tồn tại");
                    }
                    String existingManager = rs.getString("manager_id");
                    if (existingManager != null && !existingManager.isBlank()) {
                        con.rollback();
                        throw new SQLException("Vị trí đã có quản lý");
                    }
                }
            }

            String gmailId = newGuid(con);
            String userId = newGuid(con);

            // Insert Gmail_Account
            String insertGmail = "INSERT INTO Gmail_Account(gmail_id, google_sub, email) VALUES(?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(insertGmail)) {
                ps.setString(1, gmailId);
                ps.setString(2, gmailId);  // google_sub = gmailId
                ps.setString(3, email.trim());
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    con.rollback();
                    throw new SQLException("Không thể tạo Gmail Account");
                }
            }

            // Insert Users
            String insertUser = "INSERT INTO Users(user_id, gmail_id, password, full_name, phone, address, gender, role_id, status) "
                    + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, N'active')";
            try (PreparedStatement ps = con.prepareStatement(insertUser)) {
                ps.setString(1, userId);
                ps.setString(2, gmailId);
                ps.setString(3, password);
                ps.setNString(4, fullName);
                ps.setString(5, phone);
                ps.setNString(6, address);
                ps.setString(7, gender);
                ps.setString(8, roleId);
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    con.rollback();
                    throw new SQLException("Không thể tạo User Account");
                }
            }

            // Insert Manager (only user_id + start_date)
            String insertManager = "INSERT INTO Manager(user_id, start_date) VALUES(?, ?)";
            try (PreparedStatement ps = con.prepareStatement(insertManager)) {
                ps.setString(1, userId);
                ps.setDate(2, java.sql.Date.valueOf(startDate));
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    con.rollback();
                    throw new SQLException("Không thể tạo Manager record");
                }
            }

            // Assign manager to location (update Location.manager_id)
            String assignLocation = "UPDATE Location SET manager_id = ? WHERE location_id = ?";
            try (PreparedStatement ps = con.prepareStatement(assignLocation)) {
                ps.setString(1, userId);
                ps.setString(2, locationId.toString());
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    con.rollback();
                    throw new SQLException("Không thể gán quản lý cho vị trí");
                }
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            throw e;
        } finally {
            try {
                con.close();
            } catch (SQLException closeEx) {
                closeEx.printStackTrace();
            }
        }
    }

    // ================= GET ALL MANAGERS =================
    public List<Manager> getAllManagers() throws SQLException {
        List<Manager> managers = new ArrayList<>();

        String sql = "SELECT m.user_id, m.start_date, u.full_name, u.phone, u.address, u.gender, u.status, "
            + "l.location_id, l.location_name, g.email "
            + "FROM Manager m "
            + "JOIN Users u ON m.user_id = u.user_id "
            + "JOIN Gmail_Account g ON u.gmail_id = g.gmail_id "
            + "LEFT JOIN Location l ON l.manager_id = m.user_id "
            + "ORDER BY u.full_name ASC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Manager manager = new Manager();
                manager.setUserId(UUID.fromString(rs.getString("user_id")));
                manager.setStartDate(rs.getDate("start_date").toLocalDate());
                // Thêm thông tin user cho hiển thị
                manager.setFullName(rs.getString("full_name"));
                manager.setEmail(rs.getString("email"));
                manager.setPhone(rs.getString("phone"));
                manager.setAddress(rs.getString("address"));
                manager.setGender(rs.getString("gender"));
                manager.setStatus(rs.getString("status"));
                String locId = rs.getString("location_id");
                if (locId != null) manager.setLocationId(UUID.fromString(locId));
                manager.setLocationName(rs.getString("location_name"));

                managers.add(manager);
            }
        }
        return managers;
    }

    // ================= GET MANAGER BY ID =================
    public Manager getManagerById(UUID userId) throws SQLException {
        String sql = "SELECT m.user_id, m.start_date, u.full_name, u.phone, u.address, u.gender, u.status, "
            + "l.location_id, l.location_name, g.email "
            + "FROM Manager m "
            + "JOIN Users u ON m.user_id = u.user_id "
            + "JOIN Gmail_Account g ON u.gmail_id = g.gmail_id "
            + "LEFT JOIN Location l ON l.manager_id = m.user_id "
            + "WHERE m.user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, userId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Manager manager = new Manager();
                    manager.setUserId(UUID.fromString(rs.getString("user_id")));
                    manager.setStartDate(rs.getDate("start_date").toLocalDate());
                    manager.setFullName(rs.getString("full_name"));
                    manager.setEmail(rs.getString("email"));
                    manager.setPhone(rs.getString("phone"));
                    manager.setAddress(rs.getString("address"));
                    manager.setGender(rs.getString("gender"));
                    manager.setStatus(rs.getString("status"));
                    String locId = rs.getString("location_id");
                    if (locId != null) manager.setLocationId(UUID.fromString(locId));
                    manager.setLocationName(rs.getString("location_name"));
                    return manager;
                }
            }
        }
        return null;
    }

    // ================= UPDATE MANAGER INFO =================
        public boolean updateManager(
            UUID userId,
            String fullName,
            String phone,
            String address,
            String gender,
            LocalDate startDate,
            UUID newLocationId
        ) throws SQLException {

        Connection con = DBConnection.getConnection();
        if (con == null) {
            throw new SQLException("Không thể kết nối đến database");
        }

        try {
            con.setAutoCommit(false);

            // Update Users table
            String updateUser = "UPDATE Users SET full_name = ?, phone = ?, address = ?, gender = ? WHERE user_id = ?";
            try (PreparedStatement ps = con.prepareStatement(updateUser)) {
                ps.setNString(1, fullName);
                ps.setString(2, phone);
                ps.setNString(3, address);
                ps.setString(4, gender);
                ps.setString(5, userId.toString());
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    con.rollback();
                    throw new SQLException("Không thể cập nhật thông tin Users");
                }
            }

            // Update Manager table (start_date)
            String updateManager = "UPDATE Manager SET start_date = ? WHERE user_id = ?";
            try (PreparedStatement ps = con.prepareStatement(updateManager)) {
                ps.setDate(1, java.sql.Date.valueOf(startDate));
                ps.setString(2, userId.toString());
                ps.executeUpdate();
            }

            // Reassign location: clear old location.manager_id if different, set new.location.manager_id = userId
            String selectCurrent = "SELECT location_id FROM Location WHERE manager_id = ?";
            String currentLocationId = null;
            try (PreparedStatement ps = con.prepareStatement(selectCurrent)) {
                ps.setString(1, userId.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) currentLocationId = rs.getString("location_id");
                }
            }

            String newLocStr = newLocationId != null ? newLocationId.toString() : null;
            if (newLocStr != null && !newLocStr.equals(currentLocationId)) {
                // Check new location exists and is free or already assigned to this manager
                String checkNew = "SELECT manager_id FROM Location WHERE location_id = ?";
                try (PreparedStatement ps = con.prepareStatement(checkNew)) {
                    ps.setString(1, newLocStr);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            throw new SQLException("Vị trí mới không tồn tại");
                        }
                        String mgr = rs.getString("manager_id");
                        if (mgr != null && !mgr.isBlank() && !mgr.equals(userId.toString())) {
                            con.rollback();
                            throw new SQLException("Vị trí mới đã có quản lý khác");
                        }
                    }
                }

                // Clear old
                if (currentLocationId != null) {
                    String clearOld = "UPDATE Location SET manager_id = NULL WHERE location_id = ?";
                    try (PreparedStatement ps = con.prepareStatement(clearOld)) {
                        ps.setString(1, currentLocationId);
                        ps.executeUpdate();
                    }
                }

                // Assign new
                String assignNew = "UPDATE Location SET manager_id = ? WHERE location_id = ?";
                try (PreparedStatement ps = con.prepareStatement(assignNew)) {
                    ps.setString(1, userId.toString());
                    ps.setString(2, newLocStr);
                    int rows = ps.executeUpdate();
                    if (rows == 0) {
                        con.rollback();
                        throw new SQLException("Không thể gán quản lý cho vị trí mới");
                    }
                }
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            throw e;
        } finally {
            try {
                con.close();
            } catch (SQLException closeEx) {
                closeEx.printStackTrace();
            }
        }
    }

    // ================= UPDATE MANAGER STATUS =================
    public boolean updateManagerStatus(UUID userId, String status) throws SQLException {
        String sql = "UPDATE Users SET status = ? WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setNString(1, status);
            ps.setString(2, userId.toString());
            return ps.executeUpdate() > 0;
        }
    }

    // ================= DELETE MANAGER =================
    public boolean deleteManager(UUID userId) throws SQLException {
        Connection con = DBConnection.getConnection();
        if (con == null) throw new SQLException("Không thể kết nối đến database");

        try {
            con.setAutoCommit(false);

            // Clear location.manager_id references
            String clearLocation = "UPDATE Location SET manager_id = NULL WHERE manager_id = ?";
            try (PreparedStatement ps = con.prepareStatement(clearLocation)) {
                ps.setString(1, userId.toString());
                ps.executeUpdate();
            }

            // Delete Manager record
            String deleteManager = "DELETE FROM Manager WHERE user_id = ?";
            try (PreparedStatement ps = con.prepareStatement(deleteManager)) {
                ps.setString(1, userId.toString());
                ps.executeUpdate();
            }

            // Delete User record
            String deleteUser = "DELETE FROM Users WHERE user_id = ?";
            try (PreparedStatement ps = con.prepareStatement(deleteUser)) {
                ps.setString(1, userId.toString());
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (Exception e) {
            con.rollback();
            e.printStackTrace();
            throw new SQLException("Lỗi khi xóa quản lý: " + e.getMessage(), e);
        } finally {
            try { con.close(); } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }
}
