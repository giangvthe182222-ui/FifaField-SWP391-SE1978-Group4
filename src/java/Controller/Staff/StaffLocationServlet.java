package Controller.Staff;

import DAO.LocationDAO;
import DAO.StaffDAO;
import Models.Location;
import Models.StaffViewModel;
import Models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.UUID;

@WebServlet(name = "StaffLocationServlet", urlPatterns = {"/staff/location"})
public class StaffLocationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=staff/location");
            return;
        }

        User user = (User) session.getAttribute("user");
        try {
            StaffDAO staffDAO = new StaffDAO();
            StaffViewModel staff = staffDAO.getStaffById(user.getUserId().toString());
            if (staff == null || staff.getLocationId() == null || staff.getLocationId().isBlank()) {
                session.setAttribute("flash_error", "No location assigned to this staff.");
                response.sendRedirect(request.getContextPath() + "/staff/dashboard");
                return;
            }

            LocationDAO locationDAO = new LocationDAO();
            Location location = locationDAO.getLocationById(UUID.fromString(staff.getLocationId()));

            request.setAttribute("staff", staff);
            request.setAttribute("location", location);
            request.getRequestDispatcher("/View/Staff/StaffLocation.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("flash_error", "Error loading staff location: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/staff/dashboard");
        }
    }
}
