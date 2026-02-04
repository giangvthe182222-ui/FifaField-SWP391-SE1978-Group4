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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

public class VoucherDAO {

    private Voucher mapResultSet(ResultSet rs) throws SQLException {
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

    public boolean addVoucherToLocation(UUID locationId, String code, BigDecimal discountValue, String description, LocalDate startDate, LocalDate endDate) {
        String insertVoucher = "INSERT INTO Voucher(voucher_id, code, discount_value, description, start_date, end_date, used_count, status) VALUES(?, ?, ?, ?, ?, ?, 0, ?)";
        String insertMapping = "INSERT INTO Location_Voucher(location_id, voucher_id) VALUES(?, ?)";

        UUID vid = UUID.randomUUID();

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement ps1 = con.prepareStatement(insertVoucher);
                 PreparedStatement ps2 = con.prepareStatement(insertMapping)) {

                ps1.setString(1, vid.toString());
                ps1.setNString(2, code);
                if (discountValue != null) ps1.setBigDecimal(3, discountValue); else ps1.setNull(3, Types.DECIMAL);
                ps1.setNString(4, description != null ? description : "");
                if (startDate != null) ps1.setDate(5, Date.valueOf(startDate)); else ps1.setNull(5, Types.DATE);
                if (endDate != null) ps1.setDate(6, Date.valueOf(endDate)); else ps1.setNull(6, Types.DATE);
                ps1.setNString(7, "active");

                int r1 = ps1.executeUpdate();

                ps2.setString(1, locationId.toString());
                ps2.setString(2, vid.toString());
                int r2 = ps2.executeUpdate();

                if (r1 == 1 && r2 == 1) {
                    con.commit();
                    return true;
                } else {
                    con.rollback();
                    return false;
                }
            } catch (Exception ex) {
                con.rollback();
                ex.printStackTrace();
                return false;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
