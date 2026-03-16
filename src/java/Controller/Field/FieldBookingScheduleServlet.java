package Controller.Field;

import DAO.BookingDAO;
import DAO.FieldDAO;
import DAO.ScheduleDAO;
import Models.BookingViewModel;
import Models.Field;
import Models.Schedule;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@WebServlet(urlPatterns = {"/field-booking-schedule", "/customer/field-schedule"})
public class FieldBookingScheduleServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UUID fieldId;
        try {
            fieldId = UUID.fromString(request.getParameter("fieldId"));
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid fieldId format");
            return;
        }

        String weekRaw = request.getParameter("date");
        String status = request.getParameter("status");
        String fromTimeRaw = request.getParameter("fromTime");
        String toTimeRaw = request.getParameter("toTime");

        LocalDate baseWeekDate = (weekRaw != null && !weekRaw.isBlank())
                ? LocalDate.parse(weekRaw)
                : LocalDate.now();

        LocalDate weekStart = baseWeekDate.with(DayOfWeek.SUNDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        LocalTime fromTime = (fromTimeRaw != null && !fromTimeRaw.isBlank())
                ? LocalTime.parse(fromTimeRaw)
                : null;
        LocalTime toTime = (toTimeRaw != null && !toTimeRaw.isBlank())
                ? LocalTime.parse(toTimeRaw)
                : null;

        FieldDAO fieldDAO = new FieldDAO();
        ScheduleDAO scheduleDAO = new ScheduleDAO();
        BookingDAO bookingDAO = new BookingDAO();

        Field field = fieldDAO.getById(fieldId);
        if (field == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Field not found");
            return;
        }

        List<Schedule> allSchedules = scheduleDAO.getScheduleByField(fieldId);
        Map<LocalDate, List<Schedule>> schedulesByDate = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            schedulesByDate.put(weekStart.plusDays(i), new ArrayList<Schedule>());
        }

        Map<UUID, BookingViewModel> bookingBySchedule = new LinkedHashMap<>();

        for (Schedule s : allSchedules) {
            LocalDate date = s.getBookingDate();
            if (date.isBefore(weekStart) || date.isAfter(weekEnd)) {
                continue;
            }

            if (status != null && !status.isBlank() && !s.getStatus().equalsIgnoreCase(status)) {
                continue;
            }

            if (fromTime != null && s.getStartTime().isBefore(fromTime)) {
                continue;
            }

            if (toTime != null && s.getEndTime().isAfter(toTime)) {
                continue;
            }

            schedulesByDate.get(date).add(s);

            BookingViewModel booking = bookingDAO.getByScheduleIdForCalendar(s.getScheduleId());
            if (booking != null) {
                bookingBySchedule.put(s.getScheduleId(), booking);
            }
        }

        Map<LocalDate, String> displayDateMap = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd/MM", Locale.forLanguageTag("vi"));
        for (LocalDate d : schedulesByDate.keySet()) {
            displayDateMap.put(d, d.format(formatter));
        }

        request.setAttribute("field", field);
        request.setAttribute("schedulesByDate", schedulesByDate);
        request.setAttribute("bookingBySchedule", bookingBySchedule);
        request.setAttribute("displayDateMap", displayDateMap);

        request.setAttribute("weekStart", weekStart);
        request.setAttribute("weekEnd", weekEnd);
        request.setAttribute("prevWeek", weekStart.minusWeeks(1));
        request.setAttribute("nextWeek", weekStart.plusWeeks(1));

        request.setAttribute("status", status);
        request.setAttribute("fromTime", fromTimeRaw);
        request.setAttribute("toTime", toTimeRaw);
        request.setAttribute("customerView", request.getRequestURI().startsWith(request.getContextPath() + "/customer/"));

        request.getRequestDispatcher("/View/Field/FieldBookingSchedule.jsp").forward(request, response);
    }
}
