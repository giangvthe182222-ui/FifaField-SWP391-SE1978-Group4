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

    public boolean addField(Field f) {
        String sql = "INSERT INTO Field(field_id, field_name, field_type, image_url, status, condition, location_id) VALUES(?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, f.getFieldId().toString());
            ps.setNString(2, f.getFieldName());
            ps.setNString(3, f.getFieldType());
            ps.setString(4, f.getImageUrl());
            ps.setNString(5, f.getStatus());
            ps.setNString(6, f.getFieldCondition());
            ps.setString(7, f.getLocationId().toString());

            int rows = ps.executeUpdate();
            return rows == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
