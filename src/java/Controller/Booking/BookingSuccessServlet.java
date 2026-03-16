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
import java.util.UUID;

@WebServlet(name = "BookingSuccessServlet", urlPatterns = {"/booking-success"})
public class BookingSuccessServlet extends HttpServlet {

    private String resolveBookingListPath(User user) {
        if (user != null
                && user.getRole() != null
                && "STAFF".equalsIgnoreCase(user.getRole().getRoleName())) {
            return "/staff/locationBookings";
        }
        return "/customer/bookings";
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=booking");
            return;
        }

        User user = (User) session.getAttribute("user");
        String bookingIdRaw = request.getParameter("bookingId");
        if (bookingIdRaw == null || bookingIdRaw.isBlank()) {
            response.sendRedirect(request.getContextPath() + resolveBookingListPath(user));
            return;
        }

        UUID bookingId;
        try {
            bookingId = UUID.fromString(bookingIdRaw);
        } catch (IllegalArgumentException e) {
            response.sendRedirect(request.getContextPath() + resolveBookingListPath(user));
            return;
        }

        BookingViewModel booking = new BookingDAO().getById(bookingId);
        if (booking == null || !user.getUserId().equals(booking.getBookerId())) {
            response.sendRedirect(request.getContextPath() + resolveBookingListPath(user));
            return;
        }

        String bookingListPath = resolveBookingListPath(user);
        String roleName = user.getRole() != null ? user.getRole().getRoleName() : "";
        String bookingDetailPath = "STAFF".equalsIgnoreCase(roleName)
                ? "/staff/bookingDetail"
                : "/customer/bookingDetail";

        request.setAttribute("booking", booking);
        request.setAttribute("bookingListPath", bookingListPath);
        request.setAttribute("bookingDetailPath", bookingDetailPath);
        request.getRequestDispatcher("/View/Booking/BookingSuccess.jsp").forward(request, response);
    }
}
