package Controller.Staff;

import DAO.BookingDAO;
import DAO.FieldDAO;
import DAO.ScheduleDAO;
import DAO.StaffDAO;
import Models.BookingViewModel;
import Models.Field;
import Models.Schedule;
import Models.StaffViewModel;
import Models.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@WebServlet(name = "StaffFieldDetailServlet", urlPatterns = {"/staff/fields/detail"})
public class StaffFieldDetailServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=staff/fields");
            return;
        }

        String fieldIdRaw = request.getParameter("fieldId");
        if (fieldIdRaw == null || fieldIdRaw.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/staff/fields");
            return;
        }

        User user = (User) session.getAttribute("user");

        try {
            StaffDAO staffDAO = new StaffDAO();
            StaffViewModel staff = staffDAO.getStaffById(user.getUserId().toString());
            if (staff == null || staff.getLocationId() == null) {
                session.setAttribute("flash_error", "No location assigned to this staff.");
                response.sendRedirect(request.getContextPath() + "/");
                return;
            }

            UUID fieldId = UUID.fromString(fieldIdRaw);
            FieldDAO fieldDAO = new FieldDAO();
            Field field = fieldDAO.getById(fieldId);
            if (field == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Field not found");
                return;
            }

            if (!field.getLocationId().toString().equalsIgnoreCase(staff.getLocationId())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You cannot access this field");
                return;
            }

            String weekRaw = request.getParameter("date");
            LocalDate baseDate = (weekRaw != null && !weekRaw.isBlank()) ? LocalDate.parse(weekRaw) : LocalDate.now();
            LocalDate weekStart = baseDate.with(DayOfWeek.SUNDAY);
            LocalDate weekEnd = weekStart.plusDays(6);

            ScheduleDAO scheduleDAO = new ScheduleDAO();
            BookingDAO bookingDAO = new BookingDAO();

            List<Schedule> allSchedules = scheduleDAO.getScheduleByField(fieldId);
            Map<LocalDate, List<Schedule>> schedulesByDate = new LinkedHashMap<>();
            for (int i = 0; i < 7; i++) {
                schedulesByDate.put(weekStart.plusDays(i), new ArrayList<>());
            }

            Map<UUID, BookingViewModel> bookingBySchedule = new LinkedHashMap<>();
            for (Schedule s : allSchedules) {
                LocalDate bookingDate = s.getBookingDate();
                if (bookingDate.isBefore(weekStart) || bookingDate.isAfter(weekEnd)) {
                    continue;
                }
                schedulesByDate.get(bookingDate).add(s);

                if ("unavailable".equalsIgnoreCase(s.getStatus())) {
                    BookingViewModel booking = bookingDAO.getByScheduleId(s.getScheduleId());
                    if (booking != null) {
                        bookingBySchedule.put(s.getScheduleId(), booking);
                    }
                }
            }

            Map<LocalDate, String> displayDateMap = new LinkedHashMap<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd/MM", Locale.forLanguageTag("vi"));
            for (LocalDate d : schedulesByDate.keySet()) {
                displayDateMap.put(d, d.format(formatter));
            }

            request.setAttribute("field", field);
            request.setAttribute("locationName", staff.getLocationName());
            request.setAttribute("schedulesByDate", schedulesByDate);
            request.setAttribute("displayDateMap", displayDateMap);
            request.setAttribute("bookingBySchedule", bookingBySchedule);
            request.setAttribute("weekStart", weekStart);
            request.setAttribute("weekEnd", weekEnd);
            request.setAttribute("prevWeek", weekStart.minusWeeks(1));
            request.setAttribute("nextWeek", weekStart.plusWeeks(1));

            request.getRequestDispatcher("/View/Staff/StaffFieldDetail.jsp").forward(request, response);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid UUID format");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("flash_error", "Error loading field detail: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/staff/fields");
        }
    }
}
