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

    public boolean addEquipment(Equipment e) {
        String sql = "INSERT INTO Equipment (equipment_id, name, equipment_type, image_url, rental_price, damage_fee, status, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

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

    public Equipment getById(UUID id) {
        String sql = "SELECT * FROM Equipment WHERE equipment_id = ?";

        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

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
    
    public boolean update(Equipment e) {
        String sql = "UPDATE Equipment SET name = ?, equipment_type = ?, image_url = ?, rental_price = ?, damage_fee = ?, status = ?, description = ? WHERE equipment_id = ?";

        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

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

    public boolean updateStatus(UUID id, String status) {
        String sql = "UPDATE Equipment SET status = ? WHERE equipment_id = ?";

        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setObject(2, id);

            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public List<Equipment> getAll() {
        List<Equipment> list = new ArrayList<>();
        String sql = "SELECT * FROM Equipment";

        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public List<String> getAllTypes() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT equipment_type FROM Equipment";

        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(rs.getString("equipment_type"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

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

        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {

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

    private Equipment map(ResultSet rs) throws SQLException {
        Equipment e = new Equipment();
        e.setEquipmentId(UUID.fromString(rs.getString("equipment_id")));
        e.setName(rs.getString("name"));
        e.setEquipmentType(rs.getString("equipment_type"));
        e.setImageUrl(rs.getString("image_url"));
        e.setRentalPrice(rs.getBigDecimal("rental_price"));
        e.setDamageFee(rs.getBigDecimal("damage_fee"));
        e.setStatus(rs.getString("status"));
        e.setDescription(rs.getString("description"));

        return e;
    }

    public List<Equipment> filter(
            String search,
            String status,
            String type,
            String sort,
            int page,
            int pageSize
    ) {
        List<Equipment> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM Equipment WHERE 1=1 "
        );

        if (search != null) {
            sql.append("AND name LIKE ? ");
        }
        if (status != null) {
            sql.append("AND status = ? ");
        }
        if (type != null) {
            sql.append("AND equipment_type = ? ");
        }

        if ("asc".equals(sort)) {
            sql.append("ORDER BY rental_price ASC ");
        } else if ("desc".equals(sort)) {
            sql.append("ORDER BY rental_price DESC ");
        } else {
            sql.append("ORDER BY name ");
        }

        sql.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {

            int i = 1;
            if (search != null) {
                ps.setString(i++, "%" + search + "%");
            }
            if (status != null) {
                ps.setString(i++, status);
            }
            if (type != null) {
                ps.setString(i++, type);
            }

            ps.setInt(i++, (page - 1) * pageSize);
            ps.setInt(i, pageSize);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public int count(String search, String status, String type) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM Equipment WHERE 1=1 "
        );

        if (search != null) {
            sql.append("AND name LIKE ? ");
        }
        if (status != null) {
            sql.append("AND status = ? ");
        }
        if (type != null) {
            sql.append("AND equipment_type = ? ");
        }

        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {

            int i = 1;
            if (search != null) {
                ps.setString(i++, "%" + search + "%");
            }
            if (status != null) {
                ps.setString(i++, status);
            }
            if (type != null) {
                ps.setString(i++, type);
            }

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    

}
