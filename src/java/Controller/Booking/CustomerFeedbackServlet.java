package Controller.Booking;

import DAO.FeedbackDAO;
import Models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.UUID;

@WebServlet(name = "CustomerFeedbackServlet", urlPatterns = {"/customer/feedback"})
public class CustomerFeedbackServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=customer/bookings");
            return;
        }

        String bookingIdRaw = request.getParameter("bookingId");
        if (bookingIdRaw == null || bookingIdRaw.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/customer/bookings");
            return;
        }

        try {
            UUID bookingId = UUID.fromString(bookingIdRaw);
            FeedbackDAO feedbackDAO = new FeedbackDAO();
            if (!feedbackDAO.canCustomerFeedback(bookingId, user.getUserId())) {
                session.setAttribute("flash_error", "Chỉ có thể gửi đánh giá cho đơn đã completed và chưa đánh giá.");
                response.sendRedirect(request.getContextPath() + "/customer/bookings");
                return;
            }

            request.setAttribute("bookingId", bookingId);
            request.getRequestDispatcher("/View/Booking/FeedbackForm.jsp").forward(request, response);
        } catch (IllegalArgumentException ex) {
            response.sendRedirect(request.getContextPath() + "/customer/bookings");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=customer/bookings");
            return;
        }

        String bookingIdRaw = request.getParameter("bookingId");
        String ratingRaw = request.getParameter("rating");
        String comment = request.getParameter("comment");

        if (bookingIdRaw == null || bookingIdRaw.isBlank() || ratingRaw == null || ratingRaw.isBlank()) {
            session.setAttribute("flash_error", "Vui lòng nhập đầy đủ thông tin đánh giá.");
            response.sendRedirect(request.getContextPath() + "/customer/bookings");
            return;
        }

        try {
            UUID bookingId = UUID.fromString(bookingIdRaw);
            int rating = Integer.parseInt(ratingRaw);

            if (rating < 1 || rating > 5) {
                session.setAttribute("flash_error", "Số sao đánh giá phải từ 1 đến 5.");
                response.sendRedirect(request.getContextPath() + "/customer/feedback?bookingId=" + bookingId);
                return;
            }

            FeedbackDAO feedbackDAO = new FeedbackDAO();
            if (!feedbackDAO.canCustomerFeedback(bookingId, user.getUserId())) {
                session.setAttribute("flash_error", "Đơn này không hợp lệ để gửi đánh giá.");
                response.sendRedirect(request.getContextPath() + "/customer/bookings");
                return;
            }

            boolean ok = feedbackDAO.insert(bookingId, user.getUserId(), rating, comment);
            if (ok) {
                session.setAttribute("flash_success", "Gửi đánh giá thành công. Cảm ơn bạn!");
            } else {
                session.setAttribute("flash_error", "Không thể gửi đánh giá. Vui lòng thử lại.");
            }
            response.sendRedirect(request.getContextPath() + "/customer/bookings");
        } catch (Exception ex) {
            session.setAttribute("flash_error", "Dữ liệu đánh giá không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/customer/bookings");
        }
    }
}
