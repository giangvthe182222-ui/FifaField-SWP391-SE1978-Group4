package DAO;

import Utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PasswordResetDAO {

    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM Gmail_Account WHERE email = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updatePasswordByEmail(String email, String newPassword) {
        // Update Users theo gmail_id join Gmail_Account
        String sql
                = "UPDATE u SET u.password = ? "
                + "FROM Users u "
                + "JOIN Gmail_Account g ON u.gmail_id = g.gmail_id "
                + "WHERE g.email = ?";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, email.trim());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
