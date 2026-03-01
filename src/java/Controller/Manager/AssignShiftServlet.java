package Controller.Manager;

import DAO.ShiftDAO;
import DAO.StaffDAO;
import DAO.StaffShiftDAO;
import Models.StaffViewModel;
import Models.Shift;
import Models.StaffShift;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@WebServlet("/manager/assign-shift")
public class AssignShiftServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            StaffDAO staffDAO = new StaffDAO();
            ShiftDAO shiftDAO = new ShiftDAO();
            List<StaffViewModel> staff = staffDAO.getAllStaff();
            List<Shift> shifts = shiftDAO.getAllShifts();
            request.setAttribute("staffList", staff);
            request.setAttribute("shifts", shifts);
            request.getRequestDispatcher("/View/Manager/assign-shift.jsp").forward(request, response);
        } catch (SQLException ex) {
            ex.printStackTrace();
            request.setAttribute("error", "Lỗi khi tải dữ liệu: " + ex.getMessage());
            request.getRequestDispatcher("/View/Manager/assign-shift.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String staffId = request.getParameter("staffId");
            String shiftId = request.getParameter("shiftId");
            String date = request.getParameter("workingDate");
            // assignedBy: take current user id from session if available
            String assignedBy = (String) request.getSession().getAttribute("userId");
            if (assignedBy == null) assignedBy = request.getParameter("assignedBy");

            StaffShift ss = new StaffShift();
            ss.setStaffId(UUID.fromString(staffId));
            ss.setShiftId(UUID.fromString(shiftId));
            ss.setFieldId(UUID.fromString(request.getParameter("fieldId")));
            ss.setWorkingDate(LocalDate.parse(date));
            ss.setAssignedBy(UUID.fromString(assignedBy));
            ss.setStatus("assigned");

            StaffShiftDAO dao = new StaffShiftDAO();
            dao.assignShift(ss);
            response.sendRedirect(request.getContextPath() + "/manager/assign-shift");
        } catch (Exception ex) {
            ex.printStackTrace();
            request.setAttribute("error", "Lỗi khi phân ca: " + ex.getMessage());
            doGet(request, response);
        }
    }
}
