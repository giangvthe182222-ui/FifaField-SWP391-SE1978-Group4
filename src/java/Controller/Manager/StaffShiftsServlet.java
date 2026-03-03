package Controller.Manager;

import DAO.StaffShiftDAO;
import Models.StaffShiftViewModel;
import Models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class StaffShiftsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // derive manager id from logged-in user object rather than session attribute
            User sessionUser = (User) request.getSession().getAttribute("user");
            String managerId = null;
            if (sessionUser != null) {
                managerId = sessionUser.getUserId().toString();
            }
            // fall back to explicit parameter if supplied (e.g. for testing)
            if (managerId == null) {
                managerId = request.getParameter("managerId");
            }

            // Check if managerId is null
            if (managerId == null || managerId.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            
            StaffShiftDAO dao = new StaffShiftDAO();
            // read optional filters
            String staffName = request.getParameter("staffName");
            String startStr = request.getParameter("startDate");
            String endStr = request.getParameter("endDate");
            java.time.LocalDate startDate = null, endDate = null;
            try {
                if (startStr != null && !startStr.isEmpty()) {
                    startDate = java.time.LocalDate.parse(startStr);
                }
                if (endStr != null && !endStr.isEmpty()) {
                    endDate = java.time.LocalDate.parse(endStr);
                }
            } catch (java.time.format.DateTimeParseException e) {
                // ignore invalid date format; controller may set an error message later
            }

            List<StaffShiftViewModel> shifts = dao.searchShiftsAssignedBy(
                    UUID.fromString(managerId), staffName, startDate, endDate);
            request.setAttribute("staffShifts", shifts);
            request.setAttribute("staffNameFilter", staffName);
            request.setAttribute("startDateFilter", startStr);
            request.setAttribute("endDateFilter", endStr);
            request.getRequestDispatcher("/View/Manager/staff-shifts.jsp").forward(request, response);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            request.setAttribute("error", "ID quản lý không hợp lệ");
            request.getRequestDispatcher("/View/Manager/staff-shifts.jsp").forward(request, response);
        } catch (SQLException ex) {
            ex.printStackTrace();
            request.setAttribute("error", "Lỗi khi tải ca: " + ex.getMessage());
            request.getRequestDispatcher("/View/Manager/staff-shifts.jsp").forward(request, response);
        }
    }
}
