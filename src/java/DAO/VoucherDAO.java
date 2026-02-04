package DAO;

import Models.Voucher;
import Utils.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class VoucherDAO {

    public Voucher getVoucherById(String id) throws SQLException {
        String sql = "SELECT voucher_id, location_id, code, discount_value, description, start_date, end_date, used_count, status, created_at FROM Voucher WHERE voucher_id = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Voucher v = new Voucher();
                    v.setVoucherId(java.util.UUID.fromString(rs.getString("voucher_id")));
                    v.setLocationId(java.util.UUID.fromString(rs.getString("location_id")));
                    v.setCode(rs.getString("code"));
                    v.setDiscountValue(rs.getBigDecimal("discount_value"));
                    v.setDescription(rs.getString("description"));
                    Date sd = rs.getDate("start_date"); if (sd != null) v.setStartDate(sd.toLocalDate());
                    Date ed = rs.getDate("end_date"); if (ed != null) v.setEndDate(ed.toLocalDate());
                    v.setUsedCount(rs.getInt("used_count"));
                    v.setStatus(rs.getString("status"));
                    Timestamp ct = rs.getTimestamp("created_at"); if (ct != null) v.setCreatedAt(ct.toLocalDateTime());
                    return v;
                }
            }
        }
        return null;
    }

    public boolean updateVoucher(String id, String code, java.math.BigDecimal discountValue, String description, Date startDate, Date endDate, String status) throws SQLException {
        String sql = "UPDATE Voucher SET code = ?, discount_value = ?, description = ?, start_date = ?, end_date = ?, status = ? WHERE voucher_id = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setBigDecimal(2, discountValue);
            ps.setString(3, description);
            ps.setDate(4, startDate);
            ps.setDate(5, endDate);
            ps.setString(6, status);
            ps.setString(7, id);
            return ps.executeUpdate() > 0;
        }
    }
}
