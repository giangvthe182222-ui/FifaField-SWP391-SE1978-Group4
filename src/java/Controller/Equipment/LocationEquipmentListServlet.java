package Controller.Equipment;

import DAO.LocationEquipmentDAO;
import Models.LocationEquipmentViewModel;
import Utils.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@WebServlet("/location-equipment-list")
public class LocationEquipmentListServlet extends HttpServlet {

    // 🔥 hardcode location_id (anh sửa lại cho đúng DB)
    private static final UUID LOCATION_ID =
        UUID.fromString("E41577BF-A373-4389-ADC6-44B6E132AF66");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        LocationEquipmentDAO dao =
            new LocationEquipmentDAO(new DBConnection());

        List<LocationEquipmentViewModel> list = dao.getByLocation(LOCATION_ID);

        request.setAttribute("locationEquipmentList", list);
        request.setAttribute("locationId", LOCATION_ID);

        request.getRequestDispatcher(
            "/View/Equipment/LocationEquipmentList.jsp"
        ).forward(request, response);
        
        
    }
    
}
