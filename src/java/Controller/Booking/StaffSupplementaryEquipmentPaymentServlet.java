package Controller.Booking;

import DAO.SupplementaryEquipmentRentalDAO;
import Models.SupplementaryEquipmentRental;
import Models.SupplementaryEquipment;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.sql.Timestamp;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "StaffSupplementaryEquipmentPaymentServlet", urlPatterns = {"/staff/supplementaryEquipmentPayment"})
public class StaffSupplementaryEquipmentPaymentServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String rentalIdStr = request.getParameter("rentalId");
        
        if (rentalIdStr == null || rentalIdStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing rentalId parameter");
            return;
        }
        
        try {
            UUID rentalId = UUID.fromString(rentalIdStr);
            SupplementaryEquipmentRentalDAO dao = new SupplementaryEquipmentRentalDAO();
            
            SupplementaryEquipmentRental rental = dao.getRentalById(rentalId);
            if (rental == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Rental not found");
                return;
            }
            
            // Load equipment items for this rental
            List<SupplementaryEquipment> equipments = dao.getRentalEquipments(rentalId);
            
            // Convert LocalDateTime to java.sql.Timestamp for JSTL formatting
            java.sql.Timestamp createdTimeStamp = null;
            if (rental.getCreatedTime() != null) {
                createdTimeStamp = Timestamp.valueOf(rental.getCreatedTime());
            }
            
            // Set request attributes for JSP
            request.setAttribute("rental", rental);
            request.setAttribute("createdTime", createdTimeStamp);
            request.setAttribute("equipments", equipments);
            request.setAttribute("equipmentCount", equipments == null ? 0 : equipments.size());
            request.setAttribute("rentalId", rentalIdStr);
            
            // Move flash messages from session to request
            moveFlashMessages(request);
            
            request.getRequestDispatcher("/View/Booking/StaffSupplementaryEquipmentPayment.jsp")
                    .forward(request, response);
                    
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid rentalId format");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String rentalIdStr = request.getParameter("rentalId");
        
        if (rentalIdStr == null || rentalIdStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing rentalId parameter");
            return;
        }
        
        try {
            UUID rentalId = UUID.fromString(rentalIdStr);
            
            if ("confirm_payment".equals(action)) {
                handlePaymentConfirmation(request, response, rentalId);
            } else if ("cancel".equals(action)) {
                // Redirect back to booking list without processing payment
                response.sendRedirect(request.getContextPath() + "/staff/locationBookings");
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
            }
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid rentalId format");
        }
    }

    private void handlePaymentConfirmation(HttpServletRequest request, 
                                          HttpServletResponse response, 
                                          UUID rentalId) throws ServletException, IOException {
        String paymentMethod = request.getParameter("paymentMethod");
        String paymentNotes = request.getParameter("paymentNotes");
        
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            HttpSession session = request.getSession();
            session.setAttribute("flash_error", "Vui lòng chọn phương thức thanh toán");
            response.sendRedirect(request.getContextPath() + "/staff/supplementaryEquipmentPayment?rentalId=" + rentalId);
            return;
        }
        
        try {
            SupplementaryEquipmentRentalDAO dao = new SupplementaryEquipmentRentalDAO();
            
            // Update rental status to 'paid' or 'completed' depending on payment method
            // For now, we mark it as 'paid' after payment confirmation
            boolean updateSuccess = dao.updateStatus(rentalId, "paid");
            
            if (updateSuccess) {
                // Update supplementary rental payment status and method
                // This would require extending the DB schema, but for now we just update status
                
                // TODO: Create Payment record if needed
                // PaymentDAO.createSupplementaryPayment(rentalId, amount, paymentMethod)
                
                HttpSession session = request.getSession();
                session.setAttribute("flash_success", "Thanh toán thành công cho thiết bị bổ sung. Mã rental: " + rentalId);
                response.sendRedirect(request.getContextPath() + "/staff/locationBookings");
            } else {
                HttpSession session = request.getSession();
                session.setAttribute("flash_error", "Cập nhật trạng thái thanh toán thất bại. Vui lòng thử lại.");
                response.sendRedirect(request.getContextPath() + "/staff/supplementaryEquipmentPayment?rentalId=" + rentalId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            HttpSession session = request.getSession();
            session.setAttribute("flash_error", "Lỗi xử lý thanh toán: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/staff/supplementaryEquipmentPayment?rentalId=" + rentalId);
        }
    }

    private void moveFlashMessages(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object successMsg = session.getAttribute("flash_success");
            Object errorMsg = session.getAttribute("flash_error");
            
            if (successMsg != null) {
                request.setAttribute("flash_success", successMsg);
                session.removeAttribute("flash_success");
            }
            if (errorMsg != null) {
                request.setAttribute("flash_error", errorMsg);
                session.removeAttribute("flash_error");
            }
        }
    }
}
