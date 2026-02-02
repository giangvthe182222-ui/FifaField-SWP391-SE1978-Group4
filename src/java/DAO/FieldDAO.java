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

}
