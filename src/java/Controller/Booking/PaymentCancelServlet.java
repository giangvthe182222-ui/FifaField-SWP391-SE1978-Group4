package Controller.Booking;

import DAO.*;
import Models.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.UUID;

/**
 * PaymentCancelServlet - Cancel booking when payment deadline expires Also
 * provides a scheduled task endpoint to auto-cancel expired payments
 */
@WebServlet(name = "PaymentCancelServlet", urlPatterns = {"/payment-cancel"})
public class PaymentCancelServlet extends HttpServlet {

    /**
     * Cancel a specific booking due to payment timeout
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("user");
        String bookingIdParam = request.getParameter("bookingId");

        if (bookingIdParam == null || bookingIdParam.isBlank()) {
            session.setAttribute("flash_error", "Invalid booking ID.");
            response.sendRedirect(request.getContextPath() + "/customer/bookings");
            return;
        }

        UUID bookingId;
        try {
            bookingId = UUID.fromString(bookingIdParam);
        } catch (IllegalArgumentException e) {
            session.setAttribute("flash_error", "Invalid booking ID format.");
            response.sendRedirect(request.getContextPath() + "/customer/bookings");
            return;
        }

        BookingDAO bookingDAO = new BookingDAO();
        PaymentDAO paymentDAO = new PaymentDAO();

        Booking booking = bookingDAO.getBookingById(bookingId);
        if (booking == null || !booking.getBookerId().equals(user.getUserId())) {
            session.setAttribute("flash_error", "Booking not found.");
            response.sendRedirect(request.getContextPath() + "/customer/bookings");
            return;
        }

        Payment payment = paymentDAO.getPaymentByBookingId(bookingId);
        if (payment == null) {
            session.setAttribute("flash_error", "Payment not found.");
            response.sendRedirect(request.getContextPath() + "/customer/bookings");
            return;
        }

        // Verify payment is still PENDING
        if (!"PENDING".equalsIgnoreCase(payment.getPaymentStatus())) {
            session.setAttribute("flash_error", "Payment is not in pending state.");
            response.sendRedirect(request.getContextPath() + "/customer/bookings");
            return;
        }

        // Cancel the booking and mark payment as FAILED
        boolean cancelled = bookingDAO.cancelBookingForPayment(bookingId);

        if (cancelled) {
            session.setAttribute("flash_success", "Booking cancelled due to payment timeout.");
        } else {
            session.setAttribute("flash_error", "Failed to cancel booking.");
        }

        response.sendRedirect(request.getContextPath() + "/customer/bookings");
    }

    /**
     * Scheduled task endpoint - cancel all expired bookings This could be
     * called by a cron job or scheduled task
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // TODO: Add proper authentication for scheduled tasks

        PaymentDAO paymentDAO = new PaymentDAO();
        java.util.List<Payment> expiredPayments = paymentDAO.getExpiredPendingPayments();

        int cancelledCount = 0;
        BookingDAO bookingDAO = new BookingDAO();
        for (Payment payment : expiredPayments) {
            UUID bookingId = payment.getBookingId();
            boolean cancelled = bookingDAO.cancelBookingForPayment(bookingId);
            if (cancelled) {
                cancelledCount++;
            }
        }

        response.setContentType("text/plain");
        response.getWriter().write("Auto-cancel task completed. Cancelled " + cancelledCount + " expired bookings.");
    }

}
