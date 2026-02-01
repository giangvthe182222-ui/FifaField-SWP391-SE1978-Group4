package Controller.Equipment;

import DAO.LocationEquipmentDAO;
import Models.LocationEquipmentViewModel;
import Utils.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

@WebServlet(name = "UpdateLocationEquipmentServlet", urlPatterns = {"/update-location-equipment"})
public class UpdateLocationEquipmentServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            UUID locationId =
            UUID.fromString("E41577BF-A373-4389-ADC6-44B6E132AF66");
//            UUID locationId = UUID.fromString(request.getParameter("locationId"));
            UUID equipmentId = UUID.fromString(request.getParameter("equipmentId"));

            LocationEquipmentDAO dao = new LocationEquipmentDAO(new DBConnection());
            LocationEquipmentViewModel le = dao.getOne(locationId, equipmentId);

            if (le == null) {
                response.sendRedirect(request.getContextPath() + "/location-equipment-list");
                return;
            }

            request.setAttribute("locationEquipment", le);
            request.setAttribute("locationId", locationId);
            request.setAttribute("equipmentId", equipmentId);

            request.getRequestDispatcher(
                    "/View/Equipment/UpdateLocationEquipment.jsp"
            ).forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/location-equipment-list");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            UUID locationId = UUID.fromString(request.getParameter("locationId"));
            UUID equipmentId = UUID.fromString(request.getParameter("equipmentId"));

            String status = request.getParameter("status");
            int quantity = Integer.parseInt(request.getParameter("quantity"));

            LocationEquipmentDAO dao = new LocationEquipmentDAO(new DBConnection());
            dao.updateStatusAndQuantity(locationId, equipmentId, status, quantity);

            // quay lại list
            response.sendRedirect(
                request.getContextPath() + "/location-equipment-list?locationId=" + locationId
            );

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/location-equipment-list");
        }
    }
}
