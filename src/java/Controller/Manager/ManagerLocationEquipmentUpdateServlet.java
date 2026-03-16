package Controller.Manager;

import DAO.LocationEquipmentDAO;
import DAO.ManagerDAO;
import Models.LocationEquipmentViewModel;
import Models.Manager;
import Models.User;
import Utils.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;

@WebServlet("/manager/location-equipment/update")
public class ManagerLocationEquipmentUpdateServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=manager/location-equipment");
            return;
        }

        User user = (User) session.getAttribute("user");
        String equipmentIdRaw = request.getParameter("equipmentId");
        String quantityRaw = request.getParameter("quantity");
        String status = request.getParameter("status");

        try {
            Manager manager = new ManagerDAO().getManagerById(user.getUserId());
            if (manager == null || manager.getLocationId() == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Manager has no assigned location");
                return;
            }

            if (equipmentIdRaw == null || equipmentIdRaw.isBlank()) {
                session.setAttribute("flash_error", "Thiếu mã dụng cụ.");
                response.sendRedirect(request.getContextPath() + "/manager/location-equipment");
                return;
            }

            int quantity = 0;
            try {
                quantity = Integer.parseInt(quantityRaw);
                if (quantity < 0) {
                    quantity = 0;
                }
            } catch (Exception e) {
                quantity = 0;
            }

            if (status == null || status.isBlank()) {
                status = quantity > 0 ? "available" : "unavailable";
            }
            status = status.toLowerCase();
            if (!"available".equals(status) && !"unavailable".equals(status)) {
                status = quantity > 0 ? "available" : "unavailable";
            }

            UUID equipmentId = UUID.fromString(equipmentIdRaw);
            LocationEquipmentDAO locationEquipmentDAO = new LocationEquipmentDAO(new DBConnection());
            LocationEquipmentViewModel existing = locationEquipmentDAO.getOne(manager.getLocationId(), equipmentId);
            if (existing == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Equipment is outside manager location");
                return;
            }

            boolean ok = locationEquipmentDAO.updateStatusAndQuantity(manager.getLocationId(), equipmentId, status, quantity);
            if (ok) {
                session.setAttribute("flash_success", "Đã cập nhật dụng cụ tại cơ sở.");
            } else {
                session.setAttribute("flash_error", "Không thể cập nhật dụng cụ.");
            }

            response.sendRedirect(request.getContextPath() + "/manager/location-equipment");
        } catch (Exception e) {
            throw new ServletException("Cannot update manager location equipment", e);
        }
    }
}
