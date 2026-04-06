package Controller.Booking;

import DAO.BookingDAO;
import DAO.PaymentDAO;
import DAO.WeeklyBookingGroupDAO;
import Models.Booking;
import Models.Payment;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.BufferedReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adapter endpoint for payment providers to confirm successful QR payments.
 * This endpoint intentionally accepts either form parameters or a simple JSON body
 * so the project can be wired to a QR/payment provider without changing booking flow.
 */
@WebServlet(name = "PaymentWebhookServlet", urlPatterns = {"/payment-webhook"})
public class PaymentWebhookServlet extends HttpServlet {

    private static final Pattern JSON_STRING_PATTERN_TEMPLATE = Pattern.compile("\"%s\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern JSON_NUMBER_PATTERN_TEMPLATE = Pattern.compile("\"%s\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)");
    private static final Pattern JSON_BOOLEAN_PATTERN_TEMPLATE = Pattern.compile("\"%s\"\\s*:\\s*(true|false)", Pattern.CASE_INSENSITIVE);
    private static final BigDecimal DEPOSIT_RATE = new BigDecimal("0.30");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String configuredSecret = System.getenv("FFF_PAYMENT_WEBHOOK_SECRET");
        String providedSecret = firstNonBlank(
                request.getHeader("X-Webhook-Secret"),
                request.getParameter("secret")
        );

