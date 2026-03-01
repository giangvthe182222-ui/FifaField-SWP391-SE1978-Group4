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
import java.util.List;
import java.util.UUID;

@WebServlet(name = "StaffBookingHistoryServlet", urlPatterns = {"/staff/bookings"})
public class StaffBookingHistoryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=staff/bookings");
            return;
        }

        User user = (User) session.getAttribute("user");
        UUID userId = user.getUserId();

        String date = request.getParameter("date");
        String time = request.getParameter("time");
        String status = request.getParameter("status");

        BookingDAO bookingDAO = new BookingDAO();
        List<BookingViewModel> bookings = bookingDAO.getByBookerFiltered(userId, date, time, status);

        request.setAttribute("bookings", bookings);
        request.getRequestDispatcher("/View/Booking/StaffBookingHistory.jsp").forward(request, response);
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=staff/bookings");
            return;
        }

        String bookingIdParam = request.getParameter("bookingId");
        String status = request.getParameter("status");
        if (bookingIdParam == null || bookingIdParam.isBlank() || status == null || status.isBlank()) {
            session.setAttribute("flash_error", "Invalid request.");
            response.sendRedirect(request.getContextPath() + "/staff/bookings");
            return;
        }

        BookingDAO bookingDAO = new BookingDAO();
        boolean ok = bookingDAO.updateStatus(UUID.fromString(bookingIdParam), status);
        if (ok) session.setAttribute("flash_success", "Updated booking status.");
        else session.setAttribute("flash_error", "Failed to update status.");

        response.sendRedirect(request.getContextPath() + "/staff/bookings");
    }
}