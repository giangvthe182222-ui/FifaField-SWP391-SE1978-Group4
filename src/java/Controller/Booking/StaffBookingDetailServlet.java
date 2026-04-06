package Controller.Booking;

import DAO.BookingDAO;
import DAO.LocationEquipmentDAO;
import Models.BookingEquipment;
import Models.BookingEquipmentViewModel;
import Models.BookingViewModel;
import Models.LocationEquipmentViewModel;

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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import Utils.DBConnection;

@WebServlet(name = "StaffBookingDetailServlet", urlPatterns = {"/staff/bookingDetail"})
public class StaffBookingDetailServlet extends HttpServlet {

    private static final Set<String> PLAY_STATUS_VALUES = new HashSet<>(Arrays.asList(
        "booked", "checked in", "checked out", "completed", "cancelled"
    ));

    private static final Set<String> PAYMENT_STATUS_VALUES = new HashSet<>(Arrays.asList(
        "pending", "deposited", "paid", "pending refund", "refunded", "failed"
    ));

    private static final Set<String> EXTRA_PAYMENT_STATUS_VALUES = new HashSet<>(Arrays.asList(
        "none", "pending extra", "paid extra"
    ));

    private static final class SplitStateUpdateResolution {
    private String playStatus;
    private String paymentStatus;
    private String extraPaymentStatus;
    private String error;
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
        String currentPlayStatus = resolvePlayStatus(booking);
        String currentPaymentStatus = resolvePaymentStatus(booking);
        String currentExtraPaymentStatus = resolveExtraPaymentStatus(booking);
        BigDecimal outstandingAmount = bookingDAO.getOutstandingAmount(bookingId);

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

        boolean canCheckIn = "booked".equals(currentPlayStatus)
                && ("paid".equals(currentPaymentStatus) || "deposited".equals(currentPaymentStatus))
                && booking.getBookingDate() != null
                && booking.getStartTime() != null
                && !LocalDateTime.of(booking.getBookingDate(), booking.getStartTime()).isAfter(LocalDateTime.now());
        boolean canCheckOut = "checked in".equals(currentPlayStatus);
        boolean canMarkPendingRefund = "booked".equals(currentPlayStatus)
            && ("paid".equals(currentPaymentStatus) || "deposited".equals(currentPaymentStatus))
            && isRefundAllowedByTime(booking);
        boolean canRefund = "pending refund".equals(currentPaymentStatus)
            && isRefundAllowedByTime(booking);
        boolean canMarkPaid = "deposited".equals(currentPaymentStatus)
            && !"checked in".equals(currentPlayStatus);
        boolean canMarkExtraPaid = "pending extra".equals(currentExtraPaymentStatus)
            && "checked out".equals(currentPlayStatus);

        moveFlashMessages(session, request);

        request.setAttribute("booking", booking);
        request.setAttribute("outstandingAmount", outstandingAmount);
        request.setAttribute("currentPlayStatus", currentPlayStatus);
        request.setAttribute("currentPaymentStatus", currentPaymentStatus);
        request.setAttribute("currentExtraPaymentStatus", currentExtraPaymentStatus);
        request.setAttribute("equipments", equipments);
        request.setAttribute("canAddEquipment", canAddEquipment);
        request.setAttribute("availableEquipments", availableEquipments);
        request.setAttribute("canCheckIn", canCheckIn);
        request.setAttribute("canCheckOut", canCheckOut);
        request.setAttribute("canMarkPendingRefund", canMarkPendingRefund);
        request.setAttribute("canRefund", canRefund);
        request.setAttribute("canMarkPaid", canMarkPaid);
        request.setAttribute("canMarkExtraPaid", canMarkExtraPaid);
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

        String requestedPlayStatus = normalizeStatus(request.getParameter("playStatus"));
        String requestedPaymentStatus = normalizeStatus(request.getParameter("paymentStatus"));
        String requestedExtraPaymentStatus = normalizeStatus(request.getParameter("extraPaymentStatus"));

