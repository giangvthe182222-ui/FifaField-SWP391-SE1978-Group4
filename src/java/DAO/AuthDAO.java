package DAO;

import Utils.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import Models.User;
import Models.Role;
import java.util.UUID;

public class AuthDAO {

    public String login(String email, String password) {
        String sql
                = "SELECT u.user_id "
                + "FROM Users u "
                + "JOIN Gmail_Account g ON u.gmail_id = g.gmail_id "
                + "WHERE g.email = ? AND u.password = ? AND (u.status IS NULL OR u.status = N'active')";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("user_id");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUserById(String userId) {
        String sql = "SELECT u.user_id, u.gmail_id, u.password, u.full_name, u.phone, u.address, u.gender, u.role_id, u.status, u.created_at, "
                + "r.role_name, r.description, g.email "
                + "FROM Users u "
                + "JOIN Role r ON u.role_id = r.role_id "
                + "JOIN Gmail_Account g ON u.gmail_id = g.gmail_id "
                + "WHERE u.user_id = ?";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(UUID.fromString(rs.getString("user_id")));
                    user.setGmailId(UUID.fromString(rs.getString("gmail_id")));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));
                    user.setFullName(rs.getString("full_name"));
                    user.setPhone(rs.getString("phone"));
                    user.setAddress(rs.getString("address"));
                    user.setGender(rs.getString("gender"));
                    user.setRoleId(UUID.fromString(rs.getString("role_id")));
                    user.setStatus(rs.getString("status"));
                    user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                    Role role = new Role();
                    role.setRoleId(UUID.fromString(rs.getString("role_id")));
                    role.setRoleName(rs.getString("role_name"));
                    role.setDescription(rs.getString("description"));
                    user.setRole(role);

                    return user;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    // register staff: create Gmail_Account, Users (role 'staff') and Staff record
    public void registerStaff(
            String fullName,
            String email,
            String password,
            String phone,
            String address,
            String gender,
            String employeeCode,
            java.sql.Date hireDate,
            String staffStatus,
            String locationId
    )
            throws SQLException {

        if (password.length() > 20) {
            throw new SQLException("Password tối đa 20 ký tự (do DB đang NVARCHAR(20)).");
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                String roleId = getRoleIdByName(con, "staff");
                if (roleId == null) {
                    throw new SQLException("Role 'staff' chưa tồn tại. Hãy insert role trước.");
                }

                String gmailId = newGuid(con);
                String userId = newGuid(con);

                String insertGmail
                        = "INSERT INTO Gmail_Account(gmail_id, google_sub, email) VALUES(?, ?, ?)";

                String insertUser
                        = "INSERT INTO Users(user_id, gmail_id, password, full_name, phone, address, gender, role_id, status) "
                        + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, N'active')";

                String insertStaff
                        = "INSERT INTO Staff(user_id, employee_code, hire_date, status, location_id) VALUES(?, ?, ?, ?, ?)";

                // Gmail_Account
                try (PreparedStatement ps = con.prepareStatement(insertGmail)) {
                    ps.setString(1, gmailId);
                    ps.setString(2, "local_" + email.trim());
                    ps.setString(3, email.trim());
                    ps.executeUpdate();
                }

                // Users
                try (PreparedStatement ps = con.prepareStatement(insertUser)) {
                    ps.setString(1, userId);
                    ps.setString(2, gmailId);
                    ps.setString(3, password);
                    ps.setString(4, fullName);

                    ps.setString(5, (phone == null || phone.isBlank()) ? null : phone);
                    ps.setString(6, (address == null || address.isBlank()) ? null : address);
                    ps.setString(7, (gender == null || gender.isBlank()) ? null : gender);

                    ps.setString(8, roleId);
                    ps.executeUpdate();
                }

                // Staff
                try (PreparedStatement ps = con.prepareStatement(insertStaff)) {
                    ps.setString(1, userId);
                    ps.setString(2, (employeeCode == null || employeeCode.isBlank()) ? null : employeeCode);
                    ps.setDate(3, hireDate);
                    ps.setString(4, (staffStatus == null || staffStatus.isBlank()) ? null : staffStatus);
                    ps.setString(5, locationId);
                    ps.executeUpdate();
                }

                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    // check email ton tai chua?
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM Gmail_Account WHERE email = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // check employee_code exists in Staff
    public boolean employeeCodeExists(String employeeCode) throws SQLException {
        if (employeeCode == null || employeeCode.isBlank()) return false;
        String sql = "SELECT 1 FROM Staff WHERE employee_code = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, employeeCode.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // lay role_id theo role_name
    private String getRoleIdByName(Connection con, String roleName) throws SQLException {
        String sql = "SELECT role_id FROM Role WHERE role_name = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, roleName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    // tao GUID
    private String newGuid(Connection con) throws SQLException {
        String sql = "SELECT CONVERT(VARCHAR(36), NEWID())";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getString(1);
        }
    }

    // register: Full name + Email + Password
    public void registerCustomer(
            String fullName,
            String email,
            String password,
            String phone,
            String address,
            String gender
    )
            throws SQLException {

        if (password.length() > 20) {
            throw new SQLException("Password tối đa 20 ký tự (do DB đang NVARCHAR(20)).");
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                String roleId = getRoleIdByName(con, "customer");
                if (roleId == null) {
                    throw new SQLException("Role 'customer' chưa tồn tại. Hãy insert role trước.");
                }

                String gmailId = newGuid(con);
                String userId = newGuid(con);

                String insertGmail
                        = "INSERT INTO Gmail_Account(gmail_id, google_sub, email) VALUES(?, ?, ?)";

                String insertUser
                        = "INSERT INTO Users(user_id, gmail_id, password, full_name, phone, address, gender, role_id, status) "
                        + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, N'active')";

                String insertCustomer
                        = "INSERT INTO Customer(user_id, loyalty_points) VALUES(?, 0)";

                // Gmail_Account
                try (PreparedStatement ps = con.prepareStatement(insertGmail)) {
                    ps.setString(1, gmailId);
                    ps.setString(2, "local_" + email.trim()); // placeholder google_sub
                    ps.setString(3, email.trim());
                    ps.executeUpdate();
                }

                // Users
                try (PreparedStatement ps = con.prepareStatement(insertUser)) {
                    ps.setString(1, userId);
                    ps.setString(2, gmailId);
                    ps.setString(3, password);
                    ps.setString(4, fullName);

                    // field KHÔNG bắt buộc
                    ps.setString(5, (phone == null || phone.isBlank()) ? null : phone);
                    ps.setString(6, (address == null || address.isBlank()) ? null : address);
                    ps.setString(7, (gender == null || gender.isBlank()) ? null : gender);

                    ps.setString(8, roleId);
                    ps.executeUpdate();
                }

                // Customer
                try (PreparedStatement ps = con.prepareStatement(insertCustomer)) {
                    ps.setString(1, userId);
                    ps.executeUpdate();
                }

                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    // Cập nhật thông tin cơ bản của user
    public boolean updateUserBasic(String userId, String fullName, String phone, String address, String gender) throws SQLException {
        String sql = "UPDATE Users SET full_name = ?, phone = ?, address = ?, gender = ? WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, (phone == null || phone.isBlank()) ? null : phone);
            ps.setString(3, (address == null || address.isBlank()) ? null : address);
            ps.setString(4, (gender == null || gender.isBlank()) ? null : gender);
            ps.setString(5, userId);
            return ps.executeUpdate() > 0;
        }
    }
}
