package Controller.Manager;

import DAO.ShiftDAO;
import DAO.StaffDAO;
import DAO.StaffShiftDAO;
import Models.StaffViewModel;
import Models.Shift;
import Models.StaffShift;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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
        // Server-side validation
        String staffId = request.getParameter("staffId");
        String shiftId = request.getParameter("shiftId");
        String date = request.getParameter("workingDate");
        String fieldId = request.getParameter("fieldId");

        if (staffId == null || staffId.isEmpty() || shiftId == null || shiftId.isEmpty() || date == null || date.isEmpty() || fieldId == null || fieldId.isEmpty()) {
            request.setAttribute("error", "Vui lòng điền đầy đủ thông tin: nhân viên, ca, ngày và mã sân.");
            doGet(request, response);
            return;
        }

        try {
            // get assignedBy from session user object
            Models.User user = (Models.User) request.getSession().getAttribute("user");
            if (user == null) {
                request.setAttribute("error", "Người dùng chưa đăng nhập.");
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            UUID staffUuid = UUID.fromString(staffId);
            UUID shiftUuid = UUID.fromString(shiftId);
            UUID fieldUuid = UUID.fromString(fieldId);
            UUID assignedBy = user.getUserId();

            java.time.LocalDate workingDate = LocalDate.parse(date);
            if (workingDate.isBefore(java.time.LocalDate.now())) {
                request.setAttribute("error", "Ngày phân ca không được là quá khứ.");
                doGet(request, response);
                return;
            }

            StaffShift ss = new StaffShift();
            ss.setStaffId(staffUuid);
            ss.setShiftId(shiftUuid);
            ss.setFieldId(fieldUuid);
            ss.setWorkingDate(workingDate);
            ss.setAssignedBy(assignedBy);
            ss.setStatus("assigned");

            StaffShiftDAO dao = new StaffShiftDAO();
            dao.assignShift(ss);
            response.sendRedirect(request.getContextPath() + "/manager/assign-shift");
        } catch (IllegalArgumentException iae) {
            request.setAttribute("error", "Định dạng ID/Ngày không hợp lệ.");
            doGet(request, response);
        } catch (Exception ex) {
            ex.printStackTrace();
            request.setAttribute("error", "Lỗi khi phân ca: " + ex.getMessage());
            doGet(request, response);
        }
    }
}
