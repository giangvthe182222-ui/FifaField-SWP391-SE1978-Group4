package Controller.Manager;

import DAO.StaffShiftDAO;
import Models.StaffShift;
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
            String managerId = (String) request.getSession().getAttribute("userId");
            if (managerId == null) managerId = request.getParameter("managerId");
            
            StaffShiftDAO dao = new StaffShiftDAO();
            List<StaffShift> shifts = dao.getShiftsAssignedBy(UUID.fromString(managerId));
            request.setAttribute("staffShifts", shifts);
            request.getRequestDispatcher("/View/Manager/staff-shifts.jsp").forward(request, response);
        } catch (SQLException ex) {
            ex.printStackTrace();
            request.setAttribute("error", "Lỗi khi tải ca: " + ex.getMessage());
            request.getRequestDispatcher("/View/Manager/staff-shifts.jsp").forward(request, response);
        }
    }
}
