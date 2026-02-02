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
            "(location_id, location_name, address, phone_number, image_url, status) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

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

            int rows = ps.executeUpdate();

            System.out.println("ROWS INSERTED = " + rows);
            System.out.println("=================");

            return rows == 1;
        }
    }
    public List<Location> getAllLocations() throws SQLException {
        List<Location> locations = new ArrayList<>();

        String sql = 
            "SELECT location_id, location_name, address, phone_number, image_url, status " +
            "FROM dbo.Location " +
            "ORDER BY location_name ASC";

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
                // nếu model có managerId thì thêm dòng này:
                // loc.setManagerId(rs.getString("manager_id") != null ? UUID.fromString(rs.getString("manager_id")) : null);

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
}
