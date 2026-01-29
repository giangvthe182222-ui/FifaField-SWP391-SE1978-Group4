package DAO;

import Models.Equipment;
import Utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EquipmentDAO {

    private final DBConnection db;

    public EquipmentDAO(DBConnection db) {
        this.db = db;
    }

    // ================= ADD =================
    public boolean addEquipment(Equipment e) {
        String sql = "INSERT INTO Equipment (equipment_id, name, equipment_type, image_url, rental_price, damage_fee, status, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, e.getEquipmentId());
            ps.setString(2, e.getName());
            ps.setString(3, e.getEquipmentType());
            ps.setString(4, e.getImageUrl());
            ps.setBigDecimal(5, e.getRentalPrice());
            ps.setBigDecimal(6, e.getDamageFee());
            ps.setString(7, e.getStatus());
            ps.setString(8, e.getDescription());

            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    // ================= GET BY ID =================
    public Equipment getById(UUID id) {
        String sql = "SELECT * FROM Equipment WHERE equipment_id = ?";

        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return map(rs);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // ================= UPDATE =================
    public boolean update(Equipment e) {
        String sql = "UPDATE Equipment SET name = ?, equipment_type = ?, image_url = ?, rental_price = ?, damage_fee = ?, status = ?, description = ? WHERE equipment_id = ?";

        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, e.getName());
            ps.setString(2, e.getEquipmentType());
            ps.setString(3, e.getImageUrl());
            ps.setBigDecimal(4, e.getRentalPrice());
            ps.setBigDecimal(5, e.getDamageFee());
            ps.setString(6, e.getStatus());
            ps.setString(7, e.getDescription());
            ps.setObject(8, e.getEquipmentId());

            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    // ================= UPDATE STATUS =================
    public boolean updateStatus(UUID id, String status) {
        String sql = "UPDATE Equipment SET status = ? WHERE equipment_id = ?";

        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setObject(2, id);

            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    // ================= GET ALL =================
    public List<Equipment> getAll() {
        List<Equipment> list = new ArrayList<>();
        String sql = "SELECT * FROM Equipment ORDER BY created_at DESC";

        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    // ================= GET TYPES =================
    public List<String> getAllTypes() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT equipment_type FROM Equipment";

        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(rs.getString("equipment_type"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    // ================= FILTER =================
    public List<Equipment> filter(String keyword, String status, String type) {
        List<Equipment> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM Equipment WHERE 1=1 ");

        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND name LIKE ? ");
        }
        if (status != null && !status.isBlank()) {
            sql.append("AND status = ? ");
        }
        if (type != null && !type.isBlank()) {
            sql.append("AND equipment_type = ? ");
        }

        sql.append("ORDER BY created_at DESC");

        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {

            int i = 1;
            if (keyword != null && !keyword.isBlank()) {
                ps.setString(i++, "%" + keyword + "%");
            }
            if (status != null && !status.isBlank()) {
                ps.setString(i++, status);
            }
            if (type != null && !type.isBlank()) {
                ps.setString(i++, type);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    // ================= MAP =================
    private Equipment map(ResultSet rs) throws SQLException {
        Equipment e = new Equipment();
        e.setEquipmentId(rs.getObject("equipment_id", UUID.class));
        e.setName(rs.getString("name"));
        e.setEquipmentType(rs.getString("equipment_type"));
        e.setImageUrl(rs.getString("image_url"));
        e.setRentalPrice(rs.getBigDecimal("rental_price"));
        e.setDamageFee(rs.getBigDecimal("damage_fee"));
        e.setStatus(rs.getString("status"));
        e.setDescription(rs.getString("description"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            e.setCreatedAt(ts.toLocalDateTime());
        }

        return e;
    }
}
