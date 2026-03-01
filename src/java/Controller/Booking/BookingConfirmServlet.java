package Controller.Booking;

import DAO.*;
import Models.*;
import Utils.DBConnection;

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

        Booking booking = new Booking();
        booking.setBookingId(UUID.randomUUID());
        booking.setBookerId(bookerId);
        booking.setFieldId(fieldId);
        booking.setScheduleId(scheduleId);
        booking.setVoucherId(voucherId);
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus("pending");
        booking.setTotalPrice(total);

        for (BookingEquipment be : equipmentList) {
            be.setBookingId(booking.getBookingId());
        }

        BookingDAO bookingDAO = new BookingDAO();
        boolean success = bookingDAO.insert(booking, equipmentList);

        StringBuilder redirect = new StringBuilder(request.getContextPath()).append("/booking");
        if (locationIdParam != null && !locationIdParam.isBlank()) {
            redirect.append("?locationId=").append(locationIdParam);
            if (fieldIdParam != null && !fieldIdParam.isBlank()) {
                redirect.append("&fieldId=").append(fieldIdParam);
            }
        }
        if (success) {
            session.setAttribute("flash_success", "Booking confirmed successfully!");
        } else {
            session.setAttribute("flash_error", "Failed to create booking. Please try again.");
        }
        response.sendRedirect(redirect.toString());
    }
}
