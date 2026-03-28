package Controller.Booking;

import DAO.BookingDAO;
import DAO.LocationEquipmentDAO;
import DAO.VoucherDAO;
import DAO.WeeklyBookingGroupDAO;
import Models.Booking;
import Models.BookingEquipment;
import Models.LocationEquipmentViewModel;
import Models.User;
import Models.Voucher;
import Utils.DBConnection;

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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@WebServlet(name = "WeeklyBookingConfirmServlet", urlPatterns = {"/booking/weekly-confirm"})
public class WeeklyBookingConfirmServlet extends HttpServlet {

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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user    = (User) session.getAttribute("user");
        UUID bookerId = user.getUserId();
        boolean staffUser = isStaffUser(user);
        String requestedPhone = trimToNull(request.getParameter("bookingPhone"));
        String bookingPhone;
        if (staffUser) {
            bookingPhone = requestedPhone;
            if (bookingPhone == null) {
                session.setAttribute("flash_error", "Staff booking requires phone number.");
                response.sendRedirect(buildReturnUrl(request, request.getParameter("locationId"), request.getParameter("fieldId"), request.getParameter("weekStart"), requestedPhone));
                return;
            }
        } else {
            bookingPhone = requestedPhone != null ? requestedPhone : trimToNull(user.getPhone());
            if (bookingPhone == null) {
                session.setAttribute("flash_error", "Vui lòng nhập số điện thoại liên hệ trước khi đặt sân.");
                response.sendRedirect(buildReturnUrl(request, request.getParameter("locationId"), request.getParameter("fieldId"), request.getParameter("weekStart"), requestedPhone));
                return;
            }
        }

        String fieldIdParam    = request.getParameter("fieldId");
        String locationIdParam = request.getParameter("locationId");
        String weekStartParam  = request.getParameter("weekStart");
        String[] scheduleIdParams = request.getParameterValues("scheduleIds");

        // --- Input validation ---
        if (fieldIdParam == null || fieldIdParam.isBlank()) {
            session.setAttribute("flash_error", "Vui lòng chọn sân.");
            response.sendRedirect(request.getContextPath() + "/booking/weekly");
            return;
        }

        if (scheduleIdParams == null || scheduleIdParams.length == 0) {
            session.setAttribute("flash_error", "Vui lòng chọn ít nhất một khung giờ trong tuần.");
            response.sendRedirect(buildReturnUrl(request, locationIdParam, fieldIdParam, weekStartParam, requestedPhone));
            return;
        }

        UUID fieldId = null;
        try {
            fieldId = UUID.fromString(fieldIdParam);
        } catch (IllegalArgumentException e) {
            session.setAttribute("flash_error", "Dữ liệu sân không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/booking/weekly");
            return;
        }

        List<UUID> scheduleIds = new ArrayList<>();
        for (String sid : scheduleIdParams) {
            try { scheduleIds.add(UUID.fromString(sid.trim())); }
            catch (IllegalArgumentException ignored) {}
        }

        if (scheduleIds.isEmpty()) {
            session.setAttribute("flash_error", "Dữ liệu khung giờ không hợp lệ.");
            response.sendRedirect(buildReturnUrl(request, locationIdParam, fieldIdParam, weekStartParam, requestedPhone));
            return;
        }

        // --- Equipment (optional) ---
        List<BookingEquipment> equipmentList = new ArrayList<>();
        if (locationIdParam != null && !locationIdParam.isBlank()) {
            try {
                UUID locationId = UUID.fromString(locationIdParam);
                LocationEquipmentDAO locEquipDAO = new LocationEquipmentDAO(new DBConnection());
                List<LocationEquipmentViewModel> availEquip = locEquipDAO.getByLocation(locationId);
                for (LocationEquipmentViewModel e : availEquip) {
                    String paramName = "equipment_" + e.getEquipmentId();
                    String qtyStr = request.getParameter(paramName);
                    int qty = 0;
                    if (qtyStr != null && !qtyStr.isBlank()) {
                        try { qty = Math.max(0, Math.min(Integer.parseInt(qtyStr), e.getQuantity())); }
                        catch (NumberFormatException ignored) {}
                    }
                    if (qty > 0) {
                        BookingEquipment be = new BookingEquipment();
                        be.setEquipmentId(e.getEquipmentId());
                        be.setQuantity(qty);
                        equipmentList.add(be);
                    }
                }
            } catch (Exception ignored) {}
        }

