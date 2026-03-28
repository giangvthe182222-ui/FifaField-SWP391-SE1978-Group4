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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * PaymentCancelServlet - Cancel booking when payment deadline expires Also
 * provides a scheduled task endpoint to auto-cancel expired payments
 */
@WebServlet(name = "PaymentCancelServlet", urlPatterns = {"/payment-cancel"})
public class PaymentCancelServlet extends HttpServlet {

    private boolean isStaffUser(User user) {
        return user != null
                && user.getRole() != null
                && user.getRole().getRoleName() != null
                && "STAFF".equalsIgnoreCase(user.getRole().getRoleName());
    }

    private String resolveHistoryPath(User user) {
        return isStaffUser(user) ? "/staff/locationBookings" : "/customer/bookings";
    }

    private String buildStaffBookingRedirect(HttpServletRequest request) {
        String locationId = request.getParameter("locationId");
        String fieldId = request.getParameter("fieldId");
        String bookingPhone = request.getParameter("bookingPhone");

        StringBuilder url = new StringBuilder(request.getContextPath()).append("/booking");
        boolean hasQuery = false;

        if (locationId != null && !locationId.isBlank()) {
            url.append(hasQuery ? '&' : '?').append("locationId=").append(locationId.trim());
            hasQuery = true;
        }

        if (fieldId != null && !fieldId.isBlank()) {
            url.append(hasQuery ? '&' : '?').append("fieldId=").append(fieldId.trim());
            hasQuery = true;
        }

        if (bookingPhone != null && !bookingPhone.isBlank()) {
            url.append(hasQuery ? '&' : '?')
                    .append("bookingPhone=")
                    .append(URLEncoder.encode(bookingPhone.trim(), StandardCharsets.UTF_8));
        }

        return url.toString();
    }

    private String resolveRedirectPath(HttpServletRequest request, User user) {
        if (isStaffUser(user) && "1".equals(request.getParameter("redirectToBooking"))) {
            return buildStaffBookingRedirect(request);
        }
        return request.getContextPath() + resolveHistoryPath(user);
    }

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
        String redirectPath = resolveRedirectPath(request, user);
        String bookingIdParam = request.getParameter("bookingId");
        String weeklyGroupIdParam = request.getParameter("weeklyGroupId");

        if (weeklyGroupIdParam != null && !weeklyGroupIdParam.isBlank()) {
            UUID weeklyGroupId;
            try {
                weeklyGroupId = UUID.fromString(weeklyGroupIdParam);
            } catch (IllegalArgumentException e) {
                session.setAttribute("flash_error", "Invalid weekly group ID format.");
                response.sendRedirect(redirectPath);
                return;
            }

            WeeklyBookingGroupDAO groupDAO = new WeeklyBookingGroupDAO();
            WeeklyBookingGroup group = groupDAO.getById(weeklyGroupId);
            if (group == null || !group.getBookerId().equals(user.getUserId())) {
                session.setAttribute("flash_error", "Weekly group not found.");
                response.sendRedirect(redirectPath);
                return;
            }

            BookingDAO bookingDAO = new BookingDAO();
            PaymentDAO paymentDAO = new PaymentDAO();
            Payment payment = paymentDAO.getPaymentByWeeklyGroupId(weeklyGroupId);

            boolean cancelled = bookingDAO.cancelWeeklyGroupForPayment(weeklyGroupId);
            if (payment != null) {
                paymentDAO.updatePaymentFailed(payment.getPaymentId());
            }
            groupDAO.updateStatus(weeklyGroupId, "cancelled");

            if (cancelled) {
                session.setAttribute("flash_success", "Weekly booking group cancelled due to payment timeout.");
            } else {
                session.setAttribute("flash_error", "Failed to cancel weekly booking group.");
            }
            response.sendRedirect(redirectPath);
            return;
        }

        if (bookingIdParam == null || bookingIdParam.isBlank()) {
            session.setAttribute("flash_error", "Invalid booking ID.");
            response.sendRedirect(redirectPath);
            return;
        }

        UUID bookingId;
        try {
            bookingId = UUID.fromString(bookingIdParam);
        } catch (IllegalArgumentException e) {
            session.setAttribute("flash_error", "Invalid booking ID format.");
            response.sendRedirect(redirectPath);
            return;
        }

        BookingDAO bookingDAO = new BookingDAO();
        PaymentDAO paymentDAO = new PaymentDAO();

        Booking booking = bookingDAO.getBookingById(bookingId);
        if (booking == null || !booking.getBookerId().equals(user.getUserId())) {
            session.setAttribute("flash_error", "Booking not found.");
            response.sendRedirect(redirectPath);
            return;
        }

        Payment payment = paymentDAO.getPaymentByBookingId(bookingId);
        if (payment == null) {
            session.setAttribute("flash_error", "Payment not found.");
            response.sendRedirect(redirectPath);
            return;
        }

        // Verify payment is still PENDING
        if (!"PENDING".equalsIgnoreCase(payment.getPaymentStatus())) {
            session.setAttribute("flash_error", "Payment is not in pending state.");
            response.sendRedirect(redirectPath);
            return;
        }

        // Cancel the booking and mark payment as FAILED
        boolean cancelled = bookingDAO.cancelBookingForPayment(bookingId);

        if (cancelled) {
            session.setAttribute("flash_success", "Booking cancelled due to payment timeout.");
        } else {
            session.setAttribute("flash_error", "Failed to cancel booking.");
        }

        response.sendRedirect(redirectPath);
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
