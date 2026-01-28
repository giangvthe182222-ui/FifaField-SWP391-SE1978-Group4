package DAO;

import Models.Equipment;
import Utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipmentDAO {

    private final DBConnection db;

    public EquipmentDAO(DBConnection db) {
        this.db = db;
    }

    // ADD
    public boolean addEquipment(Equipment e) {
        String sql = " INSERT INTO Equipment (equipment_id, name, equipment_type, image_url, rental_price, damage_fee, status, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, e.getId());
            ps.setString(2, e.getName());
            ps.setString(3, e.getEquipmentType());
            ps.setString(4, e.getImageUrl());
            ps.setFloat(5, e.getRentalPrice());
            ps.setFloat(6, e.getDamageFee());
            ps.setString(7, e.getStatus()); // AVAILABLE / UNAVAILABLE
            ps.setString(8, e.getDescription());

            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    // GET DETAIL
    public Equipment getEquipmentById(String id) {
        String sql = "SELECT * FROM Equipment WHERE equipment_id = ?";

        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return map(rs);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // UPDATE FULL INFO
    public boolean updateEquipment(Equipment e) {
        String sql = "UPDATE Equipment SET name = ?, equipment_type = ?, image_url = ?, rental_price = ?,damage_fee = ?, status = ?, description = ? WHERE equipment_id = ?";

        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, e.getName());
            ps.setString(2, e.getEquipmentType());
            ps.setString(3, e.getImageUrl());
            ps.setFloat(4, e.getRentalPrice());
            ps.setFloat(5, e.getDamageFee());
            ps.setString(6, e.getStatus());
            ps.setString(7, e.getDescription());
            ps.setString(8, e.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    // GET ALL (FOR LIST)
    public List<Equipment> getAll() {
        List<Equipment> list = new ArrayList<>();
        String sql = "SELECT * FROM Equipment ORDER BY name";

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
    public void updateStatus(String id, String status) {
    String sql = "UPDATE Equipment SET status = ? WHERE equipment_id = ?";

    try (Connection c = db.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {

        ps.setString(1, status);
        ps.setString(2, id);
        ps.executeUpdate();

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    
    // MAP RESULTSET → OBJECT
    private Equipment map(ResultSet rs) throws SQLException {
        return new Equipment(
                rs.getString("equipment_id"),
                rs.getString("name"),
                rs.getString("equipment_type"),
                rs.getString("image_url"),
                rs.getFloat("rental_price"),
                rs.getFloat("damage_fee"),
                rs.getString("status"),
                rs.getString("description")
        );
    }
    public List<String> getAllTypes() {
    List<String> list = new ArrayList<>();
    String sql = "SELECT DISTINCT equipment_type FROM Equipment";

    try (Connection c = db.getConnection();
         PreparedStatement ps = c.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            list.add(rs.getString("equipment_type"));
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return list;
}
public List<Equipment> filter(String keyword, String status, String type) {
    List<Equipment> list = new ArrayList<>();

    StringBuilder sql = new StringBuilder("SELECT * FROM Equipment WHERE 1=1 ");

    if (keyword != null && !keyword.isEmpty()) {
        sql.append("AND name LIKE ? ");
    }
    if (status != null && !status.isEmpty()) {
        sql.append("AND status = ? ");
    }
    if (type != null && !type.isEmpty()) {
        sql.append("AND equipment_type = ? ");
    }

    try (Connection c = db.getConnection();
         PreparedStatement ps = c.prepareStatement(sql.toString())) {

        int i = 1;
        if (keyword != null && !keyword.isEmpty()) {
            ps.setString(i++, "%" + keyword + "%");
        }
        if (status != null && !status.isEmpty()) {
            ps.setString(i++, status);
        }
        if (type != null && !type.isEmpty()) {
            ps.setString(i++, type);
        }

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Equipment e = new Equipment();
            e.setId(rs.getString("equipment_id"));
            e.setName(rs.getString("name"));
            e.setEquipment_type(rs.getString("equipment_type"));
            e.setImage_url(rs.getString("image_url"));
            e.setRental_price(rs.getFloat("rental_price"));
            e.setDamage_fee(rs.getFloat("damage_fee"));
            e.setStatus(rs.getString("status"));
            e.setDescription(rs.getString("description"));
            list.add(e);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
    return list;
}

}