        // --- Voucher (optional) ---
        UUID voucherId = null;
        BigDecimal discountPercent = BigDecimal.ZERO;
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

        // For weekly bookings give users 2 hours to pay each booking
        LocalDateTime paymentDeadline = LocalDateTime.now().plusHours(2);
        UUID weeklyGroupId = UUID.randomUUID();

        WeeklyBookingGroupDAO weeklyGroupDAO = new WeeklyBookingGroupDAO();
        Models.WeeklyBookingGroup group = new Models.WeeklyBookingGroup();
        group.setWeeklyGroupId(weeklyGroupId);
        group.setBookerId(bookerId);
        group.setStatus("pending");
        group.setPaymentDeadline(paymentDeadline);
        group.setTotalAmount(BigDecimal.ZERO);

        if (!weeklyGroupDAO.create(group)) {
            session.setAttribute("flash_error", "Không thể tạo nhóm đặt sân theo tuần.");
            response.sendRedirect(buildReturnUrl(request, locationIdParam, fieldIdParam, weekStartParam, requestedPhone));
            return;
        }

        // --- Atomic weekly insert ---
        BookingDAO bookingDAO = new BookingDAO();
        try {
            List<Booking> created = bookingDAO.insertWeekly(
                    bookerId, fieldId, scheduleIds, equipmentList, voucherId, discountPercent, paymentDeadline, weeklyGroupId, bookingPhone);

            BigDecimal total = BigDecimal.ZERO;
            for (Booking b : created) {
                if (b.getTotalPrice() != null) {
                    total = total.add(b.getTotalPrice());
                }
            }
            weeklyGroupDAO.updateTotalAmount(weeklyGroupId, total);

            // Jump straight to weekly payment flow; success/failure will return to role-based booking history.
            response.sendRedirect(request.getContextPath() + "/payment?weeklyGroupId=" + weeklyGroupId);

        } catch (Exception e) {
            weeklyGroupDAO.delete(weeklyGroupId);
            String msg = e.getMessage();
            if (msg == null || msg.isBlank()) msg = "Đặt sân theo tuần thất bại. Vui lòng thử lại.";
            session.setAttribute("flash_error", msg);
            response.sendRedirect(buildReturnUrl(request, locationIdParam, fieldIdParam, weekStartParam, requestedPhone));
        }
    }

    private String buildReturnUrl(HttpServletRequest req,
                                  String locationId, String fieldId, String weekStart, String bookingPhone) {
        StringBuilder sb = new StringBuilder(req.getContextPath() + "/booking/weekly?");
        if (locationId != null && !locationId.isBlank()) sb.append("locationId=").append(locationId).append("&");
        if (fieldId    != null && !fieldId.isBlank())    sb.append("fieldId=").append(fieldId).append("&");
        if (weekStart  != null && !weekStart.isBlank()) {
            // Normalize to Monday
            try {
                LocalDate d = LocalDate.parse(weekStart).with(DayOfWeek.MONDAY);
                sb.append("weekStart=").append(d);
            } catch (Exception ignored) {
                sb.append("weekStart=").append(weekStart);
            }
        }
        if (bookingPhone != null && !bookingPhone.isBlank()) {
            sb.append("&bookingPhone=").append(URLEncoder.encode(bookingPhone, StandardCharsets.UTF_8));
        }
        return sb.toString();
    }
}
