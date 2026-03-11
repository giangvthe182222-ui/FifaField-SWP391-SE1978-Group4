package Controller.Manager;

import DAO.FieldDAO;
import DAO.ShiftDAO;
import DAO.StaffDAO;
import DAO.StaffShiftDAO;
import Models.StaffShift;
import DAO.LocationDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EditStaffShiftServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Load supporting lists for the form
        try {
            String staffId = request.getParameter("staffId");
            
            StaffDAO staffDAO = new StaffDAO();
            ShiftDAO shiftDAO = new ShiftDAO();
            FieldDAO fieldDAO = new FieldDAO();
            LocationDAO locationDAO = new LocationDAO();
            
            // Get staff location to filter fields
            UUID staffLocationId = null;
            if (staffId != null && !staffId.isEmpty()) {
                Models.StaffViewModel staffVm = staffDAO.getStaffById(staffId);
                if (staffVm != null && staffVm.getLocationId() != null) {
                    staffLocationId = UUID.fromString(staffVm.getLocationId());
                }
            }
            
            List<Models.Field> fields;
            List<Models.Location> locations;
            
            if (staffLocationId != null) {
                // Only show fields in staff's location
                fields = fieldDAO.getByLocation(staffLocationId);
                // Only show staff's location
                Models.Location staffLocation = locationDAO.getLocationById(staffLocationId);
                locations = new ArrayList<>();
                if (staffLocation != null) {
                    locations.add(staffLocation);
                }
            } else {
                // Fallback
                fields = fieldDAO.getAllFields();
                locations = locationDAO.getAllLocations();
            }
            
            request.setAttribute("staffList", staffDAO.getAllStaff());
            request.setAttribute("shifts", shiftDAO.getAllShifts());
            request.setAttribute("locations", locations);
            request.setAttribute("fields", fields);
            request.setAttribute("editMode", true);
            String origDate = request.getParameter("startDate");
            if (origDate == null) origDate = request.getParameter("workingDate");

            request.setAttribute("origStaffId", request.getParameter("staffId"));
            request.setAttribute("origFieldId", request.getParameter("fieldId"));
            request.setAttribute("origShiftId", request.getParameter("shiftId"));
            request.setAttribute("origWorkingDate", origDate);

            // Get current field name for display
            String currentFieldId = request.getParameter("fieldId");
            if (currentFieldId != null && !currentFieldId.isEmpty()) {
                Models.Field currentField = fieldDAO.getById(UUID.fromString(currentFieldId));
                if (currentField != null) {
                    request.setAttribute("currentFieldName", currentField.getFieldName());
                }
            }

            // Also set the current values to populate selects
            request.setAttribute("staffId", request.getParameter("staffId"));
            request.setAttribute("locationId", request.getParameter("locationId"));
            request.setAttribute("fieldId", request.getParameter("fieldId"));
            request.setAttribute("shiftId", request.getParameter("shiftId"));
            request.setAttribute("workingDate", origDate);
            
            // For date range editing - check if we have startDate/endDate params
            String startDateParam = request.getParameter("startDate");
            String endDateParam = request.getParameter("endDate");
            if (startDateParam != null && !startDateParam.isEmpty()) {
                request.setAttribute("startDate", startDateParam);
            } else if (origDate != null) {
                request.setAttribute("startDate", origDate);
            }
            if (endDateParam != null && !endDateParam.isEmpty()) {
                request.setAttribute("endDate", endDateParam);
            } else if (origDate != null) {
                request.setAttribute("endDate", origDate);
            }

            request.getRequestDispatcher("/View/Manager/edit-staff-shift.jsp").forward(request, response);
        } catch (SQLException ex) {
            ex.printStackTrace();
            request.setAttribute("error", "Lỗi khi tải dữ liệu: " + ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/manager/staff-shifts");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String shiftId = request.getParameter("shiftId");
        String fieldId = request.getParameter("fieldId");
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");

        if (shiftId == null || shiftId.isEmpty() || fieldId == null || fieldId.isEmpty() ||
            startDateStr == null || startDateStr.isEmpty() || endDateStr == null || endDateStr.isEmpty()) {
            request.setAttribute("error", "Vui lòng điền đầy đủ thông tin.");
            doGet(request, response);
            return;
        }
        
        try {
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);
            
            // Validation
            if (startDate.isAfter(endDate)) {
                request.setAttribute("error", "Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.");
                doGet(request, response);
                return;
            }
            if (endDate.isBefore(LocalDate.now())) {
                request.setAttribute("error", "Ngày kết thúc không được ở quá khứ.");
                doGet(request, response);
                return;
            }
            
            // Get manager info for assigned_by
            Models.User user = (Models.User) request.getSession().getAttribute("user");
            if (user == null) {
                request.setAttribute("error", "Người dùng chưa đăng nhập.");
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            
            StaffShiftDAO dao = new StaffShiftDAO();
            
            // Get original values
            String origStaff = request.getParameter("origStaffId");
            String origField = request.getParameter("origFieldId");
            String origShift = request.getParameter("origShiftId");
            
            // Update using the new group method
            boolean updated = dao.updateStaffShiftGroup(
                    UUID.fromString(origStaff),
                    UUID.fromString(origField),
                    UUID.fromString(origShift),
                    UUID.fromString(fieldId),
                    UUID.fromString(shiftId),
                    startDate,
                    endDate,
                    user.getUserId()
            );

            if (!updated) {
                request.setAttribute("error", "Cập nhật thất bại.");
                doGet(request, response);
                return;
            }

            request.getSession().setAttribute("success", "Cập nhật ca thành công.");
            response.sendRedirect(request.getContextPath() + "/manager/staff-shifts");
        } catch (Exception ex) {
            ex.printStackTrace();
            request.setAttribute("error", "Lỗi khi cập nhật: " + ex.getMessage());
            doGet(request, response);
        }
    }
}
