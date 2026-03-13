package Controller.Booking;

import DAO.*;
import Models.*;
import Utils.DBConnection;
import Utils.PayOSClient;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@WebServlet(name = "BookingConfirmServlet", urlPatterns = {"/booking-confirm"})
public class BookingConfirmServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=booking");
            return;
        }

        User user = (User) session.getAttribute("user");
        UUID bookerId = user.getUserId();
        if (bookerId == null) {
            session.setAttribute("flash_error", "Invalid user session.");
            response.sendRedirect(request.getContextPath() + "/booking");
            return;
        }

        String scheduleIdParam = request.getParameter("scheduleId");
        String fieldIdParam = request.getParameter("fieldId");
        if (scheduleIdParam == null || scheduleIdParam.isBlank() || fieldIdParam == null || fieldIdParam.isBlank()) {
            request.getSession().setAttribute("flash_error", "Please select a location, field and schedule.");
            response.sendRedirect(request.getContextPath() + "/booking");
            return;
        }

        UUID scheduleId = UUID.fromString(scheduleIdParam);
        UUID fieldId = UUID.fromString(fieldIdParam);

        ScheduleDAO scheduleDAO = new ScheduleDAO();
        Schedule schedule = null;
        for (Schedule s : scheduleDAO.getScheduleByField(fieldId)) {
            if (s.getScheduleId().equals(scheduleId)) {
                schedule = s;
                break;
            }
        }
        if (schedule == null) {
            request.getSession().setAttribute("flash_error", "Selected schedule is not valid.");
            response.sendRedirect(request.getContextPath() + "/booking?" + request.getQueryString());
            return;
        }

        BigDecimal schedulePrice = schedule.getPrice() != null ? schedule.getPrice() : BigDecimal.ZERO;
        BigDecimal equipmentTotal = BigDecimal.ZERO;
        List<BookingEquipment> equipmentList = new ArrayList<>();

        String locationIdParam = request.getParameter("locationId");
        if (locationIdParam != null && !locationIdParam.isBlank()) {
            UUID locationId = UUID.fromString(locationIdParam);
            LocationEquipmentDAO locEquipDAO = new LocationEquipmentDAO(new DBConnection());
            List<LocationEquipmentViewModel> equipments = locEquipDAO.getByLocation(locationId);

            for (LocationEquipmentViewModel e : equipments) {
                String paramName = "equipment_" + e.getEquipmentId();
                String qtyStr = request.getParameter(paramName);
                int qty = 0;
                if (qtyStr != null && !qtyStr.isBlank()) {
                    try {
                        qty = Integer.parseInt(qtyStr);
                        qty = Math.max(0, Math.min(qty, e.getQuantity()));
                    } catch (NumberFormatException ignored) {}
                }
                if (qty > 0 && e.getRentalPrice() != null) {
                    equipmentTotal = equipmentTotal.add(e.getRentalPrice().multiply(BigDecimal.valueOf(qty)));
                    BookingEquipment be = new BookingEquipment();
                    be.setEquipmentId(e.getEquipmentId());
                    be.setQuantity(qty);
                    equipmentList.add(be);
                }
            }
        }

        BigDecimal subtotal = schedulePrice.add(equipmentTotal);
        BigDecimal discountPercent = BigDecimal.ZERO;
        UUID voucherId = null;

        String voucherIdParam = request.getParameter("voucherId");
        if (voucherIdParam != null && !voucherIdParam.isBlank()) {
            try {
                voucherId = UUID.fromString(voucherIdParam);
                VoucherDAO voucherDAO = new VoucherDAO();
                Voucher v = voucherDAO.getVoucherById(voucherId.toString());
                if (v != null && v.getDiscountValue() != null) {
                    discountPercent = v.getDiscountValue();
                }
            } catch (Exception ignored) {}
        }

        BigDecimal total = subtotal.multiply(BigDecimal.ONE.subtract(discountPercent.divide(BigDecimal.valueOf(100))));

        LocalDateTime paymentDeadline = LocalDateTime.now().plusMinutes(15);

        Booking booking = new Booking();
        booking.setBookingId(UUID.randomUUID());
        booking.setBookerId(bookerId);
        booking.setFieldId(fieldId);
        booking.setScheduleId(scheduleId);
        booking.setVoucherId(voucherId);
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus("pending");
        booking.setTotalPrice(total);
        booking.setPaymentDeadline(paymentDeadline);

        for (BookingEquipment be : equipmentList) {
            be.setBookingId(booking.getBookingId());
        }

        BookingDAO bookingDAO = new BookingDAO();
        boolean success = bookingDAO.insert(booking, equipmentList);

        if (!success) {
            String insertError = bookingDAO.getLastInsertError();
            if (insertError == null || insertError.isBlank()) {
                insertError = "Failed to create booking. Please try again.";
            }
            session.setAttribute("flash_error", insertError);
            StringBuilder redirect = new StringBuilder(request.getContextPath()).append("/booking");
            if (locationIdParam != null && !locationIdParam.isBlank()) {
                redirect.append("?locationId=").append(locationIdParam);
                if (fieldIdParam != null && !fieldIdParam.isBlank()) {
                    redirect.append("&fieldId=").append(fieldIdParam);
                }
            }
            response.sendRedirect(redirect.toString());
            return;
        }

        // Create Payment record using payOS
        long orderCode = generateOrderCode();
        String description = buildPayOSDescription(booking.getBookingId());

        PayOSClient payOSClient = new PayOSClient();
        if (!payOSClient.isConfigured()) {
            session.setAttribute("flash_error", "Booking created but payOS config is incomplete. Missing: " + payOSClient.getMissingConfigSummary());
            response.sendRedirect(request.getContextPath() + "/customer/bookings");
            return;
        }

        String paymentPageUrl = buildCurrentAppPaymentUrl(request);

        PayOSClient.PaymentLinkResponse payOSLink = payOSClient.createPaymentLink(
                orderCode,
                total,
                description,
                booking.getBookingId(),
            paymentDeadline,
            paymentPageUrl,
            paymentPageUrl
        );

        if (!payOSLink.isSuccess()) {
            session.setAttribute("flash_error", "Booking created but payOS payment initialization failed: " + payOSLink.getMessage());
            response.sendRedirect(request.getContextPath() + "/customer/bookings");
            return;
        }

        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID());
        payment.setBookingId(booking.getBookingId());
        payment.setAmount(total);
        payment.setPaymentMethod("payOS");
        payment.setPaymentStatus("PENDING");
        payment.setBankCode(payOSLink.getBankCode());
        payment.setAccountNumber(payOSLink.getAccountNumber());
        payment.setQrContent(payOSLink.getQrCode());
        payment.setTransactionCode(String.valueOf(orderCode));

        PaymentDAO paymentDAO = new PaymentDAO();
        boolean paymentCreated = paymentDAO.createPayment(payment);

        if (!paymentCreated) {
            String paymentError = paymentDAO.getLastError();
            if (paymentError == null || paymentError.isBlank()) {
                paymentError = "Unknown database error.";
            }
            session.setAttribute("flash_error", "Booking created but payment initialization failed. " + paymentError);
            response.sendRedirect(request.getContextPath() + "/customer/bookings");
            return;
        }

        // Redirect to payment page
        session.setAttribute("flash_success", "Booking created! Please complete payment within 15 minutes.");
        response.sendRedirect(request.getContextPath() + "/payment?bookingId=" + booking.getBookingId().toString());
    }

    private String buildCurrentAppPaymentUrl(HttpServletRequest request) {
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
        url.append(contextPath).append("/payment");
        return url.toString();
    }

    private long generateOrderCode() {
        long millis = System.currentTimeMillis();
        int randomSuffix = ThreadLocalRandom.current().nextInt(100, 1000);
        return Long.parseLong(String.valueOf(millis) + randomSuffix);
    }

    private String buildPayOSDescription(UUID bookingId) {
        String compact = bookingId.toString().replace("-", "").toUpperCase();
        return "FFF" + compact.substring(0, 10);
    }
}
