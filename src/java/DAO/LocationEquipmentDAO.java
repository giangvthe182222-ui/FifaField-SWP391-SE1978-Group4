package DAO;

import Models.LocationEquipmentViewModel;
import Utils.DBConnection;

import java.sql.*;
import java.util.*;
import java.util.UUID;

public class LocationEquipmentDAO {

    private final DBConnection db;

    public LocationEquipmentDAO(DBConnection db) {
        this.db = db;
    }

    public List<LocationEquipmentViewModel> getByLocation(UUID locationId) {
        List<LocationEquipmentViewModel> list = new ArrayList<>();

        String sql = "SELECT e.equipment_id,e.name,e.equipment_type,e.image_url,le.quantity,le.status FROM Location_Equipment le JOIN Equipment e ON e.equipment_id = le.equipment_id WHERE le.location_id = ? ORDER BY e.name";

        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, locationId.toString());

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LocationEquipmentViewModel vm = new LocationEquipmentViewModel();
                vm.setEquipmentId(UUID.fromString(rs.getString("equipment_id")));

                vm.setName(rs.getString("name"));
                vm.setEquipmentType(rs.getString("equipment_type"));
                vm.setImageUrl(rs.getString("image_url"));
                vm.setQuantity(rs.getInt("quantity"));
                vm.setStatus(rs.getString("status"));

                list.add(vm);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }
}
