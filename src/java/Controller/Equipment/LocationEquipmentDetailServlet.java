package Controller.Equipment;

import DAO.LocationEquipmentDAO;
import Models.LocationEquipmentViewModel;
import Utils.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.UUID;

@WebServlet("/location-equipment-detail")
public class LocationEquipmentDetailServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String locationIdRaw = request.getParameter("locationId");
        String equipmentIdRaw = request.getParameter("equipmentId");

        if (locationIdRaw == null || equipmentIdRaw == null) {
            response.sendRedirect(request.getContextPath() + "/location-equipment-list");
            return;
        }

        try {
            UUID locationId = UUID.fromString(locationIdRaw);
            UUID equipmentId = UUID.fromString(equipmentIdRaw);

            LocationEquipmentDAO dao =
                    new LocationEquipmentDAO(new DBConnection());

            LocationEquipmentViewModel le =
                    dao.getOne(locationId, equipmentId);

            if (le == null) {
                response.sendRedirect(
                        request.getContextPath()
                        + "/location-equipment-list?locationId=" + locationId
                );
                return;
            }

            request.setAttribute("locationEquipment", le);
            request.setAttribute("locationId", locationId);
            request.setAttribute("equipmentId", equipmentId);

            request.getRequestDispatcher(
                    "/View/Equipment/LocationEquipmentDetail.jsp"
            ).forward(request, response);

        } catch (Exception ex) {
            ex.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/location-equipment-list");
        }
    }
}
