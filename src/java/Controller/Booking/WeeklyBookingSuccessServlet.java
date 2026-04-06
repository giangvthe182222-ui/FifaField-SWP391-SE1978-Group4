package Controller.Booking;

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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@WebServlet(name = "WeeklyBookingSuccessServlet", urlPatterns = {"/booking/weekly-success"})
public class WeeklyBookingSuccessServlet extends HttpServlet {

    private String normalizePaymentOption(String raw) {
        if (raw == null) {
            return "full";
        }
        String normalized = raw.trim().toLowerCase();
        return "deposit".equals(normalized) ? "deposit" : "full";
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        @SuppressWarnings("unchecked")
        List<String> bookingIds = (List<String>) session.getAttribute("weeklyBookingIds");
        String weeklyGroupId = (String) session.getAttribute("weeklyBookingGroupId");
        String weeklyPaymentOption = (String) session.getAttribute("weeklyBookingPaymentOption");

        if ((weeklyGroupId == null || weeklyGroupId.isBlank())) {
            weeklyGroupId = request.getParameter("weeklyGroupId");
        }
        if (weeklyPaymentOption == null || weeklyPaymentOption.isBlank()) {
            weeklyPaymentOption = request.getParameter("paymentOption");
        }

        if ((bookingIds == null || bookingIds.isEmpty()) && (weeklyGroupId == null || weeklyGroupId.isBlank())) {
            response.sendRedirect(request.getContextPath() + "/booking/weekly");
            return;
        }
        // consume from session so refreshing doesn't re-show stale data
        session.removeAttribute("weeklyBookingIds");
        session.removeAttribute("weeklyBookingGroupId");
        session.removeAttribute("weeklyBookingPaymentOption");

        BookingDAO bookingDAO = new BookingDAO();
        List<BookingViewModel> bookings = new ArrayList<>();
        if (weeklyGroupId != null && !weeklyGroupId.isBlank()) {
            try {
                bookings = bookingDAO.getByWeeklyGroupId(UUID.fromString(weeklyGroupId));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (bookings.isEmpty()) {
            for (String id : bookingIds) {
                try {
                    BookingViewModel bvm = bookingDAO.getById(UUID.fromString(id));
                    if (bvm != null) bookings.add(bvm);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        User user = (User) session.getAttribute("user");
        String role = (user.getRole() != null) ? user.getRole().getRoleName() : "";
        String historyPath = "STAFF".equalsIgnoreCase(role)
                ? request.getContextPath() + "/staff/locationBookings"
                : request.getContextPath() + "/customer/bookings";

        request.setAttribute("bookings",      bookings);
        request.setAttribute("historyPath",   historyPath);
        request.setAttribute("weeklyGroupId", weeklyGroupId);
        request.setAttribute("weeklyPaymentOption", normalizePaymentOption(weeklyPaymentOption));
        request.getRequestDispatcher("/View/Booking/WeeklyBookingSuccess.jsp").forward(request, response);
    }
}
