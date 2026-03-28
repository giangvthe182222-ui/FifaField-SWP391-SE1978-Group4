package Controller.Booking;

import DAO.BookingDAO;
import Models.BookingViewModel;
import Models.BookingEquipmentViewModel;
import Models.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@WebServlet(name = "BookingDetailServlet", urlPatterns = {"/customer/bookingDetail"})
public class BookingDetailServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=customer/bookings");
            return;
        }

        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/customer/bookings");
            return;
        }

        UUID bookingId = UUID.fromString(idParam);
        BookingDAO bookingDAO = new BookingDAO();
        BookingViewModel booking = bookingDAO.getById(bookingId);
        if (booking == null) {
            session.setAttribute("flash_error", "Booking not found.");
            response.sendRedirect(request.getContextPath() + "/customer/bookings");
            return;
        }

        // ensure owner
        User user = (User) session.getAttribute("user");
        if (!user.getUserId().equals(booking.getBookerId())) {
            session.setAttribute("flash_error", "Unauthorized access to booking.");
            response.sendRedirect(request.getContextPath() + "/customer/bookings");
            return;
        }

        List<BookingEquipmentViewModel> equipments = bookingDAO.getBookingEquipments(bookingId);
        boolean canRequestRefund = canRequestRefund(booking);
        boolean refundBlockedByPolicy = "paid".equalsIgnoreCase(booking.getStatus()) && !canRequestRefund;

        request.setAttribute("booking", booking);
        request.setAttribute("equipments", equipments);
        request.setAttribute("canRequestRefund", canRequestRefund);
        request.setAttribute("refundBlockedByPolicy", refundBlockedByPolicy);
        request.getRequestDispatcher("/View/Booking/BookingDetail.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=customer/bookings");
            return;
        }

        String action = request.getParameter("action");
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/customer/bookings");
            return;
        }
        UUID bookingId = UUID.fromString(idParam);

        if ("cancel".equalsIgnoreCase(action)) {
            BookingDAO bookingDAO = new BookingDAO();

            BookingViewModel booking = bookingDAO.getById(bookingId);
            User user = (User) session.getAttribute("user");
            if (booking == null || !user.getUserId().equals(booking.getBookerId())) {
                session.setAttribute("flash_error", "Unauthorized access to booking.");
                response.sendRedirect(request.getContextPath() + "/customer/bookings");
                return;
            }

            if (!canRequestRefund(booking)) {
                if ("paid".equalsIgnoreCase(booking.getStatus())) {
                    session.setAttribute("flash_error", "Đơn không được refund vì lịch đấu còn 2 ngày hoặc ít hơn.");
                } else {
                    session.setAttribute("flash_error", "Đơn hiện tại không thể yêu cầu refund.");
                }
                response.sendRedirect(request.getContextPath() + "/customer/bookings");
                return;
            }

            boolean ok = bookingDAO.cancelBooking(bookingId);
            if (ok) {
                session.setAttribute("flash_success", "Booking moved to pending refund.");
            } else {
                session.setAttribute("flash_error", "Could not request refund for this booking.");
            }
        }

        response.sendRedirect(request.getContextPath() + "/customer/bookings");
    }

    private boolean canRequestRefund(BookingViewModel booking) {
        if (booking == null || !"paid".equalsIgnoreCase(booking.getStatus())) {
            return false;
        }
        if (booking.getBookingDate() == null || booking.getStartTime() == null) {
            return false;
        }

        LocalDateTime scheduleStart = LocalDateTime.of(booking.getBookingDate(), booking.getStartTime());
        return scheduleStart.isAfter(LocalDateTime.now().plusDays(2));
    }
}
