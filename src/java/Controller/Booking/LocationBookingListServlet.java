package Controller.Booking;

import DAO.BookingDAO;
import DAO.StaffDAO;
import Models.BookingViewModel;
import Models.StaffViewModel;
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

@WebServlet(name = "LocationBookingListServlet", urlPatterns = {"/staff/locationBookings"})
public class LocationBookingListServlet extends HttpServlet {

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

            String date = request.getParameter("date");
            String time = request.getParameter("time");
            String status = request.getParameter("status");
            String customerName = request.getParameter("customerName");

            BookingDAO bookingDAO = new BookingDAO();
            List<BookingViewModel> bookings = bookingDAO.getByLocationFiltered(UUID.fromString(staff.getLocationId()), date, time, status, customerName);

            request.setAttribute("bookings", bookings);
            request.setAttribute("locationName", staff.getLocationName());
            request.getRequestDispatcher("/View/Booking/LocationBookingList.jsp").forward(request, response);
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

        BookingDAO bookingDAO = new BookingDAO();
        boolean ok = bookingDAO.updateStatus(UUID.fromString(bookingIdParam), status);
        if (ok) session.setAttribute("flash_success", "Updated booking status.");
        else session.setAttribute("flash_error", "Failed to update status.");

        response.sendRedirect(request.getContextPath() + "/staff/locationBookings");
    }
}
