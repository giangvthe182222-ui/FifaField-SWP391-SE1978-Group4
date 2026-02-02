package Controller.Location;

import DAO.LocationDAO;
import Models.Location;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

@WebServlet(name = "LocationAddServlet", urlPatterns = {"/locations/add"})
public class LocationAddServlet extends HttpServlet {

    private final LocationDAO locationDAO = new LocationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("/View/Location/location-add.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String name = request.getParameter("locationName");
        String address = request.getParameter("address");
        String phone = request.getParameter("phoneNumber");
        String imageUrl = request.getParameter("imageUrl");
        String status = request.getParameter("status");

        // ===== FALLBACK =====
        if (imageUrl == null || imageUrl.isBlank()) {
            imageUrl = "default_cluster.jpg";
        }
        if (status == null || status.isBlank()) {
            status = "ACTIVE";
        }

        // ===== BASIC VALIDATE =====
        if (name == null || name.trim().isEmpty()) {
            request.setAttribute("error", "❌ Tên cụm sân không được để trống");
            request.getRequestDispatcher("/View/Location/location-add.jsp")
                    .forward(request, response);
            return;
        }

        // ===== BUILD MODEL =====
        Location loc = new Location();
        loc.setLocationId(UUID.randomUUID());
        loc.setLocationName(name.trim());
        loc.setAddress(address);
        loc.setPhoneNumber(phone);
        loc.setImageUrl(imageUrl);
        loc.setStatus(status);
        loc.setManagerId(null);

        try {
            System.out.println("=== SERVLET ADD LOCATION ===");
            System.out.println("Name   = " + name);
            System.out.println("Addr   = " + address);
            System.out.println("Phone  = " + phone);
            System.out.println("Image  = " + imageUrl);
            System.out.println("Status = " + status);

            boolean success = locationDAO.addLocation(loc);

            System.out.println("INSERT RESULT = " + success);

            if (success) {
                response.sendRedirect(request.getContextPath() + "/locations");
            } else {
                request.setAttribute(
                    "error",
                    "❌ INSERT KHÔNG THÀNH CÔNG (ROWS = 0). Có thể trigger rollback hoặc schema sai."
                );
                request.getRequestDispatcher("/View/Location/location-add.jsp")
                        .forward(request, response);
            }

        } catch (SQLException e) {

            String debugMessage =
                "❌ LỖI SQL<br/>" +
                "Message: " + e.getMessage() + "<br/>" +
                "SQLState: " + e.getSQLState() + "<br/>" +
                "ErrorCode: " + e.getErrorCode();

            e.printStackTrace();

            request.setAttribute("error", debugMessage);
            request.getRequestDispatcher("/View/Location/location-add.jsp")
                    .forward(request, response);

        } catch (Exception e) {

            String debugMessage =
                "❌ EXCEPTION KHÁC<br/>" +
                "Message: " + e.getMessage();

            e.printStackTrace();

            request.setAttribute("error", debugMessage);
            request.getRequestDispatcher("/View/Location/location-add.jsp")
                    .forward(request, response);
        }
    }
}
