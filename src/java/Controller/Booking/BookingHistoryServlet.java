package Controller.Booking;

import Models.BookingViewModel;
import Models.User;
import DAO.FeedbackDAO;
import Service.BookingService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.time.LocalDateTime;

@WebServlet(name = "BookingHistoryServlet", urlPatterns = {"/customer/bookings"})
public class BookingHistoryServlet extends HttpServlet {
    private static final int PAGE_SIZE = 9;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=customer/bookings");
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

        User user = (User) session.getAttribute("user");
        UUID userId = user.getUserId();

        String date = request.getParameter("date");
        String time = request.getParameter("time");
        String status = request.getParameter("status");

        BookingService bookingService = new BookingService();
        List<BookingViewModel> allBookings = bookingService.getCustomerBookingHistory(userId, date, time, status);

        // Pagination
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
        if (totalPages == 0) {
            totalPages = 1;
        }
        if (pageNum > totalPages && totalPages > 0) pageNum = totalPages;
        
        int startIdx = (pageNum - 1) * PAGE_SIZE;
        int endIdx = Math.min(startIdx + PAGE_SIZE, totalItems);
        List<BookingViewModel> pageBookings = new ArrayList<>(allBookings.subList(startIdx, endIdx));

        FeedbackDAO feedbackDAO = new FeedbackDAO();
        Set<UUID> feedbackBookingIds = feedbackDAO.getFeedbackBookingIdsByCustomer(userId);
        Map<UUID, Boolean> feedbackBookingMap = new HashMap<>();
        Map<UUID, Boolean> reviewableBookingMap = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        for (BookingViewModel booking : pageBookings) {
            feedbackBookingMap.put(booking.getBookingId(), feedbackBookingIds.contains(booking.getBookingId()));
                boolean reviewable = "completed".equalsIgnoreCase(booking.getStatus())
                    && booking.getBookingDate() != null
                    && booking.getEndTime() != null
                    && !LocalDateTime.of(booking.getBookingDate(), booking.getEndTime()).isAfter(now);
            reviewableBookingMap.put(booking.getBookingId(), reviewable);
        }

        request.setAttribute("bookings", pageBookings);
        request.setAttribute("feedbackBookingMap", feedbackBookingMap);
        request.setAttribute("reviewableBookingMap", reviewableBookingMap);
        request.setAttribute("currentPage", pageNum);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalItems", totalItems);
        request.setAttribute("viewMode", "customer");
        request.getRequestDispatcher("/View/Booking/BookingHistory.jsp").forward(request, response);
    }
}
