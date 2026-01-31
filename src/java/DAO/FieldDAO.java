package DAO;

import Models.Field;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import Utils.DBConnection; 

public class FieldDAO {

    private Field mapResultSet(ResultSet rs) throws SQLException {
        Field f = new Field();
        f.setFieldId(UUID.fromString(rs.getString("field_id")));
        f.setFieldName(rs.getString("field_name"));
        f.setFieldType(rs.getString("field_type"));
        f.setImageUrl(rs.getString("image_url"));
        f.setStatus(rs.getString("status"));
        f.setFieldCondition(rs.getString("condition"));

        

        f.setLocationId(UUID.fromString(rs.getString("location_id")));
        return f;
    }

    public List<Field> getAll() {
        List<Field> list = new ArrayList<>();
        String sql = "SELECT * FROM Field";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Field getById(UUID fieldId) {
        String sql = "SELECT * FROM Field WHERE field_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, fieldId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Field> getByLocation(UUID locationId) {
        List<Field> list = new ArrayList<>();
        String sql = "SELECT * FROM Field WHERE location_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, locationId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(Field f) {
        String sql = "INSERT INTO Field (field_id, field_name, field_type,image_url, status, field_condition,created_at, location_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, f.getFieldId());
            ps.setString(2, f.getFieldName());
            ps.setString(3, f.getFieldType());
            ps.setString(4, f.getImageUrl());
            ps.setString(5, f.getStatus());
            ps.setString(6, f.getFieldCondition());
           
            ps.setObject(8, f.getLocationId());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean update(Field f) {
        String sql = "UPDATE Field SET field_name = ?,field_type = ?,image_url = ?,status = ?,field_condition = ?,location_id = ? WHERE field_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, f.getFieldName());
            ps.setString(2, f.getFieldType());
            ps.setString(3, f.getImageUrl());
            ps.setString(4, f.getStatus());
            ps.setString(5, f.getFieldCondition());
            ps.setObject(6, f.getLocationId());
            ps.setObject(7, f.getFieldId());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean disable(UUID fieldId) {
        String sql = "UPDATE Field SET status = N'inactive' WHERE field_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, fieldId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
