package Controller.Manager;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import DAO.StaffDAO;
import DAO.StaffShiftDAO;
import DAO.ManagerDAO;
import Models.Manager;

import java.io.IOException;

public class ManagerDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Models.User user = (Models.User) request.getSession().getAttribute("user");
        if (user != null) {
            try {
                StaffDAO staffDAO = new StaffDAO();
                StaffShiftDAO shiftDAO = new StaffShiftDAO();
                ManagerDAO managerDAO = new ManagerDAO();

                Manager manager = managerDAO.getManagerById(user.getUserId());
                int totalStaff = 0;
                if (manager != null && manager.getLocationId() != null) {
                    totalStaff = staffDAO.getAllStaffByLocation(manager.getLocationId()).size();
                    request.setAttribute("locationName", manager.getLocationName());
                    request.setAttribute("locationId", manager.getLocationId().toString());
                } else {
                    totalStaff = staffDAO.getAllStaff().size();
                }

                int assignedCount = shiftDAO.countAssignedBy(user.getUserId());
                int upcoming = shiftDAO.countUpcoming(user.getUserId(),
                        java.time.LocalDate.now(), java.time.LocalDate.now().plusDays(7));
                int todayCount = shiftDAO.countAssignedOnDate(user.getUserId(), java.time.LocalDate.now());

                request.setAttribute("totalStaff", totalStaff);
                request.setAttribute("assignedCount", assignedCount);
                request.setAttribute("upcoming", upcoming);
                request.setAttribute("todayCount", todayCount);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        request.getRequestDispatcher("/View/Manager/dashboard.jsp").forward(request, response);
    }
}

