package Controller.Booking;

import DAO.BookingDAO;
import DAO.StaffDAO;
import Models.BookingViewModel;
import Models.StaffViewModel;
import Models.User;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@WebServlet(name = "LocationBookingListServlet", urlPatterns = {"/staff/locationBookings"})
public class LocationBookingListServlet extends HttpServlet {
    private static final int PAGE_SIZE = 10;

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

        User user = (User) session.getAttribute("user");
        String userId = user.getUserId().toString();

        try {
            StaffDAO staffDAO = new StaffDAO();
            StaffViewModel staff = staffDAO.getStaffById(userId);
            if (staff == null || staff.getLocationId() == null) {
                session.setAttribute("flash_error", "No location assigned to this staff.");
                response.sendRedirect(request.getContextPath() + "/");
                return;
            }

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

            String date = request.getParameter("date");
                String playStatus = request.getParameter("playStatus");
                String paymentStatus = request.getParameter("paymentStatus");
                String extraPaymentStatus = request.getParameter("extraPaymentStatus");
            String customerKeyword = request.getParameter("customerKeyword");
            if (customerKeyword == null || customerKeyword.isBlank()) {
                customerKeyword = request.getParameter("customerName");
            }

            BookingDAO bookingDAO = new BookingDAO();
                List<BookingViewModel> allBookings = bookingDAO.getByLocationFilteredByState(
                    UUID.fromString(staff.getLocationId()),
                    date,
                    normalizeStatus(playStatus),
                    normalizeStatus(paymentStatus),
                    normalizeStatus(extraPaymentStatus),
                    customerKeyword);
                List<BookingViewModel> pendingRefundBookings = bookingDAO.getByLocationFilteredByState(
                    UUID.fromString(staff.getLocationId()),
                    null,
                    null,
                    "pending refund",
                    null,
                    null);

            int pageNum = 1;
            String pageParam = request.getParameter("page");
            if (pageParam != null && !pageParam.isBlank()) {
                try {
                    pageNum = Integer.parseInt(pageParam);
                    if (pageNum < 1) {
                        pageNum = 1;
                    }
                } catch (NumberFormatException e) {
                    pageNum = 1;
                }
            }

            int totalItems = allBookings.size();
            int totalPages = (totalItems + PAGE_SIZE - 1) / PAGE_SIZE;
            if (pageNum > totalPages && totalPages > 0) {
                pageNum = totalPages;
            }

            int startIdx = (pageNum - 1) * PAGE_SIZE;
            int endIdx = Math.min(startIdx + PAGE_SIZE, totalItems);
            List<BookingViewModel> pageBookings = new ArrayList<>(allBookings.subList(startIdx, endIdx));

            LocalDateTime now = LocalDateTime.now();
            Map<UUID, Boolean> staffCanCheckInMap = new HashMap<>();
            Map<UUID, Boolean> staffCanCheckOutMap = new HashMap<>();
            Map<UUID, Boolean> staffCanPendingRefundMap = new HashMap<>();
            Map<UUID, Boolean> staffCanRefundMap = new HashMap<>();
            Map<UUID, Boolean> staffCanMarkPaidMap = new HashMap<>();
            Map<UUID, Boolean> staffCanMarkExtraPaidMap = new HashMap<>();
            for (BookingViewModel booking : pageBookings) {
                booking.setEquipmentBookingAllowed(isEquipmentBookingAllowed(booking));
                BigDecimal outstandingAmount = bookingDAO.getOutstandingAmount(booking.getBookingId());
                booking.setOutstandingAmount(outstandingAmount);

                String normalizedPlayStatus = resolvePlayStatus(booking);
                String normalizedPaymentStatus = resolvePaymentStatus(booking);
                String normalizedExtraPaymentStatus = normalizeStatus(booking.getExtraPaymentStatus());
                boolean canCheckIn = "booked".equals(normalizedPlayStatus)
                    && ("paid".equals(normalizedPaymentStatus) || "deposited".equals(normalizedPaymentStatus))
                        && booking.getBookingDate() != null
                        && booking.getStartTime() != null
                        && !LocalDateTime.of(booking.getBookingDate(), booking.getStartTime()).isAfter(now);
                boolean canCheckOut = "checked in".equals(normalizedPlayStatus);
                boolean canPendingRefund = "booked".equals(normalizedPlayStatus)
                        && ("paid".equals(normalizedPaymentStatus) || "deposited".equals(normalizedPaymentStatus))
                        && isRefundAllowedByTime(booking);
                boolean canRefund = "pending refund".equals(normalizedPaymentStatus)
                        && isRefundAllowedByTime(booking);
                boolean canMarkPaid = "deposited".equals(normalizedPaymentStatus)
                    && !"checked in".equals(normalizedPlayStatus);
                boolean canMarkExtraPaid = "pending extra".equals(normalizedExtraPaymentStatus)
                    && "checked out".equals(normalizedPlayStatus);

                staffCanCheckInMap.put(booking.getBookingId(), canCheckIn);
                staffCanCheckOutMap.put(booking.getBookingId(), canCheckOut);
                staffCanPendingRefundMap.put(booking.getBookingId(), canPendingRefund);
                staffCanRefundMap.put(booking.getBookingId(), canRefund);
                staffCanMarkPaidMap.put(booking.getBookingId(), canMarkPaid);
                staffCanMarkExtraPaidMap.put(booking.getBookingId(), canMarkExtraPaid);
            }

            request.setAttribute("bookings", pageBookings);
            request.setAttribute("staffCanCheckInMap", staffCanCheckInMap);
            request.setAttribute("staffCanCheckOutMap", staffCanCheckOutMap);
            request.setAttribute("staffCanPendingRefundMap", staffCanPendingRefundMap);
            request.setAttribute("staffCanRefundMap", staffCanRefundMap);
            request.setAttribute("staffCanMarkPaidMap", staffCanMarkPaidMap);
            request.setAttribute("staffCanMarkExtraPaidMap", staffCanMarkExtraPaidMap);
            request.setAttribute("currentPage", pageNum);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalItems", totalItems);
            request.setAttribute("locationName", staff.getLocationName());
            request.setAttribute("refundPendingBookings", pendingRefundBookings);
            request.setAttribute("refundPendingCount", pendingRefundBookings.size());
            request.setAttribute("viewMode", "staff");
            request.getRequestDispatcher("/View/Booking/BookingHistory.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("flash_error", "Error loading bookings: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=staff/locationBookings");
            return;
        }

        String bookingIdParam = request.getParameter("bookingId");
        if (bookingIdParam == null || bookingIdParam.isBlank()) {
            session.setAttribute("flash_error", "Invalid request.");
            response.sendRedirect(request.getContextPath() + "/staff/locationBookings");
            return;
        }

        UUID bookingId;
        try {
            bookingId = UUID.fromString(bookingIdParam);
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
            String currentExtraPaymentStatus = normalizeStatus(booking.getExtraPaymentStatus());
            if (currentExtraPaymentStatus.isEmpty()) {
                currentExtraPaymentStatus = "none";
            }

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
                response.sendRedirect(request.getContextPath() + "/staff/locationBookings");
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

            response.sendRedirect(request.getContextPath() + "/staff/locationBookings");
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
            response.sendRedirect(request.getContextPath() + "/staff/locationBookings");
            return;
        }

        boolean ok = bookingDAO.updateStatus(bookingId, normalizeStatus(status));
        if (ok) {
            session.setAttribute("flash_success", "Updated booking status.");
        } else {
            session.setAttribute("flash_error", "Failed to update status.");
        }

        response.sendRedirect(request.getContextPath() + "/staff/locationBookings");
    }

    private boolean isEquipmentBookingAllowed(BookingViewModel booking) {
        if (booking == null || booking.getBookingDate() == null || booking.getStartTime() == null || booking.getEndTime() == null) {
            return false;
        }

        if (!"checked in".equals(resolvePlayStatus(booking))) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = LocalDateTime.of(booking.getBookingDate(), booking.getStartTime());
        LocalDateTime end = LocalDateTime.of(booking.getBookingDate(), booking.getEndTime());
        return !now.isBefore(start) && now.isBefore(end);
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

    private String normalizeStatus(String status) {
        return status == null ? "" : status.trim().toLowerCase(Locale.ROOT);
    }
}
