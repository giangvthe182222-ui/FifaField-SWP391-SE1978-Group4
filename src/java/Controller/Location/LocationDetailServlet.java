package Controller.Location;

import DAO.LocationDAO;
import Models.Location;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@WebServlet(name = "LocationDetailServlet", urlPatterns = {"/locations/view"})
public class LocationDetailServlet extends HttpServlet {
    private final LocationDAO locationDAO = new LocationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("location_id");
        if (id == null || id.isBlank()) {
            // try alternate param name used elsewhere
            id = request.getParameter("locationId");
        }

        if (id == null || id.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing location_id");
            return;
        }

        try {
            UUID uuid = UUID.fromString(id);
            Location loc = locationDAO.getLocationById(uuid);
            if (loc == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Location not found");
                return;
            }

            request.setAttribute("location", loc);
            request.getRequestDispatcher("/View/Location/location-detail.jsp").forward(request, response);

        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid UUID format");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }
}
