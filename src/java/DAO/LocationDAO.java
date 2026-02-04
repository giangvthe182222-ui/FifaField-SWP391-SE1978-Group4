package DAO;

import Models.Location;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LocationDAO {

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ KHÔNG TÌM THẤY JDBC DRIVER");
        }

        String url =
            "jdbc:sqlserver://localhost:1433;"
          + "databaseName=FifaFieldDB;"
          + "encrypt=true;"
          + "trustServerCertificate=true;";

        return DriverManager.getConnection(url, "sa", "123");
    }

    // =========================
    // ADD LOCATION (DEBUG FULL)
    // =========================
    public boolean addLocation(Location loc) throws SQLException {

        String sql =
            "INSERT INTO dbo.Location " +
            "(location_id, location_name, address, phone_number, image_url, status, manager_id) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // ===== DEBUG CONNECTION =====
            System.out.println("=== DAO DEBUG ===");
            System.out.println("DB URL      = " + conn.getMetaData().getURL());
            System.out.println("DB NAME     = " + conn.getCatalog());
            System.out.println("AUTOCOMMIT  = " + conn.getAutoCommit());

            // ===== SET PARAM =====
            ps.setString(1, loc.getLocationId().toString());
            ps.setNString(2, loc.getLocationName());
            ps.setNString(3, loc.getAddress());
            ps.setString(4, loc.getPhoneNumber());
            ps.setString(5, loc.getImageUrl());
            ps.setNString(6, loc.getStatus());
            if (loc.getManagerId() != null) ps.setString(7, loc.getManagerId().toString()); else ps.setNull(7, Types.VARCHAR);

            int rows = ps.executeUpdate();

            System.out.println("ROWS INSERTED = " + rows);
            System.out.println("=================");

            return rows == 1;
        }
    }
    public List<Location> getAllLocations() throws SQLException {
        List<Location> locations = new ArrayList<>();

        String sql = 
            "SELECT l.location_id, l.location_name, l.address, l.phone_number, l.image_url, l.status, l.manager_id, u.full_name AS manager_name " +
            "FROM dbo.Location l " +
            "LEFT JOIN Users u ON l.manager_id = u.user_id " +
            "ORDER BY l.location_name ASC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("=== DAO DEBUG - GET ALL LOCATIONS ===");
            System.out.println("DB URL = " + conn.getMetaData().getURL());
            System.out.println("Query executed: " + sql);

            int count = 0;
            while (rs.next()) {
                Location loc = new Location();

                loc.setLocationId(UUID.fromString(rs.getString("location_id")));
                loc.setLocationName(rs.getNString("location_name"));
                loc.setAddress(rs.getNString("address"));
                loc.setPhoneNumber(rs.getString("phone_number"));
                loc.setImageUrl(rs.getString("image_url"));
                loc.setStatus(rs.getNString("status"));
                String mgr = rs.getString("manager_id");
                if (mgr != null && !mgr.isBlank()) loc.setManagerId(UUID.fromString(mgr));
                loc.setManagerName(rs.getString("manager_name"));

                locations.add(loc);
                count++;
            }

            System.out.println("Tổng số cụm sân lấy được: " + count);
            System.out.println("=================");

            return locations;
        } catch (SQLException e) {
            System.err.println("LỖI khi lấy danh sách Location: " + e.getMessage());
            throw e; // ném tiếp để servlet xử lý
        }
    }

    public Location getLocationById(UUID id) throws SQLException {
        String sql = "SELECT l.location_id, l.location_name, l.address, l.phone_number, l.image_url, l.status, l.manager_id, u.full_name AS manager_name " +
                 "FROM dbo.Location l LEFT JOIN Users u ON l.manager_id = u.user_id WHERE l.location_id = ?";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Location loc = new Location();
                    loc.setLocationId(UUID.fromString(rs.getString("location_id")));
                    loc.setLocationName(rs.getNString("location_name"));
                    loc.setAddress(rs.getNString("address"));
                    loc.setPhoneNumber(rs.getString("phone_number"));
                    loc.setImageUrl(rs.getString("image_url"));
                    loc.setStatus(rs.getNString("status"));
                    String mgr = rs.getString("manager_id");
                    if (mgr != null && !mgr.isBlank()) loc.setManagerId(UUID.fromString(mgr));
                    loc.setManagerName(rs.getString("manager_name"));
                    return loc;
                }
                return null;
            }
        }
    }

    public boolean updateLocation(Location loc) throws SQLException {
        String sql = "UPDATE dbo.Location SET location_name = ?, address = ?, phone_number = ?, image_url = ?, status = ?, manager_id = ? " +
                 "WHERE location_id = ?";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, loc.getLocationName());
            ps.setNString(2, loc.getAddress());
            ps.setString(3, loc.getPhoneNumber());
            ps.setString(4, loc.getImageUrl());
            ps.setNString(5, loc.getStatus());
            if (loc.getManagerId() != null) ps.setString(6, loc.getManagerId().toString()); else ps.setNull(6, Types.VARCHAR);
            ps.setString(7, loc.getLocationId().toString());

            int rows = ps.executeUpdate();
            return rows == 1;
        }
    }
}
