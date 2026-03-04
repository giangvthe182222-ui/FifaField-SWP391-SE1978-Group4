package Controller.Booking;

import DAO.BookingDAO;
import DAO.PaymentDAO;
import Models.Booking;
import Models.Payment;
import Utils.QRCodeGenerator;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

public class PaymentServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String bookingIdStr = request.getParameter("bookingId");
        
        if (bookingIdStr == null || bookingIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/View/Booking/Booking.jsp?error=invalid_booking");
            return;
        }
        
        try {
            UUID bookingId = UUID.fromString(bookingIdStr);
            
            // Get booking details
            Booking booking = BookingDAO.getBookingById(bookingId);
            if (booking == null) {
                response.sendRedirect(request.getContextPath() + "/View/Booking/Booking.jsp?error=booking_not_found");
                return;
            }
            
            // Check if booking is pending confirmation
            if (!booking.getStatus().equals("PENDING")) {
                response.sendRedirect(request.getContextPath() + "/View/Booking/Booking.jsp?error=invalid_status");
                return;
            }
            
            // Get or create payment
            Payment payment = PaymentDAO.getPaymentByBookingId(bookingId);
            if (payment == null) {
                // Create new payment record
                BigDecimal amount = booking.getTotalPrice();
                long amountInVND = amount.longValue();
                
                String vietQRString = QRCodeGenerator.generateVietQRString(bookingId, amountInVND);
                PaymentDAO.createPayment(bookingId, amount, vietQRString);
                
                payment = PaymentDAO.getPaymentByBookingId(bookingId);
            }
            
            // Generate QR Code URL
            String qrCodeURL = QRCodeGenerator.generateQRCodeURL(payment.getQrContent());
            
            // Get remaining time (in seconds)
            long timeRemaining = PaymentDAO.getTimeRemainingForPayment(bookingId);
            
            // Set attributes for JSP
            request.setAttribute("booking", booking);
            request.setAttribute("payment", payment);
            request.setAttribute("qrCodeURL", qrCodeURL);
            request.setAttribute("timeRemaining", timeRemaining);
            request.setAttribute("bankCode", PaymentDAO.BANK_CODE);
            request.setAttribute("accountNumber", PaymentDAO.ACCOUNT_NUMBER);
            request.setAttribute("accountName", PaymentDAO.ACCOUNT_NAME);
            
            request.getRequestDispatcher("/View/Booking/Payment.jsp").forward(request, response);
            
        } catch (IllegalArgumentException e) {
            response.sendRedirect(request.getContextPath() + "/View/Booking/Booking.jsp?error=invalid_format");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if ("check_payment".equals(action)) {
            checkPaymentStatus(request, response);
        } else if ("cancel_on_exit".equals(action)) {
            cancelOnExit(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
        }
    }
    
    /**
     * Check payment status via AJAX
     */
    private void checkPaymentStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        String bookingIdStr = request.getParameter("bookingId");
        
        response.setContentType("application/json;charset=UTF-8");
        
        if (bookingIdStr == null || bookingIdStr.isEmpty()) {
            response.getWriter().write("{\"status\": \"error\", \"message\": \"Invalid booking ID\"}");
            return;
        }
        
        try {
            UUID bookingId = UUID.fromString(bookingIdStr);
            
            // Get booking status
            Booking booking = BookingDAO.getBookingById(bookingId);
            if (booking == null) {
                response.getWriter().write("{\"status\": \"error\", \"message\": \"Booking not found\"}");
                return;
            }
            
            Payment payment = PaymentDAO.getPaymentByBookingId(bookingId);
            if (payment == null) {
                response.getWriter().write("{\"status\": \"error\", \"message\": \"Payment not found\"}");
                return;
            }
            
            long timeRemaining = PaymentDAO.getTimeRemainingForPayment(bookingId);
            String paymentStatus = payment.getPaymentStatus();
            String bookingStatus = booking.getStatus();

            // Immediate release: if payment is failed or timeout reached while booking is still pending,
            // cancel booking now (restore schedule/equipment) instead of waiting for scheduler tick.
            BookingDAO bookingDAO = new BookingDAO();
            boolean isPending = bookingStatus != null && "PENDING".equalsIgnoreCase(bookingStatus);

            if ("FAILED".equalsIgnoreCase(paymentStatus) && isPending) {
                if (bookingDAO.cancelBookingByTimeout(bookingId)) {
                    bookingStatus = "CANCELLED";
                    timeRemaining = 0;
                }
            } else if (timeRemaining <= 0 && isPending) {
                if (bookingDAO.cancelBookingByTimeout(bookingId)) {
                    PaymentDAO.updatePaymentStatus(payment.getPaymentId(), "CANCELLED", "TIMEOUT");
                    paymentStatus = "CANCELLED";
                    bookingStatus = "CANCELLED";
                    timeRemaining = 0;
                }
            }
            
            // Build response
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"paymentStatus\": \"").append(paymentStatus).append("\",");
            json.append("\"bookingStatus\": \"").append(bookingStatus).append("\",");
            json.append("\"timeRemaining\": ").append(timeRemaining).append(",");
            json.append("\"bookingId\": \"").append(bookingId).append("\"");
            
            if ("SUCCESS".equalsIgnoreCase(paymentStatus)) {
                json.append(",\"message\": \"Payment received successfully!\"");
            } else if ("FAILED".equalsIgnoreCase(paymentStatus)) {
                json.append(",\"message\": \"Payment failed. Booking cancelled.\"");
                json.append(",\"expired\": true");
            } else if ("CANCELLED".equalsIgnoreCase(paymentStatus) || "CANCELLED".equalsIgnoreCase(bookingStatus)) {
                // Booking was cancelled (either by timeout or manual cancellation)
                json.append(",\"message\": \"Booking cancelled due to timeout.\"");
                json.append(",\"expired\": true");
            } else if (timeRemaining <= 0) {
                json.append(",\"message\": \"Payment timeout. Booking cancelled.\"");
                json.append(",\"expired\": true");
            } else {
                json.append(",\"message\": \"Waiting for payment...\"");
            }
            
            json.append("}");
            response.getWriter().write(json.toString());
            
        } catch (IllegalArgumentException e) {
            response.getWriter().write("{\"status\": \"error\", \"message\": \"Invalid booking ID format\"}");
        }
    }

    /**
     * Cancel booking immediately when user exits the payment page.
     * Called via navigator.sendBeacon() — only cancels if still PENDING.
     */
    private void cancelOnExit(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String bookingIdStr = request.getParameter("bookingId");
        response.setContentType("application/json;charset=UTF-8");

        if (bookingIdStr == null || bookingIdStr.isEmpty()) {
            response.getWriter().write("{\"status\": \"error\"}");
            return;
        }

        try {
            UUID bookingId = UUID.fromString(bookingIdStr);

            // Only cancel if booking is still PENDING (not paid)
            Booking booking = BookingDAO.getBookingById(bookingId);
            if (booking != null && "PENDING".equalsIgnoreCase(booking.getStatus())) {
                BookingDAO bookingDAO = new BookingDAO();
                boolean cancelled = bookingDAO.cancelBookingByTimeout(bookingId);
                if (cancelled) {
                    Payment payment = PaymentDAO.getPaymentByBookingId(bookingId);
                    if (payment != null) {
                        PaymentDAO.updatePaymentStatus(payment.getPaymentId(), "CANCELLED", "USER_EXIT");
                    }
                }
            }

            response.getWriter().write("{\"status\": \"ok\"}");
        } catch (IllegalArgumentException e) {
            response.getWriter().write("{\"status\": \"error\"}");
        }
    }
}
