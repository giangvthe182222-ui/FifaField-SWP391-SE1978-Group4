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
import java.util.concurrent.ThreadLocalRandom;

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

        String weeklyGroupIdParam = request.getParameter("weeklyGroupId");
        if (weeklyGroupIdParam != null && !weeklyGroupIdParam.isBlank()) {
            handleWeeklyGroupPayment(request, response, user, bookingHistoryPath, bookingDetailPath, weeklyGroupIdParam);
            return;
        }

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

        // Verify booking belongs to current user
        if (!booking.getBookerId().equals(user.getUserId())) {
            session.setAttribute("flash_error", "Unauthorized access to booking.");
            response.sendRedirect(request.getContextPath() + bookingHistoryPath);
            return;
        }

        // Get payment info
        PaymentDAO paymentDAO = new PaymentDAO();
        Payment payment = paymentDAO.getPaymentByBookingId(bookingId);

        if (payment == null) {
            session.setAttribute("flash_error", "Payment information not found.");
            response.sendRedirect(request.getContextPath() + bookingHistoryPath);
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
            response.sendRedirect(request.getContextPath() + "/booking-success?bookingId=" + bookingId.toString());
            return;
        }

        if ("FAILED".equalsIgnoreCase(paymentStatus) || "CANCELLED".equalsIgnoreCase(paymentStatus)) {
            session.setAttribute("flash_error", "Payment has been " + paymentStatus.toLowerCase() + ".");
            response.sendRedirect(request.getContextPath() + bookingHistoryPath);
            return;
        }

        // Check if payment deadline has passed
        LocalDateTime paymentDeadline = booking.getPaymentDeadline();
        LocalDateTime now = LocalDateTime.now();

        if (paymentDeadline != null && !now.isBefore(paymentDeadline)) {
            // Payment expired -> release slot/equipment immediately
            bookingDAO.cancelBookingForPayment(bookingId);
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

        request.getRequestDispatcher("/View/Booking/Payment.jsp").forward(request, response);
    }

    private void handleWeeklyGroupPayment(HttpServletRequest request,
                                          HttpServletResponse response,
                                          User user,
                                          String bookingHistoryPath,
                                          String bookingDetailPath,
                                          String weeklyGroupIdParam) throws IOException, ServletException {
        UUID weeklyGroupId;
        try {
            weeklyGroupId = UUID.fromString(weeklyGroupIdParam);
        } catch (IllegalArgumentException ex) {
            request.getSession().setAttribute("flash_error", "Mã nhóm đặt tuần không hợp lệ.");
            response.sendRedirect(request.getContextPath() + bookingHistoryPath);
            return;
        }

        WeeklyBookingGroupDAO groupDAO = new WeeklyBookingGroupDAO();
        WeeklyBookingGroup group = groupDAO.getById(weeklyGroupId);
        if (group == null || user == null || !group.getBookerId().equals(user.getUserId())) {
            request.getSession().setAttribute("flash_error", "Không tìm thấy nhóm đặt tuần.");
            response.sendRedirect(request.getContextPath() + bookingHistoryPath);
            return;
        }

        BookingDAO bookingDAO = new BookingDAO();
        java.util.List<BookingViewModel> weeklyBookings = bookingDAO.getByWeeklyGroupId(weeklyGroupId);
        if (weeklyBookings == null || weeklyBookings.isEmpty()) {
            request.getSession().setAttribute("flash_error", "Nhóm đặt tuần không có booking hợp lệ.");
            response.sendRedirect(request.getContextPath() + bookingHistoryPath);
            return;
        }

        UUID representativeBookingId = weeklyBookings.get(0).getBookingId();
        Booking representativeBooking = bookingDAO.getBookingById(representativeBookingId);
        BookingViewModel representativeVm = bookingDAO.getById(representativeBookingId);

        PaymentDAO paymentDAO = new PaymentDAO();
        Payment payment = paymentDAO.getPaymentByWeeklyGroupId(weeklyGroupId);

        if (payment == null) {
            long orderCode = generateOrderCode();
            String description = buildWeeklyPayOSDescription(weeklyGroupId);
            PayOSClient payOSClient = new PayOSClient();
            PayOSClient.PaymentLinkResponse link = payOSClient.createPaymentLink(
                    orderCode,
                    group.getTotalAmount(),
                    description,
                    representativeBookingId,
                    group.getPaymentDeadline());

            if (!link.isSuccess()) {
                request.getSession().setAttribute("flash_error", "Không thể tạo link thanh toán tuần: " + link.getMessage());
                response.sendRedirect(request.getContextPath() + bookingHistoryPath);
                return;
            }

            Payment newPayment = new Payment();
            newPayment.setPaymentId(UUID.randomUUID());
            newPayment.setBookingId(representativeBookingId);
            newPayment.setWeeklyGroupId(weeklyGroupId);
            newPayment.setAmount(group.getTotalAmount());
            newPayment.setPaymentMethod("payOS");
            newPayment.setPaymentStatus("PENDING");
            newPayment.setTransactionCode(String.valueOf(link.getOrderCode()));
            newPayment.setQrContent(link.getQrCode());
            newPayment.setBankCode(link.getBankCode());
            newPayment.setAccountNumber(link.getAccountNumber());

            if (!paymentDAO.createPayment(newPayment)) {
                request.getSession().setAttribute("flash_error", "Không thể lưu thông tin thanh toán tuần.");
                response.sendRedirect(request.getContextPath() + bookingHistoryPath);
                return;
            }
            payment = paymentDAO.getPaymentByWeeklyGroupId(weeklyGroupId);
        }

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
                            bookingDAO.markWeeklyGroupPaid(weeklyGroupId);
                            groupDAO.updateStatus(weeklyGroupId, "paid");
                            request.getSession().setAttribute("flash_success", "Thanh toán lịch tuần thành công.");
                            response.sendRedirect(request.getContextPath() + bookingHistoryPath);
                            return;
                        }
                    }
                }
            }
        }

        if ("SUCCESS".equalsIgnoreCase(paymentStatus)) {
            request.getSession().setAttribute("flash_success", "Thanh toán lịch tuần thành công.");
            response.sendRedirect(request.getContextPath() + bookingHistoryPath);
            return;
        }

        LocalDateTime deadline = group.getPaymentDeadline();
        LocalDateTime now = LocalDateTime.now();
        if (deadline != null && !now.isBefore(deadline)) {
            bookingDAO.cancelWeeklyGroupForPayment(weeklyGroupId);
            paymentDAO.updatePaymentFailed(payment.getPaymentId());
            groupDAO.updateStatus(weeklyGroupId, "cancelled");
            request.getSession().setAttribute("flash_error", "Đã hết hạn thanh toán lịch tuần.");
            response.sendRedirect(request.getContextPath() + bookingHistoryPath);
            return;
        }

        long remainingSeconds = 0;
        if (deadline != null) {
            remainingSeconds = Duration.between(now, deadline).getSeconds();
            if (remainingSeconds < 0) remainingSeconds = 0;
        }

        String qrCodeURL = buildQrCodeUrl(payment, qrContent);
        String paymentDeadlineText = "";
        if (deadline != null) {
            paymentDeadlineText = deadline.format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"));
        }

        String bookingDateText = "";
        if (representativeVm != null && representativeVm.getBookingDate() != null) {
            bookingDateText = representativeVm.getBookingDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }

        request.setAttribute("booking", representativeBooking);
        request.setAttribute("bookingVM", representativeVm);
        request.setAttribute("payment", payment);
        request.setAttribute("qrCodeURL", qrCodeURL);
        request.setAttribute("qrContent", qrContent);
        request.setAttribute("timeRemaining", remainingSeconds);
        request.setAttribute("remainingSeconds", remainingSeconds);
        request.setAttribute("paymentDeadline", deadline);
        request.setAttribute("paymentDeadlineText", paymentDeadlineText);
        request.setAttribute("bookingDateText", bookingDateText);
        request.setAttribute("bankCode", payment.getBankCode());
        request.setAttribute("accountNumber", payment.getAccountNumber());
        request.setAttribute("accountName", QRCodeGenerator.ACCOUNT_NAME);
        request.setAttribute("checkoutUrl", checkoutUrl);
        request.setAttribute("bookingHistoryPath", bookingHistoryPath);
        request.setAttribute("bookingDetailPath", bookingDetailPath);
        request.setAttribute("isWeeklyGroupPayment", true);
        request.setAttribute("weeklySessionCount", weeklyBookings.size());
        request.setAttribute("weeklyGroupId", weeklyGroupId.toString());

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
                        if (payment.getWeeklyGroupId() != null) {
                            bookingDAO.markWeeklyGroupPaid(payment.getWeeklyGroupId());
                            new WeeklyBookingGroupDAO().updateStatus(payment.getWeeklyGroupId(), "paid");
                        } else {
                            bookingDAO.markBookingPaid(bookingId);
                        }
                        paymentStatus = "SUCCESS";
                    } else if ("FAILED".equalsIgnoreCase(providerStatus)) {
                        paymentDAO.updatePaymentFailed(payment.getPaymentId());
                        paymentStatus = "FAILED";
                    }
                }
            }
        }

        if (expired && "PENDING".equalsIgnoreCase(paymentStatus)) {
            if (payment.getWeeklyGroupId() != null) {
                bookingDAO.cancelWeeklyGroupForPayment(payment.getWeeklyGroupId());
                new WeeklyBookingGroupDAO().updateStatus(payment.getWeeklyGroupId(), "cancelled");
            } else {
                bookingDAO.cancelBookingForPayment(bookingId);
            }
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

    private long generateOrderCode() {
        long millis = System.currentTimeMillis();
        int randomSuffix = ThreadLocalRandom.current().nextInt(100, 1000);
        return Long.parseLong(String.valueOf(millis) + randomSuffix);
    }

    private String buildWeeklyPayOSDescription(UUID weeklyGroupId) {
        String compact = weeklyGroupId.toString().replace("-", "").toUpperCase();
        return "WEEK" + compact.substring(0, 8);
    }
}
