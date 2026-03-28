package Controller.Staff;

import DAO.BookingDAO;
import DAO.StaffDAO;
import Models.StaffViewModel;
import Models.User;
import Models.BookingViewModel;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@WebServlet(name = "StaffDashboardServlet", urlPatterns = {"/staff/dashboard"})
public class StaffDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=staff/dashboard");
            return;
        }

        User user = (User) session.getAttribute("user");
        try {
            StaffDAO staffDAO = new StaffDAO();
            StaffViewModel staff = staffDAO.getStaffById(user.getUserId().toString());
            if (staff == null || staff.getLocationId() == null) {
                session.setAttribute("flash_error", "No location assigned to this staff.");
                response.sendRedirect(request.getContextPath() + "/");
                return;
            }

                BookingDAO bookingDAO = new BookingDAO();
                List<BookingViewModel> pendingRefundBookings = bookingDAO.getByLocationFiltered(
                    UUID.fromString(staff.getLocationId()), null, "pending refund", null);

            request.setAttribute("staff", staff);
            request.setAttribute("refundPendingBookings", pendingRefundBookings);
            request.setAttribute("refundPendingCount", pendingRefundBookings.size());
            request.getRequestDispatcher("/View/Staff/dashboard.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("flash_error", "Error loading staff dashboard: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/");
        }
    }
}
