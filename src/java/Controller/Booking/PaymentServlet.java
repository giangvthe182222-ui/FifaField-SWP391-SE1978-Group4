package Controller.Booking;

import DAO.*;
import Models.*;
import Utils.PayOSClient;
import Utils.QRCodeGenerator;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * PaymentServlet - Display QR Code and countdown for payment
 */
@WebServlet(name = "PaymentServlet", urlPatterns = {"/payment"})
public class PaymentServlet extends HttpServlet {

    private void writeStatus(HttpServletResponse response, String paymentStatus,
                             boolean expired, long timeRemaining, String message) throws IOException {
        response.getWriter().print("paymentStatus=" + safeValue(paymentStatus)
                + "\nexpired=" + expired
                + "\ntimeRemaining=" + timeRemaining
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
        Booking booking = bookingDAO.getBookingById(bookingId);

        if (booking == null) {
            session.setAttribute("flash_error", "Booking not found.");
            response.sendRedirect(request.getContextPath() + "/customer/bookings");
            return;
        }

        // Verify booking belongs to current user
        if (!booking.getBookerId().equals(user.getUserId())) {
            session.setAttribute("flash_error", "Unauthorized access to booking.");
            response.sendRedirect(request.getContextPath() + "/customer/bookings");
            return;
        }

        // Get payment info
        PaymentDAO paymentDAO = new PaymentDAO();
        Payment payment = paymentDAO.getPaymentByBookingId(bookingId);

        if (payment == null) {
            session.setAttribute("flash_error", "Payment information not found.");
            response.sendRedirect(request.getContextPath() + "/customer/bookings");
            return;
        }

        // Check payment status
        String paymentStatus = payment.getPaymentStatus();

        String qrContent = payment.getQrContent();
        String checkoutUrl = null;
        if ("payOS".equalsIgnoreCase(payment.getPaymentMethod())) {
            PayOSClient payOSClient = new PayOSClient();
            if (payOSClient.isConfigured()) {
                Long orderCode = parseOrderCode(payment.getTransactionCode());
                if (orderCode != null) {
                    PayOSClient.PaymentStatusResponse statusResponse = payOSClient.getPaymentStatus(orderCode);
                    if (statusResponse.isSuccess()) {
                        if (notBlank(statusResponse.getQrCode())) {
                            qrContent = statusResponse.getQrCode();
                        }
                        checkoutUrl = statusResponse.getCheckoutUrl();
                        String providerStatus = normalizeProviderStatus(statusResponse.getStatus());
                        if ("SUCCESS".equalsIgnoreCase(providerStatus) && !"SUCCESS".equalsIgnoreCase(paymentStatus)) {
                            paymentDAO.updatePaymentSuccess(payment.getPaymentId());
                            bookingDAO.markBookingPaid(bookingId);
                            paymentStatus = "SUCCESS";
                        }
                    }
                }
            }
        }

        if ("SUCCESS".equalsIgnoreCase(paymentStatus)) {
            session.setAttribute("flash_success", "Payment already completed!");
            response.sendRedirect(request.getContextPath() + "/customer/bookingDetail?id=" + bookingId.toString());
            return;
        }

        if ("FAILED".equalsIgnoreCase(paymentStatus) || "CANCELLED".equalsIgnoreCase(paymentStatus)) {
            session.setAttribute("flash_error", "Payment has been " + paymentStatus.toLowerCase() + ".");
            response.sendRedirect(request.getContextPath() + "/customer/bookings");
            return;
        }

        // Check if payment deadline has passed
        LocalDateTime paymentDeadline = booking.getPaymentDeadline();
        LocalDateTime now = LocalDateTime.now();

        if (paymentDeadline != null && !now.isBefore(paymentDeadline)) {
            // Payment expired -> release slot/equipment immediately
            bookingDAO.cancelBookingForPayment(bookingId);
            session.setAttribute("flash_error", "Payment deadline has expired.");
            response.sendRedirect(request.getContextPath() + "/customer/bookings");
            return;
        }

        // Calculate remaining time in seconds
        long remainingSeconds = 0;
        if (paymentDeadline != null) {
            Duration duration = Duration.between(now, paymentDeadline);
            remainingSeconds = duration.getSeconds();
            if (remainingSeconds < 0) remainingSeconds = 0;
        }

        // Get or generate QR code URL
        String qrCodeURL = buildQrCodeUrl(payment, qrContent);

        // Get booking details for display
        BookingViewModel bookingVM = bookingDAO.getById(bookingId);

        String paymentDeadlineText = "";
        if (paymentDeadline != null) {
            paymentDeadlineText = paymentDeadline.format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"));
        }

        String bookingDateText = "";
        if (bookingVM != null && bookingVM.getBookingDate() != null) {
            bookingDateText = bookingVM.getBookingDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }

        // Set attributes for JSP
        request.setAttribute("booking", booking);
        request.setAttribute("bookingVM", bookingVM);
        request.setAttribute("payment", payment);
        request.setAttribute("qrCodeURL", qrCodeURL);
        request.setAttribute("qrContent", qrContent);
        request.setAttribute("timeRemaining", remainingSeconds);
        request.setAttribute("remainingSeconds", remainingSeconds);
        request.setAttribute("paymentDeadline", paymentDeadline);
        request.setAttribute("paymentDeadlineText", paymentDeadlineText);
        request.setAttribute("bookingDateText", bookingDateText);
        request.setAttribute("bankCode", payment.getBankCode());
        request.setAttribute("accountNumber", payment.getAccountNumber());
        request.setAttribute("accountName", QRCodeGenerator.ACCOUNT_NAME);
        request.setAttribute("checkoutUrl", checkoutUrl);

        request.getRequestDispatcher("/View/Booking/Payment.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        if (!"check_payment".equalsIgnoreCase(action)) {
            writeStatus(response, "ERROR", false, 0, "Invalid action");
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            writeStatus(response, "ERROR", false, 0, "Unauthorized");
            return;
        }

        User user = (User) session.getAttribute("user");
        String bookingIdParam = request.getParameter("bookingId");
        if (bookingIdParam == null || bookingIdParam.isBlank()) {
            writeStatus(response, "ERROR", false, 0, "Missing bookingId");
            return;
        }

        UUID bookingId;
        try {
            bookingId = UUID.fromString(bookingIdParam);
        } catch (IllegalArgumentException e) {
            writeStatus(response, "ERROR", false, 0, "Invalid bookingId");
            return;
        }

        BookingDAO bookingDAO = new BookingDAO();
        Booking booking = bookingDAO.getBookingById(bookingId);
        if (booking == null || !booking.getBookerId().equals(user.getUserId())) {
            writeStatus(response, "ERROR", false, 0, "Booking not found");
            return;
        }

        PaymentDAO paymentDAO = new PaymentDAO();
        Payment payment = paymentDAO.getPaymentByBookingId(bookingId);
        if (payment == null) {
            writeStatus(response, "ERROR", false, 0, "Payment not found");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = booking.getPaymentDeadline();
        long timeRemaining = 0;
        boolean expired = false;

        if (deadline != null) {
            timeRemaining = Duration.between(now, deadline).getSeconds();
            if (timeRemaining <= 0) {
                timeRemaining = 0;
                expired = true;
            }
        }

        String paymentStatus = payment.getPaymentStatus() == null ? "PENDING" : payment.getPaymentStatus();

        if ("payOS".equalsIgnoreCase(payment.getPaymentMethod()) && "PENDING".equalsIgnoreCase(paymentStatus)) {
            PayOSClient payOSClient = new PayOSClient();
            Long orderCode = parseOrderCode(payment.getTransactionCode());
            if (payOSClient.isConfigured() && orderCode != null) {
                PayOSClient.PaymentStatusResponse statusResponse = payOSClient.getPaymentStatus(orderCode);
                if (statusResponse.isSuccess()) {
                    String providerStatus = normalizeProviderStatus(statusResponse.getStatus());
                    if ("SUCCESS".equalsIgnoreCase(providerStatus)) {
                        paymentDAO.updatePaymentSuccess(payment.getPaymentId());
                        bookingDAO.markBookingPaid(bookingId);
                        paymentStatus = "SUCCESS";
                    } else if ("FAILED".equalsIgnoreCase(providerStatus)) {
                        paymentDAO.updatePaymentFailed(payment.getPaymentId());
                        paymentStatus = "FAILED";
                    }
                }
            }
        }

        if (expired && "PENDING".equalsIgnoreCase(paymentStatus)) {
            bookingDAO.cancelBookingForPayment(bookingId);
            paymentStatus = "FAILED";
        }

        writeStatus(response, paymentStatus, expired, timeRemaining, "OK");
    }

    private String buildQrCodeUrl(Payment payment, String qrContent) throws IOException {
        if (qrContent == null || qrContent.trim().isEmpty()) {
            return "";
        }

        if ("payOS".equalsIgnoreCase(payment.getPaymentMethod())) {
            if (qrContent.startsWith("http://") || qrContent.startsWith("https://")) {
                return qrContent;
            }
            return "https://api.qrserver.com/v1/create-qr-code/?size=320x320&data="
                    + URLEncoder.encode(qrContent, StandardCharsets.UTF_8.name());
        }

        return QRCodeGenerator.generateQRCodeURL(payment.getAmount(), qrContent);
    }

    private String normalizeProviderStatus(String providerStatus) {
        if (providerStatus == null) {
            return "PENDING";
        }

        String status = providerStatus.trim().toUpperCase();
        if ("PAID".equals(status) || "SUCCESS".equals(status) || "SUCCEEDED".equals(status)) {
            return "SUCCESS";
        }
        if ("CANCELLED".equals(status) || "FAILED".equals(status) || "EXPIRED".equals(status)) {
            return "FAILED";
        }
        return "PENDING";
    }

    private Long parseOrderCode(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
