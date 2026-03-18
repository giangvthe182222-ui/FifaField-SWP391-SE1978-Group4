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
import java.math.BigDecimal;
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

    private String resolveBookingHistoryPath(User user) {
        if (user != null
                && user.getRole() != null
                && "STAFF".equalsIgnoreCase(user.getRole().getRoleName())) {
            return "/staff/locationBookings";
        }
        return "/customer/bookings";
    }

    private String resolveBookingDetailPath(User user) {
        if (user != null
                && user.getRole() != null
                && "STAFF".equalsIgnoreCase(user.getRole().getRoleName())) {
            return "/staff/bookingDetail";
        }
        return "/customer/bookingDetail";
    }

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
        String bookingHistoryPath = resolveBookingHistoryPath(user);
        String bookingDetailPath = resolveBookingDetailPath(user);
        String source = request.getParameter("source");
        boolean supplementarySource = "supplementary".equalsIgnoreCase(source);

        String bookingIdParam = request.getParameter("bookingId");
        if (bookingIdParam == null || bookingIdParam.isBlank()) {
            session.setAttribute("flash_error", "Invalid booking ID.");
            response.sendRedirect(request.getContextPath() + bookingHistoryPath);
            return;
        }

        UUID bookingId;
        try {
            bookingId = UUID.fromString(bookingIdParam);
        } catch (IllegalArgumentException e) {
            session.setAttribute("flash_error", "Invalid booking ID format.");
            response.sendRedirect(request.getContextPath() + bookingHistoryPath);
            return;
        }

        BookingDAO bookingDAO = new BookingDAO();
        Booking booking = bookingDAO.getBookingById(bookingId);

        if (booking == null) {
            session.setAttribute("flash_error", "Booking not found.");
            response.sendRedirect(request.getContextPath() + bookingHistoryPath);
            return;
        }

        // Authorization: customer can access only own booking; staff/manager/admin can access location bookings.
        if (!canAccessBooking(user, booking)) {
            session.setAttribute("flash_error", "Unauthorized access to booking.");
            response.sendRedirect(request.getContextPath() + bookingHistoryPath);
            return;
        }

        String resetDeadlineParam = request.getParameter("resetDeadline");
        boolean isPrivileged = false;
        if (user.getRole() != null && user.getRole().getRoleName() != null) {
            String role = user.getRole().getRoleName().trim().toLowerCase();
            isPrivileged = "staff".equals(role) || "manager".equals(role) || "admin".equals(role);
        }
        if (isPrivileged && "1".equals(resetDeadlineParam)) {
            LocalDateTime refreshedDeadline = LocalDateTime.now().plusMinutes(15);
            bookingDAO.resetPaymentDeadline(bookingId, refreshedDeadline);
            booking = bookingDAO.getBookingById(bookingId);
        }

        PaymentDAO paymentDAO = new PaymentDAO();
        Payment payment;
        LocalDateTime paymentDeadline;
        String checkoutUrl = null;

        if (supplementarySource) {
            String deadlineKey = "supp_payment_deadline_" + bookingId;
            Object storedDeadline = session.getAttribute(deadlineKey);
            paymentDeadline = storedDeadline instanceof LocalDateTime ? (LocalDateTime) storedDeadline : null;
            if (paymentDeadline == null || (isPrivileged && "1".equals(resetDeadlineParam))) {
                paymentDeadline = LocalDateTime.now().plusMinutes(15);
                session.setAttribute(deadlineKey, paymentDeadline);
            }

            String orderCodeKey = "supp_payment_order_code_" + bookingId;
            String qrCodeKey = "supp_payment_qr_code_" + bookingId;
            String checkoutUrlKey = "supp_payment_checkout_url_" + bookingId;
            String bankCodeKey = "supp_payment_bank_code_" + bookingId;
            String accountNumberKey = "supp_payment_account_number_" + bookingId;

            Long orderCode = session.getAttribute(orderCodeKey) instanceof Long
                    ? (Long) session.getAttribute(orderCodeKey)
                    : null;
            String storedQrCode = session.getAttribute(qrCodeKey) instanceof String
                    ? (String) session.getAttribute(qrCodeKey)
                    : null;
            checkoutUrl = session.getAttribute(checkoutUrlKey) instanceof String
                    ? (String) session.getAttribute(checkoutUrlKey)
                    : null;
            String storedBankCode = session.getAttribute(bankCodeKey) instanceof String
                    ? (String) session.getAttribute(bankCodeKey)
                    : QRCodeGenerator.BANK_CODE;
            String storedAccountNumber = session.getAttribute(accountNumberKey) instanceof String
                    ? (String) session.getAttribute(accountNumberKey)
                    : QRCodeGenerator.ACCOUNT_NUMBER;

                BigDecimal supplementaryAmount = bookingDAO.getSupplementaryAmountByBookingId(bookingId);

            if (orderCode == null || storedQrCode == null || storedQrCode.isBlank() || (isPrivileged && "1".equals(resetDeadlineParam))) {
                PayOSClient payOSClient = new PayOSClient();
                if (payOSClient.isConfigured()) {
                    orderCode = generateSupplementaryOrderCode();
                    String description = buildSupplementaryPayOSDescription(bookingId);
                    String paymentPageUrl = buildSupplementaryPaymentUrl(request, bookingId);

                    PayOSClient.PaymentLinkResponse payOSLink = payOSClient.createPaymentLink(
                            orderCode,
                        supplementaryAmount,
                            description,
                            bookingId,
                            paymentDeadline,
                            paymentPageUrl,
                            paymentPageUrl
                    );

                    if (payOSLink.isSuccess()) {
                        storedQrCode = payOSLink.getQrCode();
                        checkoutUrl = payOSLink.getCheckoutUrl();
                        storedBankCode = notBlank(payOSLink.getBankCode()) ? payOSLink.getBankCode() : QRCodeGenerator.BANK_CODE;
                        storedAccountNumber = notBlank(payOSLink.getAccountNumber()) ? payOSLink.getAccountNumber() : QRCodeGenerator.ACCOUNT_NUMBER;

                        session.setAttribute(orderCodeKey, orderCode);
                        session.setAttribute(qrCodeKey, storedQrCode);
                        session.setAttribute(checkoutUrlKey, checkoutUrl);
                        session.setAttribute(bankCodeKey, storedBankCode);
                        session.setAttribute(accountNumberKey, storedAccountNumber);
                    } else {
                        session.setAttribute("flash_error", "Cannot initialize supplementary payOS payment: " + payOSLink.getMessage());
                        response.sendRedirect(request.getContextPath() + bookingHistoryPath);
                        return;
                    }
                } else {
                    session.setAttribute("flash_error", "payOS config is incomplete. Missing: " + payOSClient.getMissingConfigSummary());
                    response.sendRedirect(request.getContextPath() + bookingHistoryPath);
                    return;
                }
            }

            payment = new Payment();
            payment.setPaymentId(bookingId);
            payment.setBookingId(bookingId);
            payment.setAmount(supplementaryAmount);
            payment.setPaymentMethod("payOS");
            Payment dbPayment = paymentDAO.getPaymentByBookingId(bookingId);
            String dbStatus = dbPayment == null ? "PENDING" : dbPayment.getPaymentStatus();
            payment.setPaymentStatus(dbStatus == null ? "PENDING" : dbStatus);
            payment.setTransactionCode(String.valueOf(orderCode));
            payment.setQrContent(storedQrCode);
            payment.setBankCode(storedBankCode);
            payment.setAccountNumber(storedAccountNumber);
        } else {
            payment = paymentDAO.getPaymentByBookingId(bookingId);

            if (payment == null) {
                session.setAttribute("flash_error", "Payment information not found.");
                response.sendRedirect(request.getContextPath() + bookingHistoryPath);
                return;
            }
            paymentDeadline = booking.getPaymentDeadline();
            checkoutUrl = null;
        }

        // Check payment status
        String paymentStatus = payment.getPaymentStatus();

        String qrContent = payment.getQrContent();
        if (!supplementarySource && "payOS".equalsIgnoreCase(payment.getPaymentMethod())) {
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
            if (supplementarySource) {
                session.setAttribute("flash_success", "Supplementary equipment payment completed.");
                response.sendRedirect(request.getContextPath() + bookingHistoryPath);
                return;
            }
            response.sendRedirect(request.getContextPath() + "/booking-success?bookingId=" + bookingId.toString());
            return;
        }

        if ("FAILED".equalsIgnoreCase(paymentStatus) || "CANCELLED".equalsIgnoreCase(paymentStatus)) {
            session.setAttribute("flash_error", "Payment has been " + paymentStatus.toLowerCase() + ".");
            response.sendRedirect(request.getContextPath() + bookingHistoryPath);
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        if (paymentDeadline != null && !now.isBefore(paymentDeadline)) {
            if (supplementarySource) {
                Payment dbPayment = paymentDAO.getPaymentByBookingId(bookingId);
                if (dbPayment != null) {
                    paymentDAO.updatePaymentFailed(dbPayment.getPaymentId());
                }
            } else {
                bookingDAO.cancelBookingForPayment(bookingId);
            }
            session.setAttribute("flash_error", "Payment deadline has expired.");
            response.sendRedirect(request.getContextPath() + bookingHistoryPath);
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
        request.setAttribute("bookingHistoryPath", bookingHistoryPath);
        request.setAttribute("bookingDetailPath", bookingDetailPath);
        request.setAttribute("paymentSource", supplementarySource ? "supplementary" : "booking");
        request.setAttribute("supplementaryRentalId", "");
        request.setAttribute("paymentDescription", supplementarySource ? "Thanh toán equipment bổ sung" : "Thanh toán đặt sân");

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
        String source = request.getParameter("source");
        boolean supplementarySource = "supplementary".equalsIgnoreCase(source);
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
        if (booking == null || !canAccessBooking(user, booking)) {
            writeStatus(response, "ERROR", false, 0, "Booking not found");
            return;
        }

        if (supplementarySource) {
            String deadlineKey = "supp_payment_deadline_" + bookingId;
            String orderCodeKey = "supp_payment_order_code_" + bookingId;
            Object storedDeadline = session.getAttribute(deadlineKey);
            LocalDateTime deadline = storedDeadline instanceof LocalDateTime ? (LocalDateTime) storedDeadline : LocalDateTime.now();
            LocalDateTime now = LocalDateTime.now();
            long timeRemaining = Duration.between(now, deadline).getSeconds();
            boolean expired = false;
            if (timeRemaining <= 0) {
                timeRemaining = 0;
                expired = true;
            }

            PaymentDAO paymentDAO = new PaymentDAO();
            Payment payment = paymentDAO.getPaymentByBookingId(bookingId);
            if (payment == null) {
                writeStatus(response, "ERROR", false, 0, "Payment not found");
                return;
            }
            String paymentStatus = payment.getPaymentStatus() == null ? "PENDING" : payment.getPaymentStatus();

            Long orderCode = session.getAttribute(orderCodeKey) instanceof Long
                    ? (Long) session.getAttribute(orderCodeKey)
                    : null;

            if ("PENDING".equalsIgnoreCase(paymentStatus) && orderCode != null) {
                PayOSClient payOSClient = new PayOSClient();
                if (payOSClient.isConfigured()) {
                    PayOSClient.PaymentStatusResponse statusResponse = payOSClient.getPaymentStatus(orderCode);
                    if (statusResponse.isSuccess()) {
                        String providerStatus = normalizeProviderStatus(statusResponse.getStatus());
                        if ("SUCCESS".equalsIgnoreCase(providerStatus)) {
                            paymentDAO.updatePaymentSuccess(payment.getPaymentId());
                            paymentStatus = "SUCCESS";
                        } else if ("FAILED".equalsIgnoreCase(providerStatus)) {
                            paymentDAO.updatePaymentFailed(payment.getPaymentId());
                            paymentStatus = "FAILED";
                        }
                    }
                }
            }

            if (expired && "PENDING".equalsIgnoreCase(paymentStatus)) {
                paymentDAO.updatePaymentFailed(payment.getPaymentId());
                paymentStatus = "FAILED";
            }

            writeStatus(response, paymentStatus, expired, timeRemaining, "OK");
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

    private long generateSupplementaryOrderCode() {
        long millis = System.currentTimeMillis();
        long suffix = Math.abs(java.util.concurrent.ThreadLocalRandom.current().nextInt(100, 999));
        String raw = String.valueOf(millis) + suffix;
        if (raw.length() > 18) {
            raw = raw.substring(raw.length() - 18);
        }
        return Long.parseLong(raw);
    }

    private String buildSupplementaryPayOSDescription(UUID bookingId) {
        String compactId = bookingId.toString().replace("-", "").toUpperCase();
        return "SUPP" + compactId.substring(0, Math.min(10, compactId.length()));
    }

    private String buildSupplementaryPaymentUrl(HttpServletRequest request, UUID bookingId) {
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        String contextPath = request.getContextPath();

        boolean defaultPort = ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);

        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(host);
        if (!defaultPort) {
            url.append(":").append(port);
        }
        url.append(contextPath)
            .append("/payment?source=supplementary&bookingId=")
            .append(java.net.URLEncoder.encode(bookingId.toString(), java.nio.charset.StandardCharsets.UTF_8));
        return url.toString();
    }

    private boolean canAccessBooking(User user, Booking booking) {
        if (user == null || booking == null) {
            return false;
        }

        String roleName = null;
        if (user.getRole() != null) {
            roleName = user.getRole().getRoleName();
        }

        if (roleName != null) {
            String role = roleName.trim().toLowerCase();
            if ("staff".equals(role) || "manager".equals(role) || "admin".equals(role)) {
                return true;
            }
        }

        return booking.getBookerId() != null && booking.getBookerId().equals(user.getUserId());
    }
}
