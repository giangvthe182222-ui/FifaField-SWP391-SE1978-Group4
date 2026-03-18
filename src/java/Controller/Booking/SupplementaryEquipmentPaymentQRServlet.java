package Controller.Booking;

import DAO.SupplementaryEquipmentRentalDAO;
import Models.SupplementaryEquipmentRental;
import Models.SupplementaryEquipment;
import Utils.QRCodeGenerator;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * SupplementaryEquipmentPaymentQRServlet - QR payment page for supplementary equipment rental.
 * Uses 15-minute timeout; when expired and unpaid then rental is cancelled.
 */
@WebServlet(name = "SupplementaryEquipmentPaymentQRServlet", urlPatterns = {"/supplementaryEquipmentPaymentQR"})
public class SupplementaryEquipmentPaymentQRServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String rentalIdParam = request.getParameter("rentalId");
        if (rentalIdParam == null || rentalIdParam.isBlank()) {
            session.setAttribute("flash_error", "Mã rental không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/staff/locationBookings");
            return;
        }

        UUID rentalId;
        try {
            rentalId = UUID.fromString(rentalIdParam);
        } catch (IllegalArgumentException e) {
            session.setAttribute("flash_error", "Mã rental không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/staff/locationBookings");
            return;
        }

        SupplementaryEquipmentRentalDAO rentalDAO = new SupplementaryEquipmentRentalDAO();
        SupplementaryEquipmentRental rental = rentalDAO.getRentalById(rentalId);

        if (rental == null) {
            session.setAttribute("flash_error", "Không tìm thấy đơn rental.");
            response.sendRedirect(request.getContextPath() + "/staff/locationBookings");
            return;
        }

        List<SupplementaryEquipment> equipments = rentalDAO.getRentalEquipments(rentalId);
        int equipmentCount = equipments == null ? 0 : equipments.size();

        // Create / reuse session-based 15-minute deadline for this rental.
        String deadlineKey = "supp_payment_deadline_" + rentalId;
        Object storedDeadline = session.getAttribute(deadlineKey);
        LocalDateTime paymentDeadline = null;
        if (storedDeadline instanceof LocalDateTime) {
            paymentDeadline = (LocalDateTime) storedDeadline;
        }
        if (paymentDeadline == null || !paymentDeadline.isAfter(LocalDateTime.now().minusMinutes(120))) {
            paymentDeadline = LocalDateTime.now().plusMinutes(15);
            session.setAttribute(deadlineKey, paymentDeadline);
        }

        LocalDateTime now = LocalDateTime.now();

        String bankCode = QRCodeGenerator.BANK_CODE;
        String accountNumber = QRCodeGenerator.ACCOUNT_NUMBER;
        String accountName = QRCodeGenerator.ACCOUNT_NAME;

        StringBuilder qrContent = new StringBuilder();
        qrContent.append("00020126360014vn.com.vietqr");
        qrContent.append("0132170060").append(bankCode);
        qrContent.append("0208QRIBFTT");
        qrContent.append("0312").append(accountNumber.replaceAll("\\D", ""));
        qrContent.append("5802VN");
        qrContent.append("5913").append(accountName);
        qrContent.append("6009HA NOI");
        qrContent.append("6308").append(String.format("%04d", rental.getTotalPrice().intValue()));
        qrContent.append("810360406");
        qrContent.append("99999999");

        String qrCodeURL = "https://img.vietqr.io/image/" + bankCode + "-" + accountNumber
            + "-compact2.png?amount=" + rental.getTotalPrice().setScale(0, java.math.RoundingMode.DOWN).toPlainString()
            + "&addInfo=SUPP-" + rentalId.toString().replace("-", "").substring(0, 10)
            + "&accountName=" + java.net.URLEncoder.encode(accountName, java.nio.charset.StandardCharsets.UTF_8.toString());

        // Calculate remaining time
        long remainingSeconds = 0;
        if (paymentDeadline != null) {
            Duration duration = Duration.between(now, paymentDeadline);
            remainingSeconds = duration.getSeconds();
            if (remainingSeconds < 0) remainingSeconds = 0;
        }

        String paymentDeadlineText = "";
        if (paymentDeadline != null) {
            paymentDeadlineText = paymentDeadline.format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"));
        }

        // Set attributes for JSP
        request.setAttribute("rental", rental);
        request.setAttribute("equipmentCount", equipmentCount);
        request.setAttribute("createdTime", java.sql.Timestamp.valueOf(rental.getCreatedTime()));
        request.setAttribute("qrCodeURL", qrCodeURL);
        request.setAttribute("qrContent", qrContent.toString());
        request.setAttribute("timeRemaining", remainingSeconds);
        request.setAttribute("remainingSeconds", remainingSeconds);
        request.setAttribute("paymentDeadlineText", paymentDeadlineText);
        request.setAttribute("bankCode", bankCode);
        request.setAttribute("accountNumber", accountNumber);
        request.setAttribute("accountName", accountName);
        request.setAttribute("rentalId", rentalId.toString());

        moveFlashMessages(request);

        request.getRequestDispatcher("/View/Booking/SupplementaryEquipmentPayment.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        if (!"check_payment".equalsIgnoreCase(action)) {
            writeStatus(response, "ERROR", false, 0, "Invalid action");
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            writeStatus(response, "ERROR", false, 0, "Unauthorized");
            return;
        }

        String rentalIdParam = request.getParameter("rentalId");
        if (rentalIdParam == null || rentalIdParam.isBlank()) {
            writeStatus(response, "ERROR", false, 0, "Missing rentalId");
            return;
        }

        UUID rentalId;
        try {
            rentalId = UUID.fromString(rentalIdParam);
        } catch (IllegalArgumentException e) {
            writeStatus(response, "ERROR", false, 0, "Invalid rentalId format");
            return;
        }

        SupplementaryEquipmentRentalDAO rentalDAO = new SupplementaryEquipmentRentalDAO();
        SupplementaryEquipmentRental rental = rentalDAO.getRentalById(rentalId);

        if (rental == null) {
            writeStatus(response, "ERROR", false, 0, "Rental not found");
            return;
        }

        HttpSession httpSession = request.getSession(false);
        String deadlineKey = "supp_payment_deadline_" + rentalId;
        Object storedDeadline = httpSession == null ? null : httpSession.getAttribute(deadlineKey);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = storedDeadline instanceof LocalDateTime ? (LocalDateTime) storedDeadline : now.plusMinutes(15);
        long timeRemaining = 0;
        boolean expired = false;

        if (deadline != null) {
            timeRemaining = Duration.between(now, deadline).getSeconds();
            if (timeRemaining <= 0) {
                timeRemaining = 0;
                expired = true;
            }
        }

        String rentalStatus = rental.getStatus() == null ? "pending" : rental.getStatus().trim().toLowerCase();
        String paymentStatus = ("paid".equals(rentalStatus) || "completed".equals(rentalStatus)) ? "SUCCESS" : "PENDING";

        if (expired && "PENDING".equalsIgnoreCase(paymentStatus)) {
            rentalDAO.updateStatus(rentalId, "cancelled");
            paymentStatus = "FAILED";
        }

        writeStatus(response, paymentStatus, expired, timeRemaining, "OK");
    }

    private void writeStatus(HttpServletResponse response, String paymentStatus,
                             boolean expired, long timeRemaining, String message) throws IOException {
        response.getWriter().print("paymentStatus=" + safeValue(paymentStatus)
                + "\nexpired=" + expired
                + "\ntimeRemaining=" + timeRemaining
                + "\nmessage=" + safeValue(message));
    }

    private String safeValue(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\n", " ").replace("\r", " ").trim();
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
