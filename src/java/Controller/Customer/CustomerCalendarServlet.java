package Controller.Customer;

import DAO.BookingDAO;
import Models.BookingViewModel;
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

@WebServlet("/customer/my-calendar")
public class CustomerCalendarServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=customer/my-calendar");
            return;
        }

        String dateRaw = request.getParameter("date");
        String weekDateRaw = request.getParameter("weekDate");

        LocalDate selectedDate = null;
        try {
            if (dateRaw != null && !dateRaw.isBlank()) {
                selectedDate = LocalDate.parse(dateRaw);
            }
        } catch (Exception e) {
            selectedDate = null;
        }

        LocalDate weekDate = null;
        try {
            if (weekDateRaw != null && !weekDateRaw.isBlank()) {
                weekDate = LocalDate.parse(weekDateRaw);
            }
        } catch (Exception e) {
            weekDate = null;
        }

        LocalDate baseDate = weekDate != null ? weekDate : (selectedDate != null ? selectedDate : LocalDate.now());
        LocalDate weekStart = baseDate.with(DayOfWeek.SUNDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        BookingDAO bookingDAO = new BookingDAO();
        List<BookingViewModel> bookings = bookingDAO.getCustomerCalendarBookings(
            user.getUserId(), weekStart, weekEnd, selectedDate, null, null
        );

        Map<LocalDate, List<BookingViewModel>> bookingsByDate = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            bookingsByDate.put(weekStart.plusDays(i), new ArrayList<BookingViewModel>());
        }

        for (BookingViewModel b : bookings) {
            if (b.getBookingDate() != null && bookingsByDate.containsKey(b.getBookingDate())) {
                bookingsByDate.get(b.getBookingDate()).add(b);
            }
        }

        Map<LocalDate, String> displayDateMap = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd/MM", Locale.forLanguageTag("vi"));
        for (LocalDate d : bookingsByDate.keySet()) {
            displayDateMap.put(d, d.format(formatter));
        }

        request.setAttribute("bookingsByDate", bookingsByDate);
        request.setAttribute("displayDateMap", displayDateMap);
        request.setAttribute("selectedDate", selectedDate != null ? selectedDate.toString() : "");
        request.setAttribute("weekStart", weekStart);
        request.setAttribute("weekEnd", weekEnd);
        request.setAttribute("prevWeek", weekStart.minusWeeks(1));
        request.setAttribute("nextWeek", weekStart.plusWeeks(1));

        request.getRequestDispatcher("/View/Customer/customer-calendar.jsp").forward(request, response);
    }
}
