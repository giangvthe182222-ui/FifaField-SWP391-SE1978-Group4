package Controller.Booking;

import DAO.BookingDAO;
import Models.BookingViewModel;
import Models.BookingEquipmentViewModel;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@WebServlet(name = "StaffBookingDetailServlet", urlPatterns = {"/staff/bookingDetail"})
public class StaffBookingDetailServlet extends HttpServlet {

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
        boolean canCheckIn = "paid".equals(normalizeStatus(booking.getStatus()))
            && booking.getBookingDate() != null
            && booking.getStartTime() != null
            && !LocalDateTime.of(booking.getBookingDate(), booking.getStartTime()).isAfter(LocalDateTime.now());
        boolean canRefund = "pending refund".equals(normalizeStatus(booking.getStatus()));

        request.setAttribute("booking", booking);
        request.setAttribute("equipments", equipments);
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
        String status = request.getParameter("status");
        if (idParam == null || idParam.isBlank() || status == null || status.isBlank()) {
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
