package Controller.Booking;

import DAO.BookingDAO;
import DAO.LocationEquipmentDAO;
import DAO.SupplementaryEquipmentRentalDAO;
import Models.BookingViewModel;
import Models.LocationEquipmentViewModel;
import Models.SupplementaryEquipmentRental;
import Models.SupplementaryEquipment;
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
import java.util.UUID;

@WebServlet(name = "StaffAddSupplementaryEquipmentServlet", urlPatterns = {"/staff/addSupplementaryEquipment"})
public class StaffAddSupplementaryEquipmentServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=staff/locationBookings");
            return;
        }

        String bookingIdParam = request.getParameter("bookingId");
        if (bookingIdParam == null || bookingIdParam.isBlank()) {
            session.setAttribute("flash_error", "Booking ID is required.");
            response.sendRedirect(request.getContextPath() + "/staff/locationBookings");
            return;
        }

        UUID bookingId = UUID.fromString(bookingIdParam);
        BookingDAO bookingDAO = new BookingDAO();
        BookingViewModel booking = bookingDAO.getById(bookingId);

        if (booking == null) {
            session.setAttribute("flash_error", "Booking not found.");
            response.sendRedirect(request.getContextPath() + "/staff/locationBookings");
            return;
        }

        if (booking.getLocationId() == null) {
            session.setAttribute("flash_error", "Booking location not found.");
            response.sendRedirect(request.getContextPath() + "/staff/locationBookings");
            return;
        }

        LocationEquipmentDAO locationEquipmentDAO = new LocationEquipmentDAO(new DBConnection());
        List<LocationEquipmentViewModel> availableEquipments = new ArrayList<>();
        for (LocationEquipmentViewModel equipment : locationEquipmentDAO.getByLocation(booking.getLocationId())) {
            if (equipment.getQuantity() > 0 && "available".equalsIgnoreCase(equipment.getStatus())) {
                availableEquipments.add(equipment);
            }
        }

        moveFlashMessages(session, request);

        request.setAttribute("booking", booking);
        request.setAttribute("availableEquipments", availableEquipments);
        request.getRequestDispatcher("/View/Booking/StaffAddSupplementaryEquipment.jsp").forward(request, response);
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
        if (bookingIdParam == null || bookingIdParam.isBlank()) {
            session.setAttribute("flash_error", "Invalid booking.");
            response.sendRedirect(request.getContextPath() + "/staff/locationBookings");
            return;
        }

        UUID bookingId = UUID.fromString(bookingIdParam);
        BookingDAO bookingDAO = new BookingDAO();
        BookingViewModel booking = bookingDAO.getById(bookingId);

        if (booking == null || booking.getLocationId() == null) {
            session.setAttribute("flash_error", "Booking not found or invalid.");
            response.sendRedirect(request.getContextPath() + "/staff/locationBookings");
            return;
        }

        LocationEquipmentDAO locationEquipmentDAO = new LocationEquipmentDAO(new DBConnection());
        List<LocationEquipmentViewModel> locationEquipments = locationEquipmentDAO.getByLocation(booking.getLocationId());
        List<SupplementaryEquipment> selectedEquipments = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (LocationEquipmentViewModel equipment : locationEquipments) {
            if (equipment.getQuantity() <= 0 || !"available".equalsIgnoreCase(equipment.getStatus())) {
                continue;
            }

            String qtyParam = request.getParameter("equipment_" + equipment.getEquipmentId());
            if (qtyParam == null || qtyParam.isBlank()) {
                continue;
            }

            int quantity;
            try {
                quantity = Integer.parseInt(qtyParam.trim());
            } catch (NumberFormatException ex) {
                session.setAttribute("flash_error", "Equipment quantity must be a valid number.");
                response.sendRedirect(request.getContextPath() + "/staff/addSupplementaryEquipment?bookingId=" + bookingId);
                return;
            }

            if (quantity <= 0) {
                continue;
            }

            if (quantity > equipment.getQuantity()) {
                session.setAttribute("flash_error", "Requested quantity exceeds remaining stock for " + equipment.getName() + ".");
                response.sendRedirect(request.getContextPath() + "/staff/addSupplementaryEquipment?bookingId=" + bookingId);
                return;
            }

            SupplementaryEquipment suppEquip = new SupplementaryEquipment();
            suppEquip.setEquipmentId(equipment.getEquipmentId());
            suppEquip.setQuantity(quantity);
            selectedEquipments.add(suppEquip);

            if (equipment.getRentalPrice() != null) {
                totalPrice = totalPrice.add(equipment.getRentalPrice().multiply(BigDecimal.valueOf(quantity)));
            }
        }

        if (selectedEquipments.isEmpty()) {
            session.setAttribute("flash_error", "Please choose at least one equipment item.");
            response.sendRedirect(request.getContextPath() + "/staff/addSupplementaryEquipment?bookingId=" + bookingId);
            return;
        }

        SupplementaryEquipmentRental rental = new SupplementaryEquipmentRental(
            bookingId,
            booking.getBookerId(),
            booking.getFieldId(),
            booking.getLocationId()
        );
        rental.setTotalPrice(totalPrice);

        SupplementaryEquipmentRentalDAO dao = new SupplementaryEquipmentRentalDAO();
        boolean ok = dao.createSupplementaryRental(rental, selectedEquipments);

        if (ok) {
            // Redirect to payment page for the new supplementary equipment rental
            response.sendRedirect(request.getContextPath() + "/staff/supplementaryEquipmentPayment?rentalId=" + rental.getRentalId());
        } else {
            String error = dao.getLastInsertError();
            session.setAttribute("flash_error", (error == null || error.isBlank())
                    ? "Failed to add equipment."
                    : error);
            response.sendRedirect(request.getContextPath() + "/staff/addSupplementaryEquipment?bookingId=" + bookingId);
        }
    }

    private void moveFlashMessages(HttpSession session, HttpServletRequest request) {
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
    }
}
