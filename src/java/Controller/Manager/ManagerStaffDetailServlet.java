package Controller.Manager;

import DAO.ManagerDAO;
import DAO.StaffDAO;
import DAO.StaffShiftDAO;
import Models.Manager;
import Models.StaffShiftViewModel;
import Models.StaffViewModel;
import Models.User;
import Models.WeeklyShiftGroup;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

        // parse month/year filter (default to current month/year)
        int filterMonth = LocalDate.now().getMonthValue();
        int filterYear  = LocalDate.now().getYear();
        String monthParam = request.getParameter("month");
        String yearParam  = request.getParameter("year");
        if (monthParam != null && !monthParam.isBlank()) {
            try { filterMonth = Integer.parseInt(monthParam); } catch (NumberFormatException ignored) {}
        }
        if (yearParam != null && !yearParam.isBlank()) {
            try { filterYear = Integer.parseInt(yearParam); } catch (NumberFormatException ignored) {}
        }
        // clamp month to valid range
        if (filterMonth < 1 || filterMonth > 12) filterMonth = LocalDate.now().getMonthValue();

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

            // load shifts for the selected month/year and group by week
            List<StaffShiftViewModel> monthlyShifts = staffShiftDAO.getShiftsForStaffByMonthYear(
                    UUID.fromString(staff.getUserId()), filterMonth, filterYear);
            List<WeeklyShiftGroup> weekGroups = buildWeekGroups(monthlyShifts, filterMonth, filterYear);

            request.setAttribute("staff", staff);
            request.setAttribute("workedShiftCount", workedShiftCount);
            request.setAttribute("weekGroups", weekGroups);
            request.setAttribute("monthlyShiftCount", monthlyShifts.size());
            request.setAttribute("filterMonth", filterMonth);
            request.setAttribute("filterYear", filterYear);
            request.getRequestDispatcher("/View/Manager/manager-staff-detail.jsp").forward(request, response);
        } catch (IllegalArgumentException e) {
            response.sendRedirect(request.getContextPath() + "/manager/staff/list?error=invalid_id");
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi khi tải chi tiết nhân viên: " + e.getMessage());
            request.getRequestDispatcher("/View/Manager/manager-staff-list.jsp").forward(request, response);
        }
    }

    private List<WeeklyShiftGroup> buildWeekGroups(List<StaffShiftViewModel> shifts, int month, int year) {
        Map<Integer, List<StaffShiftViewModel>> weekMap = new LinkedHashMap<>();
        for (StaffShiftViewModel shift : shifts) {
            int dayOfMonth = shift.getWorkingDate().getDayOfMonth();
            int weekNum = (dayOfMonth - 1) / 7 + 1;
            weekMap.computeIfAbsent(weekNum, k -> new ArrayList<>()).add(shift);
        }
        YearMonth ym = YearMonth.of(year, month);
        List<WeeklyShiftGroup> groups = new ArrayList<>();
        for (Map.Entry<Integer, List<StaffShiftViewModel>> entry : weekMap.entrySet()) {
            int weekNum  = entry.getKey();
            int startDay = (weekNum - 1) * 7 + 1;
            int endDay   = Math.min(weekNum * 7, ym.lengthOfMonth());
            String label = String.format("Tuần %d  (%02d/%02d – %02d/%02d)", weekNum, startDay, month, endDay, month);
            WeeklyShiftGroup group = new WeeklyShiftGroup();
            group.setLabel(label);
            group.setWeekNumber(weekNum);
            group.setShifts(entry.getValue());
            group.setShiftCount(entry.getValue().size());
            groups.add(group);
        }
        return groups;
    }
}

