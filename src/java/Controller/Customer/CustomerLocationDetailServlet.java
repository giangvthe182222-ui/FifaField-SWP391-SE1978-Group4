package Controller.Customer;

import DAO.FieldDAO;
import DAO.LocationDAO;
import Models.Field;
import Models.Location;
import Models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@WebServlet(name = "CustomerLocationDetailServlet", urlPatterns = {"/customer/location-detail"})
public class CustomerLocationDetailServlet extends HttpServlet {

    private final LocationDAO locationDAO = new LocationDAO();
    private final FieldDAO fieldDAO = new FieldDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=customer/dashboard");
            return;
        }

        String idRaw = request.getParameter("locationId");
        if (idRaw == null || idRaw.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/customer/dashboard");
            return;
        }

        try {
            UUID locationId = UUID.fromString(idRaw);
            Location location = locationDAO.getLocationById(locationId);
            if (location == null || location.getStatus() == null || !"ACTIVE".equalsIgnoreCase(location.getStatus())) {
                response.sendRedirect(request.getContextPath() + "/customer/dashboard");
                return;
            }

            List<Field> fields = fieldDAO.getByLocation(locationId).stream()
                    .filter(f -> f.getStatus() != null
                            && ("ACTIVE".equalsIgnoreCase(f.getStatus()) || "AVAILABLE".equalsIgnoreCase(f.getStatus())))
                    .collect(Collectors.toList());

            request.setAttribute("location", location);
            request.setAttribute("fields", fields);
            request.getRequestDispatcher("/View/Customer/location-detail.jsp").forward(request, response);
        } catch (Exception ex) {
            response.sendRedirect(request.getContextPath() + "/customer/dashboard");
        }
    }
}