        if (configuredSecret == null || configuredSecret.isBlank()) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.getWriter().write("{\"success\":false,\"message\":\"Webhook secret is not configured\"}");
            return;
        }

        if (!configuredSecret.equals(providedSecret)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"success\":false,\"message\":\"Invalid webhook secret\"}");
            return;
        }

        String rawBody = readRequestBody(request);
        String paymentStatus = firstNonBlank(
                request.getParameter("paymentStatus"),
                request.getParameter("status"),
                extractJsonString(rawBody, "paymentStatus"),
                extractJsonString(rawBody, "status"),
                extractJsonString(rawBody, "code")
        );

        boolean success = isSuccessStatus(paymentStatus)
                || Boolean.parseBoolean(firstNonBlank(
                        request.getParameter("success"),
                        extractJsonBoolean(rawBody, "success")
                ));

        String bookingIdRaw = firstNonBlank(
                request.getParameter("bookingId"),
                extractJsonString(rawBody, "bookingId")
        );

        String referenceCode = firstNonBlank(
                request.getParameter("transactionCode"),
                request.getParameter("referenceCode"),
                request.getParameter("description"),
                extractJsonString(rawBody, "transactionCode"),
                extractJsonString(rawBody, "referenceCode"),
                extractJsonString(rawBody, "description")
        );

        String orderCode = firstNonBlank(
            request.getParameter("orderCode"),
            extractJsonNumber(rawBody, "orderCode")
        );

        String amountRaw = firstNonBlank(
                request.getParameter("amount"),
                extractJsonNumber(rawBody, "amount")
        );

        if (!success) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\":false,\"message\":\"Payment callback is not marked successful\"}");
            return;
        }

        PaymentDAO paymentDAO = new PaymentDAO();
        Payment payment = null;

        if (bookingIdRaw != null && !bookingIdRaw.isBlank()) {
            try {
                payment = paymentDAO.getPaymentByBookingId(UUID.fromString(bookingIdRaw));
            } catch (IllegalArgumentException ignored) {
                // Fall back to reference code below.
            }
        }

        if (payment == null && referenceCode != null && !referenceCode.isBlank()) {
            payment = paymentDAO.getPaymentByTransactionCode(referenceCode);
        }

        if (payment == null && orderCode != null && !orderCode.isBlank()) {
            payment = paymentDAO.getPaymentByTransactionCode(orderCode);
        }

        if (payment == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"success\":false,\"message\":\"Payment not found\"}");
            return;
        }

        if (amountRaw != null && !amountRaw.isBlank()) {
            try {
                BigDecimal callbackAmount = new BigDecimal(amountRaw);
                if (payment.getAmount() != null && payment.getAmount().compareTo(callbackAmount) != 0) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"success\":false,\"message\":\"Amount mismatch\"}");
                    return;
                }
            } catch (NumberFormatException ex) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"success\":false,\"message\":\"Invalid amount\"}");
                return;
            }
        }

        boolean paymentUpdated = paymentDAO.updatePaymentSuccess(payment.getPaymentId());
        boolean bookingUpdated;
        if (payment.getWeeklyGroupId() != null) {
            BookingDAO bookingDAO = new BookingDAO();
            WeeklyBookingGroupDAO groupDAO = new WeeklyBookingGroupDAO();
            List<Models.BookingViewModel> weeklyBookings = bookingDAO.getByWeeklyGroupId(payment.getWeeklyGroupId());
            bookingUpdated = applyWeeklyGroupPaymentSuccess(payment.getWeeklyGroupId(), payment, weeklyBookings, bookingDAO, paymentDAO, groupDAO);
        } else {
            bookingUpdated = applySingleBookingPaymentSuccess(new BookingDAO(), payment, payment.getBookingId());
        }

        if (!paymentUpdated || !bookingUpdated) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().write("{\"success\":false,\"message\":\"Unable to update payment or booking state\"}");
            return;
        }

        response.getWriter().write("{\"success\":true,\"message\":\"Payment confirmed\"}");
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            char[] buffer = new char[1024];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                body.append(buffer, 0, read);
            }
        }
        return new String(body.toString().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }

    private boolean isSuccessStatus(String paymentStatus) {
        if (paymentStatus == null) {
            return false;
        }
        String normalized = paymentStatus.trim().toUpperCase();
        return "SUCCESS".equals(normalized) || "SUCCEEDED".equals(normalized) || "00".equals(normalized) || "PAID".equals(normalized);
    }

    private boolean applySingleBookingPaymentSuccess(BookingDAO bookingDAO, Payment payment, UUID bookingId) {
        if (bookingDAO == null || payment == null || bookingId == null) {
            return false;
        }

        String paymentMethod = payment.getPaymentMethod() == null ? "" : payment.getPaymentMethod().trim().toLowerCase();
        boolean depositPlan = paymentMethod.contains("|deposit");
        if (depositPlan) {
            Booking booking = bookingDAO.getBookingById(bookingId);
            if (booking != null && "deposited".equals(normalizeBookingState(booking.getPaymentStatus()))) {
                return true;
            }
            return bookingDAO.markBookingDeposited(bookingId);
        }

        Booking booking = bookingDAO.getBookingById(bookingId);
        if (booking == null) {
            return false;
        }

        String paymentStatus = normalizeBookingState(booking.getPaymentStatus());
        if (!"paid".equals(paymentStatus)) {
            if (!bookingDAO.markBookingPaid(bookingId)) {
                return false;
            }
            booking = bookingDAO.getBookingById(bookingId);
            if (booking == null) {
                return false;
            }
        }

        String playStatus = normalizeBookingState(booking.getPlayStatus());
        if (!"checked out".equals(playStatus)) {
            return true;
        }

        String extraPaymentStatus = normalizeBookingState(booking.getExtraPaymentStatus());
        if ("pending extra".equals(extraPaymentStatus)) {
            if (!bookingDAO.updateSplitStates(bookingId, "checked out", "paid", "paid extra")) {
                return false;
            }
        }

        Booking latest = bookingDAO.getBookingById(bookingId);
        if (latest != null && "completed".equals(normalizeBookingState(latest.getPlayStatus()))) {
            return true;
        }

        return bookingDAO.updateStatus(bookingId, "completed");
    }

    private String normalizeBookingState(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private BigDecimal resolveDepositAmount(BigDecimal totalAmount) {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal deposit = totalAmount.multiply(DEPOSIT_RATE).setScale(0, RoundingMode.HALF_UP);
        if (deposit.compareTo(BigDecimal.ZERO) <= 0) {
            return totalAmount;
        }
        return deposit;
    }

    private boolean applyWeeklyGroupPaymentSuccess(UUID weeklyGroupId,
                                                   Payment weeklyPayment,
                                                   List<Models.BookingViewModel> weeklyBookings,
                                                   BookingDAO bookingDAO,
                                                   PaymentDAO paymentDAO,
                                                   WeeklyBookingGroupDAO groupDAO) {
        if (weeklyGroupId == null || weeklyPayment == null || weeklyBookings == null || weeklyBookings.isEmpty()) {
            return false;
        }

        String paymentMethod = weeklyPayment.getPaymentMethod() == null ? "" : weeklyPayment.getPaymentMethod().trim().toLowerCase();
        boolean depositPlan = paymentMethod.contains("|deposit");
        String bookingPaymentMethod = depositPlan ? "payOS|deposit" : "payOS|full";

        for (Models.BookingViewModel vm : weeklyBookings) {
            if (vm == null || vm.getBookingId() == null) {
                return false;
            }
            BigDecimal bookingTotal = vm.getTotalPrice() == null ? BigDecimal.ZERO : vm.getTotalPrice();
            BigDecimal paidAmount = depositPlan ? resolveDepositAmount(bookingTotal) : bookingTotal;

            if (!paymentDAO.upsertSettledBookingPayment(vm.getBookingId(), paidAmount, bookingPaymentMethod, "SUCCESS")) {
                return false;
            }

            Payment perBookingPayment = new Payment();
            perBookingPayment.setPaymentMethod(bookingPaymentMethod);
            if (!applySingleBookingPaymentSuccess(bookingDAO, perBookingPayment, vm.getBookingId())) {
                return false;
            }
        }

        return groupDAO.updateStatus(weeklyGroupId, depositPlan ? "deposited" : "paid");
    }

    private String extractJsonString(String json, String fieldName) {
        if (json == null || json.isBlank() || fieldName == null || fieldName.isBlank()) {
            return null;
        }
        Pattern pattern = Pattern.compile(String.format(JSON_STRING_PATTERN_TEMPLATE.pattern(), Pattern.quote(fieldName)));
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String extractJsonNumber(String json, String fieldName) {
        if (json == null || json.isBlank() || fieldName == null || fieldName.isBlank()) {
            return null;
        }
        Pattern pattern = Pattern.compile(String.format(JSON_NUMBER_PATTERN_TEMPLATE.pattern(), Pattern.quote(fieldName)));
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String extractJsonBoolean(String json, String fieldName) {
        if (json == null || json.isBlank() || fieldName == null || fieldName.isBlank()) {
            return null;
        }
        Pattern pattern = Pattern.compile(String.format(JSON_BOOLEAN_PATTERN_TEMPLATE.pattern(), Pattern.quote(fieldName)), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }
}