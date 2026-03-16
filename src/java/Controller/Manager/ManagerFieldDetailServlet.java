package Controller.Manager;

import DAO.BookingDAO;
import DAO.FieldDAO;
import DAO.LocationDAO;
import DAO.ManagerDAO;
import DAO.ScheduleDAO;
import Models.BookingViewModel;
import Models.Field;
import Models.Location;
import Models.Manager;
import Models.Schedule;
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

@WebServlet("/manager/fields/detail")
public class ManagerFieldDetailServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=manager/fields");
            return;
        }

        User user = (User) session.getAttribute("user");
        String fieldIdRaw = request.getParameter("fieldId");
        if (fieldIdRaw == null || fieldIdRaw.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/manager/fields");
            return;
        }

        try {
            Manager manager = new ManagerDAO().getManagerById(user.getUserId());
            if (manager == null || manager.getLocationId() == null) {
                response.sendRedirect(request.getContextPath() + "/manager/dashboard");
                return;
            }

            UUID fieldId = UUID.fromString(fieldIdRaw);
            Field field = new FieldDAO().getById(fieldId);
            if (field == null || !manager.getLocationId().equals(field.getLocationId())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Field is outside manager location");
                return;
            }

            Location location = new LocationDAO().getLocationById(field.getLocationId());

            String weekRaw = request.getParameter("date");
            LocalDate baseDate = (weekRaw != null && !weekRaw.isBlank()) ? LocalDate.parse(weekRaw) : LocalDate.now();
            LocalDate weekStart = baseDate.with(DayOfWeek.SUNDAY);
            LocalDate weekEnd = weekStart.plusDays(6);

            List<Schedule> allSchedules = new ScheduleDAO().getScheduleByField(fieldId);
            BookingDAO bookingDAO = new BookingDAO();
            Map<LocalDate, List<Schedule>> schedulesByDate = new LinkedHashMap<>();
            for (int i = 0; i < 7; i++) {
                schedulesByDate.put(weekStart.plusDays(i), new ArrayList<Schedule>());
            }

            Map<UUID, BookingViewModel> bookingBySchedule = new LinkedHashMap<>();
            for (Schedule schedule : allSchedules) {
                LocalDate bookingDate = schedule.getBookingDate();
                if (bookingDate.isBefore(weekStart) || bookingDate.isAfter(weekEnd)) {
                    continue;
                }
                schedulesByDate.get(bookingDate).add(schedule);
                BookingViewModel booking = bookingDAO.getByScheduleIdForCalendar(schedule.getScheduleId());
                if (booking != null) {
                    bookingBySchedule.put(schedule.getScheduleId(), booking);
                }
            }

            Map<LocalDate, String> displayDateMap = new LinkedHashMap<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd/MM", Locale.forLanguageTag("vi"));
            for (LocalDate d : schedulesByDate.keySet()) {
                displayDateMap.put(d, d.format(formatter));
            }

            request.setAttribute("field", field);
            request.setAttribute("locationName", manager.getLocationName());
            request.setAttribute("location", location);
            request.setAttribute("schedulesByDate", schedulesByDate);
            request.setAttribute("displayDateMap", displayDateMap);
            request.setAttribute("bookingBySchedule", bookingBySchedule);
            request.setAttribute("weekStart", weekStart);
            request.setAttribute("weekEnd", weekEnd);
            request.setAttribute("prevWeek", weekStart.minusWeeks(1));
            request.setAttribute("nextWeek", weekStart.plusWeeks(1));
            request.getRequestDispatcher("/View/Manager/manager-field-detail.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Cannot load manager field detail", e);
        }
    }
}
