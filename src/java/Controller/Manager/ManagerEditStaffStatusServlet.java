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

        if (staffId == null || staffId.isBlank() || newStatus == null || newStatus.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/manager/staff/list?error=invalid");
            return;
        }

        try {
            // Verify manager has permission (staff is in their location)
            ManagerDAO managerDAO = new ManagerDAO();
            Manager manager = managerDAO.getManagerById(user.getUserId());
            
            if (manager == null || manager.getLocationId() == null) {
                response.sendRedirect(request.getContextPath() + "/manager/staff/list?error=no_location");
                return;
            }

            // Verify staff belongs to manager's location
            StaffDAO staffDAO = new StaffDAO();
            UUID.fromString(staffId);
            StaffViewModel staff = staffDAO.getStaffById(staffId);
            
            if (staff == null || !staff.getLocationId().equals(manager.getLocationId())) {
                response.sendRedirect(request.getContextPath() + "/manager/staff/list?error=unauthorized");
                return;
            }

            // Update status
            boolean success = staffDAO.updateStaffStatus(staffId, newStatus);
            
            if (success) {
                response.sendRedirect(request.getContextPath() + "/manager/staff/list?success=true");
            } else {
                response.sendRedirect(request.getContextPath() + "/manager/staff/list?error=update_failed");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/manager/staff/list?error=database");
        } catch (IllegalArgumentException e) {
            response.sendRedirect(request.getContextPath() + "/manager/staff/list?error=invalid_id");
        }
    }
}
