package Controller.Booking;

import DAO.BookingDAO;
import DAO.FeedbackDAO;
import Models.BookingViewModel;
import Models.Feedback;
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
            BookingDAO bookingDAO = new BookingDAO();
            BookingViewModel booking = bookingDAO.getById(bookingId);
            if (booking == null || !user.getUserId().equals(booking.getBookerId())) {
                session.setAttribute("flash_error", "Bạn không có quyền đánh giá đơn này.");
                response.sendRedirect(request.getContextPath() + "/customer/bookings");
                return;
            }

            FeedbackDAO feedbackDAO = new FeedbackDAO();
            if (!feedbackDAO.canCustomerManageFeedback(bookingId, user.getUserId())) {
                session.setAttribute("flash_error", "Chỉ có thể gửi đánh giá cho đơn đã completed và chưa đánh giá.");
                response.sendRedirect(request.getContextPath() + "/customer/bookings");
                return;
            }

            Feedback feedback = feedbackDAO.getFeedbackByBookingAndCustomer(bookingId, user.getUserId());
            request.setAttribute("bookingId", bookingId);
            request.setAttribute("booking", booking);
            request.setAttribute("feedback", feedback);
            request.setAttribute("isEditMode", feedback != null);
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

        String action = request.getParameter("action");
        String bookingIdRaw = request.getParameter("bookingId");
        String ratingRaw = request.getParameter("rating");
        String comment = request.getParameter("comment");

        if (bookingIdRaw == null || bookingIdRaw.isBlank()) {
            session.setAttribute("flash_error", "Vui lòng nhập đầy đủ thông tin đánh giá.");
            response.sendRedirect(request.getContextPath() + "/customer/bookings");
            return;
        }

        try {
            UUID bookingId = UUID.fromString(bookingIdRaw);
            BookingDAO bookingDAO = new BookingDAO();
            BookingViewModel booking = bookingDAO.getById(bookingId);
            if (booking == null || !user.getUserId().equals(booking.getBookerId())) {
                session.setAttribute("flash_error", "Bạn không có quyền thao tác với đơn này.");
                response.sendRedirect(request.getContextPath() + "/customer/bookings");
                return;
            }

            FeedbackDAO feedbackDAO = new FeedbackDAO();
            if (!feedbackDAO.canCustomerManageFeedback(bookingId, user.getUserId())) {
                session.setAttribute("flash_error", "Chỉ có thể thao tác với feedback của đơn đã completed.");
                response.sendRedirect(request.getContextPath() + "/customer/bookings");
                return;
            }

            Feedback existingFeedback = feedbackDAO.getFeedbackByBookingAndCustomer(bookingId, user.getUserId());

            if ("delete".equalsIgnoreCase(action)) {
                if (existingFeedback == null) {
                    session.setAttribute("flash_error", "Không tìm thấy đánh giá để xóa.");
                } else if (feedbackDAO.delete(bookingId, user.getUserId())) {
                    session.setAttribute("flash_success", "Xóa đánh giá thành công.");
                } else {
                    session.setAttribute("flash_error", "Không thể xóa đánh giá. Vui lòng thử lại.");
                }
                response.sendRedirect(request.getContextPath() + "/customer/bookings");
                return;
            }

            if (ratingRaw == null || ratingRaw.isBlank()) {
                session.setAttribute("flash_error", "Vui lòng nhập đủ số sao đánh giá.");
                response.sendRedirect(request.getContextPath() + "/customer/feedback?bookingId=" + bookingId);
                return;
            }

            int rating = Integer.parseInt(ratingRaw);

            if (rating < 1 || rating > 5) {
                session.setAttribute("flash_error", "Số sao đánh giá phải từ 1 đến 5.");
                response.sendRedirect(request.getContextPath() + "/customer/feedback?bookingId=" + bookingId);
                return;
            }

            boolean ok = existingFeedback != null
                    ? feedbackDAO.update(bookingId, user.getUserId(), rating, comment)
                    : feedbackDAO.insert(bookingId, user.getUserId(), rating, comment);
            if (ok) {
                session.setAttribute("flash_success", existingFeedback != null
                        ? "Cập nhật đánh giá thành công."
                        : "Gửi đánh giá thành công. Cảm ơn bạn!");
            } else {
                session.setAttribute("flash_error", existingFeedback != null
                        ? "Không thể cập nhật đánh giá. Vui lòng thử lại."
                        : "Không thể gửi đánh giá. Vui lòng thử lại.");
            }
            response.sendRedirect(request.getContextPath() + "/customer/bookings");
        } catch (Exception ex) {
            session.setAttribute("flash_error", "Dữ liệu đánh giá không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/customer/feedback?bookingId=" + bookingIdRaw);
        }
    }
}