        if (!requestedPlayStatus.isEmpty() || !requestedPaymentStatus.isEmpty() || !requestedExtraPaymentStatus.isEmpty()) {
            String currentPlayStatus = resolvePlayStatus(booking);
            String currentPaymentStatus = resolvePaymentStatus(booking);
            String currentExtraPaymentStatus = resolveExtraPaymentStatus(booking);

            if (requestedPlayStatus.isEmpty()) {
                requestedPlayStatus = currentPlayStatus;
            }
            if (requestedPaymentStatus.isEmpty()) {
                requestedPaymentStatus = currentPaymentStatus;
            }
            if (requestedExtraPaymentStatus.isEmpty()) {
                requestedExtraPaymentStatus = currentExtraPaymentStatus;
            }

            SplitStateUpdateResolution resolution = resolveSplitStateUpdate(
                    booking,
                    currentPlayStatus,
                    currentPaymentStatus,
                    currentExtraPaymentStatus,
                    requestedPlayStatus,
                    requestedPaymentStatus,
                    requestedExtraPaymentStatus);

            if (resolution.error != null) {
                session.setAttribute("flash_error", resolution.error);
                response.sendRedirect(request.getContextPath() + "/staff/bookingDetail?id=" + idParam);
                return;
            }

            boolean ok = bookingDAO.updateSplitStates(
                    bookingId,
                    resolution.playStatus,
                    resolution.paymentStatus,
                    resolution.extraPaymentStatus);
            if (ok) {
                bookingDAO.updateStatus(bookingId, "completed");
                session.setAttribute("flash_success", "Updated booking states.");
            } else {
                session.setAttribute("flash_error", "Failed to update booking states.");
            }

            response.sendRedirect(request.getContextPath() + "/staff/bookingDetail?id=" + idParam);
            return;
        }

        String status = request.getParameter("status");
        if (status == null || status.isBlank()) {
            session.setAttribute("flash_error", "Invalid request.");
            response.sendRedirect(request.getContextPath() + "/staff/locationBookings");
            return;
        }

        String validationError = validateStaffStatusTransition(booking, status, bookingId);
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

        boolean finalized = bookingDAO.finalizeSupplementaryEquipment(bookingId, selectedEquipments, additionalAmount);
        if (!finalized) {
            String error = bookingDAO.getLastInsertError();
            session.setAttribute("flash_error", error == null || error.isBlank()
                    ? "Failed to add supplementary equipment."
                    : error);
            response.sendRedirect(request.getContextPath() + "/staff/bookingDetail?id=" + bookingId);
            return;
        }

        session.setAttribute("flash_success", "Đã thêm dụng cụ và cộng vào công nợ còn lại của booking.");
        response.sendRedirect(request.getContextPath() + "/staff/bookingDetail?id=" + bookingId);
    }

