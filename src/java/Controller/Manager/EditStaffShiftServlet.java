package Controller.Manager;

import DAO.FieldDAO;
import DAO.ShiftDAO;
import DAO.StaffDAO;
import DAO.StaffShiftDAO;
import Models.StaffShift;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;

public class EditStaffShiftServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Load supporting lists for the form
        try {
            StaffDAO staffDAO = new StaffDAO();
            ShiftDAO shiftDAO = new ShiftDAO();
            FieldDAO fieldDAO = new FieldDAO();

            request.setAttribute("staffList", staffDAO.getAllStaff());
            request.setAttribute("shifts", shiftDAO.getAllShifts());
            request.setAttribute("fields", fieldDAO.getAllFields());

            // Pass original identifying params to prefill form
            request.setAttribute("editMode", true);
            request.setAttribute("origStaffId", request.getParameter("staffId"));
            request.setAttribute("origFieldId", request.getParameter("fieldId"));
            request.setAttribute("origShiftId", request.getParameter("shiftId"));
            request.setAttribute("origWorkingDate", request.getParameter("workingDate"));

            // Also set the current values to populate selects
            request.setAttribute("staffId", request.getParameter("staffId"));
            request.setAttribute("fieldId", request.getParameter("fieldId"));
            request.setAttribute("shiftId", request.getParameter("shiftId"));
            request.setAttribute("workingDate", request.getParameter("workingDate"));

            request.getRequestDispatcher("/View/Manager/edit-staff-shift.jsp").forward(request, response);
        } catch (SQLException ex) {
            ex.printStackTrace();
            request.setAttribute("error", "Lỗi khi tải dữ liệu: " + ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/manager/staff-shifts");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Similar validation & update logic as AssignShiftServlet
        String staffId = request.getParameter("staffId");
        String shiftId = request.getParameter("shiftId");
        String date = request.getParameter("workingDate");
        String fieldId = request.getParameter("fieldId");

        if (staffId == null || staffId.isEmpty() || shiftId == null || shiftId.isEmpty() || date == null || date.isEmpty()
                || fieldId == null || fieldId.isEmpty()) {
            request.setAttribute("error", "Vui lòng điền đầy đủ thông tin.");
            doGet(request, response);
            return;
        }

        try {
            StaffShiftDAO dao = new StaffShiftDAO();

            // Build new data
            StaffShift ss = new StaffShift();
            ss.setStaffId(UUID.fromString(staffId));
            ss.setFieldId(UUID.fromString(fieldId));
            ss.setShiftId(UUID.fromString(shiftId));
            ss.setWorkingDate(LocalDate.parse(date));
            ss.setStatus("assigned");

            String origStaff = request.getParameter("origStaffId");
            String origField = request.getParameter("origFieldId");
            String origShift = request.getParameter("origShiftId");
            String origDate = request.getParameter("origWorkingDate");

            boolean updated = dao.updateStaffShift(
                    UUID.fromString(origStaff),
                    UUID.fromString(origField),
                    UUID.fromString(origShift),
                    LocalDate.parse(origDate),
                    ss
            );

            if (!updated) {
                request.setAttribute("error", "Cập nhật thất bại.");
                doGet(request, response);
                return;
            }

            response.sendRedirect(request.getContextPath() + "/manager/staff-shifts");
        } catch (Exception ex) {
            ex.printStackTrace();
            request.setAttribute("error", "Lỗi khi cập nhật: " + ex.getMessage());
            doGet(request, response);
        }
    }
}
