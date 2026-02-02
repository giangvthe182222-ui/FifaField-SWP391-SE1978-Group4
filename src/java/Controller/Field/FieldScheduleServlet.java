package Controller.Field;

import DAO.FieldDAO;
import DAO.ScheduleDAO;
import Models.Field;
import Models.Schedule;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@WebServlet("/field-schedule")
public class FieldScheduleServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UUID fieldId = UUID.fromString(
                request.getParameter("fieldId") != null
                        ? request.getParameter("fieldId")
                        : "36BB5060-CC7E-466F-975E-C72DF50E8C18"
        );

        /* ===== FILTER PARAM ===== */
        String weekRaw = request.getParameter("week");   // yyyy-MM-dd
        String status = request.getParameter("status");
        String fromTimeRaw = request.getParameter("fromTime");
        String toTimeRaw = request.getParameter("toTime");

        LocalDate baseWeekDate =
                (weekRaw != null && !weekRaw.isBlank())
                        ? LocalDate.parse(weekRaw)
                        : LocalDate.now();

        LocalDate weekStart = baseWeekDate.with(DayOfWeek.SUNDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        LocalTime fromTime =
                (fromTimeRaw != null && !fromTimeRaw.isBlank())
                        ? LocalTime.parse(fromTimeRaw)
                        : null;

        LocalTime toTime =
                (toTimeRaw != null && !toTimeRaw.isBlank())
                        ? LocalTime.parse(toTimeRaw)
                        : null;

        FieldDAO fieldDAO = new FieldDAO();
        ScheduleDAO scheduleDAO = new ScheduleDAO();

        Field field = fieldDAO.getById(fieldId);
        List<Schedule> allSchedules =
                scheduleDAO.getScheduleByField(fieldId);

        /* ===== FILTER + GROUP BY DATE ===== */
        Map<LocalDate, List<Schedule>> schedulesByDate = new LinkedHashMap<>();

        // luôn tạo đủ 7 ngày
        for (int i = 0; i < 7; i++) {
            schedulesByDate.put(weekStart.plusDays(i), new ArrayList<>());
        }

        for (Schedule s : allSchedules) {

            LocalDate d = s.getBookingDate();

            // 🔹 FILTER WEEK
            if (d.isBefore(weekStart) || d.isAfter(weekEnd)) continue;

            // 🔹 FILTER STATUS
            if (status != null && !status.isBlank()
                    && !s.getStatus().equalsIgnoreCase(status)) {
                continue;
            }

            // 🔹 FILTER TIME
            if (fromTime != null && s.getStartTime().isBefore(fromTime)) {
                continue;
            }

            if (toTime != null && s.getEndTime().isAfter(toTime)) {
                continue;
            }

            schedulesByDate.get(d).add(s);
        }

        /* ===== ATTR ===== */
        request.setAttribute("field", field);
        request.setAttribute("schedulesByDate", schedulesByDate);

        request.setAttribute("week", weekStart);
        request.setAttribute("weekStart", weekStart);
        request.setAttribute("weekEnd", weekEnd);

        request.setAttribute("status", status);
        request.setAttribute("fromTime", fromTimeRaw);
        request.setAttribute("toTime", toTimeRaw);

        request.setAttribute("prevWeek", weekStart.minusWeeks(1));
        request.setAttribute("nextWeek", weekStart.plusWeeks(1));

        request.getRequestDispatcher("/View/Field/FieldSchedule.jsp")
                .forward(request, response);
    }
}


