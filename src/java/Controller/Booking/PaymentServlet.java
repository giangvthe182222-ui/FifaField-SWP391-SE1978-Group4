package Controller.Booking;

import DAO.*;
import Models.*;
import Utils.PayOSClient;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * PaymentServlet - Display QR Code and countdown for payment
 */
@WebServlet(name = "PaymentServlet", urlPatterns = {"/payment"})
public class PaymentServlet extends HttpServlet {

    private static final String DEFAULT_BANK_CODE = "BIDV";
    private static final String DEFAULT_ACCOUNT_NUMBER = "8828154445";
    private static final String DEFAULT_ACCOUNT_NAME = "FIFA FIELD";
    private static final String PAYMENT_METHOD_PREFIX_PAYOS = "payos";
    private static final String PAYMENT_PLAN_DEPOSIT = "deposit";
    private static final String PAYMENT_PLAN_REMAINING = "remaining";
    private static final String PAYMENT_PLAN_FULL = "full";
    private static final BigDecimal DEPOSIT_RATE = new BigDecimal("0.30");

    private static String payloadKey(UUID bookingId) {
        return "supp_equipment_payload_" + bookingId;
    }

    private static String amountKey(UUID bookingId) {
        return "supp_equipment_amount_" + bookingId;
    }

    private static class SupplementaryDraft {
        private final List<BookingEquipment> equipments;
        private final BigDecimal amount;

        private SupplementaryDraft(List<BookingEquipment> equipments, BigDecimal amount) {
            this.equipments = equipments;
            this.amount = amount;
        }
    }

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
        boolean remainingSource = "remaining".equalsIgnoreCase(source);

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

        if (remainingSource) {
            paymentDeadline = null;

            Payment dbPayment = paymentDAO.getPaymentByBookingId(bookingId);
            if (dbPayment == null) {
                session.setAttribute("flash_error", "Payment information not found.");
                response.sendRedirect(request.getContextPath() + bookingHistoryPath);
                return;
            }

            if (!canStartRemainingPayment(booking, dbPayment)) {
                session.setAttribute("flash_error", "Remaining payment is only available for deposited or checked out bookings.");
                response.sendRedirect(request.getContextPath() + bookingHistoryPath);
                return;
            }

            String orderCodeKey = "remaining_payment_order_code_" + bookingId;
            String qrCodeKey = "remaining_payment_qr_code_" + bookingId;
            String checkoutUrlKey = "remaining_payment_checkout_url_" + bookingId;
            String bankCodeKey = "remaining_payment_bank_code_" + bookingId;
            String accountNumberKey = "remaining_payment_account_number_" + bookingId;

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
                    : DEFAULT_BANK_CODE;
            String storedAccountNumber = session.getAttribute(accountNumberKey) instanceof String
                    ? (String) session.getAttribute(accountNumberKey)
                    : DEFAULT_ACCOUNT_NUMBER;

            BigDecimal remainingAmount = resolveRemainingAmount(booking, dbPayment);
            if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
                if (!applyBookingStatusAfterRemainingSuccess(bookingDAO, bookingId)) {
                    session.setAttribute("flash_error", "No remaining amount but booking status update failed.");
                } else {
                    session.setAttribute("flash_success", "Booking has no remaining amount.");
                }
                response.sendRedirect(request.getContextPath() + bookingHistoryPath);
                return;
            }

