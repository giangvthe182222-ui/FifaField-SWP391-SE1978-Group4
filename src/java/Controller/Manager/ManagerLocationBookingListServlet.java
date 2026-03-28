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
            String status = request.getParameter("status");
            String customerKeyword = request.getParameter("customerKeyword");
            if (customerKeyword == null || customerKeyword.isBlank()) {
                customerKeyword = request.getParameter("customerName");
            }

            BookingDAO bookingDAO = new BookingDAO();
            List<BookingViewModel> allBookings = bookingDAO.getByLocationFiltered(manager.getLocationId(), date, status, customerKeyword);

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
        String status = request.getParameter("status");
        if (bookingIdParam == null || bookingIdParam.isBlank() || status == null || status.isBlank()) {
            session.setAttribute("flash_error", "Invalid request.");
            response.sendRedirect(request.getContextPath() + "/manager/locationBookings");
            return;
        }

        boolean ok = new BookingDAO().updateStatus(java.util.UUID.fromString(bookingIdParam), status);
        if (ok) session.setAttribute("flash_success", "Updated booking status.");
        else session.setAttribute("flash_error", "Failed to update status.");

        response.sendRedirect(request.getContextPath() + "/manager/locationBookings");
    }
}
