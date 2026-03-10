package Controller.Booking;

import Models.BookingViewModel;
import Models.User;
import Service.BookingService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@WebServlet(name = "BookingHistoryServlet", urlPatterns = {"/customer/bookings"})
public class BookingHistoryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=customer/bookings");
            return;
        }

        String flashSuccess = (String) session.getAttribute("flash_success");
        if (flashSuccess != null) {
            request.setAttribute("flashSuccess", flashSuccess);
            session.removeAttribute("flash_success");
        }
        String flashError = (String) session.getAttribute("flash_error");
        if (flashError != null) {
            request.setAttribute("flashError", flashError);
            session.removeAttribute("flash_error");
        }

        User user = (User) session.getAttribute("user");
        UUID userId = user.getUserId();

        String date = request.getParameter("date");
        String time = request.getParameter("time");
        String status = request.getParameter("status");

        BookingService bookingService = new BookingService();
        List<BookingViewModel> bookings = bookingService.getCustomerBookingHistory(userId, date, time, status);

        request.setAttribute("bookings", bookings);
        request.setAttribute("viewMode", "customer");
        request.getRequestDispatcher("/View/Booking/BookingHistory.jsp").forward(request, response);
    }
}