            if (orderCode == null || storedQrCode == null || storedQrCode.isBlank() || (isPrivileged && "1".equals(resetDeadlineParam))) {
                PayOSClient payOSClient = new PayOSClient();
                if (!payOSClient.isConfigured()) {
                    session.setAttribute("flash_error", "payOS config is incomplete. Missing: " + payOSClient.getMissingConfigSummary());
                    response.sendRedirect(request.getContextPath() + bookingHistoryPath);
                    return;
                }

                orderCode = generateOrderCode();
                String description = buildRemainingPayOSDescription(bookingId);
                String paymentPageUrl = buildRemainingPaymentUrl(request, bookingId);

                PayOSClient.PaymentLinkResponse payOSLink = payOSClient.createPaymentLink(
                        orderCode,
                        remainingAmount,
                        description,
                        bookingId,
                        resolveRemainingPayOSExpiry(),
                        paymentPageUrl,
                        paymentPageUrl
                );

                if (!payOSLink.isSuccess()) {
                    session.setAttribute("flash_error", "Cannot initialize remaining payment: " + payOSLink.getMessage());
                    response.sendRedirect(request.getContextPath() + bookingHistoryPath);
                    return;
                }

                storedQrCode = payOSLink.getQrCode();
                checkoutUrl = payOSLink.getCheckoutUrl();
                storedBankCode = notBlank(payOSLink.getBankCode()) ? payOSLink.getBankCode() : DEFAULT_BANK_CODE;
                storedAccountNumber = notBlank(payOSLink.getAccountNumber()) ? payOSLink.getAccountNumber() : DEFAULT_ACCOUNT_NUMBER;

                session.setAttribute(orderCodeKey, orderCode);
                session.setAttribute(qrCodeKey, storedQrCode);
                session.setAttribute(checkoutUrlKey, checkoutUrl);
                session.setAttribute(bankCodeKey, storedBankCode);
                session.setAttribute(accountNumberKey, storedAccountNumber);

                paymentDAO.markRemainingPending(
                        bookingId,
                        remainingAmount,
                        "payOS|remaining",
                        String.valueOf(orderCode),
                        storedQrCode,
                        storedBankCode,
                        storedAccountNumber
                );
                dbPayment = paymentDAO.getPaymentByBookingId(bookingId);
                if (dbPayment == null) {
                    session.setAttribute("flash_error", "Cannot refresh remaining payment information.");
                    response.sendRedirect(request.getContextPath() + bookingHistoryPath);
                    return;
                }
            }

