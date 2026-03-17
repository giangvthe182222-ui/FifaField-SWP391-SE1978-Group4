package Controller.Manager;

import DAO.ManagerDAO;
import DAO.StaffDAO;
import DAO.StaffShiftDAO;
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

public class ManagerStaffDetailServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("user");
        String staffId = request.getParameter("id");
        if (staffId == null || staffId.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/manager/staff/list?error=invalid_id");
            return;
        }

        try {
            ManagerDAO managerDAO = new ManagerDAO();
            Manager manager = managerDAO.getManagerById(user.getUserId());
            if (manager == null || manager.getLocationId() == null) {
                response.sendRedirect(request.getContextPath() + "/manager/staff/list?error=no_location");
                return;
            }

            StaffDAO staffDAO = new StaffDAO();
            StaffViewModel staff = staffDAO.getStaffById(staffId);
            if (staff == null) {
                response.sendRedirect(request.getContextPath() + "/manager/staff/list?error=not_found");
                return;
            }

            if (staff.getLocationId() == null || !staff.getLocationId().equals(manager.getLocationId().toString())) {
                response.sendRedirect(request.getContextPath() + "/manager/staff/list?error=unauthorized");
                return;
            }

            StaffShiftDAO staffShiftDAO = new StaffShiftDAO();
            int workedShiftCount = staffShiftDAO.countWorkedShifts(UUID.fromString(staff.getUserId()));

            request.setAttribute("staff", staff);
            request.setAttribute("workedShiftCount", workedShiftCount);
            request.getRequestDispatcher("/View/Manager/manager-staff-detail.jsp").forward(request, response);
        } catch (IllegalArgumentException e) {
            response.sendRedirect(request.getContextPath() + "/manager/staff/list?error=invalid_id");
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi khi tải chi tiết nhân viên: " + e.getMessage());
            request.getRequestDispatcher("/View/Manager/manager-staff-list.jsp").forward(request, response);
        }
    }
}
