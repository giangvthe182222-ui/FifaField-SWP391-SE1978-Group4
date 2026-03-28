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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@WebServlet(name = "BookingConfirmServlet", urlPatterns = {"/booking-confirm"})
public class BookingConfirmServlet extends HttpServlet {

    private boolean isStaffUser(User user) {
        return user != null
                && user.getRole() != null
                && user.getRole().getRoleName() != null
                && "STAFF".equalsIgnoreCase(user.getRole().getRoleName());
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String resolveBookingHistoryPath(User user) {
        if (user != null
                && user.getRole() != null
                && "STAFF".equalsIgnoreCase(user.getRole().getRoleName())) {
            return "/staff/locationBookings";
        }
        return "/customer/bookings";
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=booking");
            return;
        }

        User user = (User) session.getAttribute("user");
        boolean staffUser = isStaffUser(user);
        UUID bookerId = user.getUserId();
        String requestedPhone = trimToNull(request.getParameter("bookingPhone"));
        String bookingHistoryPath = resolveBookingHistoryPath(user);
        if (bookerId == null) {
            session.setAttribute("flash_error", "Invalid user session.");
            response.sendRedirect(request.getContextPath() + "/booking");
            return;
        }

        String scheduleIdParam = request.getParameter("scheduleId");
        String fieldIdParam = request.getParameter("fieldId");
        if (scheduleIdParam == null || scheduleIdParam.isBlank() || fieldIdParam == null || fieldIdParam.isBlank()) {
            request.getSession().setAttribute("flash_error", "Please select a location, field and schedule.");
            response.sendRedirect(buildBookingReturnUrl(request, request.getParameter("locationId"), fieldIdParam, requestedPhone));
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
            response.sendRedirect(buildBookingReturnUrl(request, request.getParameter("locationId"), fieldIdParam, requestedPhone));
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

            // Doc so luong equipment bo sung tu cac field equipment_<equipmentId>.
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

        String bookingPhone;
        if (staffUser) {
            bookingPhone = requestedPhone;
            if (bookingPhone == null) {
                session.setAttribute("flash_error", "Staff booking requires phone number.");
                response.sendRedirect(buildBookingReturnUrl(request, locationIdParam, fieldIdParam, requestedPhone));
                return;
            }
        } else {
            bookingPhone = requestedPhone != null ? requestedPhone : trimToNull(user.getPhone());
            if (bookingPhone == null) {
                session.setAttribute("flash_error", "Please enter a contact phone number before booking.");
                response.sendRedirect(buildBookingReturnUrl(request, locationIdParam, fieldIdParam, requestedPhone));
                return;
            }
        }

        LocalDateTime paymentDeadline = LocalDateTime.now().plusMinutes(15);

        Booking booking = new Booking();
        booking.setBookingId(UUID.randomUUID());
        booking.setBookerId(bookerId);
        booking.setPhoneNumber(bookingPhone);
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
            response.sendRedirect(buildBookingReturnUrl(request, locationIdParam, fieldIdParam, requestedPhone));
            return;
        }

        // Khoi tao thanh toan payOS sau khi da tao booking va danh sach equipment.
        long orderCode = generateOrderCode();
        String description = buildPayOSDescription(booking.getBookingId());

        PayOSClient payOSClient = new PayOSClient();
        if (!payOSClient.isConfigured()) {
            session.setAttribute("flash_error", "Booking created but payOS config is incomplete. Missing: " + payOSClient.getMissingConfigSummary());
            response.sendRedirect(request.getContextPath() + bookingHistoryPath);
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
            response.sendRedirect(request.getContextPath() + bookingHistoryPath);
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
            response.sendRedirect(request.getContextPath() + bookingHistoryPath);
            return;
        }

        // Chuyen den trang payment de hien thi QR/link va hoan tat thanh toan.
        response.sendRedirect(request.getContextPath() + "/payment?bookingId=" + booking.getBookingId().toString());
    }

    private String buildBookingReturnUrl(HttpServletRequest request, String locationId, String fieldId, String bookingPhone) {
        StringBuilder sb = new StringBuilder(request.getContextPath()).append("/booking?");
        boolean hasAny = false;

        if (locationId != null && !locationId.isBlank()) {
            sb.append("locationId=").append(locationId);
            hasAny = true;
        }
        if (fieldId != null && !fieldId.isBlank()) {
            if (hasAny) sb.append("&");
            sb.append("fieldId=").append(fieldId);
            hasAny = true;
        }
        if (bookingPhone != null && !bookingPhone.isBlank()) {
            if (hasAny) sb.append("&");
            sb.append("bookingPhone=").append(URLEncoder.encode(bookingPhone, StandardCharsets.UTF_8));
            hasAny = true;
        }
        if (!hasAny) {
            return request.getContextPath() + "/booking";
        }
        return sb.toString();
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
