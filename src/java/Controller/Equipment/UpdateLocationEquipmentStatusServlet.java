package Controller.Equipment;

import DAO.LocationEquipmentDAO;
import Utils.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.UUID;

@WebServlet(
    name = "UpdateLocationEquipmentStatusServlet",
    urlPatterns = "/update-location-equipment-status"
)
public class UpdateLocationEquipmentStatusServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String locationIdParam = request.getParameter("locationId");
        String equipmentIdParam = request.getParameter("equipmentId");
        String newStatus = request.getParameter("newStatus");

        if (locationIdParam == null || locationIdParam.isBlank()
                || equipmentIdParam == null || equipmentIdParam.isBlank()
                || newStatus == null || newStatus.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/location-equipment-list");
            return;
        }

        UUID locationId, equipmentId;
        try {
            locationId = UUID.fromString(locationIdParam);
            equipmentId = UUID.fromString(equipmentIdParam);
        } catch (IllegalArgumentException e) {
            response.sendRedirect(request.getContextPath() + "/location-equipment-list");
            return;
        }

        LocationEquipmentDAO dao = new LocationEquipmentDAO(new DBConnection());
        boolean success = dao.updateStatus(locationId, equipmentId, newStatus);

        // có thể log nếu cần
        // if (!success) { ... }

        response.sendRedirect(request.getContextPath() + "/location-equipment-list?locationId=" + locationId);
    }
}