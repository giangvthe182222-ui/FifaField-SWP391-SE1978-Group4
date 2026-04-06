package Controller.Customer;

import DAO.BookingDAO;
import Models.BookingViewModel;
import Models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@WebServlet(name = "CustomerDashboardServlet", urlPatterns = {"/customer/dashboard"})
public class CustomerDashboardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=customer/dashboard");
            return;
        }

        try {
            BookingDAO bookingDAO = new BookingDAO();
            List<BookingViewModel> bookings = bookingDAO.getByBooker(user.getUserId());

            BigDecimal totalSpent = BigDecimal.ZERO;
            BigDecimal totalOutstanding = BigDecimal.ZERO;
            int completedCount = 0;
            int upcomingCount = 0;
            LocalDate today = LocalDate.now();

            for (BookingViewModel booking : bookings) {
                BigDecimal outstanding = bookingDAO.getOutstandingAmount(booking.getBookingId());
                booking.setOutstandingAmount(outstanding);

                BigDecimal totalPrice = booking.getTotalPrice() == null ? BigDecimal.ZERO : booking.getTotalPrice();
                BigDecimal settled = totalPrice.subtract(outstanding == null ? BigDecimal.ZERO : outstanding);
                if (settled.compareTo(BigDecimal.ZERO) < 0) {
                    settled = BigDecimal.ZERO;
                }

                String paymentStatus = booking.getPaymentStatus() == null ? "" : booking.getPaymentStatus().trim().toLowerCase(Locale.ROOT);
                if (!"refunded".equals(paymentStatus)) {
                    totalSpent = totalSpent.add(settled);
                }
                totalOutstanding = totalOutstanding.add(outstanding == null ? BigDecimal.ZERO : outstanding);

                String playStatus = booking.getPlayStatus() == null ? "" : booking.getPlayStatus().trim().toLowerCase(Locale.ROOT);
                if ("completed".equals(playStatus)) {
                    completedCount++;
                }
                if ("booked".equals(playStatus) && booking.getBookingDate() != null && !booking.getBookingDate().isBefore(today)) {
                    upcomingCount++;
                }
            }

            List<BookingViewModel> recentBookings = bookings.stream().limit(6).collect(Collectors.toList());
                List<BookingViewModel> refundedBookings = bookings.stream()
                    .filter(vm -> vm != null
                        && vm.getPaymentStatus() != null
                        && "refunded".equalsIgnoreCase(vm.getPaymentStatus().trim()))
                    .limit(5)
                    .collect(Collectors.toList());

            request.setAttribute("totalBookings", bookings.size());
            request.setAttribute("completedBookings", completedCount);
            request.setAttribute("upcomingBookings", upcomingCount);
            request.setAttribute("totalSpent", totalSpent);
            request.setAttribute("totalOutstanding", totalOutstanding);
            request.setAttribute("recentBookings", recentBookings);
            request.setAttribute("refundNotifications", refundedBookings);
            request.setAttribute("refundNotificationCount", refundedBookings.size());
            request.getRequestDispatcher("/View/Customer/dashboard.jsp").forward(request, response);
        } catch (Exception ex) {
            request.setAttribute("error", "Khong the tai du lieu tong quan khach hang. Vui long thu lai sau.");
            request.getRequestDispatcher("/View/Customer/dashboard.jsp").forward(request, response);
        }
    }
}
