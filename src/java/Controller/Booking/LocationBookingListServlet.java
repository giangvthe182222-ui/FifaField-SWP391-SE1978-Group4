package Controller.Booking;

import DAO.StaffDAO;
import Models.BookingViewModel;
import Models.StaffViewModel;
import Models.User;
import Service.BookingService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@WebServlet(name = "LocationBookingListServlet", urlPatterns = {"/staff/locationBookings"})
public class LocationBookingListServlet extends HttpServlet {
    private static final int PAGE_SIZE = 10;

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
            String status = request.getParameter("status");
            String customerKeyword = request.getParameter("customerKeyword");
            if (customerKeyword == null || customerKeyword.isBlank()) {
                customerKeyword = request.getParameter("customerName");
            }

            BookingService bookingService = new BookingService();
            List<BookingViewModel> allBookings = bookingService.getLocationBookingHistory(
                    UUID.fromString(staff.getLocationId()), date, status, customerKeyword);
                List<BookingViewModel> pendingRefundBookings = bookingService.getLocationBookingHistory(
                    UUID.fromString(staff.getLocationId()), null, "pending refund", null);

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
            Map<UUID, Boolean> staffCanRefundMap = new HashMap<>();
            for (BookingViewModel booking : pageBookings) {
                booking.setEquipmentBookingAllowed(isEquipmentBookingAllowed(booking));

                String normalizedStatus = normalizeStatus(booking.getStatus());
                boolean canCheckIn = "paid".equals(normalizedStatus)
                        && booking.getBookingDate() != null
                        && booking.getStartTime() != null
                        && !LocalDateTime.of(booking.getBookingDate(), booking.getStartTime()).isAfter(now);
                boolean canRefund = "pending refund".equals(normalizedStatus);

                staffCanCheckInMap.put(booking.getBookingId(), canCheckIn);
                staffCanRefundMap.put(booking.getBookingId(), canRefund);
            }

            request.setAttribute("bookings", pageBookings);
            request.setAttribute("staffCanCheckInMap", staffCanCheckInMap);
            request.setAttribute("staffCanRefundMap", staffCanRefundMap);
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
        String status = request.getParameter("status");
        if (bookingIdParam == null || bookingIdParam.isBlank() || status == null || status.isBlank()) {
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

        DAO.BookingDAO bookingDAO = new DAO.BookingDAO();
        BookingViewModel booking = bookingDAO.getById(bookingId);
        if (booking == null) {
            session.setAttribute("flash_error", "Booking not found.");
            response.sendRedirect(request.getContextPath() + "/staff/locationBookings");
            return;
        }

        String validationError = validateStaffStatusTransition(booking, status);
        if (validationError != null) {
            session.setAttribute("flash_error", validationError);
            response.sendRedirect(request.getContextPath() + "/staff/locationBookings");
            return;
        }

        BookingService bookingService = new BookingService();
        boolean ok = bookingService.updateBookingStatus(bookingId, normalizeStatus(status));
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

        String status = normalizeStatus(booking.getStatus());
        if (!"checked in".equals(status)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = LocalDateTime.of(booking.getBookingDate(), booking.getStartTime());
        LocalDateTime end = LocalDateTime.of(booking.getBookingDate(), booking.getEndTime());
        return !now.isBefore(start) && now.isBefore(end);
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