            payment = dbPayment;
        } else if (supplementarySource) {
            // Supplementary equipment payment can be completed at any time in app flow.
            // Keep payment deadline null to disable local expiration logic.
            paymentDeadline = null;

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
                    : DEFAULT_BANK_CODE;
            String storedAccountNumber = session.getAttribute(accountNumberKey) instanceof String
                    ? (String) session.getAttribute(accountNumberKey)
                    : DEFAULT_ACCOUNT_NUMBER;

            Payment dbPayment = paymentDAO.getPaymentByBookingId(bookingId);
            if (dbPayment == null) {
                session.setAttribute("flash_error", "Payment information not found.");
                response.sendRedirect(request.getContextPath() + bookingHistoryPath);
                return;
            }

            BigDecimal supplementaryAmount = dbPayment.getAmount();
            if (supplementaryAmount == null || supplementaryAmount.compareTo(BigDecimal.ZERO) <= 0) {
                supplementaryAmount = bookingDAO.getSupplementaryAmountByBookingId(bookingId);
            }

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
                            resolveSupplementaryPayOSExpiry(),
                            paymentPageUrl,
                            paymentPageUrl
                        );

                    if (payOSLink.isSuccess()) {
                        storedQrCode = payOSLink.getQrCode();
                        checkoutUrl = payOSLink.getCheckoutUrl();
                        storedBankCode = notBlank(payOSLink.getBankCode()) ? payOSLink.getBankCode() : DEFAULT_BANK_CODE;
                        storedAccountNumber = notBlank(payOSLink.getAccountNumber()) ? payOSLink.getAccountNumber() : DEFAULT_ACCOUNT_NUMBER;

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
        if (!supplementarySource && isPayOSMethod(payment.getPaymentMethod())) {
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
                            if (remainingSource) {
                                if (!applyBookingStatusAfterRemainingSuccess(bookingDAO, bookingId)) {
                                    session.setAttribute("flash_error", "Payment succeeded but booking status update failed.");
                                    response.sendRedirect(request.getContextPath() + bookingHistoryPath);
                                    return;
                                }
                            } else if (!applyBookingStatusAfterPaymentSuccess(bookingDAO, payment, bookingId)) {
                                session.setAttribute("flash_error", "Payment succeeded but booking status update failed.");
                                response.sendRedirect(request.getContextPath() + bookingHistoryPath);
                                return;
                            }
                            paymentStatus = "SUCCESS";
                        }
                    }
                }
            }
        }

        if ("SUCCESS".equalsIgnoreCase(paymentStatus)) {
            if (supplementarySource) {
                if (!finalizeSupplementaryIfNeeded(session, bookingId, bookingDAO)) {
                    session.setAttribute("flash_error", "Payment succeeded but failed to apply supplementary equipment. Please contact support.");
                    response.sendRedirect(request.getContextPath() + bookingHistoryPath);
                    return;
                }
                if (!bookingDAO.settlePendingExtraStatus(bookingId)) {
                    session.setAttribute("flash_error", "Payment succeeded but booking status settlement failed. Please contact support.");
                    response.sendRedirect(request.getContextPath() + bookingHistoryPath);
                    return;
                }
                session.setAttribute("flash_success", "Supplementary equipment payment completed.");
                response.sendRedirect(request.getContextPath() + bookingHistoryPath);
                return;
            }
            if (remainingSource) {
                if (!applyBookingStatusAfterRemainingSuccess(bookingDAO, bookingId)) {
                    session.setAttribute("flash_error", "Payment succeeded but booking status update failed.");
                    response.sendRedirect(request.getContextPath() + bookingHistoryPath);
                    return;
                }
                session.setAttribute("flash_success", "Thanh toán phần còn lại thành công.");
                response.sendRedirect(request.getContextPath() + bookingHistoryPath);
                return;
            }
            if (!applyBookingStatusAfterPaymentSuccess(bookingDAO, payment, bookingId)) {
                session.setAttribute("flash_error", "Payment succeeded but booking status update failed.");
                response.sendRedirect(request.getContextPath() + bookingHistoryPath);
                return;
            }
            session.setAttribute("flash_success", "Thanh toán đặt sân thành công.");
            response.sendRedirect(request.getContextPath() + bookingHistoryPath);
            return;
        }

        if ("FAILED".equalsIgnoreCase(paymentStatus) || "CANCELLED".equalsIgnoreCase(paymentStatus)) {
            session.setAttribute("flash_error", "Payment has been " + paymentStatus.toLowerCase() + ".");
            response.sendRedirect(request.getContextPath() + bookingHistoryPath);
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        if (!supplementarySource && !remainingSource && paymentDeadline != null && !now.isBefore(paymentDeadline)) {
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
        String qrCodeURL = buildQrCodeUrl(qrContent);

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

        BigDecimal bookingTotalAmount = booking.getTotalPrice() == null ? BigDecimal.ZERO : booking.getTotalPrice();
        boolean depositPayment = !supplementarySource && !remainingSource && isDepositPaymentMethod(payment.getPaymentMethod());
        BigDecimal remainingAmount = BigDecimal.ZERO;
        if (depositPayment) {
            remainingAmount = bookingTotalAmount.subtract(payment.getAmount() == null ? BigDecimal.ZERO : payment.getAmount());
            if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
                remainingAmount = BigDecimal.ZERO;
            }
        }
        String remainingMethod = "bank_transfer";

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
        request.setAttribute("accountName", DEFAULT_ACCOUNT_NAME);
        request.setAttribute("checkoutUrl", checkoutUrl);
        request.setAttribute("bookingHistoryPath", bookingHistoryPath);
        request.setAttribute("bookingDetailPath", bookingDetailPath);
        request.setAttribute("paymentSource", supplementarySource ? "supplementary" : (remainingSource ? "remaining" : "booking"));
        request.setAttribute("supplementaryRentalId", "");
        request.setAttribute("paymentDescription", supplementarySource
            ? "Thanh toán equipment bổ sung"
            : (remainingSource ? "Thanh toán phần còn lại" : "Thanh toán đặt sân"));
        request.setAttribute("bookingTotalAmount", bookingTotalAmount);
        request.setAttribute("isDepositPayment", depositPayment);
        request.setAttribute("remainingAmount", remainingAmount);
        request.setAttribute("paymentMethodLabel", toPaymentMethodLabel(payment.getPaymentMethod(), supplementarySource));
        request.setAttribute("remainingPaymentMethodLabel", toRemainingPaymentMethodLabel(remainingMethod));

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
        String paymentOption = normalizePaymentOption(request.getParameter("paymentOption"));

        if (payment == null) {
            long orderCode = generateOrderCode();
            String description = buildWeeklyPayOSDescription(weeklyGroupId);
            BigDecimal amountDueNow = resolveWeeklyAmountDueNow(group.getTotalAmount(), paymentOption);
            PayOSClient payOSClient = new PayOSClient();
            PayOSClient.PaymentLinkResponse link = payOSClient.createPaymentLink(
                    orderCode,
                amountDueNow,
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
            newPayment.setAmount(amountDueNow);
            newPayment.setPaymentMethod(buildWeeklyPaymentMethodMetadata(paymentOption));
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
                            if (!applyWeeklyGroupPaymentSuccess(weeklyGroupId, payment, weeklyBookings, bookingDAO, paymentDAO, groupDAO)) {
                                request.getSession().setAttribute("flash_error", "Thanh toán tuần thành công nhưng không thể cập nhật trạng thái booking.");
                                response.sendRedirect(request.getContextPath() + bookingHistoryPath);
                                return;
                            }
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

        String qrCodeURL = buildQrCodeUrl(qrContent);
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
        request.setAttribute("accountName", DEFAULT_ACCOUNT_NAME);
        request.setAttribute("checkoutUrl", checkoutUrl);
        request.setAttribute("bookingHistoryPath", bookingHistoryPath);
        request.setAttribute("bookingDetailPath", bookingDetailPath);
        request.setAttribute("isWeeklyGroupPayment", true);
        request.setAttribute("isDepositPayment", isDepositPaymentMethod(payment.getPaymentMethod()));
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
        String source = request.getParameter("source");
        boolean supplementarySource = "supplementary".equalsIgnoreCase(source);
        boolean remainingSource = "remaining".equalsIgnoreCase(source);
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
            String orderCodeKey = "supp_payment_order_code_" + bookingId;
            long timeRemaining = 0;
            boolean expired = false;

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
                            if (!finalizeSupplementaryIfNeeded(session, bookingId, bookingDAO)) {
                                writeStatus(response, "ERROR", false, 0, "Payment succeeded but supplementary equipment finalize failed");
                                return;
                            }
                            if (!bookingDAO.settlePendingExtraStatus(bookingId)) {
                                writeStatus(response, "ERROR", false, 0, "Payment succeeded but booking status settlement failed");
                                return;
                            }
                            paymentStatus = "SUCCESS";
                        } else if ("FAILED".equalsIgnoreCase(providerStatus)) {
                            paymentDAO.updatePaymentFailed(payment.getPaymentId());
                            paymentStatus = "FAILED";
                        }
                    }
                }
            }

            writeStatus(response, paymentStatus, expired, timeRemaining, "OK");
            return;
        }

        if (remainingSource) {
            String orderCodeKey = "remaining_payment_order_code_" + bookingId;
            long timeRemaining = 0;
            boolean expired = false;

            PaymentDAO paymentDAO = new PaymentDAO();
            Payment payment = paymentDAO.getPaymentByBookingId(bookingId);
            if (payment == null) {
                writeStatus(response, "ERROR", false, 0, "Payment not found");
                return;
            }

            String paymentStatus = payment.getPaymentStatus() == null ? "PENDING" : payment.getPaymentStatus();
            Long orderCode = session.getAttribute(orderCodeKey) instanceof Long
                    ? (Long) session.getAttribute(orderCodeKey)
                    : parseOrderCode(payment.getTransactionCode());

            if ("PENDING".equalsIgnoreCase(paymentStatus) && orderCode != null) {
                PayOSClient payOSClient = new PayOSClient();
                if (payOSClient.isConfigured()) {
                    PayOSClient.PaymentStatusResponse statusResponse = payOSClient.getPaymentStatus(orderCode);
                    if (statusResponse.isSuccess()) {
                        String providerStatus = normalizeProviderStatus(statusResponse.getStatus());
                        if ("SUCCESS".equalsIgnoreCase(providerStatus)) {
                            paymentDAO.updatePaymentSuccess(payment.getPaymentId());
                            if (!applyBookingStatusAfterRemainingSuccess(bookingDAO, bookingId)) {
                                writeStatus(response, "ERROR", false, 0, "Payment succeeded but booking status update failed");
                                return;
                            }
                            paymentStatus = "SUCCESS";
                        } else if ("FAILED".equalsIgnoreCase(providerStatus)) {
                            paymentDAO.updatePaymentFailed(payment.getPaymentId());
                            paymentStatus = "FAILED";
                        }
                    }
                }
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

        if (isPayOSMethod(payment.getPaymentMethod()) && "PENDING".equalsIgnoreCase(paymentStatus)) {
            PayOSClient payOSClient = new PayOSClient();
            Long orderCode = parseOrderCode(payment.getTransactionCode());
            if (payOSClient.isConfigured() && orderCode != null) {
                PayOSClient.PaymentStatusResponse statusResponse = payOSClient.getPaymentStatus(orderCode);
                if (statusResponse.isSuccess()) {
                    String providerStatus = normalizeProviderStatus(statusResponse.getStatus());
                    if ("SUCCESS".equalsIgnoreCase(providerStatus)) {
                        paymentDAO.updatePaymentSuccess(payment.getPaymentId());
                        if (payment.getWeeklyGroupId() != null) {
                                WeeklyBookingGroupDAO groupDAO = new WeeklyBookingGroupDAO();
                                java.util.List<BookingViewModel> weeklyBookings = bookingDAO.getByWeeklyGroupId(payment.getWeeklyGroupId());
                                if (!applyWeeklyGroupPaymentSuccess(payment.getWeeklyGroupId(), payment, weeklyBookings, bookingDAO, paymentDAO, groupDAO)) {
                                    writeStatus(response, "ERROR", false, timeRemaining, "Payment succeeded but weekly booking settlement failed");
                                    return;
                                }
                        } else {
                            if (!applyBookingStatusAfterPaymentSuccess(bookingDAO, payment, bookingId)) {
                                writeStatus(response, "ERROR", false, timeRemaining, "Payment succeeded but booking status update failed");
                                return;
                            }
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

        if ("SUCCESS".equalsIgnoreCase(paymentStatus) && payment.getWeeklyGroupId() == null) {
            if (!applyBookingStatusAfterPaymentSuccess(bookingDAO, payment, bookingId)) {
                writeStatus(response, "ERROR", false, timeRemaining, "Payment succeeded but booking status update failed");
                return;
            }
        }

        writeStatus(response, paymentStatus, expired, timeRemaining, "OK");
    }

    private String buildQrCodeUrl(String qrContent) throws IOException {
        if (qrContent == null || qrContent.trim().isEmpty()) {
            return "";
        }

        if (qrContent.startsWith("http://") || qrContent.startsWith("https://")) {
            return qrContent;
        }

        return "https://api.qrserver.com/v1/create-qr-code/?size=320x320&data="
                + URLEncoder.encode(qrContent, StandardCharsets.UTF_8.name());
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

    private boolean isPayOSMethod(String paymentMethod) {
        return paymentMethod != null && paymentMethod.trim().toLowerCase().startsWith(PAYMENT_METHOD_PREFIX_PAYOS);
    }

    private boolean isDepositPaymentMethod(String paymentMethod) {
        return PAYMENT_PLAN_DEPOSIT.equalsIgnoreCase(extractPaymentPlan(paymentMethod));
    }

    private String extractPaymentPlan(String paymentMethod) {
        if (paymentMethod == null) {
            return null;
        }
        String[] parts = paymentMethod.trim().toLowerCase().split("\\|");
        if (parts.length >= 2) {
            return parts[1];
        }
        return null;
    }

    private String toPaymentMethodLabel(String paymentMethod, boolean supplementarySource) {
        if (supplementarySource) {
            return "payOS (supplementary)";
        }
        if (PAYMENT_PLAN_REMAINING.equalsIgnoreCase(extractPaymentPlan(paymentMethod))) {
            return "payOS - thanh toan phan con lai";
        }
        if (isDepositPaymentMethod(paymentMethod)) {
            return "payOS - dat coc 30%";
        }
        if (PAYMENT_PLAN_FULL.equalsIgnoreCase(extractPaymentPlan(paymentMethod))) {
            return "payOS - thanh toan toan bo";
        }
        if (isPayOSMethod(paymentMethod)) {
            return "payOS";
        }
        return paymentMethod == null ? "--" : paymentMethod;
    }

    private String toRemainingPaymentMethodLabel(String remainingMethod) {
        if (remainingMethod == null) {
            return "--";
        }
        if ("cash".equalsIgnoreCase(remainingMethod)) {
            return "Tien mat";
        }
        if ("bank_transfer".equalsIgnoreCase(remainingMethod)) {
            return "Chuyen khoan";
        }
        return remainingMethod;
    }

    private String normalizePaymentOption(String value) {
        if (value == null) {
            return PAYMENT_PLAN_FULL;
        }
        String normalized = value.trim().toLowerCase();
        if (PAYMENT_PLAN_DEPOSIT.equals(normalized)) {
            return PAYMENT_PLAN_DEPOSIT;
        }
        return PAYMENT_PLAN_FULL;
    }

    private BigDecimal resolveWeeklyAmountDueNow(BigDecimal totalAmount, String paymentOption) {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (PAYMENT_PLAN_DEPOSIT.equals(paymentOption)) {
            BigDecimal deposit = totalAmount.multiply(DEPOSIT_RATE).setScale(0, RoundingMode.HALF_UP);
            if (deposit.compareTo(BigDecimal.ZERO) <= 0) {
                return totalAmount;
            }
            return deposit;
        }
        return totalAmount;
    }

    private String buildWeeklyPaymentMethodMetadata(String paymentOption) {
        if (PAYMENT_PLAN_DEPOSIT.equals(paymentOption)) {
            return "payOS|deposit";
        }
        return "payOS|full";
    }

    private String normalizeBookingState(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private String getBookingPlayStatus(Booking booking) {
        if (booking == null) {
            return "";
        }
        return normalizeBookingState(booking.getPlayStatus());
    }

    private String getBookingPaymentStatus(Booking booking) {
        if (booking == null) {
            return "";
        }
        return normalizeBookingState(booking.getPaymentStatus());
    }

    private boolean canStartRemainingPayment(Booking booking, Payment payment) {
        if (booking == null || payment == null) {
            return false;
        }

        String bookingPaymentStatus = getBookingPaymentStatus(booking);
        String bookingPlayStatus = getBookingPlayStatus(booking);
        if (!"deposited".equals(bookingPaymentStatus)
            && !"checked out".equals(bookingPlayStatus)) {
            return false;
        }

        if (!isPayOSMethod(payment.getPaymentMethod())) {
            return false;
        }

        return resolveRemainingAmount(booking, payment).compareTo(BigDecimal.ZERO) > 0;
    }

    private BigDecimal resolveRemainingAmount(Booking booking, Payment payment) {
        BigDecimal totalAmount = booking != null && booking.getTotalPrice() != null
                ? booking.getTotalPrice()
                : BigDecimal.ZERO;

        if (payment == null) {
            return BigDecimal.ZERO;
        }

        String plan = extractPaymentPlan(payment.getPaymentMethod());
        if (PAYMENT_PLAN_DEPOSIT.equalsIgnoreCase(plan)) {
            BigDecimal paidPart = payment.getAmount() == null ? BigDecimal.ZERO : payment.getAmount();
            BigDecimal remaining = totalAmount.subtract(paidPart);
            return remaining.compareTo(BigDecimal.ZERO) > 0 ? remaining : BigDecimal.ZERO;
        }

        if (PAYMENT_PLAN_REMAINING.equalsIgnoreCase(plan)) {
            BigDecimal remaining = payment.getAmount() == null ? BigDecimal.ZERO : payment.getAmount();
            return remaining.compareTo(BigDecimal.ZERO) > 0 ? remaining : BigDecimal.ZERO;
        }

        String bookingPlayStatus = getBookingPlayStatus(booking);
        if ("checked out".equals(bookingPlayStatus)) {
            BigDecimal paidPart = payment.getAmount() == null ? BigDecimal.ZERO : payment.getAmount();
            BigDecimal remaining = totalAmount.subtract(paidPart);
            return remaining.compareTo(BigDecimal.ZERO) > 0 ? remaining : BigDecimal.ZERO;
        }

        return BigDecimal.ZERO;
    }

    private boolean applyBookingStatusAfterRemainingSuccess(BookingDAO bookingDAO, UUID bookingId) {
        return settleBookingAfterOnlinePayment(bookingDAO, bookingId, true);
    }

    private boolean applyBookingStatusAfterPaymentSuccess(BookingDAO bookingDAO, Payment payment, UUID bookingId) {
        if (bookingDAO == null || payment == null || bookingId == null) {
            return false;
        }

        Booking latestBooking = bookingDAO.getBookingById(bookingId);
        String currentPaymentStatus = getBookingPaymentStatus(latestBooking);

        if (isDepositPaymentMethod(payment.getPaymentMethod())) {
            if ("deposited".equals(currentPaymentStatus)) {
                return true;
            }
            return bookingDAO.markBookingDeposited(bookingId);
        }

        if (!"paid".equals(currentPaymentStatus)) {
            if (!bookingDAO.markBookingPaid(bookingId)) {
                return false;
            }
        }

        // Online payment success should auto settle checked-out bookings.
        return settleBookingAfterOnlinePayment(bookingDAO, bookingId, false);
    }

    private boolean settleBookingAfterOnlinePayment(BookingDAO bookingDAO, UUID bookingId, boolean upgradeDepositedToPaid) {
        if (bookingDAO == null || bookingId == null) {
            return false;
        }

        Booking booking = bookingDAO.getBookingById(bookingId);
        if (booking == null) {
            return false;
        }

        String playStatus = getBookingPlayStatus(booking);
        String paymentStatus = getBookingPaymentStatus(booking);
        String extraPaymentStatus = normalizeBookingState(booking.getExtraPaymentStatus());
        if (extraPaymentStatus.isEmpty()) {
            extraPaymentStatus = "none";
        }

        if (upgradeDepositedToPaid && "deposited".equals(paymentStatus)) {
            if (!bookingDAO.markBookingPaid(bookingId)) {
                return false;
            }
            booking = bookingDAO.getBookingById(bookingId);
            if (booking == null) {
                return false;
            }
            playStatus = getBookingPlayStatus(booking);
            paymentStatus = getBookingPaymentStatus(booking);
            extraPaymentStatus = normalizeBookingState(booking.getExtraPaymentStatus());
            if (extraPaymentStatus.isEmpty()) {
                extraPaymentStatus = "none";
            }
        }

        // Only checked-out bookings are auto-promoted to completed.
        if (!"checked out".equals(playStatus)) {
            return true;
        }

        if (!"paid".equals(paymentStatus)) {
            if (!bookingDAO.markBookingPaid(bookingId)) {
                return false;
            }
            booking = bookingDAO.getBookingById(bookingId);
            if (booking == null) {
                return false;
            }
            paymentStatus = getBookingPaymentStatus(booking);
            extraPaymentStatus = normalizeBookingState(booking.getExtraPaymentStatus());
            if (extraPaymentStatus.isEmpty()) {
                extraPaymentStatus = "none";
            }
            if (!"paid".equals(paymentStatus)) {
                return false;
            }
        }

        if ("pending extra".equals(extraPaymentStatus)) {
            if (!bookingDAO.updateSplitStates(bookingId, "checked out", "paid", "paid extra")) {
                return false;
            }
        }

        Booking latest = bookingDAO.getBookingById(bookingId);
        if (latest != null && "completed".equals(getBookingPlayStatus(latest))) {
            return true;
        }

        return bookingDAO.updateStatus(bookingId, "completed");
    }

    private boolean applyWeeklyGroupPaymentSuccess(UUID weeklyGroupId,
                                                   Payment weeklyPayment,
                                                   java.util.List<BookingViewModel> weeklyBookings,
                                                   BookingDAO bookingDAO,
                                                   PaymentDAO paymentDAO,
                                                   WeeklyBookingGroupDAO groupDAO) {
        if (weeklyGroupId == null || weeklyPayment == null || weeklyBookings == null || weeklyBookings.isEmpty()) {
            return false;
        }

        boolean depositPlan = isDepositPaymentMethod(weeklyPayment.getPaymentMethod());
        String bookingPaymentMethod = depositPlan ? "payOS|deposit" : "payOS|full";

        for (BookingViewModel vm : weeklyBookings) {
            if (vm == null || vm.getBookingId() == null) {
                return false;
            }

            BigDecimal bookingTotal = vm.getTotalPrice() == null ? BigDecimal.ZERO : vm.getTotalPrice();
            BigDecimal paidAmount = depositPlan
                    ? resolveWeeklyAmountDueNow(bookingTotal, PAYMENT_PLAN_DEPOSIT)
                    : bookingTotal;

            if (!paymentDAO.upsertSettledBookingPayment(vm.getBookingId(), paidAmount, bookingPaymentMethod, "SUCCESS")) {
                return false;
            }

            Payment perBookingPayment = new Payment();
            perBookingPayment.setPaymentMethod(bookingPaymentMethod);
            if (!applyBookingStatusAfterPaymentSuccess(bookingDAO, perBookingPayment, vm.getBookingId())) {
                return false;
            }
        }

        return groupDAO.updateStatus(weeklyGroupId, depositPlan ? "deposited" : "paid");
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

    private long generateSupplementaryOrderCode() {
        long millis = System.currentTimeMillis();
        long suffix = Math.abs(ThreadLocalRandom.current().nextInt(100, 999));
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

    private String buildRemainingPayOSDescription(UUID bookingId) {
        String compactId = bookingId.toString().replace("-", "").toUpperCase();
        return "REST" + compactId.substring(0, Math.min(10, compactId.length()));
    }

    private LocalDateTime resolveSupplementaryPayOSExpiry() {
        return LocalDateTime.now().plusYears(1);
    }

    private LocalDateTime resolveRemainingPayOSExpiry() {
        return LocalDateTime.now().plusYears(1);
    }

    // Applies supplementary draft only after payment callback confirms success.
    private boolean finalizeSupplementaryIfNeeded(HttpSession session, UUID bookingId, BookingDAO bookingDAO) {
        SupplementaryDraft draft = readSupplementaryDraft(session, bookingId);
        if (draft == null) {
            return true;
        }
        boolean ok = bookingDAO.finalizeSupplementaryEquipment(bookingId, draft.equipments, draft.amount);
        if (ok) {
            session.removeAttribute(payloadKey(bookingId));
            session.removeAttribute(amountKey(bookingId));
        }
        return ok;
    }

    // Reads draft payload created by StaffAddSupplementaryEquipmentServlet.doPost.
    private SupplementaryDraft readSupplementaryDraft(HttpSession session, UUID bookingId) {
        Object payloadObj = session.getAttribute(payloadKey(bookingId));
        Object amountObj = session.getAttribute(amountKey(bookingId));

        if (!(payloadObj instanceof String) || !(amountObj instanceof String)) {
            return null;
        }

        String payload = ((String) payloadObj).trim();
        String amountRaw = ((String) amountObj).trim();

        if (payload.isEmpty() || amountRaw.isEmpty()) {
            return null;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountRaw);
        } catch (NumberFormatException ex) {
            return null;
        }

        List<BookingEquipment> list = new ArrayList<>();
        String[] items = payload.split(",");
        for (String item : items) {
            if (item == null || item.isBlank()) {
                continue;
            }

            String[] parts = item.split(":");
            if (parts.length != 2) {
                continue;
            }

            try {
                UUID equipmentId = UUID.fromString(parts[0].trim());
                int quantity = Integer.parseInt(parts[1].trim());
                if (quantity <= 0) {
                    continue;
                }
                BookingEquipment be = new BookingEquipment();
                be.setBookingId(bookingId);
                be.setEquipmentId(equipmentId);
                be.setQuantity(quantity);
                list.add(be);
            } catch (Exception ignored) {
                // Ignore malformed draft row and continue parsing remaining rows.
            }
        }

        if (list.isEmpty() || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        return new SupplementaryDraft(list, amount);
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

    private String buildRemainingPaymentUrl(HttpServletRequest request, UUID bookingId) {
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
                .append("/payment?source=remaining&bookingId=")
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
