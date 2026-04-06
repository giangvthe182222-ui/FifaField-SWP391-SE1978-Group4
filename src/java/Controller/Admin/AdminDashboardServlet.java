package Controller.Admin;

import DAO.FieldDAO;
import DAO.LocationDAO;
import DAO.EquipmentDAO;
import DAO.BookingDAO;
import Models.Field;
import Models.Location;
import Models.BookingViewModel;
import Models.User;
import Utils.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@WebServlet("/admin-dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private String normalizeStatus(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (user.getRole() == null || !"ADMIN".equalsIgnoreCase(user.getRole().getRoleName())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. Admin role required.");
            return;
        }

        try {
            // Get statistics
            LocationDAO locationDAO = new LocationDAO();
            List<Location> locations = locationDAO.getAllLocations();
            int totalLocations = locations.size();

            FieldDAO fieldDAO = new FieldDAO();
            int totalFields = 0;
            for (Location loc : locations) {
                List<Field> fields = fieldDAO.getByLocation(loc.getLocationId());
                totalFields += fields.size();
            }
            
            DBConnection db = new DBConnection();
            EquipmentDAO equipmentDAO = new EquipmentDAO(db);
            int totalEquipment = equipmentDAO.getAll().size();

            BookingDAO bookingDAO = new BookingDAO();
            List<BookingViewModel> refundBookings = new ArrayList<>();
            List<BookingViewModel> checkedOutUnpaidBookings = new ArrayList<>();

            for (Location loc : locations) {
                List<BookingViewModel> locationBookings = bookingDAO.getByLocation(loc.getLocationId());
                for (BookingViewModel booking : locationBookings) {
                    String paymentStatus = normalizeStatus(booking.getPaymentStatus());
                    if (paymentStatus.contains("refund")) {
                        refundBookings.add(booking);
                    }

                    String playStatus = normalizeStatus(booking.getPlayStatus());
                    if ("checked out".equals(playStatus)) {
                        BigDecimal outstanding = bookingDAO.getOutstandingAmount(booking.getBookingId());
                        booking.setOutstandingAmount(outstanding);
                        if (outstanding != null && outstanding.compareTo(BigDecimal.ZERO) > 0) {
                            checkedOutUnpaidBookings.add(booking);
                        }
                    }
                }
            }

            
            int totalStaff = 28; 
            int todayBookings = 15; 

            
            request.setAttribute("totalLocations", totalLocations);
            request.setAttribute("totalFields", totalFields);
            request.setAttribute("totalEquipment", totalEquipment);
            request.setAttribute("totalStaff", totalStaff);
            request.setAttribute("todayBookings", todayBookings);
            request.setAttribute("adminName", user.getFullName());
            request.setAttribute("refundBookings", refundBookings);
            request.setAttribute("refundBookingCount", refundBookings.size());
            request.setAttribute("checkedOutUnpaidBookings", checkedOutUnpaidBookings);
            request.setAttribute("checkedOutUnpaidCount", checkedOutUnpaidBookings.size());

            request.getRequestDispatcher("/View/Admin/AdminDashboard.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
        }
    }
}