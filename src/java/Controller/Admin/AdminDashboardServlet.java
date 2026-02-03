package Controller.Admin;

import DAO.FieldDAO;
import DAO.LocationDAO;
import DAO.EquipmentDAO;
import DAO.AuthDAO;
import Models.Field;
import Models.Location;
import Models.Equipment;
import Models.User;
import Utils.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin-dashboard")
public class AdminDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (user.getRole() == null || !"ADMIN".equalsIgnoreCase(user.getRole().getRoleName())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. Admin role required.");
            return;
        }

        try {
            // Get statistics
            LocationDAO locationDAO = new LocationDAO();
            List<Location> locations = locationDAO.getAllLocations();
            int totalLocations = locations.size();

            FieldDAO fieldDAO = new FieldDAO();
            int totalFields = 0;
            for (Location loc : locations) {
                List<Field> fields = fieldDAO.getByLocation(loc.getLocationId());
                totalFields += fields.size();
            }
            
            DBConnection db = new DBConnection();
            EquipmentDAO equipmentDAO = new EquipmentDAO(db);
            int totalEquipment = equipmentDAO.getAll().size();

            
            int totalStaff = 28; 
            int todayBookings = 15; 

            
            request.setAttribute("totalLocations", totalLocations);
            request.setAttribute("totalFields", totalFields);
            request.setAttribute("totalEquipment", totalEquipment);
            request.setAttribute("totalStaff", totalStaff);
            request.setAttribute("todayBookings", todayBookings);
            request.setAttribute("adminName", user.getFullName());

            request.getRequestDispatcher("/View/Admin/AdminDashboard.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
        }
    }
}