    private boolean isEquipmentBookingAllowed(BookingViewModel booking) {
        if (booking == null || booking.getBookingDate() == null || booking.getStartTime() == null || booking.getEndTime() == null) {
            return false;
        }

        if (!"checked in".equals(resolvePlayStatus(booking))) {
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

    private String validateStaffStatusTransition(BookingViewModel booking, String requestedStatus, UUID bookingId) {
        String normalizedRequestedStatus = normalizeStatus(requestedStatus);
        if (!"checked in".equals(normalizedRequestedStatus)
                && !"checked out".equals(normalizedRequestedStatus)
                && !"pending refund".equals(normalizedRequestedStatus)
                && !"refunded".equals(normalizedRequestedStatus)
                && !"paid".equals(normalizedRequestedStatus)) {
            return "Staff can only update booking status to paid, checked in, checked out, pending refund or refunded.";
        }

        String currentPlayStatus = resolvePlayStatus(booking);
        String currentPaymentStatus = resolvePaymentStatus(booking);
        if ("paid".equals(normalizedRequestedStatus)) {
            if (!"deposited".equals(currentPaymentStatus)) {
                return "Paid is only available for deposited bookings.";
            }
            if ("checked in".equals(currentPlayStatus)) {
                return "Payment status cannot be changed while booking is checked in.";
            }
            return null;
        }

        if ("pending refund".equals(normalizedRequestedStatus)) {
            if (!"booked".equals(currentPlayStatus)) {
                return "Pending refund is only available for upcoming booked slots.";
            }
            if (!"paid".equals(currentPaymentStatus) && !"deposited".equals(currentPaymentStatus)) {
                return "Pending refund is only available for paid or deposited bookings.";
            }
            if (!isRefundAllowedByTime(booking)) {
                return "Refund is only available before 2 days from slot start.";
            }
            return null;
        }

        if ("checked out".equals(normalizedRequestedStatus)) {
            if (!"checked in".equals(currentPlayStatus)) {
                return "Checked out is only available for checked in bookings.";
            }
            return null;
        }

        if ("checked in".equals(normalizedRequestedStatus)) {
            if (!"booked".equals(currentPlayStatus)) {
                return "Checked in is only available for booked play-status.";
            }
            if (!"paid".equals(currentPaymentStatus) && !"deposited".equals(currentPaymentStatus)) {
                return "Checked in is only available for paid or deposited bookings.";
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

        if (!"pending refund".equals(currentPaymentStatus)) {
            return "Refunded is only available for bookings in pending refund state.";
        }
        if (!isRefundAllowedByTime(booking)) {
            return "Refund is only available before 2 days from slot start.";
        }
        return null;
    }

    private SplitStateUpdateResolution resolveSplitStateUpdate(BookingViewModel booking,
                                                               String currentPlayStatus,
                                                               String currentPaymentStatus,
                                                               String currentExtraPaymentStatus,
                                                               String requestedPlayStatus,
                                                               String requestedPaymentStatus,
                                                               String requestedExtraPaymentStatus) {
        SplitStateUpdateResolution resolution = new SplitStateUpdateResolution();
        resolution.playStatus = currentPlayStatus;
        resolution.paymentStatus = currentPaymentStatus;
        resolution.extraPaymentStatus = currentExtraPaymentStatus;

        if (!PLAY_STATUS_VALUES.contains(requestedPlayStatus)) {
            resolution.error = "Invalid play status.";
            return resolution;
        }
        if (!PAYMENT_STATUS_VALUES.contains(requestedPaymentStatus)) {
            resolution.error = "Invalid payment status.";
            return resolution;
        }
        if (!EXTRA_PAYMENT_STATUS_VALUES.contains(requestedExtraPaymentStatus)) {
            resolution.error = "Invalid extra payment status.";
            return resolution;
        }

        boolean playChanged = !requestedPlayStatus.equals(currentPlayStatus);
        boolean paymentChanged = !requestedPaymentStatus.equals(currentPaymentStatus);
        boolean extraChanged = !requestedExtraPaymentStatus.equals(currentExtraPaymentStatus);

        int changedDimensions = (playChanged ? 1 : 0) + (paymentChanged ? 1 : 0) + (extraChanged ? 1 : 0);
        if (changedDimensions == 0) {
            resolution.error = "No status changes detected.";
            return resolution;
        }
        if (changedDimensions > 1) {
            resolution.error = "Staff can only perform one valid status action at a time.";
            return resolution;
        }

        if (playChanged) {
            if ("checked in".equals(requestedPlayStatus)) {
                if (!"booked".equals(currentPlayStatus)) {
                    resolution.error = "Checked in is only available for booked play-status.";
                    return resolution;
                }
                if (!"paid".equals(currentPaymentStatus) && !"deposited".equals(currentPaymentStatus)) {
                    resolution.error = "Checked in requires payment status paid or deposited.";
                    return resolution;
                }
                if (booking.getBookingDate() == null || booking.getStartTime() == null) {
                    resolution.error = "Cannot check in because booking schedule is incomplete.";
                    return resolution;
                }
                LocalDateTime slotStart = LocalDateTime.of(booking.getBookingDate(), booking.getStartTime());
                if (slotStart.isAfter(LocalDateTime.now())) {
                    resolution.error = "Checked in is only available when the slot has started.";
                    return resolution;
                }
                resolution.playStatus = "checked in";
                return resolution;
            }

            if ("checked out".equals(requestedPlayStatus)) {
                if (!"checked in".equals(currentPlayStatus)) {
                    resolution.error = "Checked out is only available after checked in.";
                    return resolution;
                }
                resolution.playStatus = "checked out";
                return resolution;
            }

            if ("completed".equals(requestedPlayStatus)) {
                resolution.error = "Completed is auto-updated when booking satisfies checkout and payment conditions.";
                return resolution;
            }

            resolution.error = "Staff cannot change play status to the selected value.";
            return resolution;
        }

        if (paymentChanged) {
            if ("checked in".equals(currentPlayStatus)) {
                resolution.error = "Payment status cannot be changed while booking is checked in.";
                return resolution;
            }

            if ("paid".equals(requestedPaymentStatus)) {
                if (!"deposited".equals(currentPaymentStatus)) {
                    resolution.error = "Paid is only available for deposited bookings.";
                    return resolution;
                }
                if (!"booked".equals(currentPlayStatus) && !"checked out".equals(currentPlayStatus)) {
                    resolution.error = "Paid is only available before check-in or after checked-out.";
                    return resolution;
                }
                resolution.paymentStatus = "paid";
                return resolution;
            }

            if ("pending refund".equals(requestedPaymentStatus)) {
                if (!"booked".equals(currentPlayStatus)) {
                    resolution.error = "Pending refund is only available for upcoming booked slots.";
                    return resolution;
                }
                if (!"paid".equals(currentPaymentStatus) && !"deposited".equals(currentPaymentStatus)) {
                    resolution.error = "Pending refund is only available for paid or deposited bookings.";
                    return resolution;
                }
                if (!isRefundAllowedByTime(booking)) {
                    resolution.error = "Refund is only available before 2 days from slot start.";
                    return resolution;
                }
                resolution.paymentStatus = "pending refund";
                resolution.playStatus = "cancelled";
                resolution.extraPaymentStatus = "none";
                return resolution;
            }

            if ("refunded".equals(requestedPaymentStatus)) {
                if (!"pending refund".equals(currentPaymentStatus)) {
                    resolution.error = "Refunded is only available for bookings in pending refund state.";
                    return resolution;
                }
                if (!isRefundAllowedByTime(booking)) {
                    resolution.error = "Refund is only available before 2 days from slot start.";
                    return resolution;
                }
                resolution.paymentStatus = "refunded";
                resolution.playStatus = "cancelled";
                resolution.extraPaymentStatus = "none";
                return resolution;
            }

            resolution.error = "Staff cannot change payment status to the selected value.";
            return resolution;
        }

        if ("paid extra".equals(requestedExtraPaymentStatus)) {
            if (!"pending extra".equals(currentExtraPaymentStatus)) {
                resolution.error = "Paid extra is only available for pending extra state.";
                return resolution;
            }
            if ("checked in".equals(currentPlayStatus)) {
                resolution.error = "Extra payment status cannot be changed while booking is checked in.";
                return resolution;
            }
            if (!"checked out".equals(currentPlayStatus)) {
                resolution.error = "Paid extra is only available after checked out.";
                return resolution;
            }
            resolution.extraPaymentStatus = "paid extra";
            return resolution;
        }

        resolution.error = "Staff cannot change extra payment status to the selected value.";
        return resolution;
    }

    private boolean isRefundAllowedByTime(BookingViewModel booking) {
        if (booking == null || booking.getBookingDate() == null || booking.getStartTime() == null) {
            return false;
        }
        LocalDateTime slotStart = LocalDateTime.of(booking.getBookingDate(), booking.getStartTime());
        return slotStart.isAfter(LocalDateTime.now().plusDays(2));
    }

    private String resolvePlayStatus(BookingViewModel booking) {
        String playStatus = normalizeStatus(booking.getPlayStatus());
        if (!playStatus.isEmpty()) {
            return playStatus;
        }
        return "booked";
    }

    private String resolvePaymentStatus(BookingViewModel booking) {
        String paymentStatus = normalizeStatus(booking.getPaymentStatus());
        if (!paymentStatus.isEmpty()) {
            return paymentStatus;
        }
        return "pending";
    }

    private String resolveExtraPaymentStatus(BookingViewModel booking) {
        String extraPaymentStatus = normalizeStatus(booking.getExtraPaymentStatus());
        if (!extraPaymentStatus.isEmpty()) {
            return extraPaymentStatus;
        }
        return "none";
    }

    private String normalizeStatus(String status) {
        return status == null ? "" : status.trim().toLowerCase(Locale.ROOT);
    }
}
