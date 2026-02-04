package DAO;

import Models.Voucher;
import Utils.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

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
    
    public Voucher mapResultSet(ResultSet rs) throws SQLException {
        Voucher v = new Voucher();
        v.setVoucherId(UUID.fromString(rs.getString("voucher_id")));
        v.setCode(rs.getString("code"));
        v.setDiscountValue(rs.getBigDecimal("discount_value"));
        v.setDescription(rs.getString("description"));
        Date sd = rs.getDate("start_date");
        Date ed = rs.getDate("end_date");
        if (sd != null) v.setStartDate(sd.toLocalDate());
        if (ed != null) v.setEndDate(ed.toLocalDate());
        v.setUsedCount(rs.getInt("used_count"));
        v.setStatus(rs.getString("status"));
        return v;
    }

    public List<Voucher> getByLocation(UUID locationId) {
        List<Voucher> list = new ArrayList<>();
        String sql = "SELECT v.* FROM Voucher v JOIN Location_Voucher lv ON v.voucher_id = lv.voucher_id WHERE lv.location_id = ?";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, locationId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addVoucherToLocation(UUID locationId, String voucherName, String code, BigDecimal discountValue, LocalDate startDate, LocalDate endDate) throws SQLException {
        String insertVoucher = "INSERT INTO Voucher(voucher_id, code, discount_value, description, start_date, end_date, used_count, status) VALUES(?, ?, ?, ?, ?, ?, 0, ?)";
        String insertMapping = "INSERT INTO Location_Voucher(location_id, voucher_id) VALUES(?, ?)";

        UUID vid = UUID.randomUUID();
        Connection con = null;

        try {
            con = DBConnection.getConnection();
            if (con == null) {
                throw new SQLException("Không thể kết nối đến database");
            }
            con.setAutoCommit(false);

            try (PreparedStatement ps1 = con.prepareStatement(insertVoucher)) {
                ps1.setString(1, vid.toString());
                ps1.setNString(2, code);
                if (discountValue != null) {
                    ps1.setBigDecimal(3, discountValue);
                } else {
                    throw new SQLException("Phần trăm giảm giá không được để trống");
                }
                ps1.setNString(4, voucherName);
                ps1.setDate(5, java.sql.Date.valueOf(startDate));
                ps1.setDate(6, java.sql.Date.valueOf(endDate));
                ps1.setNString(7, "active");

                int r1 = ps1.executeUpdate();
                if (r1 != 1) {
                    con.rollback();
                    throw new SQLException("Lỗi: Không thể thêm voucher vào database");
                }
            }

            try (PreparedStatement ps2 = con.prepareStatement(insertMapping)) {
                ps2.setString(1, locationId.toString());
                ps2.setString(2, vid.toString());
                int r2 = ps2.executeUpdate();
                if (r2 != 1) {
                    con.rollback();
                    throw new SQLException("Lỗi: Không thể thêm mapping Location_Voucher");
                }
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
    }
}
