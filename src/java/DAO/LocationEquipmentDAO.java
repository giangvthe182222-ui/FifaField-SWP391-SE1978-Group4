package DAO;

import Models.LocationEquipmentViewModel;
import Utils.DBConnection;
import java.math.BigDecimal;
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

        String sql = "SELECT le.location_id, e.equipment_id,e.name,e.equipment_type,e.image_url,le.quantity,e.rental_price,e.damage_fee,le.status FROM Location_Equipment le JOIN Equipment e ON e.equipment_id = le.equipment_id WHERE le.location_id = ? ORDER BY e.name";

        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, locationId.toString());

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LocationEquipmentViewModel vm = new LocationEquipmentViewModel();
                vm.setLocationId(UUID.fromString(rs.getString("location_id")));
                vm.setEquipmentId(UUID.fromString(rs.getString("equipment_id")));
                vm.setName(rs.getString("name"));
                vm.setEquipmentType(rs.getString("equipment_type"));
                vm.setImageUrl(rs.getString("image_url"));
                vm.setRentalPrice(rs.getBigDecimal("rental_price"));
                vm.setDamageFee(rs.getBigDecimal("damage_fee"));
                vm.setQuantity(rs.getInt("quantity"));
                vm.setStatus(rs.getString("status"));

                list.add(vm);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public List<LocationEquipmentViewModel> getFiltered(
            UUID locationId,
            String search,
            String type,
            String status,
            String sort,
            int page,
            int pageSize
    ) {
        List<LocationEquipmentViewModel> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT le.location_id, e.equipment_id, e.name, e.equipment_type, e.image_url, le.quantity, e.rental_price, e.damage_fee, le.status FROM Location_Equipment le JOIN Equipment e ON e.equipment_id = le.equipment_id WHERE le.location_id = ?");

        List<Object> params = new ArrayList<>();
        params.add(locationId.toString());

        if (search != null && !search.isBlank()) {
            sql.append(" AND e.name LIKE ? ");
            params.add("%" + search + "%");
        }

        if (type != null && !type.isBlank()) {
            sql.append(" AND e.equipment_type = ? ");
            params.add(type);
        }

        if (status != null && !status.isBlank()) {
            sql.append(" AND le.status = ? ");
            params.add(status);
        }

        if ("asc".equals(sort)) {
            sql.append(" ORDER BY e.rental_price ASC ");
        } else if ("desc".equals(sort)) {
            sql.append(" ORDER BY e.rental_price DESC ");
        } else {
            sql.append(" ORDER BY e.name ");
        }

        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY ");

        int offset = (page - 1) * pageSize;
        params.add(offset);
        params.add(pageSize);

        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LocationEquipmentViewModel vm = new LocationEquipmentViewModel();
                vm.setLocationId(UUID.fromString(rs.getString("location_id")));
                vm.setEquipmentId(UUID.fromString(rs.getString("equipment_id")));
                vm.setName(rs.getString("name"));
                vm.setEquipmentType(rs.getString("equipment_type"));
                vm.setImageUrl(rs.getString("image_url"));
                vm.setRentalPrice(rs.getBigDecimal("rental_price"));
                vm.setDamageFee(rs.getBigDecimal("damage_fee"));
                vm.setQuantity(rs.getInt("quantity"));
                vm.setStatus(rs.getString("status"));
                list.add(vm);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public int countFiltered(
            UUID locationId,
            String search,
            String type,
            String status
    ) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM Location_Equipment le JOIN Equipment e ON e.equipment_id = le.equipment_id WHERE le.location_id = ?");

        List<Object> params = new ArrayList<>();
        params.add(locationId.toString());

        if (search != null && !search.isBlank()) {
            sql.append(" AND e.name LIKE ? ");
            params.add("%" + search + "%");
        }

        if (type != null && !type.isBlank()) {
            sql.append(" AND e.equipment_type = ? ");
            params.add(type);
        }

        if (status != null && !status.isBlank()) {
            sql.append(" AND le.status = ? ");
            params.add(status);
        }

        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean updateStatusAndQuantity(
            UUID locationId,
            UUID equipmentId,
            String status,
            int quantity
    ) {
        String sql = "UPDATE le SET le.status = ?, le.quantity = ? FROM Location_Equipment le JOIN Equipment e ON e.equipment_id = le.equipment_id WHERE le.location_id = ? AND le.equipment_id = ?";

        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, quantity);
            ps.setString(3, locationId.toString());
            ps.setString(4, equipmentId.toString());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public LocationEquipmentViewModel getOne(
            UUID locationId,
            UUID equipmentId
    ) {
        String sql = "SELECT le.location_id, e.equipment_id,e.name,e.equipment_type,e.image_url,le.quantity,e.rental_price, e.damage_fee, le.status FROM Location_Equipment le JOIN Equipment e ON e.equipment_id = le.equipment_id WHERE le.location_id = ? AND le.equipment_id = ?";

        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, locationId.toString());
            ps.setString(2, equipmentId.toString());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                LocationEquipmentViewModel vm = new LocationEquipmentViewModel();
                vm.setLocationId(UUID.fromString(rs.getString("location_id")));
                vm.setEquipmentId(UUID.fromString(rs.getString("equipment_id")));
                vm.setName(rs.getString("name"));
                vm.setEquipmentType(rs.getString("equipment_type"));
                vm.setImageUrl(rs.getString("image_url"));
                vm.setRentalPrice(rs.getBigDecimal("rental_price"));
                vm.setDamageFee(rs.getBigDecimal("damage_fee"));
                vm.setQuantity(rs.getInt("quantity"));
                vm.setStatus(rs.getString("status"));
                return vm;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateStatus(UUID locationId, UUID equipmentId) {
    String sql = "UPDATE Location_Equipment SET status = CASE WHEN quantity <= 0 THEN 'unavailable' ELSE 'available' END WHERE location_id = ? AND equipment_id = ?";

    try (Connection c = db.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {

        ps.setString(1, locationId.toString());
        ps.setString(2, equipmentId.toString());

        return ps.executeUpdate() > 0;

    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}

}
