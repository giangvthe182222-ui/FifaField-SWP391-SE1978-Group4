package Controller.Manager;

import DAO.StaffShiftDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;

public class DeleteStaffShiftServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String staffId = request.getParameter("staffId");
        String fieldId = request.getParameter("fieldId");
        String shiftId = request.getParameter("shiftId");
        String workingDate = request.getParameter("workingDate");
        if (workingDate == null) {
            workingDate = request.getParameter("startDate");
        }

        if (staffId == null || fieldId == null || shiftId == null) {
            response.sendRedirect(request.getContextPath() + "/manager/staff-shifts");
            return;
        }
        try {
            StaffShiftDAO dao = new StaffShiftDAO();
            // Delete entire group (all working dates for this staff-field-shift combination)
            boolean deleted = dao.deleteStaffShiftGroup(
                    UUID.fromString(staffId),
                    UUID.fromString(fieldId),
                    UUID.fromString(shiftId)
            );
            if (!deleted) {
                request.getSession().setAttribute("error", "Xóa ca thất bại.");
            } else {
                request.getSession().setAttribute("success", "Xóa ca thành công.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            request.setAttribute("error", "Lỗi hệ thống khi xóa: " + ex.getMessage());
        }
        // Redirect to avoid resubmission and ensure proper HTTP semantics
        response.sendRedirect(request.getContextPath() + "/manager/staff-shifts");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Allow simple GET delete links if used; delegate to doPost for unified handling
        doPost(request, response);
    }
}