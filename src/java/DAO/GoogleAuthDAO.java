package DAO;

import Utils.DBConnection;
import java.sql.*;

public class GoogleAuthDAO {

    public String findOrCreateUserByGoogle(String sub, String email, String name) throws Exception {

        // neu da co gmail_account theo sub thi tra user_id
        String findSql
                = "SELECT u.user_id "
                + "FROM Gmail_Account g JOIN Users u ON g.gmail_id = u.gmail_id "
                + "WHERE g.google_sub = ?";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(findSql)) {
            ps.setString(1, sub);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }

        // chua co -> tao moi (role customer)
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                String roleId = getRoleId(con, "customer");
                if (roleId == null) {
                    throw new SQLException("Role 'customer' not found.");
                }

                String gmailId = newGuid(con);
                String userId = newGuid(con);

                // Gmail_Account
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO Gmail_Account(gmail_id, google_sub, email) VALUES(?, ?, ?)")) {
                    ps.setString(1, gmailId);
                    ps.setString(2, sub);
                    ps.setString(3, email);
                    ps.executeUpdate();
                }

                // Users (password NULL)
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO Users(user_id, gmail_id, password, full_name, role_id, status) "
                        + "VALUES(?, ?, NULL, ?, ?, N'active')")) {
                    ps.setString(1, userId);
                    ps.setString(2, gmailId);
                    ps.setString(3, (name == null || name.isBlank()) ? email : name);
                    ps.setString(4, roleId);
                    ps.executeUpdate();
                }

                // Customer
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO Customer(user_id, loyalty_points) VALUES(?, 0)")) {
                    ps.setString(1, userId);
                    ps.executeUpdate();
                }

                con.commit();
                return userId;

            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    private String getRoleId(Connection con, String roleName) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT role_id FROM Role WHERE role_name = ?")) {
            ps.setString(1, roleName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    private String newGuid(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT CONVERT(VARCHAR(36), NEWID())"); ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getString(1);
        }
    }
}
