package Controller.Manager;

import DAO.BookingDAO;
import DAO.ManagerDAO;
import Models.BookingViewModel;
import Models.Manager;
import Models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@WebServlet("/manager/locationBookings")
public class ManagerLocationBookingListServlet extends HttpServlet {
    private static final int PAGE_SIZE = 10;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=manager/locationBookings");
            return;
        }

        User user = (User) session.getAttribute("user");
        try {
            Manager manager = new ManagerDAO().getManagerById(user.getUserId());
            if (manager == null || manager.getLocationId() == null) {
                request.setAttribute("error", "Bạn chưa được gán cụm sân.");
                request.getRequestDispatcher("/View/Booking/BookingHistory.jsp").forward(request, response);
                return;
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
                    manager.getLocationId(),
                    date,
                    playStatus,
                    paymentStatus,
                    extraPaymentStatus,
                    customerKeyword);

            int pageNum = 1;
            String pageParam = request.getParameter("page");
            if (pageParam != null && !pageParam.isBlank()) {
                try {
                    pageNum = Integer.parseInt(pageParam);
                    if (pageNum < 1) pageNum = 1;
                } catch (NumberFormatException e) {
                    pageNum = 1;
                }
            }

            int totalItems = allBookings.size();
            int totalPages = (totalItems + PAGE_SIZE - 1) / PAGE_SIZE;
            if (pageNum > totalPages && totalPages > 0) pageNum = totalPages;

            int startIdx = (pageNum - 1) * PAGE_SIZE;
            int endIdx = Math.min(startIdx + PAGE_SIZE, totalItems);
            List<BookingViewModel> pageBookings = new ArrayList<>(allBookings.subList(startIdx, endIdx));

            request.setAttribute("bookings", pageBookings);
            request.setAttribute("currentPage", pageNum);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalItems", totalItems);
            request.setAttribute("locationName", manager.getLocationName());
            request.setAttribute("viewMode", "manager");
            request.getRequestDispatcher("/View/Booking/BookingHistory.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Cannot load manager location bookings", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=manager/locationBookings");
            return;
        }

        String bookingIdParam = request.getParameter("bookingId");
        if (bookingIdParam == null || bookingIdParam.isBlank()) {
            session.setAttribute("flash_error", "Invalid request.");
            response.sendRedirect(request.getContextPath() + "/manager/locationBookings");
            return;
        }

        UUID bookingId;
        try {
            bookingId = UUID.fromString(bookingIdParam);
        } catch (IllegalArgumentException ex) {
            session.setAttribute("flash_error", "Invalid booking id.");
            response.sendRedirect(request.getContextPath() + "/manager/locationBookings");
            return;
        }

        BookingDAO bookingDAO = new BookingDAO();
        String playStatus = normalizeStatus(request.getParameter("playStatus"));
        String paymentStatus = normalizeStatus(request.getParameter("paymentStatus"));
        String extraPaymentStatus = normalizeStatus(request.getParameter("extraPaymentStatus"));

        if (!playStatus.isEmpty() || !paymentStatus.isEmpty() || !extraPaymentStatus.isEmpty()) {
            BookingViewModel current = bookingDAO.getById(bookingId);
            if (current == null) {
                session.setAttribute("flash_error", "Booking not found.");
                response.sendRedirect(request.getContextPath() + "/manager/locationBookings");
                return;
            }

            if (playStatus.isEmpty()) {
                playStatus = normalizeStatus(current.getPlayStatus());
                if (playStatus.isEmpty()) {
                    playStatus = "booked";
                }
            }
            if (paymentStatus.isEmpty()) {
                paymentStatus = normalizeStatus(current.getPaymentStatus());
                if (paymentStatus.isEmpty()) {
                    paymentStatus = "pending";
                }
            }
            if (extraPaymentStatus.isEmpty()) {
                extraPaymentStatus = normalizeStatus(current.getExtraPaymentStatus());
                if (extraPaymentStatus.isEmpty()) {
                    extraPaymentStatus = "none";
                }
            }

            boolean ok = bookingDAO.updateSplitStates(bookingId, playStatus, paymentStatus, extraPaymentStatus);
            if (ok) {
                bookingDAO.updateStatus(bookingId, "completed");
                session.setAttribute("flash_success", "Updated booking states.");
            } else {
                session.setAttribute("flash_error", "Failed to update booking states.");
            }
            response.sendRedirect(request.getContextPath() + "/manager/locationBookings");
            return;
        }

        String status = request.getParameter("status");
        if (status == null || status.isBlank()) {
            session.setAttribute("flash_error", "Invalid request.");
            response.sendRedirect(request.getContextPath() + "/manager/locationBookings");
            return;
        }

        boolean ok = bookingDAO.updateStatus(bookingId, status);
        if (ok) session.setAttribute("flash_success", "Updated booking status.");
        else session.setAttribute("flash_error", "Failed to update status.");

        response.sendRedirect(request.getContextPath() + "/manager/locationBookings");
    }

    private String normalizeStatus(String status) {
        return status == null ? "" : status.trim().toLowerCase();
    }
}
