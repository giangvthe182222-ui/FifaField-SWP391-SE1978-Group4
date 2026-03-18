package Controller.Booking;

import DAO.BookingDAO;
import DAO.LocationEquipmentDAO;
import DAO.PaymentDAO;
import Models.BookingEquipment;
import Models.BookingEquipmentViewModel;
import Models.BookingViewModel;
import Models.LocationEquipmentViewModel;
import Models.Payment;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import Utils.DBConnection;

@WebServlet(name = "StaffBookingDetailServlet", urlPatterns = {"/staff/bookingDetail"})
public class StaffBookingDetailServlet extends HttpServlet {

    private static String payloadKey(UUID bookingId) {
        return "supp_equipment_payload_" + bookingId;
    }

    private static String amountKey(UUID bookingId) {
        return "supp_equipment_amount_" + bookingId;
    }

    private static String serializeEquipmentPayload(List<BookingEquipment> equipmentList) {
        StringBuilder sb = new StringBuilder();
        for (BookingEquipment be : equipmentList) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(be.getEquipmentId()).append(':').append(be.getQuantity());
        }
        return sb.toString();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=staff/locationBookings");
            return;
        }

        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/staff/locationBookings");
            return;
        }

        UUID bookingId = UUID.fromString(idParam);
        BookingDAO bookingDAO = new BookingDAO();
        BookingViewModel booking = bookingDAO.getById(bookingId);
        if (booking == null) {
            session.setAttribute("flash_error", "Booking not found.");
            response.sendRedirect(request.getContextPath() + "/staff/locationBookings");
            return;
        }

        List<BookingEquipmentViewModel> equipments = bookingDAO.getBookingEquipments(bookingId);
        boolean canAddEquipment = isEquipmentBookingAllowed(booking);
        List<LocationEquipmentViewModel> availableEquipments = new ArrayList<>();

        if (canAddEquipment && booking.getLocationId() != null) {
            LocationEquipmentDAO locationEquipmentDAO = new LocationEquipmentDAO(new DBConnection());
            for (LocationEquipmentViewModel equipment : locationEquipmentDAO.getByLocation(booking.getLocationId())) {
                if (equipment.getQuantity() > 0 && "available".equalsIgnoreCase(equipment.getStatus())) {
                    availableEquipments.add(equipment);
                }
            }
        }

        boolean canCheckIn = "paid".equals(normalizeStatus(booking.getStatus()))
                && booking.getBookingDate() != null
                && booking.getStartTime() != null
                && !LocalDateTime.of(booking.getBookingDate(), booking.getStartTime()).isAfter(LocalDateTime.now());
        boolean canRefund = "pending refund".equals(normalizeStatus(booking.getStatus()));

        moveFlashMessages(session, request);

        request.setAttribute("booking", booking);
        request.setAttribute("equipments", equipments);
        request.setAttribute("canAddEquipment", canAddEquipment);
        request.setAttribute("availableEquipments", availableEquipments);
        request.setAttribute("canCheckIn", canCheckIn);
        request.setAttribute("canRefund", canRefund);
        request.getRequestDispatcher("/View/Booking/StaffBookingDetail.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=staff/locationBookings");
            return;
        }

        String idParam = request.getParameter("id");
        String action = request.getParameter("action");
        if (idParam == null || idParam.isBlank()) {
            session.setAttribute("flash_error", "Invalid request.");
            response.sendRedirect(request.getContextPath() + "/staff/locationBookings");
            return;
        }

        if ("addEquipment".equalsIgnoreCase(action)) {
            handleAddEquipment(request, response, session, UUID.fromString(idParam));
            return;
        }

        String status = request.getParameter("status");
        if (status == null || status.isBlank()) {
            session.setAttribute("flash_error", "Invalid request.");
            response.sendRedirect(request.getContextPath() + "/staff/locationBookings");
            return;
        }

        UUID bookingId;
        try {
            bookingId = UUID.fromString(idParam);
        } catch (IllegalArgumentException ex) {
            session.setAttribute("flash_error", "Invalid booking id.");
            response.sendRedirect(request.getContextPath() + "/staff/locationBookings");
            return;
        }

        BookingDAO bookingDAO = new BookingDAO();
        BookingViewModel booking = bookingDAO.getById(bookingId);
        if (booking == null) {
            session.setAttribute("flash_error", "Booking not found.");
            response.sendRedirect(request.getContextPath() + "/staff/locationBookings");
            return;
        }

        String validationError = validateStaffStatusTransition(booking, status);
        if (validationError != null) {
            session.setAttribute("flash_error", validationError);
            response.sendRedirect(request.getContextPath() + "/staff/bookingDetail?id=" + idParam);
            return;
        }

        boolean ok = bookingDAO.updateStatus(bookingId, normalizeStatus(status));
        if (ok) {
            session.setAttribute("flash_success", "Updated booking status.");
        } else {
            session.setAttribute("flash_error", "Failed to update status.");
        }

        response.sendRedirect(request.getContextPath() + "/staff/bookingDetail?id=" + idParam);
    }

    private void handleAddEquipment(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, UUID bookingId) throws IOException {
        BookingDAO bookingDAO = new BookingDAO();
        BookingViewModel booking = bookingDAO.getById(bookingId);

        if (booking == null) {
            session.setAttribute("flash_error", "Booking not found.");
            response.sendRedirect(request.getContextPath() + "/staff/locationBookings");
            return;
        }

        if (!isEquipmentBookingAllowed(booking) || booking.getLocationId() == null) {
            session.setAttribute("flash_error", "Booking must be checked in and still within its slot time to add equipment.");
            response.sendRedirect(request.getContextPath() + "/staff/bookingDetail?id=" + bookingId);
            return;
        }

        LocationEquipmentDAO locationEquipmentDAO = new LocationEquipmentDAO(new DBConnection());
        List<LocationEquipmentViewModel> locationEquipments = locationEquipmentDAO.getByLocation(booking.getLocationId());
        List<BookingEquipment> selectedEquipments = new ArrayList<>();
        BigDecimal additionalAmount = BigDecimal.ZERO;

        for (LocationEquipmentViewModel equipment : locationEquipments) {
            if (equipment.getQuantity() <= 0 || !"available".equalsIgnoreCase(equipment.getStatus())) {
                continue;
            }

            String qtyParam = request.getParameter("equipment_" + equipment.getEquipmentId());
            if (qtyParam == null || qtyParam.isBlank()) {
                continue;
            }

            int quantity;
            try {
                quantity = Integer.parseInt(qtyParam.trim());
            } catch (NumberFormatException ex) {
                session.setAttribute("flash_error", "Equipment quantity must be a valid number.");
                response.sendRedirect(request.getContextPath() + "/staff/bookingDetail?id=" + bookingId);
                return;
            }

            if (quantity <= 0) {
                continue;
            }

            if (quantity > equipment.getQuantity()) {
                session.setAttribute("flash_error", "Requested quantity exceeds remaining stock for " + equipment.getName() + ".");
                response.sendRedirect(request.getContextPath() + "/staff/bookingDetail?id=" + bookingId);
                return;
            }

            BookingEquipment bookingEquipment = new BookingEquipment();
            bookingEquipment.setBookingId(bookingId);
            bookingEquipment.setEquipmentId(equipment.getEquipmentId());
            bookingEquipment.setQuantity(quantity);
            selectedEquipments.add(bookingEquipment);

            if (equipment.getRentalPrice() != null) {
                additionalAmount = additionalAmount.add(equipment.getRentalPrice().multiply(BigDecimal.valueOf(quantity)));
            }
        }

        if (selectedEquipments.isEmpty()) {
            session.setAttribute("flash_error", "Please choose at least one equipment item.");
            response.sendRedirect(request.getContextPath() + "/staff/bookingDetail?id=" + bookingId);
            return;
        }

        PaymentDAO paymentDAO = new PaymentDAO();
        boolean pendingMarked = paymentDAO.markSupplementaryPending(bookingId, additionalAmount);
        if (!pendingMarked) {
            session.setAttribute("flash_error", "Failed to initialize supplementary payment.");
            response.sendRedirect(request.getContextPath() + "/staff/bookingDetail?id=" + bookingId);
            return;
        }

        if (!bookingDAO.markBookingPendingExtra(bookingId)) {
            Payment payment = paymentDAO.getPaymentByBookingId(bookingId);
            if (payment != null && payment.getPaymentId() != null) {
                paymentDAO.updatePaymentFailed(payment.getPaymentId());
            }
            session.setAttribute("flash_error", "Only checked-in bookings in active slot can start supplementary payment.");
            response.sendRedirect(request.getContextPath() + "/staff/bookingDetail?id=" + bookingId);
            return;
        }

        session.setAttribute(payloadKey(bookingId), serializeEquipmentPayload(selectedEquipments));
        session.setAttribute(amountKey(bookingId), additionalAmount.toPlainString());
        response.sendRedirect(request.getContextPath() + "/payment?bookingId=" + bookingId + "&source=supplementary");
    }

    private boolean isEquipmentBookingAllowed(BookingViewModel booking) {
        if (booking == null || booking.getBookingDate() == null || booking.getStartTime() == null || booking.getEndTime() == null) {
            return false;
        }

        String status = normalizeStatus(booking.getStatus());
        if (!"checked in".equals(status)) {
            return false;
        }

        LocalDate bookingDate = booking.getBookingDate();
        LocalTime startTime = booking.getStartTime();
        LocalTime endTime = booking.getEndTime();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = LocalDateTime.of(bookingDate, startTime);
        LocalDateTime end = LocalDateTime.of(bookingDate, endTime);

        return !now.isBefore(start) && now.isBefore(end);
    }

    private void moveFlashMessages(HttpSession session, HttpServletRequest request) {
        String flashSuccess = (String) session.getAttribute("flash_success");
        if (flashSuccess != null) {
            request.setAttribute("flashSuccess", flashSuccess);
            session.removeAttribute("flash_success");
        }

        String flashError = (String) session.getAttribute("flash_error");
        if (flashError != null) {
            request.setAttribute("flashError", flashError);
            session.removeAttribute("flash_error");
        }
    }

    private String validateStaffStatusTransition(BookingViewModel booking, String requestedStatus) {
        String normalizedRequestedStatus = normalizeStatus(requestedStatus);
        if (!"checked in".equals(normalizedRequestedStatus) && !"refunded".equals(normalizedRequestedStatus)) {
            return "Staff can only update booking status to checked in or refunded.";
        }

        String currentStatus = normalizeStatus(booking.getStatus());
        if ("checked in".equals(normalizedRequestedStatus)) {
            if (!"paid".equals(currentStatus)) {
                return "Checked in is only available for paid bookings.";
            }
            if (booking.getBookingDate() == null || booking.getStartTime() == null) {
                return "Cannot check in because booking schedule is incomplete.";
            }

            LocalDateTime slotStart = LocalDateTime.of(booking.getBookingDate(), booking.getStartTime());
            if (slotStart.isAfter(LocalDateTime.now())) {
                return "Checked in is only available when the slot has started.";
            }
            return null;
        }

        if (!"pending refund".equals(currentStatus)) {
            return "Refunded is only available for bookings in pending refund state.";
        }
        return null;
    }

    private String normalizeStatus(String status) {
        return status == null ? "" : status.trim().toLowerCase(Locale.ROOT);
    }
}
