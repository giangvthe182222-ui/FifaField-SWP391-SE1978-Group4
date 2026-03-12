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
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * PaymentCheckServlet - AJAX endpoint to check payment status
 * 
 * In production, this would integrate with bank API to verify transactions.
 * For now, it provides a manual confirmation mechanism.
 */
@WebServlet(name = "PaymentCheckServlet", urlPatterns = {"/payment-check"})
public class PaymentCheckServlet extends HttpServlet {

    private void writeStatus(HttpServletResponse response,
                             String status, String message) throws IOException {
        writeStatus(response, status, "", message);
    }

    private void writeStatus(HttpServletResponse response,
                             String status, String bookingStatus, String message) throws IOException {
        response.getWriter().print("status=" + safeValue(status)
                + "\nbookingStatus=" + safeValue(bookingStatus)
                + "\nmessage=" + safeValue(message));
    }

    private String safeValue(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\n", " ").replace("\r", " ").trim();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            writeStatus(response, "ERROR", "Unauthorized");
            return;
        }

        User user = (User) session.getAttribute("user");
        String bookingIdParam = request.getParameter("bookingId");

        if (bookingIdParam == null || bookingIdParam.isBlank()) {
            writeStatus(response, "ERROR", "Missing booking ID");
            return;
        }

        UUID bookingId;
        try {
            bookingId = UUID.fromString(bookingIdParam);
        } catch (IllegalArgumentException e) {
            writeStatus(response, "ERROR", "Invalid booking ID");
            return;
        }

        BookingDAO bookingDAO = new BookingDAO();
        Booking booking = bookingDAO.getBookingById(bookingId);

        if (booking == null || !booking.getBookerId().equals(user.getUserId())) {
            writeStatus(response, "ERROR", "Booking not found");
            return;
        }

        PaymentDAO paymentDAO = new PaymentDAO();
        Payment payment = paymentDAO.getPaymentByBookingId(bookingId);

        if (payment == null) {
            writeStatus(response, "ERROR", "Payment not found");
            return;
        }

        // Check if payment deadline has expired
        LocalDateTime paymentDeadline = booking.getPaymentDeadline();
        LocalDateTime now = LocalDateTime.now();

        if (paymentDeadline != null && now.isAfter(paymentDeadline)) {
            writeStatus(response, "EXPIRED", "Payment deadline has expired");
            return;
        }

        // Return current payment status
        writeStatus(response, payment.getPaymentStatus(), booking.getStatus(),
                "Payment status retrieved successfully");

        // Log the check
        PaymentLog log = new PaymentLog(payment.getPaymentId(), "CHECK", "Status: " + payment.getPaymentStatus());
        paymentDAO.logPaymentCheck(log);
    }

    /**
     * POST - Manual payment confirmation (for testing/admin)
     * In production, this would be replaced with bank API webhook
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            writeStatus(response, "ERROR", "Unauthorized");
            return;
        }

        User user = (User) session.getAttribute("user");
        String bookingIdParam = request.getParameter("bookingId");
        String transactionCode = request.getParameter("transactionCode");

        if (bookingIdParam == null || bookingIdParam.isBlank()) {
            writeStatus(response, "ERROR", "Missing booking ID");
            return;
        }

        UUID bookingId;
        try {
            bookingId = UUID.fromString(bookingIdParam);
        } catch (IllegalArgumentException e) {
            writeStatus(response, "ERROR", "Invalid booking ID");
            return;
        }

        BookingDAO bookingDAO = new BookingDAO();
        PaymentDAO paymentDAO = new PaymentDAO();

        Booking booking = bookingDAO.getBookingById(bookingId);
        if (booking == null || !booking.getBookerId().equals(user.getUserId())) {
            writeStatus(response, "ERROR", "Booking not found");
            return;
        }

        Payment payment = paymentDAO.getPaymentByBookingId(bookingId);
        if (payment == null) {
            writeStatus(response, "ERROR", "Payment not found");
            return;
        }

        // Verify transaction code matches
        String expectedCode = payment.getTransactionCode();
        if (transactionCode == null || !transactionCode.equalsIgnoreCase(expectedCode)) {
            writeStatus(response, "ERROR", "Invalid transaction code");
            
            PaymentLog log = new PaymentLog(payment.getPaymentId(), "VERIFICATION_FAILED", "Invalid code: " + transactionCode);
            paymentDAO.logPaymentCheck(log);
            return;
        }

        // Update payment to SUCCESS
        boolean paymentUpdated = paymentDAO.updatePaymentSuccess(payment.getPaymentId(), transactionCode);
        
        // Update booking to CONFIRMED
        boolean bookingUpdated = bookingDAO.updateStatus(bookingId, "confirmed");

        if (paymentUpdated && bookingUpdated) {
            writeStatus(response, "SUCCESS", "Payment confirmed successfully!");

            PaymentLog log = new PaymentLog(payment.getPaymentId(), "SUCCESS", "Payment verified and confirmed");
            paymentDAO.logPaymentCheck(log);
        } else {
            writeStatus(response, "ERROR", "Failed to update payment/booking status");

            PaymentLog log = new PaymentLog(payment.getPaymentId(), "UPDATE_FAILED", "Database update failed");
            paymentDAO.logPaymentCheck(log);
        }
    }
}
