package Controller.Manager;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import DAO.StaffDAO;
import DAO.StaffShiftDAO;

import java.io.IOException;

public class ManagerDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // calculate metrics for dashboard
        Models.User user = (Models.User) request.getSession().getAttribute("user");
        if (user != null) {
            try {
                StaffDAO staffDAO = new StaffDAO();
                StaffShiftDAO shiftDAO = new StaffShiftDAO();

                int totalStaff = staffDAO.getAllStaff().size();
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
                // non-fatal – ignore, JSP will show dashes
            }
        }
        request.getRequestDispatcher("/View/Manager/dashboard.jsp").forward(request, response);
    }
}
