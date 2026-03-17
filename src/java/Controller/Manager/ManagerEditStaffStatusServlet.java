package Controller.Manager;

import DAO.ManagerDAO;
import DAO.StaffDAO;
import Models.Manager;
import Models.StaffViewModel;
import Models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

public class ManagerEditStaffStatusServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        User user = (User) session.getAttribute("user");
        String staffId = request.getParameter("staffId");
        String newStatus = request.getParameter("status");
        String returnUrl = request.getParameter("returnUrl");

        String fallbackUrl = request.getContextPath() + "/manager/staff/list";
        String redirectTarget = fallbackUrl;
        if (returnUrl != null && !returnUrl.isBlank() && returnUrl.startsWith(request.getContextPath() + "/manager/")) {
            redirectTarget = returnUrl;
        }

        if (staffId == null || staffId.isBlank() || newStatus == null || newStatus.isBlank()) {
            response.sendRedirect(fallbackUrl + "?error=invalid");
            return;
        }

        if (!"active".equalsIgnoreCase(newStatus) && !"deactivated".equalsIgnoreCase(newStatus)) {
            response.sendRedirect(fallbackUrl + "?error=invalid");
            return;
        }

        try {
            // Verify manager has permission (staff is in their location)
            ManagerDAO managerDAO = new ManagerDAO();
            Manager manager = managerDAO.getManagerById(user.getUserId());
            
            if (manager == null || manager.getLocationId() == null) {
                response.sendRedirect(fallbackUrl + "?error=no_location");
                return;
            }

            // Verify staff belongs to manager's location
            StaffDAO staffDAO = new StaffDAO();
            UUID.fromString(staffId);
            StaffViewModel staff = staffDAO.getStaffById(staffId);
            
            if (staff == null || staff.getLocationId() == null || !staff.getLocationId().equals(manager.getLocationId().toString())) {
                response.sendRedirect(fallbackUrl + "?error=unauthorized");
                return;
            }

            // Update status
            boolean success = staffDAO.updateStaffStatus(staffId, newStatus);
            
            if (success) {
                if (redirectTarget.equals(fallbackUrl)) {
                    response.sendRedirect(fallbackUrl + "?success=true");
                } else {
                    String joiner = redirectTarget.contains("?") ? "&" : "?";
                    response.sendRedirect(redirectTarget + joiner + "success=true");
                }
            } else {
                response.sendRedirect(fallbackUrl + "?error=update_failed");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect(fallbackUrl + "?error=database");
        } catch (IllegalArgumentException e) {
            response.sendRedirect(fallbackUrl + "?error=invalid_id");
        }
    }
}
