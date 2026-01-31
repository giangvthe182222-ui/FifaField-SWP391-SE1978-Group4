package Controller.Equipment;

import DAO.EquipmentDAO;
import Utils.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.UUID;

@WebServlet(
    name = "UpdateEquipmentServlet",
    urlPatterns = "/update-equipment-status"
)
public class UpdateEquipmentStatusServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idParam = request.getParameter("id");
        String newStatus = request.getParameter("newStatus");

        if (idParam == null || idParam.isBlank()
                || newStatus == null || newStatus.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/equipment-list");
            return;
        }

        UUID id;
        try {
            id = UUID.fromString(idParam);
        } catch (IllegalArgumentException e) {
            response.sendRedirect(request.getContextPath() + "/equipment-list");
            return;
        }

        EquipmentDAO dao = new EquipmentDAO(new DBConnection());
        boolean success = dao.updateStatus(id, newStatus);

        // có thể log nếu cần
        // if (!success) { ... }

        response.sendRedirect(request.getContextPath() + "/equipment-list");
    }
}
