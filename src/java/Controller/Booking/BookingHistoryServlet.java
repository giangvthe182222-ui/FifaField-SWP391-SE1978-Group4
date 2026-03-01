package Controller.Booking;

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
import java.util.List;
import java.util.UUID;

@WebServlet(name = "BookingHistoryServlet", urlPatterns = {"/customer/bookings"})
public class BookingHistoryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=customer/bookings");
            return;
        }

        Models.User user = (Models.User) session.getAttribute("user");
        UUID userId = user.getUserId();

        String date = request.getParameter("date");
        String time = request.getParameter("time");
        String status = request.getParameter("status");

        BookingDAO bookingDAO = new BookingDAO();
        List<BookingViewModel> bookings = bookingDAO.getByBookerFiltered(userId, date, time, status);

        request.setAttribute("bookings", bookings);
        request.getRequestDispatcher("/View/Booking/BookingHistory.jsp").forward(request, response);
    }
}
