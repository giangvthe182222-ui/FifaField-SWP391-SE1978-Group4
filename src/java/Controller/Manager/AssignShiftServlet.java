package Controller.Manager;

import DAO.ShiftDAO;
import DAO.StaffDAO;
import DAO.StaffShiftDAO;
import DAO.LocationDAO;
import DAO.FieldDAO;
import Models.StaffViewModel;
import Models.Shift;
import Models.StaffShift;
import Models.Location;
import Models.Field;
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
            LocationDAO locationDAO = new LocationDAO();
            FieldDAO fieldDAO = new FieldDAO();
            
            List<StaffViewModel> staff = staffDAO.getAllStaff();
            List<Shift> shifts = shiftDAO.getAllShifts();
            List<Location> locations = locationDAO.getAllLocations();
            List<Field> fields = fieldDAO.getAllFields();
            
            request.setAttribute("staffList", staff);
            request.setAttribute("shifts", shifts);
            request.setAttribute("locations", locations);
            request.setAttribute("fields", fields);

            // handle editing prefill
            String action = request.getParameter("action");
            if ("edit".equals(action)) {
                request.setAttribute("editMode", true);
                request.setAttribute("origStaffId", request.getParameter("staffId"));
                request.setAttribute("origFieldId", request.getParameter("fieldId"));
                request.setAttribute("origShiftId", request.getParameter("shiftId"));
                request.setAttribute("origWorkingDate", request.getParameter("workingDate"));

                // pass values back to form
                request.setAttribute("staffId", request.getParameter("staffId"));
                request.setAttribute("locationId", request.getParameter("locationId"));
                request.setAttribute("fieldId", request.getParameter("fieldId"));
                request.setAttribute("shiftId", request.getParameter("shiftId"));
                request.setAttribute("workingDate", request.getParameter("workingDate"));
            }

            request.getRequestDispatcher("/View/Manager/assign-shift.jsp").forward(request, response);
        } catch (SQLException ex) {
            ex.printStackTrace();
            request.setAttribute("error", "Lỗi khi tải dữ liệu: " + ex.getMessage());
            request.getRequestDispatcher("/View/Manager/assign-shift.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Server-side validation for date range
        String staffId = request.getParameter("staffId");
        String shiftId = request.getParameter("shiftId");
        String start = request.getParameter("startDate");
        String end = request.getParameter("endDate");
        String locationId = request.getParameter("locationId");
        String fieldId = request.getParameter("fieldId");

        if (staffId == null || staffId.isEmpty() || shiftId == null || shiftId.isEmpty() || start == null || start.isEmpty()
                || end == null || end.isEmpty() || locationId == null || locationId.isEmpty() || fieldId == null || fieldId.isEmpty()) {
            request.setAttribute("error", "Vui lòng điền đầy đủ thông tin: nhân viên, cụm sân, sân, ca và khoảng ngày.");
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
            UUID locationUuid = UUID.fromString(locationId);
            UUID fieldUuid = UUID.fromString(fieldId);
            UUID assignedBy = user.getUserId();

            // verify field belongs to location
            FieldDAO fieldDAO = new FieldDAO();
            Field f = fieldDAO.getById(fieldUuid);
            if (f == null || !f.getLocationId().equals(locationUuid)) {
                request.setAttribute("error", "Sân bóng không hợp lệ cho cụm đã chọn.");
                doGet(request, response);
                return;
            }

            LocalDate startDate = LocalDate.parse(start);
            LocalDate endDate = LocalDate.parse(end);
            if (startDate.isAfter(endDate)) {
                request.setAttribute("error", "Ngày bắt đầu phải không sau ngày kết thúc.");
                doGet(request, response);
                return;
            }
            if (endDate.isBefore(LocalDate.now())) {
                request.setAttribute("error", "Khoảng ngày không được ở quá khứ.");
                doGet(request, response);
                return;
            }

            StaffShiftDAO dao = new StaffShiftDAO();
            if (dao.hasOverlap(staffUuid, startDate, endDate)) {
                request.setAttribute("error", "Nhân viên đã có lịch trong khoảng thời gian này.");
                doGet(request, response);
                return;
            }

            // assemble template shift (date not set)
            StaffShift ss = new StaffShift();
            ss.setStaffId(staffUuid);
            ss.setShiftId(shiftUuid);
            ss.setFieldId(fieldUuid);
            ss.setAssignedBy(assignedBy);
            ss.setStatus("assigned");

            // editing existed assignment?
            String origStaff = request.getParameter("origStaffId");
            if (origStaff != null && !origStaff.isEmpty()) {
                String origField = request.getParameter("origFieldId");
                String origShift = request.getParameter("origShiftId");
                String origDate = request.getParameter("origWorkingDate");
                StaffShiftDAO dao1 = new StaffShiftDAO();
                boolean updated = dao1.updateStaffShift(
                        UUID.fromString(origStaff),
                        UUID.fromString(origField),
                        UUID.fromString(origShift),
                        LocalDate.parse(origDate),
                        ss);
                if (!updated) {
                    request.setAttribute("error", "Không thể cập nhật ca.");
                    doGet(request, response);
                    return;
                }
                request.getSession().setAttribute("success", "Cập nhật ca thành công.");
                response.sendRedirect(request.getContextPath() + "/manager/staff-shifts");
                return;
            }

            StaffShiftDAO dao2 = new StaffShiftDAO();
            boolean ok = dao2.assignShiftRange(ss, startDate, endDate);
            if (!ok) {
                request.setAttribute("error", "Phân ca thất bại.");
                doGet(request, response);
                return;
            }
            request.getSession().setAttribute("success", "Phân ca thành công.");
            response.sendRedirect(request.getContextPath() + "/manager/staff-shifts");
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
